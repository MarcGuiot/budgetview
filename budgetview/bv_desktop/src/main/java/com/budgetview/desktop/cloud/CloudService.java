package com.budgetview.desktop.cloud;

import com.budgetview.budgea.model.BudgeaBank;
import com.budgetview.budgea.model.BudgeaBankField;
import com.budgetview.budgea.model.BudgeaBankFieldValue;
import org.apache.http.client.fluent.Request;
import org.globsframework.json.JsonGlobParser;
import org.globsframework.model.*;
import org.json.JSONObject;

import java.io.IOException;

import static com.budgetview.io.budgea.BudgeaDemo.json;
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
        int bankId = bank.getInt("id");

        if (bank.getBoolean("hidden") || bank.getBoolean("beta")) {
          continue;
        }

        JsonGlobParser.create(bank, BudgeaBank.TYPE, bankId, repository);

        for (Object f : bank.getJSONArray("fields")) {
          JSONObject field = (JSONObject) f;
          Glob fieldGlob = JsonGlobParser.create(field, BudgeaBankField.TYPE, repository,
                                                 value(BudgeaBankField.BANK, bankId));

          if (field.has("values")) {
            for (Object v : field.getJSONArray("values")) {
              JSONObject value = (JSONObject) v;
              JsonGlobParser.create(value, BudgeaBankFieldValue.TYPE, repository,
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

  public void createConnection() {

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
