package com.budgetview.server.cloud.functests;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.model.Bank;
import com.budgetview.shared.cloud.budgea.BudgeaAPI;
import com.budgetview.shared.cloud.budgea.BudgeaConstants;
import com.budgetview.shared.model.Provider;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.format.GlobPrinter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class CloudBanksListTest extends LoggedInFunctionalTestCase {

  public void testBudgetInsightBanksAreAligneWithInternalBanks() throws Exception {

    Map<String, JSONObject> jsonBanks = new HashMap<String, JSONObject>();
    for (Object item : getBudgeaBanks()) {
      JSONObject bank = (JSONObject) item;
      jsonBanks.put(bank.getString("name"), bank);
    }

    Map<String, Glob> globBanks = new HashMap<String, Glob>();
    for (Glob bank : repository.getAll(Bank.TYPE, fieldEquals(Bank.PROVIDER, Provider.BUDGEA.getId()))) {
      String bankName = bank.get(Bank.NAME);
      if (globBanks.containsKey(bankName)) {
        Assert.fail("Duplicate name:" + bank + " / " + globBanks.get(bankName));
      }
      globBanks.put(bankName, bank);
    }

    List<JSONObject> unpairedJsonObjects = new ArrayList<JSONObject>();
    for (JSONObject jsonBank : jsonBanks.values()) {
      if (!globBanks.containsKey(jsonBank.getString("name"))) {
        unpairedJsonObjects.add(jsonBank);
      }
    }

    GlobList unpairedGlobs = new GlobList();
    for (Glob bank : globBanks.values()) {
      if (!jsonBanks.containsKey(bank.get(Bank.NAME))) {
        unpairedGlobs.add(bank);
      }
    }

    if (!unpairedJsonObjects.isEmpty() || !unpairedGlobs.isEmpty()) {
      StringBuilder builder = new StringBuilder("\n");
      if (!unpairedJsonObjects.isEmpty()) {
        builder.append("=== Banks to be added to the XML files ===\n\n");
        for (JSONObject bank : unpairedJsonObjects) {
          builder.append(toXml(bank));
        }
        builder.append("\n\n");
      }
      if (!unpairedGlobs.isEmpty()) {
        builder.append("=== These banks are not managed by BudgetInsight ===\n");
        builder.append(GlobPrinter.toString(unpairedGlobs));
        builder.append("\n\n");
      }
      Assert.fail(builder.toString());
    }
  }

  public static void main(String... args) throws Exception {
    int count = 0;
    for (Object item : getBudgeaBanks()) {
      JSONObject bank = (JSONObject) item;
      dumpHtmlBank(bank);
      count++;
    }
    System.out.println("\n==> " + count + " banks");
  }

  public static void dumpHtmlBank(JSONObject bank) {
    if (bank.getInt("id") != 40) {
      System.out.println("   <li>" + bank.get("name") + "</li>");
    }
  }

  public static void dumpXmlBank(JSONObject bank) {
    System.out.println(toXml(bank));
  }

  private static String toXml(JSONObject bank) {
    return "   <bank name=\"" + bank.get("name") + "\" country=\"fr\" url=\"\" id=\"\"\n" +
           "          provider=\"2\" providerId=\"" + bank.get("id") + "\">\n" +
           "      <bankEntity id=\"" + bank.optString("code") + "\"/>\n" +
           "    </bank>\n";
  }

  private static JSONArray getBudgeaBanks() throws IOException {
    BudgeaConstants.setProd();
    BudgeaAPI api = new BudgeaAPI();
    api.setToken(BudgeaAPI.requestFirstTemporaryToken());
    return api.getBanks().getJSONArray("banks");
  }
}
