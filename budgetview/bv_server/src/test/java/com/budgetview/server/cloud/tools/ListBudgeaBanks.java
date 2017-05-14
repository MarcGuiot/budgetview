package com.budgetview.server.cloud.tools;

import com.budgetview.shared.cloud.budgea.BudgeaAPI;
import com.budgetview.shared.cloud.budgea.BudgeaConstants;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ListBudgeaBanks {

  public static void main(String[] args) throws Exception {

    BudgeaConstants.setProd();
    String token = BudgeaAPI.requestFirstTemporaryToken();

    BudgeaAPI api = new BudgeaAPI();
    api.setToken(token);

    System.out.println("<div class=\"row\">");
    System.out.println("  <div class=\"col-md-4\">");
    System.out.println("    <ul>");
    int count = 0;
    JSONArray banks = api.getBanks().getJSONArray("banks");
    int total = banks.length();;
    for (Object item : banks) {
      JSONObject bank = (JSONObject) item;
      if (bank.getInt("id") != 40) {
        System.out.println("      <li>" + bank.get("name") + "</li>");
        if (count == total / 3 || count == 2 * total / 3) {
          System.out.println("    <ul>");
          System.out.println("  </div>");
          System.out.println("  <div class=\"col-md-4\">");
          System.out.println("    <ul>");
        }
        count++;
      }
    }
    System.out.println("    </ul>");
    System.out.println("  </div>");
    System.out.println("</div>");

    System.out.println("\n\n====== " + count + " banks ======");
  }

  public static void dumpHtmlBank(JSONObject bank) {
    if (bank.getInt("id") != 40) {
    }
  }
}
