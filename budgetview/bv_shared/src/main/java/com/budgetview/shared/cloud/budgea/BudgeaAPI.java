package com.budgetview.shared.cloud.budgea;

import com.budgetview.shared.http.Http;
import com.budgetview.shared.json.Json;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.globsframework.utils.Strings;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.budgetview.shared.json.Json.json;

public class BudgeaAPI {

  private String tempToken;
  private boolean permanentTokenRegistered;

  public static String requestFirstTemporaryToken() throws IOException {
    String url = "/auth/init";
    JSONObject auth = json(Request.Post(BudgeaConstants.getServerUrl(url)), url);
    return auth.getString(BudgeaConstants.AUTH_TOKEN);
  }

  public static String requestTemporaryToken(String permanentToken) throws IOException {
    String url = "/auth/token/code";
    JSONObject auth = json(Request.Post(BudgeaConstants.getServerUrl(url))
                             .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + permanentToken), url);
    return auth.getString(BudgeaConstants.AUTH_TOKEN);
  }

  public void setTempToken(String token, boolean permanentTokenRegistered) {
    this.tempToken = token;
    this.permanentTokenRegistered = permanentTokenRegistered;
  }

  public JSONObject getUsers() throws IOException {
    checkTempToken();
    String url = "/users";
    return json(Request.Get(BudgeaConstants.getServerUrl(url))
                  .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + tempToken), url);
  }

  public JSONObject getUserConnections(int userId) throws IOException {
    checkTempToken();
    String url = "/users/" + userId + "/connections";
    return json(Request.Get(BudgeaConstants.getServerUrl(url))
                  .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + tempToken)
                  .addHeader("user_id", "me"), url);
  }

  public JSONObject getBanks() throws IOException {
    checkTempToken();
    String url = "/banks?expand=fields";
    return json(Request.Get(BudgeaConstants.getServerUrl(url))
                  .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + tempToken), url);
  }

  public JSONObject getBankFields(int budgeaBankId) throws IOException {
    checkTempToken();
    String url = "/banks/" + budgeaBankId + "/fields";
    return json(Request.Get(BudgeaConstants.getServerUrl(url))
                  .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + tempToken), url);
  }

  public JSONObject addBankConnection(Integer budgeaBankId, Map<String, String> params) throws IOException {
    checkTempToken();

    Form form = Form.form()
      .add("id_bank", Integer.toString(budgeaBankId));

    for (Map.Entry<String, String> entry : params.entrySet()) {
      form.add(entry.getKey(), entry.getValue());
    }

    List<NameValuePair> pairs = form.build();
    System.out.println("BudgeaAPI.addBankConnection: " + pairs + " for token " + tempToken);
    String url = BudgeaConstants.getServerUrl("/users/me/connections");
    Request request = Request.Post(url)
      .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + tempToken)
      .bodyForm(pairs, Consts.UTF_8);

    HttpResponse httpResponse = request.execute().returnResponse();
    if (httpResponse.getStatusLine().getStatusCode() != 200) {
      throw new IOException(url + " returned " + httpResponse.getStatusLine().getStatusCode() + " instead of 200");
    }

    return Json.json(httpResponse);
  }

  public Integer getUserId() throws IOException {
    checkTempToken();
    String url = "/users/me";
    JSONObject user = json(Request.Get(BudgeaConstants.getServerUrl(url))
                             .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + tempToken), url);
    return user.getInt("id");
  }

  public String getToken() throws IOException {
    return tempToken;
  }

  public boolean isPermanentTokenRegistered() {
    return permanentTokenRegistered;
  }

  private void checkTempToken() throws IOException {
    if (Strings.isNullOrEmpty(tempToken)) {
      throw new IOException("No temp token provided");
    }
  }

  public static JSONObject json(Request request, String url) throws IOException {
    return Http.executeAndGetJson(url, request);
  }
}
