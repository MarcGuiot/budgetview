package com.budgetview.server.cloud.functests.checkers;

import com.budgetview.shared.cloud.CloudAPI;
import com.budgetview.shared.model.Provider;
import org.json.JSONObject;

public class CloudChecker {

  public void register(String email, Integer budgeaUserId, String budgeaToken) throws Exception {
    CloudAPI api = new CloudAPI();
    api.addConnection(email, budgeaToken, budgeaUserId);
  }

  public void checkBankStatement(String email, int budgeaBankId, String expected) throws Exception {
    CloudAPI api = new CloudAPI();
    JSONObject object = api.getStatement(email, Provider.BUDGEA, budgeaBankId);
    System.out.println("CloudChecker.checkBankStatement: " + object.toString(2));
  }
}
