package com.budgetview.server.cloud.tools;

import com.budgetview.shared.cloud.budgea.BudgeaAPI;
import com.budgetview.shared.cloud.budgea.BudgeaConstants;
import org.json.JSONObject;

public class ListBudgeaBanks {

  public static void main(String[] args) throws Exception {

    BudgeaConstants.setProd();
    String token = BudgeaAPI.requestFirstTemporaryToken();

    BudgeaAPI api = new BudgeaAPI();
    api.setToken(token, false);

    JSONObject banks = api.getBanks();
    System.out.println(banks.toString(2));
  }
}
