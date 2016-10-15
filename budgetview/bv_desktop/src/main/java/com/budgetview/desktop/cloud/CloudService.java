package com.budgetview.desktop.cloud;

import com.budgetview.budgea.model.*;
import com.budgetview.model.Bank;
import com.budgetview.model.CloudDesktopUser;
import com.budgetview.model.Month;
import com.budgetview.model.RealAccount;
import com.budgetview.shared.cloud.CloudAPI;
import com.budgetview.shared.cloud.CloudConstants;
import com.budgetview.shared.cloud.budgea.BudgeaAPI;
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

  private final BudgeaAPI budgeaAPI = new BudgeaAPI();
  private final CloudAPI cloudAPI = new CloudAPI();

  public interface Callback {
    void processCompletion();

    void processError(Exception e);

  }

  public interface ValidationCallback {
    void processCompletion();

    void processInvalidCode();

    void processError(Exception e);
  }

  public interface DownloadCallback {
    void processCompletion(GlobList importedRealAccounts);

    void processTimeout();

    void processError(Exception e);
  }

  public void signup(String email, GlobRepository repository, Callback callback) {
    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {
          repository.findOrCreate(CloudDesktopUser.KEY);
          repository.update(CloudDesktopUser.KEY,
                            value(CloudDesktopUser.EMAIL, email),
                            value(CloudDesktopUser.TOKEN, null),
                            value(CloudDesktopUser.REGISTERED, false));
          cloudAPI.signup(email);
          callback.processCompletion();
        }
        catch (Exception e) {
          Log.write("Error during signup", e);
          callback.processError(e);
        }
      }
    });
    thread.start();
  }

  public void validate(String email, String code, GlobRepository repository, ValidationCallback callback) {
    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {
          JSONObject result = cloudAPI.validate(email, code);
          String status = result.getString(CloudConstants.STATUS);
          if ("validated".equalsIgnoreCase(status)) {
            String token = result.getString(CloudConstants.TOKEN);
            repository.update(CloudDesktopUser.KEY,
                              value(CloudDesktopUser.TOKEN, token),
                              value(CloudDesktopUser.REGISTERED, true));
            callback.processCompletion();
          }
          else if ("invalid".equalsIgnoreCase(status)) {
            callback.processInvalidCode();
          }
          else {
            callback.processError(null);
          }
        }
        catch (Exception e) {
          Log.write("Error validating email", e);
          callback.processError(e);
        }
      }
    });
    thread.start();
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
      callback.processError(e);
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
      callback.processError(e);
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
          Glob user = repository.get(CloudDesktopUser.KEY);

          cloudAPI.addConnection(user.get(CloudDesktopUser.EMAIL), user.get(CloudDesktopUser.TOKEN), budgeaAPI.getToken(), budgeaAPI.getUserId());

          repository.update(CloudDesktopUser.KEY, CloudDesktopUser.SYNCHRO_ENABLED, true);

          downloadInitialStatement(repository, callback);
        }
        catch (Exception e) {
          Log.write("Error creating connection", e);
          callback.processError(e);
        }
      }
    });
    thread.start();
  }

  public void downloadInitialStatement(GlobRepository repository, DownloadCallback callback) {

    System.out.println("\n\n --------- CloudService.downloadInitialStatement ---------");

    for (int i = 0; i < 50; i++) {
      try {
        GlobList importedRealAccounts = doDownloadStatement(repository);
        if (!importedRealAccounts.isEmpty()) {
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
      catch (Exception e) {
        Log.write("Error downloading statement", e);
        callback.processError(e);
      }
    }
    GuiUtils.runInSwingThread(new Runnable() {
      public void run() {
        callback.processTimeout();
      }
    });
  }

  public void downloadStatement(GlobRepository repository, DownloadCallback callback) {
    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {
          System.out.println("\n\n --------- CloudService.downloadStatement ---------");

          final GlobList importedRealAccounts = doDownloadStatement(repository);
          GuiUtils.runInSwingThread(new Runnable() {
            public void run() {
              callback.processCompletion(importedRealAccounts);
            }
          });
        }
        catch (Exception e) {
          Log.write("Error downloading statement", e);
          GuiUtils.runInSwingThread(new Runnable() {
            public void run() {
              callback.processError(e);
            }
          });
        }
      }
    });
    thread.start();
  }

  public GlobList doDownloadStatement(GlobRepository repository) throws IOException {
    Glob user = repository.findOrCreate(CloudDesktopUser.KEY);
    Integer lastUpdate = user.get(CloudDesktopUser.LAST_UPDATE);
    JSONObject statement = cloudAPI.getStatement(user.get(CloudDesktopUser.EMAIL), user.get(CloudDesktopUser.TOKEN), lastUpdate);
    JSONArray accounts = statement.getJSONArray("accounts");
    if (accounts.length() == 0) {
      return GlobList.EMPTY;
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
//                            value(RealAccount.BANK_ENTITY_LABEL, ),
//                            value(RealAccount.BANK_ENTITY, bankEntityId),
                        value(RealAccount.FILE_CONTENT, account.toString()));
      importedRealAccounts.add(realAccount);
    }

    if (!importedRealAccounts.isEmpty()) {
      System.out.println("DownloadStatement.run COMPLETING... - accounts");
      GlobPrinter.print(importedRealAccounts);

      int newUpdate = statement.getInt("last_update");
      System.out.println("CloudService.downloadStatement - newUpdate: " + newUpdate);
      repository.update(CloudDesktopUser.KEY, CloudDesktopUser.LAST_UPDATE, newUpdate);
    }
    return importedRealAccounts;
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
