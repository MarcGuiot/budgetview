package com.budgetview.server.cloud.functests.checkers;

import com.budgetview.server.cloud.services.CloudSubscription;
import com.budgetview.server.cloud.services.PaymentService;
import org.apache.log4j.Logger;
import org.globsframework.utils.Dates;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.OperationFailed;
import org.junit.Assert;

import java.util.Date;

import static junit.framework.TestCase.assertEquals;

public class PaymentChecker {

  private DummyPaymentService paymentService = new DummyPaymentService();
  private String lastEmail;
  private String lastToken;
  private String lastSubscriptionId;
  private Date subscriptionEndDate = Dates.tomorrow();
  private String lastCustomerId;
  private String lastDeletedCustomer;
  private String lastDeletedSubscription;

  public PaymentChecker() {
  }

  public void install(Directory directory) {
    directory.add(PaymentService.class, paymentService);
  }

  public void setSubscriptionEndDate(Date subscriptionEndDate) {
    this.subscriptionEndDate = subscriptionEndDate;
  }

  public String checkLastRequest(String email, String token) {
    assertEquals(email + " / " + token, lastEmail + " / " + lastToken);
    return lastSubscriptionId;
  }

  public void checkSubscriptionDeleted(String subscriptionId) {
    if (lastSubscriptionId == null) {
      Assert.fail("No subscription deleted");
    }
    Assert.assertEquals(subscriptionId, lastSubscriptionId);
  }

  private class DummyPaymentService implements PaymentService {

    private Logger logger = Logger.getLogger("DummyPaymentService");

    private int index = 100000;

    public CloudSubscription createSubscription(String email, String token) throws OperationFailed {
      logger.info("createSubscription(" + email + ", " + token + ")");
      PaymentChecker.this.lastEmail = email;
      PaymentChecker.this.lastToken = token;
      PaymentChecker.this.lastCustomerId = "customer/" + index;
      PaymentChecker.this.lastSubscriptionId = "subscription/" + index;
      index++;
      return new CloudSubscription(lastCustomerId, lastSubscriptionId, subscriptionEndDate);
    }

    public void deleteSubscription(String customerId, String subscriptionId) {
      PaymentChecker.this.lastDeletedCustomer = customerId;
      PaymentChecker.this.lastDeletedSubscription = subscriptionId;
    }
  }
}
