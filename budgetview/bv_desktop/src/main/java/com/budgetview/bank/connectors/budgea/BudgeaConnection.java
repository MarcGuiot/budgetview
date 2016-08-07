package com.budgetview.bank.connectors.budgea;

import com.budgetview.io.importer.json.JsonUtils;
import com.budgetview.model.RealAccount;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Strings;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

import static com.budgetview.shared.json.Json.json;
import static org.globsframework.model.FieldValue.value;

public class BudgeaConnection {

  private String bearer;
  private int userId;
  public static final int BANK_ID = 40;

  public interface AccountFactory {
    Glob findOrCreateAccount(String name, String number, String position, Date date, String budgeaId);
  }

  public BudgeaConnection() {
  }

  public GlobList loadRealAccounts(Integer bankId, AccountFactory accountFactory) throws IOException {
    String bearer = getBearer();

    Response request = Request.Post("https://budgetview.biapi.pro/2.0/users/me/connections")
      .addHeader("Authorization", "Bearer " + bearer)
      .bodyForm(Form.form()
                  .add("user_id", Integer.toString(userId))
                  .add("id_bank", "40")
                  .add("website", "par")
                  .add("login", "123456789")
                  .add("password", "1234")
                  .build())
      .execute();
    request.returnResponse().getStatusLine().getStatusCode();

    JSONObject accounts = json(Request.Get("https://budgetview.biapi.pro/2.0/users/" + userId + "/accounts")
                                 .addHeader("Authorization", "Bearer " + bearer));

    GlobList realAccounts = new GlobList();
    for (Object item : accounts.getJSONArray("accounts")) {
      JSONObject jsonAccount = (JSONObject) item;
      String name = Strings.toString(jsonAccount.get("name")).trim();
      String number = Strings.toString(jsonAccount.get("number")).trim();
      if (Strings.isNullOrEmpty(name) && Strings.isNullOrEmpty(number)) {
        continue;
      }
      String position = Strings.toString(jsonAccount.get("balance"));
      Date date = JsonUtils.parseTimestamp(Strings.toString(jsonAccount.get("last_update")));
      String budgeaId = Strings.toString(jsonAccount.get("id"));

      Glob realAccount = accountFactory.findOrCreateAccount(name, number, position, date, budgeaId);
      realAccounts.add(realAccount);
    }

    return realAccounts;
  }

  public void loadTransactionFiles(Glob realAccount, GlobRepository repository) throws IOException {

    String budgeaId = realAccount.get(RealAccount.BUDGEA_ID);
    if (budgeaId == null) {
      System.out.println("BudgeaConnection.loadTransactionFiles - not a Budgea account");
      return;
    }

    String transactions =
      Request.Get("https://budgetview.biapi.pro/2.0/users/" + userId + "/accounts/" + budgeaId + "/transactions")
        .addHeader("Authorization", "Bearer " + bearer)
        .execute().returnContent().asString();

    repository.update(realAccount,
                      value(RealAccount.FILE_CONTENT, transactions));
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
