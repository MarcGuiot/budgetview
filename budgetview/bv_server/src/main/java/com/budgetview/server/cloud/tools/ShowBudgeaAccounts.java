package com.budgetview.server.cloud.tools;

import com.budgetview.shared.cloud.budgea.BudgeaConstants;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;

public class ShowBudgeaAccounts {

  public static void main(String[] args) throws Exception {

//    JSONObject auth = json(Request.Post(BudgeaConstants.PROD_SERVER_URL + "/auth/init"));
//    String token = auth.getString("auth_token");
//    System.out.println("ShowBudgeaAccounts.main - token:" + token);

    String token = "duhxkHw+dIEeWG3iS0tozQg2Y67R4jbl";

    Request request = Request.Get(BudgeaConstants.PROD_SERVER_URL + "/users")
      .addHeader("Authorization", "Bearer " + token);
//      .bodyForm(Form.form()
//                  .add("client_id", Budgea.CLIENT_ID)
//                  .add("client_secret", Budgea.CLIENT_SECRET)
//                  .build());


    Response response = request.execute();
    System.out.println("Response: " + response.returnContent().asString());
  }
}
