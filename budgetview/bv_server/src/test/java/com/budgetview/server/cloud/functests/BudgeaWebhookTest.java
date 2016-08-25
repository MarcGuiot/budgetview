package com.budgetview.server.cloud.functests;

import com.budgetview.server.cloud.functests.checkers.CloudServerTestCase;
import com.budgetview.server.cloud.stub.BudgeaStatement;

public class BudgeaWebhookTest extends CloudServerTestCase {

  public void test() throws Exception {

    budgea.setPersistentToken("--persistent-token--");

    cloud.register("regis@mybudgetview.fr", 472, "--temporary-token--");

    budgea.callWebhook("--persistent-token--", BudgeaStatement.init()
      .addConnection(1, 472, 40, "Connecteur de Test Budgea", "2016-08-10 17:44:26")
      .addAccount(1, "Main account", "100200300", "checking", 1000.00, "2016-08-10 13:00:00")
      .addTransaction(1, "2016-08-10 13:00:00", -100.00, "AUCHAN")
      .endAccount()
      .endConnection()
      .get());

    cloud.checkBankStatement("regis@mybudgetview.fr", 40, "aaa");
  }

}
