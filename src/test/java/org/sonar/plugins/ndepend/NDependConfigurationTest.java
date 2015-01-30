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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.config.Settings;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;

public class NDependConfigurationTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private Settings settings;
  private NDependConfiguration conf;

  @Before
  public void init() {
    settings = new Settings();
    conf = new NDependConfiguration(settings);
  }

  @Test
  public void rules() {
    assertThat(conf.rules()).isEmpty();

    settings.setProperty(NDependPlugin.RULES_PROPERTY_KEY, "foo");
    assertThat(conf.rules()).isEqualTo("foo");
  }

  @Test
  public void ruleRunnerPath() {
    File file = new File("src/test/resources/NDependConfigurationTest/file.txt");
    settings.setProperty(NDependPlugin.RULE_RUNNER_PATH_PROPERTY_KEY, file.getAbsolutePath());
    assertThat(conf.ruleRunnerPath()).isEqualTo(file.getAbsolutePath());
  }

  @Test
  public void ndependProjectPath() {
    File file = new File("src/test/resources/NDependConfigurationTest/file.txt");
    settings.setProperty(NDependPlugin.NDEPEND_PROJECT_PATH_PROPERTY_KEY, file.getAbsolutePath());
    assertThat(conf.ndependProjectPath()).isEqualTo(file.getAbsolutePath());
  }

  @Test
  public void timeout() {
    settings.setProperty(NDependPlugin.TIMEOUT_PROPERTY_KEY, 42);
    assertThat(conf.timeout()).isEqualTo(42);
  }

  @Test
  public void no_rule_runner_path() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("The property \"" + NDependPlugin.RULE_RUNNER_PATH_PROPERTY_KEY + "\" must be set (to an absolute path).");
    conf.ruleRunnerPath();
  }

  @Test
  public void no_ndepend_project_path() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("The property \"" + NDependPlugin.NDEPEND_PROJECT_PATH_PROPERTY_KEY + "\" must be set (to an absolute path).");
    conf.ndependProjectPath();
  }

  @Test
  public void invalid_path() {
    settings.setProperty(NDependPlugin.RULE_RUNNER_PATH_PROPERTY_KEY, new File("src/test/resources/NDependConfigurationTest/non_existing.txt").getAbsolutePath());

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("The absolute path provided in the property \"" + NDependPlugin.RULE_RUNNER_PATH_PROPERTY_KEY + "\" does not exist: ");
    thrown.expectMessage("non_existing.txt");

    conf.ruleRunnerPath();
  }

  @Test
  public void relative_path() {
    settings.setProperty(NDependPlugin.RULE_RUNNER_PATH_PROPERTY_KEY, "src/test/resources/NDependConfigurationTest/non_existing.txt");

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("The path provided in the property \"" + NDependPlugin.RULE_RUNNER_PATH_PROPERTY_KEY + "\" must be an absolute path: ");
    thrown.expectMessage("non_existing.txt");

    conf.ruleRunnerPath();
  }

}
