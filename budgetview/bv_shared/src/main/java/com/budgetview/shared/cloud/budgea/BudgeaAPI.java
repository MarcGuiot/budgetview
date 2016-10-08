package com.budgetview.shared.cloud.budgea;

import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.budgetview.shared.json.Json.json;

public class BudgeaAPI {
  private String bearer;
  private int userId = -1;
  
  public JSONObject getBanks() throws IOException {
    return json(Request.Get(BudgeaConstants.getServerUrl("/banks?expand=fields"))
                  .addHeader("Authorization", "Bearer " + getBearer()));
  }

  public JSONObject getBankFields(int budgeaBankId) throws IOException {
    return json(Request.Get(BudgeaConstants.getServerUrl("/banks/" + budgeaBankId + "/fields"))
                  .addHeader("Authorization", "Bearer " + getBearer()));
  }

  public JSONObject registerConnection(Integer budgeaBankId, Map<String, String> params) throws IOException {
    Form form = Form.form()
      .add("id_bank", Integer.toString(budgeaBankId));

    for (Map.Entry<String, String> entry : params.entrySet()) {
      form.add(entry.getKey(), entry.getValue());
    }

    List<NameValuePair> pairs = form.build();
    System.out.println("BudgeaAPI.registerConnection: " + pairs + " for token " + getToken());
    String url = BudgeaConstants.getServerUrl("/users/me/connections");
    Request request = Request.Post(url)
      .addHeader("Authorization", "Bearer " + getBearer())
      .bodyForm(pairs, Consts.UTF_8);

    HttpResponse httpResponse = request.execute().returnResponse();
    if (httpResponse.getStatusLine().getStatusCode() != 200) {
      throw new IOException(url + " returned " + httpResponse.getStatusLine().getStatusCode() + " instead of 200");
    }

    return json(httpResponse);
  }

  public void ping() throws IOException {
    Response response = Request.Get(BudgeaConstants.getServerUrl("/ping")).execute();
    System.out.println("BudgeaAPI.ping: " + response.returnResponse().getStatusLine().getStatusCode());
  }

  public String getToken() throws IOException {
    return getBearer();
  }

  public Integer getUserId() throws IOException {
    return userId;
  }

  private String getBearer() throws IOException {
    if (bearer == null) {
      JSONObject auth = json(Request.Post(BudgeaConstants.getServerUrl("/auth/init")));
      bearer = auth.getString("auth_token");

      JSONObject user = json(Request.Get(BudgeaConstants.getServerUrl("/users/me"))
                               .addHeader("Authorization", "Bearer " + bearer));
      userId = user.getInt("id");
    }
    return bearer;
  }
}
