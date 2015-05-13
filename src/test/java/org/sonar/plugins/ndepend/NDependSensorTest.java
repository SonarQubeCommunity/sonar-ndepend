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

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issuable.IssueBuilder;
import org.sonar.api.issue.Issue;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NDependSensorTest {

  @org.junit.Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  private NDependConfiguration conf;
  private SensorContext context;
  private DefaultInputFile inputFile;
  private DefaultFileSystem fs;
  private FileLinesContext fileLinesContext;
  private FileLinesContextFactory fileLinesContextFactory;
  private NDependExecutor executor;
  private ResourcePerspectives perspectives;
  private Issuable issuable;
  private IssueBuilder issueBuilder;
  private Issue issue;

  @Test
  public void shouldExecuteOnProject() {
    DefaultFileSystem fs = new DefaultFileSystem();
    RulesProfile profile = mock(RulesProfile.class);

    Project project = mock(Project.class);

    NDependSensor sensor = new NDependSensor(mock(NDependConfiguration.class), fs, profile, mock(ResourcePerspectives.class));

    when(profile.getActiveRulesByRepository("ndepend")).thenReturn(ImmutableList.<ActiveRule>of());
    assertThat(sensor.shouldExecuteOnProject(project)).isFalse();

    when(profile.getActiveRulesByRepository("ndepend")).thenReturn(ImmutableList.<ActiveRule>of(mock(ActiveRule.class)));
    assertThat(sensor.shouldExecuteOnProject(project)).isFalse();

    fs.add(new DefaultInputFile("").setAbsolutePath("").setLanguage("foo"));
    assertThat(sensor.shouldExecuteOnProject(project)).isFalse();

    fs.add(new DefaultInputFile("").setAbsolutePath("").setLanguage("cs"));
    assertThat(sensor.shouldExecuteOnProject(project)).isTrue();

    when(profile.getActiveRulesByRepository("ndepend")).thenReturn(ImmutableList.<ActiveRule>of());
    assertThat(sensor.shouldExecuteOnProject(project)).isFalse();
  }

  @Before
  public void init() throws Exception {
    conf = mock(NDependConfiguration.class);
    when(conf.ruleRunnerPath()).thenReturn("NDepend.SonarQube.RuleRunner.exe");
    when(conf.ndependProjectPath()).thenReturn("project.ndproj");
    when(conf.timeout()).thenReturn(42);

    fs = new DefaultFileSystem();
    File workDir = tmp.newFolder("NDependSensorTest");
    fs.setWorkDir(workDir);

    File reportFile = new File(workDir, "ndepend-report.xml");
    Files.copy(new File("src/test/resources/NDependSensorTest/valid.xml"), reportFile);

    inputFile = new DefaultInputFile("Program.cs").setAbsolutePath("Program.cs");
    fs.add(inputFile);

    fileLinesContext = mock(FileLinesContext.class);
    fileLinesContextFactory = mock(FileLinesContextFactory.class);
    when(fileLinesContextFactory.createFor(inputFile)).thenReturn(fileLinesContext);

    executor = mock(NDependExecutor.class);

    perspectives = mock(ResourcePerspectives.class);
    issuable = mock(Issuable.class);
    issueBuilder = mock(IssueBuilder.class);
    when(issuable.newIssueBuilder()).thenReturn(issueBuilder);
    issue = mock(Issue.class);
    when(issueBuilder.build()).thenReturn(issue);
    when(perspectives.as(Mockito.eq(Issuable.class), Mockito.any(InputFile.class))).thenReturn(issuable);

    Rule rule = mock(Rule.class);
    when(rule.getName()).thenReturn("my rule name");
    ActiveRule activeRule = mock(ActiveRule.class);
    when(activeRule.getRule()).thenReturn(rule);

    RulesProfile rulesProfile = mock(RulesProfile.class);
    when(rulesProfile.getActiveRule(NDependPlugin.REPOSITORY_KEY, "ClassWithNoDescendantShouldBeSealedIfPossible")).thenReturn(activeRule);

    NDependSensor sensor = new NDependSensor(conf, fs, rulesProfile, perspectives);

    context = mock(SensorContext.class);
    sensor.analyze(context, executor);

    verify(executor).execute("NDepend.SonarQube.RuleRunner.exe", "project.ndproj", reportFile, 42);
  }

  @Test
  public void issue() {
    verify(issueBuilder).ruleKey(RuleKey.of(NDependPlugin.REPOSITORY_KEY, "ClassWithNoDescendantShouldBeSealedIfPossible"));
    verify(issueBuilder).message("my rule name");
    verify(issueBuilder).line(9);
    verify(issuable).addIssue(issue);
  }

}
