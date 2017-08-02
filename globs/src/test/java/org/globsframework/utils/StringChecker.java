package org.globsframework.utils;

import org.junit.Assert;

public class StringChecker {
  private String content;

  public StringChecker(String content) {
    this.content = content;
  }

  public void checkContains(String text) {
    if (!content.contains(text)) {
      Assert.fail("'" + text + "' not found in:\n" + content);
    }
  }

  public void checkLineMatches(String regexp) {
    for (String line : content.split("\n")) {
      if (line.trim().matches(regexp)) {
        return;
      }
    }
    Assert.fail("'" + regexp + "' not matched in:\n" + content);
  }

  public void checkEquals(String expected) {
    Assert.assertEquals(expected, content);
  }

  public void checkEquals(StringChecker other) {
    Assert.assertEquals(content, other.content);
  }

  public String toString() {
    return content;
  }
}
