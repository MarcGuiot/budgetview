package com.budgetview.desktop.cloud;

import com.budgetview.budgea.model.*;
import com.budgetview.model.User;
import com.budgetview.shared.cloud.BudgeaAPI;
import com.budgetview.shared.cloud.CloudAPI;
import org.globsframework.json.JsonGlobParser;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.globsframework.model.FieldValue.value;

public class CloudService {

  private final BudgeaAPI budgeaAPI = new BudgeaAPI();
  private final CloudAPI cloudAPI = new CloudAPI();

  public void updateBankList(GlobRepository repository) throws IOException {

    JsonGlobParser jsonParser = new JsonGlobParser(repository);
    try {
      repository.startChangeSet();
      repository.deleteAll(BudgeaBank.TYPE, BudgeaBankField.TYPE, BudgeaBankFieldValue.TYPE);

      JSONObject banks = budgeaAPI.getBanks();
      for (Object b : banks.getJSONArray("banks")) {
        JSONObject bank = (JSONObject) b;
        if (bank.getBoolean("hidden") || bank.getBoolean("beta")) {
          continue;
        }

        int bankId = bank.getInt("id");
        jsonParser.toGlob(bank, BudgeaBank.TYPE, bankId);

        for (Object f : bank.getJSONArray("fields")) {
          JSONObject field = (JSONObject) f;
          Glob fieldGlob = jsonParser.toGlob(field, BudgeaBankField.TYPE, repository,
                                             value(BudgeaBankField.BANK, bankId));

          if (field.has("values")) {
            for (Object v : field.getJSONArray("values")) {
              JSONObject value = (JSONObject) v;
              jsonParser.toGlob(value, BudgeaBankFieldValue.TYPE, repository,
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

    Map<String, String> params = new HashMap<String, String>();
    GlobList values = repository.findLinkedTo(connectionKey, BudgeaConnectionValue.CONNECTION);
    for (Glob value : values) {
      Glob field = repository.findLinkTarget(value, BudgeaConnectionValue.FIELD);
      String name = field.get(BudgeaBankField.NAME);
      params.put(name, value.get(BudgeaConnectionValue.VALUE));
    }

    JSONObject result = budgeaAPI.addConnection(connectionKey.get(BudgeaConnection.BANK), params);
    Glob user = repository.get(User.KEY);
    cloudAPI.addConnection(user.get(User.EMAIL), budgeaAPI.getToken(), budgeaAPI.getUserId());
  }
}
