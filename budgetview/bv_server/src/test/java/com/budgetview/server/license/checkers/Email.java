package com.budgetview.server.license.checkers;

import com.dumbster.smtp.SmtpMessage;
import junit.framework.Assert;

public class Email {
  private String content;
  private SmtpMessage message;

  Email(SmtpMessage message) {
    this.message = message;
    this.content = message.getBody();
  }

  public Email checkSubjectContains(String text) {
    String subject = message.getHeaderValue("Subject");
    if (!subject.contains(text)) {
      Assert.fail("Unexpected subject: " + subject);
    }
    return this;
  }

  public Email checkContains(String... textElements) {
    for (String textElement : textElements) {
      if (!content.contains(textElement)) {
        Assert.fail("Text '" + textElement + "' not found. Actual content: \n" + content);
      }
    }
    return this;
  }

  public Email checkDoesNotContain(String... textElements) {
    for (String textElement : textElements) {
      if (content.contains(textElement)) {
        Assert.fail("Text '" + textElement + "' unexpectedly found. Actual content: \n" + content);
      }
    }
    return this;
  }

  public String getEnd(int charCount) {
    return content.substring(content.length() - charCount, content.length()).trim();
  }

  public String getContent() {
    return content;
  }
}