package com.budgetview.server.cloud.tools;

import com.budgetview.shared.cloud.budgea.BudgeaAPI;
import com.budgetview.shared.cloud.budgea.BudgeaConstants;
import org.json.JSONObject;

public class ListBudgeaConnections {

  public static void main(String[] args) throws Exception {

    BudgeaConstants.setProd();
    String token = BudgeaAPI.requestFirstTemporaryToken();
    System.out.println("token:" + token);

    BudgeaAPI api = new BudgeaAPI();
    api.setTempToken(token, false);

    int userId = api.getUserId();
    System.out.println("ListBudgeaConnections.main: " + userId);

    JSONObject users = api.getUserConnections(userId);
    System.out.println(users.toString(2));
  }
}
