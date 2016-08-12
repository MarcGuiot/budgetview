package com.budgetview.cloud.functests.checkers;

import com.budgetview.shared.cloud.CloudAPI;
import org.json.JSONObject;

public class CloudChecker {

  public void register(String email, Integer budgeaUserId, String budgeaToken) throws Exception {
    CloudAPI api = new CloudAPI();
    api.addConnection(email, budgeaToken, budgeaUserId);
  }

  public void checkBankStatement(String email, String expected) throws Exception {
    CloudAPI api = new CloudAPI();
    String json = api.getStatement(email);
    JSONObject object = new JSONObject(json);
    System.out.println("CloudChecker.checkBankStatement: " + object.toString(2));
  }
}
