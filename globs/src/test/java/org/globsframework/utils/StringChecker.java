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

  public void checkEquals(String text) {
    Assert.assertEquals(content, text);
  }

  public String toString() {
    return content;
  }
}
