package com.budgetview.server.cloud.functests.checkers;

import com.budgetview.shared.cloud.CloudConstants;
import com.budgetview.shared.cloud.budgea.BudgeaConstants;
import com.budgetview.shared.http.Http;
import org.apache.http.Consts;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.junit.Assert;

public class SubscriptionChecker {
  public SubscriptionChecker submitStripeForm(String email, String token, String expectedRedirect) throws Exception {
    String url = CloudConstants.getServerUrl("/stripe-form");
    Request request = Request.Post(url)
      .bodyForm(Form.form()
                  .add("stripeEmail", email)
                  .add("stripeToken", token)
                  .build(),
                Consts.UTF_8);

    String redirect = Http.executeAndGetRedirect(url, request);
    Assert.assertEquals(expectedRedirect, redirect);
    return this;
  }
}
