package com.budgetview.shared.cloud.budgea;

import com.budgetview.shared.http.Http;
import com.budgetview.shared.json.Json;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.globsframework.utils.Strings;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.budgetview.shared.json.Json.json;

public class BudgeaAPI {

  private String token;

  public static String requestFirstTemporaryToken() throws IOException {
    String url = "/auth/init";
    JSONObject auth = json(Request.Post(BudgeaConstants.getServerUrl(url)), url);
    return auth.getString(BudgeaConstants.AUTH_TOKEN);
  }

  public static String requestTemporaryToken(String permanentToken) throws IOException {
    String url = BudgeaConstants.getServerUrl("/auth/token/code");
    JSONObject auth = json(Request.Get(url)
                             .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + permanentToken), url);
    String code = auth.optString(BudgeaConstants.CODE);
    if (Strings.isNullOrEmpty(code)) {
      throw new IOException("No parameter " + BudgeaConstants.CODE + " found in:\n" + auth.toString(2));
    }
    return code;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public JSONObject getUsers() throws IOException {
    checkToken();
    String url = "/users";
    return json(Request.Get(BudgeaConstants.getServerUrl(url))
                  .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + token), url);
  }

  public void deleteUser(int userId) throws IOException {
    checkToken();
    String url = "/users/" + userId;
    Http.execute(Request.Delete(BudgeaConstants.getServerUrl(url))
                   .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + token), url);
  }

  public JSONObject getBanks() throws IOException {
    checkToken();
    String url = "/banks?expand=fields";
    return json(Request.Get(BudgeaConstants.getServerUrl(url))
                  .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + token), url);
  }

  public JSONObject getBankFields(int budgeaBankId) throws IOException {
    checkToken();
    String url = "/banks/" + budgeaBankId + "/fields";
    JSONObject json = json(Request.Get(BudgeaConstants.getServerUrl(url))
                             .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + token), url);
    System.out.println("BudgeaAPI.getBankFields: \n" + json.toString(2));
    return json;
  }

  public JSONObject getUserConnections(int userId) throws IOException {
    checkToken();
    String url = "/users/" + userId + "/connections";
    return json(Request.Get(BudgeaConstants.getServerUrl(url))
                  .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + token)
                  .addHeader("user_id", "me"), url);
  }

  public static class LoginResult {
    public final boolean singleStepLogin;
    public final JSONObject json;

    public LoginResult(boolean singleStepLogin, JSONObject json) {
      this.singleStepLogin = singleStepLogin;
      this.json = json;
    }
  }

  public LoginResult addBankConnectionStep1(Integer budgeaBankId, Map<String, String> params) throws IOException {
    checkToken();

    Form form = Form.form()
      .add("id_bank", Integer.toString(budgeaBankId));

    for (Map.Entry<String, String> entry : params.entrySet()) {
      System.out.println("BudgeaAPI.addBankConnectionStep1: " + entry.getKey() + " ==> " + entry.getValue());
      form.add(entry.getKey(), entry.getValue());
    }

    List<NameValuePair> pairs = form.build();
    String url = BudgeaConstants.getServerUrl("/users/me/connections");
    Request request = Request.Post(url)
      .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + token)
      .bodyForm(pairs, Consts.UTF_8);

    HttpResponse response = request.execute().returnResponse();
    StatusLine statusLine = response.getStatusLine();
    int statusCode = statusLine.getStatusCode();
    System.out.println("BudgeaAPI.addBankConnectionStep1 (" + url + ")  returned " + statusLine + " ==> " + statusCode);
    if (statusCode != 200 && statusCode != 202) {
      throw new IOException(url + " returned " + statusCode + " instead of 200");
    }

    return new LoginResult(statusCode == 200, Json.json(response));
  }

  public JSONObject addBankConnectionStep2(Integer connectionId, Map<String, String> params) throws IOException {
    checkToken();

    Form form = Form.form();
    for (Map.Entry<String, String> entry : params.entrySet()) {
      form.add(entry.getKey(), entry.getValue());
    }

    List<NameValuePair> pairs = form.build();
    System.out.println("BudgeaAPI.addBankConnectionStep2: " + pairs + " for token " + token);
    String url = BudgeaConstants.getServerUrl("/users/me/connections/" + connectionId + "?expand=accounts");
    Request request = Request.Post(url)
      .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + token)
      .bodyForm(pairs, Consts.UTF_8);
    return Http.executeAndGetJson(url, request);
  }

  public void deleteConnection(int budgeaConnectionId) throws IOException {
    String url = "/users/me/connections/" + budgeaConnectionId;
    Http.execute(Request.Delete(BudgeaConstants.getServerUrl(url))
                   .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + token), url);
  }

  public Integer getUserId() throws IOException {
    checkToken();
    String url = "/users/me";
    JSONObject user = json(Request.Get(BudgeaConstants.getServerUrl(url))
                             .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + token), url);
    return user.getInt("id");
  }

  public String getToken() throws IOException {
    return token;
  }

  private void checkToken() throws IOException {
    if (Strings.isNullOrEmpty(token)) {
      throw new IOException("No temp token provided");
    }
  }

  public static JSONObject json(Request request, String url) throws IOException {
    return Http.executeAndGetJson(url, request);
  }
}
