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
import org.mockito.Mockito;

import java.io.File;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class NDependReportParserTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private NDependReportParserCallback callback;
  private NDependReportParser parser;

  @Before
  public void init() {
    callback = mock(NDependReportParserCallback.class);
    parser = new NDependReportParser(callback);
  }

  @Test
  public void test() {
    parser.parse(new File("src/test/resources/NDependReportParserTest/valid.xml"));

    verify(callback).onIssue("AvoidNamespacesWithFewTypes", "Program.cs", 7);
    verify(callback, Mockito.times(2)).onIssue("ClassWithNoDescendantShouldBeSealedIfPossible", "Program.cs", 9);
    verify(callback, Mockito.times(2)).onIssue("AStatelessClassOrStructureMightBeTurnedIntoAStaticType", "Program.cs", 9);
    verify(callback, Mockito.times(2)).onIssue("NonStaticClassesShouldBeInstantiatedOrTurnedToStatic", "Program.cs", 9);

    verify(callback, Mockito.times(7)).onIssue(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
  }

  @Test
  public void no_key() {
    thrown.expectMessage("Missing attribute \"Key\" in element <RuleViolated>");
    thrown.expectMessage("no_key.xml at line 5");

    parser.parse(new File("src/test/resources/NDependReportParserTest/no_key.xml"));
  }

  @Test
  public void invalid_line() {
    thrown.expectMessage("Expected an integer instead of \"foo\" for the attribute \"Line\"");
    thrown.expectMessage("invalid_line.xml at line 3");

    parser.parse(new File("src/test/resources/NDependReportParserTest/invalid_line.xml"));
  }

  @Test
  public void non_existing() {
    thrown.expectMessage("non_existing.xml");
    parser.parse(new File("src/test/resources/NDependReportParserTest/non_existing.xml"));
  }

}
