package com.budgetview.server.cloud.functests;

import com.budgetview.server.cloud.stub.BudgeaSamples;
import com.budgetview.server.cloud.functests.checkers.CloudServerTestCase;

public class BudgeaWebhookTest extends CloudServerTestCase {

  public void test() throws Exception {

    budgea.setPersistentToken("--persistent-token--");
    cloud.register("regis@mybudgetview.fr", 472, "--temporary-token--");

    budgea.callWebhook("--persistent-token--", BudgeaSamples.firstWebhookCall(472));

    cloud.checkBankStatement("regis@mybudgetview.fr", 40, "aaa");
  }

}
