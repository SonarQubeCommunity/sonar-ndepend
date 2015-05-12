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
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

import java.util.List;

public class NDependPlugin extends SonarPlugin {

  public static final String LANGUAGE_KEY = "cs";

  public static final String REPOSITORY_KEY = "ndepend";
  public static final String REPOSITORY_NAME = "NDepend";

  public static final String RULES_PROPERTY_KEY = "sonar.cs.ndepend.rules";
  public static final String RULE_RUNNER_PATH_PROPERTY_KEY = "sonar.cs.ndepend.ruleRunnerPath";
  public static final String NDEPEND_PROJECT_PATH_PROPERTY_KEY = "sonar.cs.ndepend.projectPath";
  public static final String TIMEOUT_PROPERTY_KEY = "sonar.cs.ndepend.timeoutMinutes";

  private static final String CATEGORY = "NDepend";

  @Override
  public List getExtensions() {
    ImmutableList.Builder builder = ImmutableList.builder();

    builder.add(
      NDependConfiguration.class,
      NDependRulesDefinition.class,
      NDependSensor.class);

    builder.addAll(pluginProperties());

    return builder.build();
  }

  private static ImmutableList<PropertyDefinition> pluginProperties() {
    return ImmutableList.of(
      PropertyDefinition.builder(RULES_PROPERTY_KEY)
        .name("NDepend rules")
        .description("XML output of NDepend.SonarQube.RuleSetBuilder.exe. Restart the SonarQube server to make changes to this property effective.")
        .category(CATEGORY)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .type(PropertyType.TEXT)
        .build(),
      PropertyDefinition.builder(RULE_RUNNER_PATH_PROPERTY_KEY)
        .name("Path to NDepend.SonarQube.RuleRunner.exe")
        .description("Must be an absolute path. Example: C:/NDepend.SonarQube.RuleRunner.exe")
        .category(CATEGORY)
        .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .build(),
      PropertyDefinition.builder(TIMEOUT_PROPERTY_KEY)
        .name("NDepend execution timeout")
        .description("Time in minutes after which NDepend's execution should be interrupted if not finished")
        .defaultValue("10")
        .category(CATEGORY)
        .onQualifiers(Qualifiers.PROJECT)
        .type(PropertyType.INTEGER)
        .build(),
      PropertyDefinition.builder(NDEPEND_PROJECT_PATH_PROPERTY_KEY)
        .name("Project file")
        .description("Must be an absolute path. Example: C:/project.ndproj")
        .category(CATEGORY)
        .onlyOnQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
        .build());
  }

}
