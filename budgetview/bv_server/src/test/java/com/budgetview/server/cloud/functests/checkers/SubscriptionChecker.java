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
    Form form = Form.form()
      .add("stripeEmail", email)
      .add("stripeToken", token);

    String url = CloudConstants.getServerUrl("/stripe-form");
    Request request = Request.Post(url)
      .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + token)
      .bodyForm(form.build(), Consts.UTF_8);

    String redirect = Http.executeAndGetRedirect(url, request);
    Assert.assertEquals(expectedRedirect, redirect);
    return this;
  }
}
