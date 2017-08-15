package org.globsframework.utils;

import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

public class StringChecker {
  private final String initialContent;
  private Map<String, String> variables;

  public static StringChecker init(String initialContent) {
    return new StringChecker(initialContent);
  }

  public StringChecker(String initialContent) {
    this.initialContent = initialContent;
  }

  public StringChecker with(String variable, int value) {
    return with(variable, Integer.toString(value));
  }

  public StringChecker with(String variable, String value) {
    if (variables == null) {
      variables = new HashMap<String, String>();
    }
    variables.put(variable, value);
    return this;
  }

  public StringChecker checkContains(String text) {
    String convertedText = convert(text);
    if (!initialContent.contains(convertedText)) {
      Assert.fail("'" + convertedText + "' not found in:\n" + initialContent);
    }
    return this;
  }

  public StringChecker checkLineMatches(String regexp) {
    String convertedRegexp = convert(regexp);
    for (String line : initialContent.split("\n")) {
      if (line.trim().matches(convertedRegexp)) {
        return this;
      }
    }
    Assert.fail("'" + convertedRegexp + "' not matched in:\n" + initialContent);
    return this;
  }

  public StringChecker checkEquals(String text) {
    String convertedText = convert(text);
    Assert.assertEquals(convertedText, initialContent);
    return this;
  }

  public StringChecker checkEquals(StringChecker other) {
    String convertedOther = convert(other.toString());
    Assert.assertEquals(toString(), convertedOther);
    return this;
  }

  public StringChecker checkMatches(String regexp) {
    String initial = cleanup(initialContent);
    String convertedRegexp = cleanup(convert(regexp));
    if (toString().matches(convertedRegexp)) {
      Assert.fail("'" + convertedRegexp + "' not matched in:\n" + initial);
    }
    return this;
  }

  public String toString() {
    return initialContent;
  }

  private String convert(String text) {
    if (variables == null) {
      return text;
    }
    String result = text;
    for (Map.Entry<String,String> entry : variables.entrySet()) {
      result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
    }
    return result;
  }

  private String cleanup(String text) {
    return text.trim().replaceAll("[ \t]+", " ");
  }
}
