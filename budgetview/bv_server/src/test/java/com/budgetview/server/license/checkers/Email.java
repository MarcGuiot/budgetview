package com.budgetview.server.license.checkers;

import com.dumbster.smtp.SmtpMessage;
import junit.framework.AssertionFailedError;
import org.globsframework.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

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
    System.out.println("Email.checkSubjectContainsAny - " + subject);
    for (String item : text) {
      if (subject.contains(item)) {
        return this;
      }
    }
    throw new AssertionFailedError("Unexpected subject: " + subject);
  }

  public String getSubject() throws Exception {
    String subject = message.getHeaderValue("Subject");
    System.out.println("Email.getSubject: " + subject);
    return decodeText(subject);
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
    throw new AssertionFailedError("Could not find any of " + Arrays.toString(textElements) + "\nActual content: \n" + content);
  }

  public Email checkDoesNotContain(String... textElements) {
    for (String textElement : textElements) {
      if (content.contains(textElement)) {
        throw new AssertionFailedError("Text '" + textElement + "' unexpectedly found. Actual content: \n" + content);
      }
    }
    return this;
  }

  public String to() {
    return message.getHeaderValue("To");
  }

  public boolean sentTo(String email) {
    return Utils.equalIgnoreCase(email, message.getHeaderValue("To"));
  }

  public String getContent() {
    return content;
  }

  public String toString() {
    String subject = message.getHeaderValue("Subject");
    try {
      subject = decodeText(subject);
    }
    catch (Exception e) {
      subject = "[Decoding error] " + subject;
    }
    return "To:" + to() + " - Subject:" + subject;
  }
}
