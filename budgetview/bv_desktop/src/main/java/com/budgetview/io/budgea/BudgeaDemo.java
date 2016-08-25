package com.budgetview.io.budgea;

import com.budgetview.shared.cloud.BudgeaConstants;
import org.apache.http.client.fluent.Request;
import org.globsframework.utils.Strings;
import org.json.JSONObject;

import static com.budgetview.shared.json.Json.json;

public class BudgeaDemo {
  public static void main(String[] args) throws Exception {

    BudgeaConstants.setProd();

    JSONObject auth = json(Request.Post(BudgeaConstants.getServerUrl("/auth/init")));
    String token = auth.getString("auth_token");
    System.out.println("Token: " + token);

    System.out.println("\n\n---------------- categories:\n");
    JSONObject categories = json(Request.Get(BudgeaConstants.getServerUrl("/categories"))
                                   .addHeader("Authorization", "Bearer " + token));
    System.out.println(categories.toString(2));
    dumpCategories(categories);

//    System.out.println("\n\n---------------- bank:\n");
//    JSONObject banks = json(Request.Get(BudgeaConstants.getServerUrl("/banks"))
//                             .addHeader("Authorization", "Bearer " + bearer));
//    System.out.println(banks.toString(2));
//    dumpBanks(banks);

//    System.out.println("\n\n---------------- create connection:\n");
//    Request request = Request.Post(BudgeaConstants.getServerUrl("/users/me/connections"))
//      .addHeader("Authorization", "Bearer " + bearer)
//      .bodyForm(Form.form()
//                  .add("id_bank", "40")
//                  .add("website", "par")
//                  .add("login", "123456789")
//                  .add("password", "1234")
//                  .build());
//    JSONObject connection = json(request);
//    System.out.println(connection.toString(2));

//    System.out.println("\n\n---------------- connections:\n");
//    JSONObject connections = json(Request.Get(BudgeaConstants.getServerUrl("/users/" + userId + "/connections"))
//      .addHeader("Authorization", "Bearer " + bearer));
//    System.out.println(connections.toString(2));

//    System.out.println("\n\n---------------- users/me:\n");
//    JSONObject user = json(Request.Get(BudgeaConstants.getServerUrl("/users/me"))
//                             .addHeader("Authorization", "Bearer " + bearer));
//    System.out.println(user.toString(2));
//    int userId = user.getInt("id");

//    System.out.println("\n\n---------------- categories:\n");
//    JSONObject categories = json(Request.Get(BudgeaConstants.getServerUrl("/users/" + userId + "/categories/full"))
//                                 .addHeader("Authorization", "Bearer " + bearer));
//    System.out.println(categories.toString(2));
//    dumpCategories(categories);

//    System.out.println("\n\n---------------- accounts:\n");
//    JSONObject accounts = json(Request.Get(BudgeaConstants.getServerUrl("/users/" + userId + "/accounts"))
//                                    .addHeader("Authorization", "Bearer " + bearer));
//    System.out.println(accounts.toString(2));
//
//    JSONArray accountArray = accounts.getJSONArray("accounts");
//    for (Object o : accountArray) {
//      JSONObject account = (JSONObject)o;
//      System.out.println("\n\n---------------- transactions for " + account.get("name") + " " + account.get("number") + ":\n");
//
//      JSONObject transactions = json(Request.Get(BudgeaConstants.getServerUrl("/users/" + userId + "/accounts/" + account.get("id") + "/transactions"))
//                                   .addHeader("Authorization", "Bearer " + bearer));
//      System.out.println(transactions.toString(2));
//    }


//    Content content = request.returnContent();
//    System.out.println(content.asString());
//    JSONObject connections = json(content);
//
//    System.out.println(connections);
  }

  private static void dumpCategories(JSONObject categories) {
    for (Object item : categories.getJSONArray("categories")) {
      JSONObject jsonItem = (JSONObject) item;
      printCategory("", jsonItem);

      for (Object child : jsonItem.getJSONArray("children")) {
        JSONObject jsonChild = (JSONObject) child;
        printCategory("    ", jsonChild);
      }
    }
  }

  private static void printCategory(String indent, JSONObject item) {
    System.out.println("[" + Strings.rightAlign(Integer.toString(item.getInt("id")), 4) + "] " + indent + item.getString("name"));
  }

  private static void dumpBanks(JSONObject banks) {
    System.out.println("banks:");
    for (Object item : banks.getJSONArray("banks")) {
      JSONObject bank = (JSONObject) item;
      System.out.println("  " + bank.get("name") + " (" + bank.optString("code") + ")==> " + bank.get("id"));

    }
  }

}
