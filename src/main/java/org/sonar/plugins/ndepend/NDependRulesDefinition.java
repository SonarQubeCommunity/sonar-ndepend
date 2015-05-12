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

import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.server.rule.RulesDefinition;

import javax.annotation.Nullable;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import java.io.StringReader;

public class NDependRulesDefinition implements RulesDefinition {

  private static final Logger LOG = LoggerFactory.getLogger(NDependRulesDefinition.class);

  private final NDependConfiguration conf;

  public NDependRulesDefinition(NDependConfiguration conf) {
    this.conf = conf;
  }

  @Override
  public void define(Context context) {
    NewRepository repository = context
      .createRepository(NDependPlugin.REPOSITORY_KEY, NDependPlugin.LANGUAGE_KEY)
      .setName(NDependPlugin.REPOSITORY_NAME);

    String xml = conf.rules();
    if (xml.isEmpty()) {
      LOG.warn("No NDepend rules defined.");
    } else {
      try {
        new NDependRulesParser(repository).parse(xml);
      } catch (Exception e) {
        LOG.error("Error while creating the NDepend rule repository: " + e.getMessage(), e);
      }
    }

    repository.done();
  }

  private static class NDependRulesParser {

    private final NewRepository repository;
    private XMLStreamReader stream;

    public NDependRulesParser(NewRepository repository) {
      this.repository = repository;
    }

    public void parse(String xml) {
      StringReader reader = null;
      XMLInputFactory xmlFactory = XMLInputFactory.newInstance();

      try {
        reader = new StringReader(xml);
        stream = xmlFactory.createXMLStreamReader(reader);

        while (stream.hasNext()) {
          if (stream.next() == XMLStreamConstants.START_ELEMENT) {
            String tagName = stream.getLocalName();

            if ("Rule".equals(tagName)) {
              handleRuleTag();
            }
          }
        }
      } catch (XMLStreamException e) {
        throw Throwables.propagate(e);
      } finally {
        closeXmlStream();
        Closeables.closeQuietly(reader);
      }

      return;
    }

    private void closeXmlStream() {
      if (stream != null) {
        try {
          stream.close();
        } catch (XMLStreamException e) {
          throw Throwables.propagate(e);
        }
      }
    }

    private void handleRuleTag() throws XMLStreamException {
      String key = getRequiredAttribute("Key");
      String priority = getRequiredAttribute("Priority");
      String category = getAttribute("Category");
      String name = null;
      String description = null;

      while (stream.hasNext()) {
        int next = stream.next();

        if (next == XMLStreamConstants.END_ELEMENT && "Rule".equals(stream.getLocalName())) {
          if (name == null) {
            throw parseError("Missing rule name");
          }
          if (description == null) {
            throw parseError("Missing rule description");
          }

          NewRule rule = repository.createRule(key).setName(name).setSeverity(priority).setHtmlDescription(description);
          if (category != null) {
            rule.addTags(category);
          }

          break;
        } else if (next == XMLStreamConstants.START_ELEMENT) {
          String tagName = stream.getLocalName();

          if ("Name".equals(tagName)) {
            name = stream.getElementText();
          } else if ("Description".equals(tagName)) {
            description = stream.getElementText();
          }
        }
      }
    }

    public String getRequiredAttribute(String name) {
      String value = getAttribute(name);
      if (value == null) {
        throw parseError("Missing attribute \"" + name + "\" in element <" + stream.getLocalName() + ">");
      }

      return value;
    }

    @Nullable
    public String getAttribute(String name) {
      for (int i = 0; i < stream.getAttributeCount(); i++) {
        if (name.equals(stream.getAttributeLocalName(i))) {
          return stream.getAttributeValue(i);
        }
      }

      return null;
    }

    public ParseErrorException parseError(String message) {
      return new ParseErrorException(message + " at line " + stream.getLocation().getLineNumber());
    }

  }

  private static class ParseErrorException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ParseErrorException(String message) {
      super(message);
    }

  }

}
