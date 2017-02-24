package com.budgetview.server.cloud.functests.checkers;

import com.budgetview.server.cloud.services.CloudSubscription;
import com.budgetview.server.cloud.services.PaymentService;
import org.globsframework.utils.Dates;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.OperationFailed;

import static junit.framework.TestCase.assertEquals;

public class PaymentChecker {

  private DummyPaymentService paymentService = new DummyPaymentService();
  private String lastEmail;
  private String lastToken;

  public PaymentChecker() {
  }

  public void install(Directory directory) {
    directory.add(PaymentService.class, paymentService);
  }

  public PaymentChecker checkLastRequest(String email, String token) {
    assertEquals(email + " / " + token, lastEmail + " / " + lastToken);
    return this;
  }

  private class DummyPaymentService implements PaymentService {

    private int index = 100000;

    public CloudSubscription createSubscription(String email, String token) throws OperationFailed {
      PaymentChecker.this.lastEmail = email;
      PaymentChecker.this.lastToken = token;
      return new CloudSubscription("sub" + index++, Dates.tomorrow());
    }

  }
}
