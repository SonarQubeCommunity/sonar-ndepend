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
            LOG.debug("Ignoring NDepend issue on disabled rule " + ruleKey + " for file " + file + " line " + line);
            return;
          }

          InputFile inputFile = fs.inputFile(
            fs.predicates().and(
              fs.predicates().hasAbsolutePath(file),
              fs.predicates().hasType(Type.MAIN)));

          if (inputFile != null) {
            // TODO Log message if file not available
            Issuable issuable = perspectives.as(Issuable.class, org.sonar.api.resources.File.create(inputFile.relativePath()));
            issuable.addIssue(
              issuable.newIssueBuilder()
                .ruleKey(RuleKey.of(NDependPlugin.REPOSITORY_KEY, ruleKey))
                .line(line)
                .message(rule.getRule().getName()).build());
          }
        }

      }).parse(reportFile);
  }

}
