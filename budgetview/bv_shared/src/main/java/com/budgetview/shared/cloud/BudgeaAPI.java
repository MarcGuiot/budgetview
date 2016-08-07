package com.budgetview.shared.cloud;

import org.apache.http.Consts;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import static com.budgetview.shared.json.Json.json;

public class BudgeaAPI {
  private String bearer;
  private int userId = -1;

  public JSONObject getBanks() throws IOException {
    return json(Request.Get("https://budgetview.biapi.pro/2.0/banks?expand=fields")
           .addHeader("Authorization", "Bearer " + getBearer()));
  }

  public JSONObject addConnection(Integer budgeaBankId, Map<String, String> params) throws IOException {
    Form form = Form.form()
      .add("id_bank", Integer.toString(budgeaBankId));

    for (Map.Entry<String, String> entry : params.entrySet()) {
      form.add(entry.getKey(), entry.getValue());
    }

    Request request = Request.Post("https://budgetview.biapi.pro/2.0/users/me/connections")
      .addHeader("Authorization", "Bearer " + getBearer())
      .bodyForm(form.build(), Consts.UTF_8);

    return json(request);
  }

  public String getToken() throws IOException {
    return getBearer();
  }

  public Integer getUserId() throws IOException {
    return userId;
  }

  private String getBearer() throws IOException {
    if (bearer == null) {
      JSONObject auth = json(Request.Post("https://budgetview.biapi.pro/2.0/auth/init"));
      bearer = auth.getString("auth_token");

      JSONObject user = json(Request.Get("https://budgetview.biapi.pro/2.0/users/me")
                               .addHeader("Authorization", "Bearer " + bearer));
      userId = user.getInt("id");
    }
    return bearer;
  }

}
