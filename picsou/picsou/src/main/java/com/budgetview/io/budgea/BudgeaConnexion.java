package com.budgetview.io.budgea;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.json.JSONObject;

import java.io.IOException;

public class BudgeaConnexion {
  public static void main(String[] args) throws Exception {

    JSONObject auth = json(Request.Post("https://budgetview.biapi.pro/2.0/auth/init"));

    String bearer = auth.getString("auth_token");

//    System.out.println("\n\n---------------- banks:\n");
//    JSONObject banks = json(Request.Get("https://budgetview.biapi.pro/2.0/banks?expand=fields")
//                              .addHeader("Authorization", "Bearer " + bearer));
//    JSONArray array = banks.getJSONArray("banks");
//    for (Object o : array) {
//      System.out.println(((JSONObject) o).toString(2));
//    }

    System.out.println("\n\n---------------- users:\n");
    JSONObject user = json(Request.Get("https://budgetview.biapi.pro/2.0/users/me")
                             .addHeader("Authorization", "Bearer " + bearer));
    System.out.println(user.toString(2));
    int userId = user.getInt("id");

//    System.out.println("\n\n---------------- categories:\n");
//    JSONObject categories = json(Request.Get("https://budgetview.biapi.pro/2.0/users/me/categories")
//                                   .addHeader("Authorization", "Bearer " + bearer));
//    System.out.println(categories);

    System.out.println("\n\n---------------- bank:\n");
    JSONObject bank = json(Request.Get("https://budgetview.biapi.pro/2.0/banks/40")
                             .addHeader("Authorization", "Bearer " + bearer));
    System.out.println(bank.toString(2));

    System.out.println("\n\n---------------- connections:\n");
    JSONObject connections = json(Request.Get("https://budgetview.biapi.pro/2.0/users/me/connections?expand=accounts")
                                    .addHeader("Authorization", "Bearer " + bearer));
    System.out.println(connections);

    System.out.println("\n\n---------------- create connection:\n");
    Response request = Request.Post("https://budgetview.biapi.pro/2.0/users/me/connections")
      .addHeader("Authorization", "Bearer " + bearer)
      .addHeader("user_id", Integer.toString(userId))
      .addHeader("id_bank", "40")
      .addHeader("website", "par")
      .addHeader("login", "123456789")
      .addHeader("password", "1234")
      .execute();
    System.out.println(request.returnResponse());

//    Content content = request.returnContent();
//    System.out.println(content.asString());
//    JSONObject connections = json(content);
//
//    System.out.println(connections);
  }

  public static JSONObject json(Request request) throws IOException {
    return new JSONObject(request.execute().returnContent().asString());
  }
}
