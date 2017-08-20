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
    Http.execute(url, Request.Delete(BudgeaConstants.getServerUrl(url))
      .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + token));
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
    return json(Request.Get(BudgeaConstants.getServerUrl(url))
                  .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + token), url);
  }

  public JSONObject getUserConnections(int userId) throws IOException {
    checkToken();
    String url = "/users/" + userId + "/connections";
    return json(Request.Get(BudgeaConstants.getServerUrl(url))
                  .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + token)
                  .addHeader("user_id", "me"), url);
  }

  public static class LoginResult {

    public enum Status {
      ACCEPTED,
      SECOND_STEP_NEEDED,
      CREDENTIALS_REJECTED,
      OTHER_ERROR
    }

    public final Status status;
    public final JSONObject json;

    public LoginResult(int statusCode, JSONObject json) throws IOException {
      switch (statusCode) {
        case 200:
          this.status = Status.ACCEPTED;
          break;
        case 202:
          this.status = Status.SECOND_STEP_NEEDED;
          break;
        case 400:
          this.status = Status.CREDENTIALS_REJECTED;
          break;
        default:
          this.status = Status.OTHER_ERROR;
      }
      this.json = json;
    }
  }

  public LoginResult addBankConnectionStep1(Integer budgeaBankId, Map<String, String> params) throws IOException {
    checkToken();

    Form form = Form.form()
      .add("id_bank", Integer.toString(budgeaBankId));

    for (Map.Entry<String, String> entry : params.entrySet()) {
      form.add(entry.getKey(), entry.getValue());
    }

    List<NameValuePair> pairs = form.build();
    String url = BudgeaConstants.getServerUrl("/users/me/connections");
    Request request = Request.Post(url)
      .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + token)
      .bodyForm(pairs, Consts.UTF_8);

    return getLoginResult(url, request);
  }

  public JSONObject addBankConnectionStep2(Integer connectionId, Map<String, String> params) throws IOException {
    checkToken();

    Form form = Form.form();
    for (Map.Entry<String, String> entry : params.entrySet()) {
      form.add(entry.getKey(), entry.getValue());
    }

    List<NameValuePair> pairs = form.build();
    String url = BudgeaConstants.getServerUrl("/users/me/connections/" + connectionId + "?expand=accounts");
    Request request = Request.Post(url)
      .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + token)
      .bodyForm(pairs, Consts.UTF_8);
    return Http.executeAndGetJson(url, request);
  }

  public LoginResult updateBankPassword(Integer connectionId, Map<String, String> params) throws IOException {
    checkToken();
    Form form = Form.form();
    for (Map.Entry<String, String> entry : params.entrySet()) {
      form.add(entry.getKey(), entry.getValue());
    }

    List<NameValuePair> pairs = form.build();
    String url = BudgeaConstants.getServerUrl("/users/me/connections/" + connectionId + "?expand=accounts");
    Request request = Request.Post(url)
      .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + token)
      .bodyForm(pairs, Consts.UTF_8);

    return getLoginResult(url, request);
  }

  public LoginResult getLoginResult(String url, Request request) throws IOException {
    HttpResponse response = request.execute().returnResponse();
    StatusLine statusLine = response.getStatusLine();
    int statusCode = statusLine.getStatusCode();
    if (statusCode != 200 && statusCode != 202 && statusCode != 400) {
      throw new IOException(url + " returned " + statusCode + " instead of 200/202/400");
    }

    return new LoginResult(statusCode, Json.json(response));
  }

  public void deleteConnection(int budgeaConnectionId) throws IOException {
    String url = "/users/me/connections/" + budgeaConnectionId;
    Http.execute(url, Request.Delete(BudgeaConstants.getServerUrl(url))
      .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + token));
  }

  public Integer getUserId() throws IOException {
    checkToken();
    String url = "/users/me";
    JSONObject user = json(Request.Get(BudgeaConstants.getServerUrl(url))
                             .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + token), url);
    return user.getInt("id");
  }

  public JSONObject setAccountEnabled(int budgeaAccountId, boolean enabled) throws IOException {
    checkToken();
    String url = "/users/me/accounts/" + budgeaAccountId;
    Form form = Form.form()
      .add("disabled", enabled ? "null" : "1");

    return json(Request.Put(BudgeaConstants.getServerUrl(url))
                  .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + token)
                  .addHeader("user_id", "me")
                  .bodyForm(form.build(), Consts.UTF_8), url);

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
