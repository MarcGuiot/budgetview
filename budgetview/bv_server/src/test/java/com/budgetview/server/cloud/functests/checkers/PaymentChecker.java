package com.budgetview.server.cloud.functests.checkers;

import com.budgetview.server.cloud.services.CloudSubscription;
import com.budgetview.server.cloud.services.PaymentService;
import org.apache.log4j.Logger;
import org.globsframework.utils.Dates;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.OperationFailed;

import java.util.Date;

import static junit.framework.TestCase.assertEquals;

public class PaymentChecker {

  private DummyPaymentService paymentService = new DummyPaymentService();
  private String lastEmail;
  private String lastToken;
  private Date subscriptionEndDate = Dates.tomorrow();


  public PaymentChecker() {
  }

  public void install(Directory directory) {
    directory.add(PaymentService.class, paymentService);
  }

  public void setSubscriptionEndDate(Date subscriptionEndDate) {
    this.subscriptionEndDate = subscriptionEndDate;
  }

  public PaymentChecker checkLastRequest(String email, String token) {
    assertEquals(email + " / " + token, lastEmail + " / " + lastToken);
    return this;
  }

  private class DummyPaymentService implements PaymentService {

    private Logger logger = Logger.getLogger("DummyPaymentService");

    private int index = 100000;

    public CloudSubscription createSubscription(String email, String token) throws OperationFailed {
      logger.info("createSubscription(" + email + ", " + token + ")");
      PaymentChecker.this.lastEmail = email;
      PaymentChecker.this.lastToken = token;
      return new CloudSubscription("sub" + index++, subscriptionEndDate);
    }

  }
}
