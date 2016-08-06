package com.budgetview.io.budgea;

import com.budgetview.utils.Json;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.json.JSONArray;
import org.json.JSONObject;

import static com.budgetview.utils.Json.json;

public class BudgeaDemo {
  public static void main(String[] args) throws Exception {

    JSONObject auth = json(Request.Post("https://budgetview.biapi.pro/2.0/auth/init"));
    String bearer = auth.getString("auth_token");


//    System.out.println("\n\n---------------- categories:\n");
//    JSONObject categories = json(Request.Get("https://budgetview.biapi.pro/2.0/users/me/categories")
//                                   .addHeader("Authorization", "Bearer " + bearer));
//    System.out.println(categories);

//    System.out.println("\n\n---------------- bank:\n");
//    JSONObject bank = json(Request.Get("https://budgetview.biapi.pro/2.0/banks/40")
//                             .addHeader("Authorization", "Bearer " + bearer));
//    System.out.println(bank.toString(2));

    System.out.println("\n\n---------------- create connection:\n");
    Request request = Request.Post("https://budgetview.biapi.pro/2.0/users/me/connections")
      .addHeader("Authorization", "Bearer " + bearer)
      .bodyForm(Form.form()
                  .add("id_bank", "40")
                  .add("website", "par")
                  .add("login", "123456789")
                  .add("password", "1234")
                  .build());
    JSONObject connection = json(request);
    System.out.println(connection.toString(2));

//    System.out.println("\n\n---------------- connections:\n");
//    JSONObject connections = json(Request.Get("https://budgetview.biapi.pro/2.0/users/" + userId + "/connections")
//      .addHeader("Authorization", "Bearer " + bearer));
//    System.out.println(connections.toString(2));

    System.out.println("\n\n---------------- users/me:\n");
    JSONObject user = json(Request.Get("https://budgetview.biapi.pro/2.0/users/me")
                             .addHeader("Authorization", "Bearer " + bearer));
    System.out.println(user.toString(2));
    int userId = user.getInt("id");

//    System.out.println("\n\n---------------- categories:\n");
//    JSONObject categories = json(Request.Get("https://budgetview.biapi.pro/2.0/users/" + userId + "/categories/full")
//                                 .addHeader("Authorization", "Bearer " + bearer));
//    System.out.println(categories.toString(2));
//    dumpCategories(categories);

    System.out.println("\n\n---------------- accounts:\n");
    JSONObject accounts = json(Request.Get("https://budgetview.biapi.pro/2.0/users/" + userId + "/accounts")
                                    .addHeader("Authorization", "Bearer " + bearer));
    System.out.println(accounts.toString(2));

    JSONArray accountArray = accounts.getJSONArray("accounts");
    for (Object o : accountArray) {
      JSONObject account = (JSONObject)o;
      System.out.println("\n\n---------------- transactions for " + account.get("name") + " " + account.get("number") + ":\n");

      JSONObject transactions = json(Request.Get("https://budgetview.biapi.pro/2.0/users/" + userId + "/accounts/" + account.get("id") + "/transactions")
                                   .addHeader("Authorization", "Bearer " + bearer));
      System.out.println(transactions.toString(2));
    }


//    Content content = request.returnContent();
//    System.out.println(content.asString());
//    JSONObject connections = json(content);
//
//    System.out.println(connections);
  }

  private static void dumpCategories(JSONObject categories) {
    for (Object item : categories.getJSONArray("categories")) {
      JSONObject jsonItem = (JSONObject) item;
      System.out.println(jsonItem.get("name"));

      for (Object child : jsonItem.getJSONArray("children")) {
        JSONObject jsonChild = (JSONObject) child;
        System.out.println("  "  +jsonChild.get("name"));
      }
    }

  }

}
