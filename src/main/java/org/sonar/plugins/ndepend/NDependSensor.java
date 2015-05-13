/*
 * SonarQube NDepend Plugin
 * Copyright (C) 2015 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.ndepend;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issuable.IssueBuilder;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.ActiveRule;

import java.io.File;

public class NDependSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(NDependSensor.class);

  private final NDependConfiguration conf;
  private final FileSystem fs;
  private final RulesProfile profile;
  private final ResourcePerspectives perspectives;

  public NDependSensor(NDependConfiguration conf, FileSystem fs, RulesProfile profile, ResourcePerspectives perspectives) {
    this.conf = conf;
    this.fs = fs;
    this.profile = profile;
    this.perspectives = perspectives;
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    boolean shouldExecute;

    if (!hasFilesToAnalyze()) {
      shouldExecute = false;
    } else if (profile.getActiveRulesByRepository(NDependPlugin.REPOSITORY_KEY).isEmpty()) {
      LOG.info("All NDepend rules are disabled, skipping its execution.");
      shouldExecute = false;
    } else {
      shouldExecute = true;
    }

    return shouldExecute;
  }

  private boolean hasFilesToAnalyze() {
    return fs.files(fs.predicates().hasLanguage(NDependPlugin.LANGUAGE_KEY)).iterator().hasNext();
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    analyze(context, new NDependExecutor());
  }

  @VisibleForTesting
  void analyze(SensorContext context, NDependExecutor executor) {
    File reportFile = new File(fs.workDir(), "ndepend-report.xml");
    executor.execute(conf.ruleRunnerPath(), conf.ndependProjectPath(), reportFile, conf.timeout());

    new NDependReportParser(
      new NDependReportParserCallback() {

        @Override
        public void onIssue(String ruleKey, String file, int line) {
          ActiveRule rule = profile.getActiveRule(NDependPlugin.REPOSITORY_KEY, ruleKey);
          if (rule == null) {
            logSkippedIssue("the rule is disabled in the current quality profile", ruleKey, file, line);
            return;
          }

          InputFile inputFile = fs.inputFile(
            fs.predicates().and(
              fs.predicates().hasAbsolutePath(file),
              fs.predicates().hasType(Type.MAIN)));

          if (inputFile == null) {
            logSkippedIssue("the file is not imported in SonarQube", ruleKey, file, line);
            return;
          }

          Issuable issuable = perspectives.as(Issuable.class, inputFile);
          if (issuable == null) {
            logSkippedIssue("no issuable has been found for the file", ruleKey, file, line);
            return;
          }

          IssueBuilder builder = issuable.newIssueBuilder();
          builder.ruleKey(RuleKey.of(NDependPlugin.REPOSITORY_KEY, ruleKey));
          builder.line(line);
          builder.message(rule.getRule().getName());

          issuable.addIssue(builder.build());
        }

        private void logSkippedIssue(String reason, String ruleKey, String file, int line) {
          LOG.debug("Skipping NDepend issue on file " + file + " at line " + line + " on rule " + ruleKey + " because " + reason);
        }

      }).parse(reportFile);
  }

}
