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

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import junit.framework.Assert;
import org.junit.Test;
import org.sonar.api.server.rule.RulesDefinition.Context;
import org.sonar.api.server.rule.RulesDefinition.Rule;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NDependRulesDefinitionTest {

  @Test
  public void test() throws Exception {
    NDependConfiguration conf = mock(NDependConfiguration.class);
    when(conf.rules()).thenReturn(Files.toString(new File("src/test/resources/NDependRulesDefinitionTest/valid.xml"), Charsets.UTF_8));

    Context context = new Context();
    new NDependRulesDefinition(conf).define(context);

    assertThat(context.repositories()).hasSize(1);

    for (Rule rule : context.repository("ndepend").rules()) {
      if ("Key1".equals(rule.key())) {
        assertThat(rule.key()).isEqualTo("Key1");
        assertThat(rule.severity()).isEqualTo("BLOCKER");
        assertThat(rule.name()).isEqualTo("Name1");
        assertThat(rule.htmlDescription()).isEqualTo("Description1");
        assertThat(rule.tags()).containsOnly("code-quality");
      } else if ("Key2".equals(rule.key())) {
        assertThat(rule.key()).isEqualTo("Key2");
        assertThat(rule.severity()).isEqualTo("MAJOR");
        assertThat(rule.name()).isEqualTo("Name2");
        assertThat(rule.htmlDescription()).isEqualTo("Description2");
        assertThat(rule.tags()).isEmpty();
      } else if ("Key3".equals(rule.key())) {
        assertThat(rule.severity()).isEqualTo("MINOR");
        assertThat(rule.name()).isEqualTo("Name3");
        assertThat(rule.htmlDescription()).isEqualTo("Description3");
        assertThat(rule.tags()).containsOnly("object-oriented-design");
      } else {
        Assert.fail("Unexpected key: " + rule.key());
      }
    }
    assertThat(context.repository("ndepend").rules()).hasSize(3);
  }

  @Test
  public void should_not_fail_on_empty_rules_property() {
    NDependConfiguration conf = mock(NDependConfiguration.class);
    when(conf.rules()).thenReturn("");

    Context context = new Context();
    new NDependRulesDefinition(conf).define(context);
    assertThat(context.repository("ndepend").rules()).isEmpty();
  }

  @Test
  public void should_not_fail_on_missing_key() throws Exception {
    NDependConfiguration conf = mock(NDependConfiguration.class);
    when(conf.rules()).thenReturn(Files.toString(new File("src/test/resources/NDependRulesDefinitionTest/no_key.xml"), Charsets.UTF_8));

    Context context = new Context();
    new NDependRulesDefinition(conf).define(context);
    assertThat(context.repository("ndepend").rules()).isEmpty();
  }

  @Test
  public void should_not_fail_on_missing_priority() throws Exception {
    NDependConfiguration conf = mock(NDependConfiguration.class);
    when(conf.rules()).thenReturn(Files.toString(new File("src/test/resources/NDependRulesDefinitionTest/no_priority.xml"), Charsets.UTF_8));

    Context context = new Context();
    new NDependRulesDefinition(conf).define(context);
    assertThat(context.repository("ndepend").rules()).isEmpty();
  }

  @Test
  public void should_not_fail_on_missing_name() throws Exception {
    NDependConfiguration conf = mock(NDependConfiguration.class);
    when(conf.rules()).thenReturn(Files.toString(new File("src/test/resources/NDependRulesDefinitionTest/no_name.xml"), Charsets.UTF_8));

    Context context = new Context();
    new NDependRulesDefinition(conf).define(context);
    assertThat(context.repository("ndepend").rules()).isEmpty();
  }

  @Test
  public void should_not_fail_on_missing_description() throws Exception {
    NDependConfiguration conf = mock(NDependConfiguration.class);
    when(conf.rules()).thenReturn(Files.toString(new File("src/test/resources/NDependRulesDefinitionTest/no_description.xml"), Charsets.UTF_8));

    Context context = new Context();
    new NDependRulesDefinition(conf).define(context);
    assertThat(context.repository("ndepend").rules()).isEmpty();
  }

}
