package com.budgetview.desktop.cloud;

import com.budgetview.budgea.model.*;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import static com.budgetview.utils.Json.json;
import static org.globsframework.json.JsonGlobParser.jsonToGlob;
import static org.globsframework.model.FieldValue.value;

public class CloudService {

  private String bearer;
  private int userId = -1;


  public void updateBankList(GlobRepository repository) throws IOException {

    try {
      repository.startChangeSet();
      repository.deleteAll(BudgeaBank.TYPE, BudgeaBankField.TYPE, BudgeaBankFieldValue.TYPE);

      JSONObject banks = json(Request.Get("https://budgetview.biapi.pro/2.0/banks?expand=fields")
                                .addHeader("Authorization", "Bearer " + getBearer()));
      for (Object b : banks.getJSONArray("banks")) {
        JSONObject bank = (JSONObject) b;
        if (bank.getBoolean("hidden") || bank.getBoolean("beta")) {
          continue;
        }

        int bankId = bank.getInt("id");
        jsonToGlob(bank, BudgeaBank.TYPE, bankId, repository);

        for (Object f : bank.getJSONArray("fields")) {
          JSONObject field = (JSONObject) f;
          Glob fieldGlob = jsonToGlob(field, BudgeaBankField.TYPE, repository,
                                      value(BudgeaBankField.BANK, bankId));

          if (field.has("values")) {
            for (Object v : field.getJSONArray("values")) {
              JSONObject value = (JSONObject) v;
              jsonToGlob(value, BudgeaBankFieldValue.TYPE, repository,
                         value(BudgeaBankFieldValue.FIELD, fieldGlob.get(BudgeaBankField.ID)));
            }
          }
        }
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }

  public void createConnection(Key connectionKey, GlobRepository repository) throws IOException {

    Form form = Form.form()
      .add("id_bank", Integer.toString(connectionKey.get(BudgeaConnection.BANK)));

    GlobList values = repository.findLinkedTo(connectionKey, BudgeaConnectionValue.CONNECTION);
    for (Glob value : values) {
      Glob field = repository.findLinkTarget(value, BudgeaConnectionValue.FIELD);
      String name = field.get(BudgeaBankField.NAME);
      form.add(name, value.get(BudgeaConnectionValue.VALUE));
    }

    Request request = Request.Post("https://budgetview.biapi.pro/2.0/users/me/connections")
      .addHeader("Authorization", "Bearer " + getBearer())
      .bodyForm(form.build(), Consts.UTF_8);

    JSONObject result = json(request);
    System.out.println("CloudService.createConnection: " + result.toString(2));

    registerConnectionToken();
  }

  private void registerConnectionToken() throws IOException {
    Request request = Request.Post(cloudUrl("/connections"))
      .bodyForm(Form.form()
                  .add("code", getBearer())
                  .build(), Consts.UTF_8);

    Response response = request.execute();

  }

  private String cloudUrl(String s) {
    return "http://127.0.0.1:8080" + s;
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
