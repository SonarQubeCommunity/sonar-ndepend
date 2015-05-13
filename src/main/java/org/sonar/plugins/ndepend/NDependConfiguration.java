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

import com.google.common.base.Preconditions;
import org.sonar.api.BatchExtension;
import org.sonar.api.ServerExtension;
import org.sonar.api.config.Settings;

import java.io.File;

public class NDependConfiguration implements BatchExtension, ServerExtension {

  private final Settings settings;

  public NDependConfiguration(Settings settings) {
    this.settings = settings;
  }

  public String rules() {
    String xml = settings.getString(NDependPlugin.RULES_PROPERTY_KEY);
    return xml == null ? "" : xml;
  }

  public String ruleRunnerPath() {
    return checkAbsolutePath(NDependPlugin.RULE_RUNNER_PATH_PROPERTY_KEY);
  }

  public String ndependProjectPath() {
    return checkAbsolutePath(NDependPlugin.NDEPEND_PROJECT_PATH_PROPERTY_KEY);
  }

  public int timeout() {
    return settings.getInt(NDependPlugin.TIMEOUT_PROPERTY_KEY);
  }

  private String checkAbsolutePath(String property) {
    String path = settings.getString(property);
    Preconditions.checkNotNull(path, "The property \"" + property + "\" must be set (to an absolute path).");

    File file = new File(path);
    Preconditions.checkArgument(file.isAbsolute(), "The path provided in the property \"" + property + "\" must be an absolute path: " + path);
    Preconditions.checkArgument(file.exists(), "The absolute path provided in the property \"" + property + "\" does not exist: " + path);

    return path;
  }

}
