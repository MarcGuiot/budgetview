package com.budgetview.server.cloud.functests;

import com.budgetview.server.cloud.functests.testcases.CloudDesktopTestCase;
import com.budgetview.server.cloud.utils.WebsiteUrls;
import org.junit.Test;

public class CloudSubscriptionTest extends CloudDesktopTestCase {

  @Test
  public void test() throws Exception {

//    String url = "http://127.0.0.1:8080/subscription/validation?code=absc";
//    String redirect = Http.executeAndGetRedirect(url, Request.Get(url));
//    System.out.println("CloudSubscriptionTest.test: " + redirect);
//    System.out.flush();

    subscriptions.submitStripeForm("toto@example.com", "abcdef012345", WebsiteUrls.emailSent());
    mailbox.clickSubscriptionValidationLink("toto@example.com", WebsiteUrls.subscriptionCreated());
    payments.checkLastRequest("toto@example.com", "abcdef012345");
  }
}
