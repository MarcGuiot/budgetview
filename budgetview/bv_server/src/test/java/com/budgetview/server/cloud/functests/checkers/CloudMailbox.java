package com.budgetview.server.cloud.functests.checkers;

import com.budgetview.server.license.checkers.Email;
import com.budgetview.server.license.checkers.MailServerChecker;
import com.budgetview.shared.http.Http;
import org.apache.http.client.fluent.Request;
import org.globsframework.utils.exceptions.InvalidState;
import org.junit.Assert;
import org.uispec4j.assertion.Assertion;

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
          email.checkSubjectContainsAny("statements are ready", "Vos relev");
        }
        catch (Exception e) {
          Assert.fail(e.getMessage());
        }
      }
    };
  }

  public void checkAccountDeleted(final String mailTo) throws Exception {
    Email email = getEmail(mailTo);
    email.checkContainsAny("deleted", "supprim");
  }


  public void checkInvoice(final String mailTo, String receiptNumber) throws Exception {
    Email email = getEmail(mailTo);
    email.checkContainsAny(receiptNumber);
  }

  public void checkInvoicePaymentFailed(final String mailTo, String receiptNumber) throws Exception {
    Email email = getEmail(mailTo);
    email.checkContainsAny(receiptNumber);
    email.checkContainsAny("Le r=C3=A8glement de votre abonnement =C3=A0 =C3=A9chou=C3=A9");
  }

  public void clickSubscriptionValidationLink(String mailTo) throws Exception {
    String content = getEmail(mailTo).getContent();
    Matcher matcher = SUBSCRIPTION_VALIDATION_LINK_PATTERN.matcher(content);
    if (!matcher.matches()) {
      Assert.fail("Email does not contain any link: " + content);
    }
    String url = matcher.group(1);
    Http.execute(url, Request.Get(url));
  }

  private Email getEmail(String mailTo) throws Exception {
    if (mailServer == null) {
      throw new InvalidState("start() not called");
    }

    // Warning: calling this repeatedly within an UISpecAssert will cause the first
    // message to be dumped and having a misleading "No mail received error" in the end
    return mailServer.checkReceivedMail(mailTo);
  }

  public void checkEmpty() throws Exception {
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
