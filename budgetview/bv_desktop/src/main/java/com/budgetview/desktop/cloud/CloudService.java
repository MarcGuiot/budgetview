package com.budgetview.desktop.cloud;

import com.budgetview.budgea.model.*;
import com.budgetview.model.Bank;
import com.budgetview.shared.cloud.BudgeaAPI;
import com.budgetview.shared.cloud.CloudAPI;
import com.budgetview.shared.model.Provider;
import org.globsframework.json.JsonGlobParser;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.Log;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;
import static org.globsframework.model.utils.GlobMatchers.fieldIn;

public class CloudService {

  private static final String ___TEST_EMAIL___TO_BE_REPLACED____ = "admin@mybudgetview.fr";

  private final BudgeaAPI budgeaAPI = new BudgeaAPI();
  private final CloudAPI cloudAPI = new CloudAPI();

  public interface Callback {
    void processCompletion();

    void processError();

  }

  public interface DownloadCallback {
    void processCompletion(GlobList importedRealAccounts);

    void processError();
  }

  public void updateBankList(GlobRepository repository, Callback callback) {

    JsonGlobParser jsonParser = createBankParser(repository);
    try {
      repository.startChangeSet();
      repository.deleteAll(BudgeaBank.TYPE, BudgeaBankField.TYPE, BudgeaBankFieldValue.TYPE);

      JSONObject banks = budgeaAPI.getBanks();

      System.out.println("CloudService.updateBankList:\n" + banks.toString(2));

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

      callback.processCompletion();
    }
    catch (Exception e) {
      Log.write("Error retrieving bank list", e);
      callback.processError();
    }
    finally {
      repository.completeChangeSet();
    }
  }

  public void updateBankFields(Key bankKey, GlobRepository repository, Callback callback) {
    try {
      repository.startChangeSet();
      Glob bank = repository.get(bankKey);
      if (!Utils.equal(Provider.BUDGEA.getId(), bank.get(Bank.PROVIDER))) {
        throw new InvalidParameter("Bank " + bank.get(Bank.NAME) + " (id:" + bank.get(Bank.ID) + ") is not supported by Budgea");
      }

      Integer budgeaBankId = bank.get(Bank.PROVIDER_ID);
      if (budgeaBankId == null) {
        throw new InvalidParameter("No provider set for bank: " + bank);
      }
      GlobList fields = repository.getAll(BudgeaBankField.TYPE, fieldEquals(BudgeaBankField.BANK, budgeaBankId));
      repository.delete(BudgeaBankFieldValue.TYPE, fieldIn(BudgeaBankFieldValue.FIELD, fields.getValueSet(BudgeaBankField.ID)));
      repository.delete(fields);
      repository.delete(BudgeaConnection.TYPE, fieldEquals(BudgeaConnection.BANK, budgeaBankId));
      repository.delete(BudgeaConnectionValue.TYPE, fieldEquals(BudgeaConnectionValue.CONNECTION, budgeaBankId));

      repository.findOrCreate(Key.create(BudgeaBank.TYPE, budgeaBankId),
                              value(BudgeaBank.BANK, bank.get(Bank.ID)));

      JsonGlobParser jsonParser = createBankParser(repository);

      JSONObject root = budgeaAPI.getBankFields(budgeaBankId);
      for (Object f : root.getJSONArray("fields")) {
        JSONObject field = (JSONObject) f;
        Glob fieldGlob = jsonParser.toGlob(field, BudgeaBankField.TYPE, repository,
                                           value(BudgeaBankField.BANK, budgeaBankId));

        if (field.has("values")) {
          for (Object v : field.getJSONArray("values")) {
            JSONObject value = (JSONObject) v;
            jsonParser.toGlob(value, BudgeaBankFieldValue.TYPE, repository,
                              value(BudgeaBankFieldValue.FIELD, fieldGlob.get(BudgeaBankField.ID)));
          }
        }
      }

      callback.processCompletion();
    }
    catch (Exception e) {
      Log.write("Error retrieving bank fields", e);
      callback.processError();
    }
    finally {
      repository.completeChangeSet();
    }
  }

  public void createConnection(Glob connection, GlobRepository repository, Callback callback) {
    try {
      Map<String, String> params = new HashMap<String, String>();
      for (Glob value : repository.findLinkedTo(connection.getKey(), BudgeaConnectionValue.CONNECTION)) {
        Glob field = repository.findLinkTarget(value, BudgeaConnectionValue.FIELD);
        String name = field.get(BudgeaBankField.NAME);
        params.put(name, value.get(BudgeaConnectionValue.VALUE));
      }

      budgeaAPI.registerConnection(connection.get(BudgeaConnection.BANK), params);
      cloudAPI.addConnection(___TEST_EMAIL___TO_BE_REPLACED____, budgeaAPI.getToken(), budgeaAPI.getUserId());

      callback.processCompletion();
    }
    catch (Exception e) {
      Log.write("Error creating connection", e);
      callback.processError();
    }
  }

  public void downloadStatement(Glob connection, GlobRepository repository, DownloadCallback callback) {
    try {
      JSONObject statement = cloudAPI.getStatement(___TEST_EMAIL___TO_BE_REPLACED____, Provider.BUDGEA, connection.get(BudgeaConnection.BANK));
      System.out.println("CloudService.downloadStatement\n" + statement.toString(2));

      GlobList importedRealAccounts = new GlobList();
      callback.processCompletion(importedRealAccounts);
    }
    catch (Exception e) {
      Log.write("Error downloading statement", e);
      callback.processError();
    }
  }

  public JsonGlobParser createBankParser(GlobRepository repository) {
    JsonGlobParser parser = new JsonGlobParser(repository);
    parser.setConverter(BudgeaBankField.FIELD_TYPE, new JsonGlobParser.Converter() {
      public Object convert(Object value) {
        BudgeaBankFieldType type = BudgeaBankFieldType.get(Strings.toString(value));
        return type.getId();
      }
    });
    return parser;
  }
}
