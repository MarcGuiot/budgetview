package com.budgetview.server.cloud.tools;

import com.budgetview.shared.cloud.budgea.BudgeaAPI;
import com.budgetview.shared.cloud.budgea.BudgeaConstants;
import org.json.JSONObject;

public class ListBudgeaBanks {

  public static void main(String[] args) throws Exception {

    BudgeaConstants.setProd();
    String token = BudgeaAPI.requestFirstTemporaryToken();

    BudgeaAPI api = new BudgeaAPI();
    api.setToken(token);

    JSONObject banks = api.getBanks();
    System.out.println(banks.toString(2));

    System.out.println("=========================================");

    for (Object item : banks.getJSONArray("banks")) {
      JSONObject bank = (JSONObject) item;
//      System.out.println("  " + bank.get("name") + " (BIC " + bank.optString("code") + ")==> " + bank.get("id"));

      System.out.println("   <bank name=\"" + bank.get("name") + "\" country=\"fr\" url=\"\" id=\"\"\n" +
                         "          provider=\"2\" providerId=\"" + bank.get("id") + "\">\n" +
                         "      <bankEntity id=\"" + bank.optString("code") + "\"/>\n" +
                         "    </bank>\n");
    }
  }
}
