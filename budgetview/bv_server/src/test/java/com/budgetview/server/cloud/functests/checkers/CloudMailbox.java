package com.budgetview.server.cloud.functests.checkers;

import com.budgetview.server.license.checkers.Email;
import com.budgetview.server.license.checkers.MailServerChecker;
import com.budgetview.shared.cloud.budgea.BudgeaConstants;
import com.budgetview.shared.http.Http;
import org.apache.http.client.fluent.Request;
import org.globsframework.utils.exceptions.InvalidState;
import org.junit.Assert;
import org.uispec4j.assertion.Assertion;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CloudMailbox {

  private final Pattern DEVICE_VERIFICATION_CODE_PATTERN = Pattern.compile(".*<span id=['\"]code['\"]>([A-z0-9]+)</span>.*");
  private final Pattern SUBSCRIPTION_VALIDATION_LINK_PATTERN = Pattern.compile(".*<span id=['\"]link['\"]>(http[s]?://[A-z0-9.?=:/_-]+)</span>.*");

  protected MailServerChecker mailServer;

  public void start() {
    if (mailServer != null) {
      throw new InvalidState("Mail server already started");
    }
    mailServer = new MailServerChecker();
    mailServer.start();
  }

  public String getDeviceVerificationCode(String mailTo) throws Exception {
    String content = getEmail(mailTo).getContent();

    Matcher matcher = DEVICE_VERIFICATION_CODE_PATTERN.matcher(content);
    if (!matcher.matches()) {
      Assert.fail("Email does not contain any code: " + content);
    }
    return matcher.group(1);
  }

  public Assertion checkStatementReady(final String mailTo) throws Exception {
    return new Assertion() {
      public void check() {
        try {
          Email email = getEmail(mailTo);
          email.checkSubjectContains("ready");
        }
        catch (InterruptedException e) {
          Assert.fail(e.getMessage());
        }
      }
    };
  }

  public void clickSubscriptionValidationLink(String mailTo, String expectedRedirect) throws Exception {
    String content = getEmail(mailTo).getContent();
    Matcher matcher = SUBSCRIPTION_VALIDATION_LINK_PATTERN.matcher(content);
    if (!matcher.matches()) {
      Assert.fail("Email does not contain any link: " + content);
    }
    String url = matcher.group(1);
    String redirect = Http.executeAndGetRedirect(url, Request.Get(url));
    Assert.assertEquals(expectedRedirect, redirect);
  }

  private Email getEmail(String mailTo) throws InterruptedException {
    if (mailServer == null) {
      throw new InvalidState("start() not called");
    }

    return mailServer.checkReceivedMail(mailTo);
  }

  public void checkEmpty() throws InterruptedException {
    if (mailServer == null) {
      throw new InvalidState("start() not called");
    }

    mailServer.checkEmpty();
  }

  public void stop() throws Exception {
    mailServer.stop();
    mailServer = null;
  }
}
