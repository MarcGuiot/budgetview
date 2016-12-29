package com.budgetview.desktop.cloud;

import com.budgetview.budgea.model.*;
import com.budgetview.model.*;
import com.budgetview.shared.cloud.CloudAPI;
import com.budgetview.shared.cloud.CloudConstants;
import com.budgetview.shared.cloud.CloudRequestStatus;
import com.budgetview.shared.cloud.CloudSubscriptionStatus;
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

    void processSubscriptionError(CloudSubscriptionStatus status);

    void processError(Exception e);
  }

  public interface ValidationCallback {
    void processCompletionAndSelectBank();

    void processCompletionAndDownload();

    void processInvalidCode();

    void processTempTokenExpired();

    void processSubscriptionError(CloudSubscriptionStatus subscriptionStatus);

    void processError(Exception e);
  }

  public interface DownloadCallback {
    void processCompletion(GlobList importedRealAccounts);

    void processSubscriptionError(CloudSubscriptionStatus subscriptionStatus);

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
                            value(CloudDesktopUser.BV_TOKEN, null),
                            value(CloudDesktopUser.REGISTERED, false));
          JSONObject result = cloudAPI.signup(email);
          switch (CloudRequestStatus.get(result.getString(CloudConstants.STATUS))) {
            case OK:
              callback.processCompletion();
              break;
            case NO_SUBSCRIPTION:
              callback.processSubscriptionError(getSubscriptionStatus(result));
              break;
          }
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
          System.out.println("CloudService.validate: /validate returned\n" + result.toString(2));

          switch (CloudRequestStatus.get(result.getString(CloudConstants.STATUS))) {
            case OK:
              String bvToken = result.getString(CloudConstants.BV_TOKEN);
              repository.update(CloudDesktopUser.KEY,
                                value(CloudDesktopUser.BV_TOKEN, bvToken),
                                value(CloudDesktopUser.REGISTERED, true));
              System.out.println("CloudService.validate OK: bv_token set to " + bvToken);
              if (Boolean.TRUE.equals(result.optBoolean(CloudConstants.EXISTING_STATEMENTS))) {
                callback.processCompletionAndDownload();
              }
              else {
                callback.processCompletionAndSelectBank();
              }
              break;
            case UNKNOWN_CODE:
              callback.processInvalidCode();
              break;
            case TEMP_CODE_EXPIRED:
              callback.processTempTokenExpired();
              break;
            case NO_SUBSCRIPTION:
              callback.processSubscriptionError(getSubscriptionStatus(result));
              break;
            default:
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
      Glob user = repository.get(CloudDesktopUser.KEY);

      JSONObject result = cloudAPI.getTemporaryBudgeaToken(user.get(CloudDesktopUser.EMAIL), user.get(CloudDesktopUser.BV_TOKEN));
      switch (CloudRequestStatus.get(result.getString(CloudConstants.STATUS))) {
        case OK:
          String token = result.getString(CloudConstants.PROVIDER_TOKEN);
          boolean permanentTokenRegistered = result.getBoolean(CloudConstants.PROVIDER_TOKEN_REGISTERED);
          budgeaAPI.setToken(token, permanentTokenRegistered);
          break;
        case NO_SUBSCRIPTION:
          callback.processSubscriptionError(getSubscriptionStatus(result));
          return;
        default:
          callback.processError(null);
          return;
      }

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

  public void createBankConnection(final Glob connection, final GlobRepository repository, final Callback callback) {
    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {
          Map<String, String> params = new HashMap<String, String>();
          for (Glob value : repository.findLinkedTo(connection.getKey(), BudgeaConnectionValue.CONNECTION)) {
            Glob field = repository.findLinkTarget(value, BudgeaConnectionValue.FIELD);
            String name = field.get(BudgeaBankField.NAME);
            params.put(name, value.get(BudgeaConnectionValue.VALUE));
          }

          Glob user = repository.get(CloudDesktopUser.KEY);
          String email = user.get(CloudDesktopUser.EMAIL);
          String bvToken = user.get(CloudDesktopUser.BV_TOKEN);
          if (!cloudAPI.isProviderAccessRegistered(email, bvToken)) {
            cloudAPI.addProviderAccess(email, bvToken, budgeaAPI.getToken(), budgeaAPI.getUserId());
          }

          JSONObject result = cloudAPI.getTemporaryBudgeaToken(email, bvToken);
          switch (CloudRequestStatus.get(result.getString(CloudConstants.STATUS))) {
            case OK:
              String token = result.getString(CloudConstants.PROVIDER_TOKEN);
              boolean permanentTokenRegistered = result.getBoolean(CloudConstants.PROVIDER_TOKEN_REGISTERED);
              budgeaAPI.setToken(token, permanentTokenRegistered);
              break;
            case NO_SUBSCRIPTION:
              callback.processSubscriptionError(getSubscriptionStatus(result));
              return;
            default:
              callback.processError(null);
              return;
          }

          budgeaAPI.addBankConnection(connection.get(BudgeaConnection.BANK), params);

          repository.update(CloudDesktopUser.KEY, CloudDesktopUser.SYNCHRO_ENABLED, true);

          callback.processCompletion();
        }
        catch (Exception e) {
          Log.write("Error creating connection", e);
          callback.processError(e);
        }
      }
    });
    thread.start();
  }

  public void updateBankConnections(final GlobRepository repository, final Callback callback) {
    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {
          Glob user = repository.get(CloudDesktopUser.KEY);
          JSONObject connections = cloudAPI.getConnections(user.get(CloudDesktopUser.EMAIL), user.get(CloudDesktopUser.BV_TOKEN));

          repository.startChangeSet();
          repository.deleteAll(CloudProviderConnection.TYPE);
          for (Object c : connections.getJSONArray("connections")) {
            JSONObject connection = (JSONObject)c;
            repository.create(CloudProviderConnection.TYPE,
                              value(CloudProviderConnection.PROVIDER, connection.getInt(CloudConstants.PROVIDER_ID)),
                              value(CloudProviderConnection.PROVIDER_CONNECTION_ID, connection.getInt(CloudConstants.PROVIDER_CONNECTION_ID)),
                              value(CloudProviderConnection.NAME, connection.getString(CloudConstants.NAME)));
          }
          repository.completeChangeSet();
          callback.processCompletion();
        }
        catch (Exception e) {
          Log.write("Error retrieving connections", e);
          callback.processError(e);
        }
      }
    });
    thread.start();
  }

  public void deleteBankConnection(Glob connection, final GlobRepository repository, final Callback callback) {
    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {
          Glob user = repository.get(CloudDesktopUser.KEY);

          cloudAPI.deleteConnection(user.get(CloudDesktopUser.EMAIL), user.get(CloudDesktopUser.BV_TOKEN),
                                    connection.get(CloudProviderConnection.PROVIDER), connection.get(CloudProviderConnection.PROVIDER_CONNECTION_ID));

          repository.delete(connection.getKey());
          callback.processCompletion();
        }
        catch (Exception e) {
          Log.write("Error retrieving connections", e);
          callback.processError(e);
        }
      }
    });
    thread.start();
  }


  public void downloadInitialStatement(GlobRepository repository, DownloadCallback callback) {
    Thread thread = new Thread(new Runnable() {
      public void run() {

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

            Thread.sleep(3000);
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
    });
    thread.start();
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
        catch (SubscriptionError e) {
          GuiUtils.runInSwingThread(new Runnable() {
            public void run() {
              callback.processSubscriptionError(e.status);
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

  public GlobList doDownloadStatement(GlobRepository repository) throws SubscriptionError, IOException {
    Glob user = repository.findOrCreate(CloudDesktopUser.KEY);
    Integer lastUpdate = user.get(CloudDesktopUser.LAST_UPDATE);
    JSONObject result = cloudAPI.getStatement(user.get(CloudDesktopUser.EMAIL), user.get(CloudDesktopUser.BV_TOKEN), lastUpdate);

    System.out.println("CloudService.doDownloadStatement: lastUpdate=" + lastUpdate + " ==> returned " + result.toString(2));

    String status = result.getString(CloudConstants.STATUS);
    switch (CloudRequestStatus.get(status)) {
      case OK:
        break;
      case NO_SUBSCRIPTION:
        throw new SubscriptionError(getSubscriptionStatus(result));
      case TEMP_CODE_EXPIRED:
        throw new IOException("Unexpected error status: " + status);
    }

    JSONArray accounts = result.getJSONArray("accounts");
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
        realAccount = RealAccount.findByAccountNumber(number, bank.get(Bank.ID), repository);
        if (realAccount != null) {
          repository.update(realAccount,
                            value(RealAccount.PROVIDER, Provider.BUDGEA.getId()),
                            value(RealAccount.PROVIDER_ACCOUNT_ID, budgeaAccountId));
        }
      }
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
                        value(RealAccount.ACCOUNT_TYPE, getAccountType(account).getId()),
                        value(RealAccount.FILE_NAME, "cloud.json"),
//                            value(RealAccount.BANK_ENTITY_LABEL, ),
//                            value(RealAccount.BANK_ENTITY, bankEntityId),
                        value(RealAccount.FILE_CONTENT, account.toString()));
      importedRealAccounts.add(realAccount);
    }

    if (!importedRealAccounts.isEmpty()) {
      System.out.println("DownloadStatement.run COMPLETING... - accounts");
      GlobPrinter.print(importedRealAccounts);

      int newUpdate = result.getInt("last_update");
      System.out.println("CloudService.downloadStatement - newUpdate: " + newUpdate);
      repository.update(CloudDesktopUser.KEY, CloudDesktopUser.LAST_UPDATE, newUpdate);
    }
    return importedRealAccounts;
  }

  public AccountType getAccountType(JSONObject account) {
    String type = account.optString("type");
    return Strings.isNullOrEmpty(type) ? AccountType.MAIN : AccountType.get(type);
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

  private CloudSubscriptionStatus getSubscriptionStatus(JSONObject result) {
    return CloudSubscriptionStatus.get(result.optString(CloudConstants.SUBSCRIPTION_STATUS));
  }

  private class SubscriptionError extends Exception {
    protected final CloudSubscriptionStatus status;

    public SubscriptionError(CloudSubscriptionStatus status) {
      this.status = status;
    }
  }
}
