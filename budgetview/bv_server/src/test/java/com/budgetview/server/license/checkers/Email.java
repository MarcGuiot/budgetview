package com.budgetview.server.license.checkers;

import com.dumbster.smtp.SmtpMessage;
import junit.framework.AssertionFailedError;

import java.io.UnsupportedEncodingException;

import static javax.mail.internet.MimeUtility.decodeText;

public class Email {
  private String content;
  private SmtpMessage message;

  Email(SmtpMessage message) throws Exception {
    this.message = message;
    this.content = decodeText(message.getBody());
  }

  public Email checkSubjectContains(String text) throws Exception {
    String subject = getSubject();
    if (!subject.contains(text)) {
      throw new AssertionFailedError("Unexpected subject: " + subject);
    }
    return this;
  }

  public Email checkSubjectContainsAny(String... text) throws Exception {
    String subject = getSubject();
    for (String item : text) {
      if (subject.contains(item)) {
        return this;
      }
    }
    throw new AssertionFailedError("Unexpected subject: " + subject);
  }

  public String getSubject() throws Exception {
    return decodeText(message.getHeaderValue("Subject"));
  }

  public Email checkContainsAll(String... textElements) {
    for (String textElement : textElements) {
      if (!content.contains(textElement)) {
        throw new AssertionFailedError("Text '" + textElement + "' not found. Actual content: \n" + content);
      }
    }
    return this;
  }

  public Email checkContainsAny(String... textElements) {
    for (String textElement : textElements) {
      if (content.contains(textElement)) {
        return this;
      }
    }
    throw new AssertionFailedError("Text not found. Actual content: \n" + content);
  }

  public Email checkDoesNotContain(String... textElements) {
    for (String textElement : textElements) {
      if (content.contains(textElement)) {
        throw new AssertionFailedError("Text '" + textElement + "' unexpectedly found. Actual content: \n" + content);
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
