package com.budgetview.shared.cloud.budgea;

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
    JSONObject auth = json(Request.Post(BudgeaConstants.getServerUrl("/auth/init")));
    return auth.getString(BudgeaConstants.AUTH_TOKEN);
  }

  public static String requestTemporaryToken(String permanentToken) throws IOException {
    JSONObject auth = json(Request.Post(BudgeaConstants.getServerUrl("/auth/token/code"))
                             .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + permanentToken));
    return auth.getString(BudgeaConstants.AUTH_TOKEN);
  }

  public void setTempToken(String token, boolean permanentTokenRegistered) {
    this.tempToken = token;
    this.permanentTokenRegistered = permanentTokenRegistered;
  }

  public JSONObject getBanks() throws IOException {
    checkTempToken();
    return json(Request.Get(BudgeaConstants.getServerUrl("/banks?expand=fields"))
                  .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + tempToken));
  }

  public JSONObject getBankFields(int budgeaBankId) throws IOException {
    checkTempToken();
    return json(Request.Get(BudgeaConstants.getServerUrl("/banks/" + budgeaBankId + "/fields"))
                  .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + tempToken));
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

    return json(httpResponse);
  }

  public Integer getUserId() throws IOException {
    checkTempToken();
    JSONObject user = json(Request.Get(BudgeaConstants.getServerUrl("/users/me"))
                             .addHeader(BudgeaConstants.AUTHORIZATION, "Bearer " + tempToken));
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
}
