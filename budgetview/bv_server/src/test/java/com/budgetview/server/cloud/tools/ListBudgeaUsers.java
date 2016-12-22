package com.budgetview.server.cloud.tools;

import com.budgetview.shared.cloud.budgea.BudgeaAPI;
import com.budgetview.shared.cloud.budgea.BudgeaConstants;
import org.json.JSONObject;

import java.io.IOException;

public class ListBudgeaUsers {

  public static void main(String[] args) throws Exception {

    BudgeaConstants.setProd();
    String token = BudgeaTools.getMasterToken(args);

    BudgeaAPI api = new BudgeaAPI();
    api.setToken(token, false);

    JSONObject users = api.getUsers();
    System.out.println(users.toString(2));

//    deleteAllUsers(api, users);
  }

  public static void deleteAllUsers(BudgeaAPI api, JSONObject users) throws IOException {
    for (Object u : users.getJSONArray("users")) {
      JSONObject user = (JSONObject) u;
      int id = user.getInt("id");
      System.out.println("Deleting " + id);
      api.deleteUser(id);
    }
  }
}
