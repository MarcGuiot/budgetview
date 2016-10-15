package com.budgetview.server.cloud.functests.checkers;

import com.budgetview.server.license.checkers.Email;
import com.budgetview.server.license.checkers.MailServerChecker;
import org.globsframework.utils.exceptions.InvalidState;
import org.junit.Assert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CloudMailbox {

  private final Pattern CODE_PATTERN = Pattern.compile(".*<span id=['\"]code['\"]>([A-z0-9]+)</span>.*");

  protected MailServerChecker mailServer;

  public void start() {
    if (mailServer != null) {
      throw new InvalidState("Mail server already started");
    }
    mailServer = new MailServerChecker();
    mailServer.start();
  }

  public String getVerificationCode(String mailTo) throws Exception {
    if (mailServer == null) {
      throw new InvalidState("start() not called");
    }

    Email email = mailServer.checkReceivedMail(mailTo);
    String content = email.getContent();

    Matcher matcher = CODE_PATTERN.matcher(content);
    if (!matcher.matches()) {
      Assert.fail("Email does not contain any code: " + content);
    }

    return matcher.group(1);
  }

  public void stop() throws Exception {
    mailServer.stop();
    mailServer = null;
  }
}
