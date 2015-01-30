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
import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.ActiveRule;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NDependSensorTest {

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

}
