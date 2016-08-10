package com.budgetview.cloud.functests.checkers;

import com.budgetview.shared.cloud.CloudAPI;

public class CloudChecker {

  public void register(String email, Integer budgeaUserId, String budgeaToken) throws Exception {
    CloudAPI api = new CloudAPI();
    api.addConnection(email, budgeaToken, budgeaUserId);
  }

  public void checkBankStatement(String expected) throws Exception {

  }
}
