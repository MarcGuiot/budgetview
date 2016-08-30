package com.budgetview.desktop.cloud;

import com.budgetview.budgea.model.*;
import com.budgetview.model.Bank;
import com.budgetview.model.Month;
import com.budgetview.model.RealAccount;
import com.budgetview.shared.cloud.budgea.BudgeaAPI;
import com.budgetview.shared.cloud.CloudAPI;
import com.budgetview.shared.model.AccountType;
import com.budgetview.shared.model.Provider;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.json.JsonGlobParser;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.utils.Log;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
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

    void processTimeout();

    void processError();
  }

  public void updateBankList(GlobRepository repository, Callback callback) {

    JsonGlobParser jsonParser = createBankParser(repository);
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

  public void createConnection(final Glob connection, final GlobRepository repository, final DownloadCallback callback) {
    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {
          Map<String, String> params = new HashMap<String, String>();
          for (Glob value : repository.findLinkedTo(connection.getKey(), BudgeaConnectionValue.CONNECTION)) {
            Glob field = repository.findLinkTarget(value, BudgeaConnectionValue.FIELD);
            String name = field.get(BudgeaBankField.NAME);
            params.put(name, value.get(BudgeaConnectionValue.VALUE));
          }

          budgeaAPI.registerConnection(connection.get(BudgeaConnection.BANK), params);
          cloudAPI.addConnection(___TEST_EMAIL___TO_BE_REPLACED____, budgeaAPI.getToken(), budgeaAPI.getUserId());

          downloadStatement(connection, repository, callback);
        }
        catch (Exception e) {
          Log.write("Error creating connection", e);
          callback.processError();
        }
      }
    });
    thread.start();
  }

  private void downloadStatement(Glob connection, GlobRepository repository, DownloadCallback callback) throws IOException {
    for (int i = 0; i < 50; i++) {
      try {
        JSONObject statement = cloudAPI.getStatement(___TEST_EMAIL___TO_BE_REPLACED____, Provider.BUDGEA, connection.get(BudgeaConnection.BANK));
        JSONArray accounts = statement.getJSONArray("accounts");
        if (accounts.length() == 0) {
          continue;
        }

        GlobList importedRealAccounts = new GlobList();
        for (Object item : accounts) {
          JSONObject account = (JSONObject) item;
          String name = Strings.toString(account.getString("name")).trim();
          String number = Strings.toString(account.getString("number")).trim();
          int budgeaAccountId = account.getInt("provider_account_id");
          int budgeaBankId = account.getInt("provider_bank_id");
          Glob bank = BudgeaBank.findBudgetViewBank(budgeaBankId, repository);
          if (bank == null) {
            String bankName = account.getString("provider_bank_name");
            bank = createMissingBank(budgeaBankId, bankName, repository);
          }

          Glob realAccount = RealAccount.findFromProvider(Provider.BUDGEA.getId(), budgeaAccountId, repository);
          if (realAccount == null) {
            realAccount = RealAccount.findOrCreate(name, number, bank.get(Bank.ID), repository);
            repository.update(realAccount,
                              value(RealAccount.PROVIDER, Provider.BUDGEA.getId()),
                              value(RealAccount.PROVIDER_ACCOUNT_ID, budgeaAccountId));
          }

          double position = account.getDouble("position");
          int positionMonth = account.getInt("position_month");
          int positionDay = account.getInt("position_day");
          repository.update(realAccount,
                            value(RealAccount.NAME, name),
                            value(RealAccount.NUMBER, number),
                            value(RealAccount.POSITION, Double.toString(position)),
                            value(RealAccount.POSITION_DATE, Month.toDate(positionMonth, positionDay)),
                            value(RealAccount.ACCOUNT_TYPE, AccountType.get(account.getString("type")).getId()),
                            value(RealAccount.FILE_NAME, "cloud.json"),
                            value(RealAccount.FILE_CONTENT, account.toString()));
          importedRealAccounts.add(realAccount);
        }

        if (!importedRealAccounts.isEmpty()) {

          System.out.println("DownloadStatement.run COMPLETING... - accounts");
          GlobPrinter.print(importedRealAccounts);

          GuiUtils.runInSwingThread(new Runnable() {
            public void run() {
              callback.processCompletion(importedRealAccounts);
            }
          });
          return;
        }

        Thread.sleep(2000);
      }
      catch (InterruptedException e) {
        // Ignored - will exit after repeat
      }
    }
    GuiUtils.runInSwingThread(new Runnable() {
      public void run() {
        callback.processTimeout();
      }
    });
  }

  private Glob createMissingBank(int budgeaBankId, String bankName, GlobRepository repository) {
    return repository.create(Bank.TYPE,
                             value(Bank.NAME, bankName),
                             value(Bank.PROVIDER, Provider.BUDGEA.getId()),
                             value(Bank.PROVIDER_ID, budgeaBankId));

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
