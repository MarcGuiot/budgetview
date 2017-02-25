package com.budgetview.desktop.cloud;

import com.budgetview.budgea.model.*;
import com.budgetview.model.*;
import com.budgetview.shared.cloud.*;
import com.budgetview.shared.cloud.budgea.BudgeaAPI;
import com.budgetview.shared.model.AccountType;
import com.budgetview.shared.model.Provider;
import com.budgetview.utils.Lang;
import com.oracle.javafx.jmx.json.JSONFactory;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.json.JsonGlobFormat;
import org.globsframework.json.JsonGlobParser;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.Log;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.exceptions.UnexpectedValue;
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

  public interface BankConnectionCallback {
    void processCompletion(Glob providerConnection);

    void processSecondStepResponse(int connectionId);

    void processSubscriptionError(CloudSubscriptionStatus status);

    void processCredentialsRejected();

    void processError(Exception e);
  }

  public interface BankConnectionCheckCallback {
    void processCompletion(boolean ready);

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

    void processError(Exception e);
  }

  public interface UnsubscriptionCallback {
    void processCompletion();

    void processError(Exception e);
  }

  public void signup(final String email, final GlobRepository repository, final Callback callback) {
    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {
          repository.findOrCreate(CloudDesktopUser.KEY);
          repository.update(CloudDesktopUser.KEY,
                            value(CloudDesktopUser.EMAIL, email),
                            value(CloudDesktopUser.BV_TOKEN, null),
                            value(CloudDesktopUser.REGISTERED, false));
          JSONObject result = cloudAPI.signup(email, Lang.getLang());
          switch (CloudRequestStatus.get(result.getString(CloudConstants.STATUS))) {
            case OK:
              callback.processCompletion();
              break;
            case NO_SUBSCRIPTION:
              callback.processSubscriptionError(getSubscriptionStatus(result));
              break;
            default:
              throw new UnexpectedValue(result.getString(CloudConstants.STATUS));
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

  public void validate(final String email, final String code, final GlobRepository repository, final ValidationCallback callback) {
    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {
          JSONObject result = cloudAPI.validate(email, code);

          System.out.println("CloudService.validate:" + result.toString(2));

          switch (CloudValidationStatus.get(result.getString(CloudConstants.STATUS))) {
            case OK:
              String bvToken = result.getString(CloudConstants.BV_TOKEN);
              repository.update(CloudDesktopUser.KEY,
                                value(CloudDesktopUser.BV_TOKEN, bvToken),
                                value(CloudDesktopUser.REGISTERED, true));
              if (Boolean.TRUE.equals(result.optBoolean(CloudConstants.EXISTING_STATEMENTS))) {
                callback.processCompletionAndDownload();
              }
              else {
                callback.processCompletionAndSelectBank();
              }
              break;
            case UNKNOWN_VALIDATION_CODE:
              callback.processInvalidCode();
              break;
            case TEMP_VALIDATION_CODE_EXPIRED:
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

  public void updateBankFields(Key bankKey, GlobRepository repository, Callback callback) {
    try {
      Glob user = repository.get(CloudDesktopUser.KEY);

      JSONObject result = cloudAPI.getTemporaryBudgeaToken(user.get(CloudDesktopUser.EMAIL), user.get(CloudDesktopUser.BV_TOKEN));
      switch (CloudRequestStatus.get(result.getString(CloudConstants.STATUS))) {
        case OK:
          String token = result.getString(CloudConstants.PROVIDER_TOKEN);
          budgeaAPI.setToken(token);
          break;
        case NO_SUBSCRIPTION:
          callback.processSubscriptionError(getSubscriptionStatus(result));
          return;
        default:
          throw new UnexpectedValue(result.getString(result.getString(CloudConstants.STATUS)));
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
      JSONObject root = budgeaAPI.getBankFields(budgeaBankId);
      resetBankFields(bank, root, repository);

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

  public void resetBankFields(Glob bank, JSONObject root, GlobRepository budgeaRepository) {

    Integer budgeaBankId = bank.get(Bank.PROVIDER_ID);

    GlobList fields = budgeaRepository.getAll(BudgeaBankField.TYPE, fieldEquals(BudgeaBankField.BANK, budgeaBankId));
    budgeaRepository.delete(BudgeaBankFieldValue.TYPE, fieldIn(BudgeaBankFieldValue.FIELD, fields.getValueSet(BudgeaBankField.ID)));
    budgeaRepository.delete(fields);
    budgeaRepository.delete(BudgeaConnection.TYPE, fieldEquals(BudgeaConnection.BANK, budgeaBankId));
    budgeaRepository.delete(BudgeaConnectionValue.TYPE, fieldEquals(BudgeaConnectionValue.CONNECTION, budgeaBankId));
    budgeaRepository.findOrCreate(Key.create(BudgeaBank.TYPE, budgeaBankId),
                                  value(BudgeaBank.BANK, bank.get(Bank.ID)));

    JsonGlobParser jsonParser = createBankParser(budgeaRepository);

    int index = 0;
    for (Object f : root.getJSONArray("fields")) {
      JSONObject field = (JSONObject) f;
      Glob fieldGlob = jsonParser.toGlob(field, BudgeaBankField.TYPE, budgeaRepository,
                                         value(BudgeaBankField.BANK, budgeaBankId),
                                         value(BudgeaBankField.SEQUENCE_INDEX, index++));

      if (field.has("values")) {
        for (Object v : field.getJSONArray("values")) {
          JSONObject value = (JSONObject) v;
          jsonParser.toGlob(value, BudgeaBankFieldValue.TYPE, budgeaRepository,
                            value(BudgeaBankFieldValue.FIELD, fieldGlob.get(BudgeaBankField.ID)));
        }
      }
    }
  }

  public void addBankConnection(final Glob bank, final Glob bankConnection, final GlobRepository repository, final BankConnectionCallback callback) {
    System.out.println("CloudService.addBankConnection");
    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {
          Map<String, String> params = new HashMap<String, String>();
          for (Glob value : repository.findLinkedTo(bankConnection.getKey(), BudgeaConnectionValue.CONNECTION)) {
            Glob field = repository.findLinkTarget(value, BudgeaConnectionValue.FIELD);
            String name = field.get(BudgeaBankField.NAME);
            params.put(name, value.get(BudgeaConnectionValue.VALUE));
          }

          Glob user = repository.get(CloudDesktopUser.KEY);
          String email = user.get(CloudDesktopUser.EMAIL);
          String bvToken = user.get(CloudDesktopUser.BV_TOKEN);
          System.out.println("CloudService.addBankConnection - check provider access");
          if (!cloudAPI.isProviderAccessRegistered(email, bvToken)) {
            System.out.println("CloudService.addBankConnection - add provider access");
            cloudAPI.addProviderAccess(email, bvToken, budgeaAPI.getToken(), budgeaAPI.getUserId());
          }

          System.out.println("CloudService.addBankConnection - getting temp token");

          JSONObject result = cloudAPI.getTemporaryBudgeaToken(email, bvToken);
          switch (CloudRequestStatus.get(result.getString(CloudConstants.STATUS))) {
            case OK:
              budgeaAPI.setToken(result.getString(CloudConstants.PROVIDER_TOKEN));
              break;
            case NO_SUBSCRIPTION:
              callback.processSubscriptionError(getSubscriptionStatus(result));
              return;
            default:
              throw new UnexpectedValue(result.getString(CloudConstants.STATUS));
          }

          System.out.println("CloudService.addBankConnection - starting step1");

          BudgeaAPI.LoginResult connectionResult = budgeaAPI.addBankConnectionStep1(bankConnection.get(BudgeaConnection.BANK), params);
          switch (connectionResult.status) {
            case ACCEPTED:
              System.out.println("CloudService.addBankConnection - login OK");
              processLoginOk(connectionResult.json, repository, callback);
              break;
            case SECOND_STEP_NEEDED:
              System.out.println("CloudService.addBankConnection - needs a second step \n" + connectionResult.json.toString(2));
              int connectionId = connectionResult.json.getInt("id");
              resetBankFields(bank, connectionResult.json, repository);
              callback.processSecondStepResponse(connectionId);
              break;
            case CREDENTIALS_REJECTED:
              System.out.println("CloudService.addBankConnection - credentials rejected");
              callback.processCredentialsRejected();
              break;
            case OTHER_ERROR:
              System.out.println("CloudService.addBankConnection - other error" + connectionResult.json.toString(2));
              callback.processError(null);
              break;
          }
        }
        catch (Exception e) {
          Log.write("Error creating connection", e);
          callback.processError(e);
        }
      }
    });
    thread.start();
  }

  public void addBankConnectionStep2(final int connectionId, final Glob bankConnection, final GlobRepository repository, final BankConnectionCallback callback) {
    System.out.println("CloudService.addBankConnectionStep2");
    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {
          Map<String, String> params = new HashMap<String, String>();
          for (Glob value : repository.findLinkedTo(bankConnection.getKey(), BudgeaConnectionValue.CONNECTION)) {
            Glob field = repository.findLinkTarget(value, BudgeaConnectionValue.FIELD);
            String name = field.get(BudgeaBankField.NAME);
            params.put(name, value.get(BudgeaConnectionValue.VALUE));
          }

          JSONObject connectionResult = budgeaAPI.addBankConnectionStep2(connectionId, params);

          processLoginOk(connectionResult, repository, callback);
        }
        catch (Exception e) {
          Log.write("Error creating connection", e);
          callback.processError(e);
        }
      }
    });
    thread.start();
  }

  private void processLoginOk(JSONObject connectionResult, GlobRepository repository, BankConnectionCallback callback) throws IOException {

    System.out.println("CloudService.processLoginOk");

    int providerConnectionId = connectionResult.getInt("id");

    repository.update(CloudDesktopUser.KEY, CloudDesktopUser.SYNCHRO_ENABLED, true);

    Glob bank = Bank.findByProviderId(Provider.BUDGEA.getId(), connectionResult.getInt("id_bank"), repository);
    String bankName = bank != null ? bank.get(Bank.NAME) : "Bank";

    Glob connection =
      repository.create(CloudProviderConnection.TYPE,
                        value(CloudProviderConnection.PROVIDER, Provider.BUDGEA.getId()),
                        value(CloudProviderConnection.PROVIDER_CONNECTION_ID, providerConnectionId),
                        value(CloudProviderConnection.BANK, bank.get(Bank.ID)),
                        value(CloudProviderConnection.BANK_NAME, bankName),
                        value(CloudProviderConnection.INITIALIZED, false));

    Glob user = repository.get(CloudDesktopUser.KEY);
    String email = user.get(CloudDesktopUser.EMAIL);
    String bvToken = user.get(CloudDesktopUser.BV_TOKEN);
    cloudAPI.addBankConnection(email, bvToken, providerConnectionId);

    callback.processCompletion(connection);
  }

  public void checkBankConnectionReady(final Glob providerConnection, final GlobRepository repository, final BankConnectionCheckCallback callback) {
    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {

          System.out.println("CloudService.checkBankConnectionReady");

          Glob user = repository.get(CloudDesktopUser.KEY);
          Integer providerConnectionId = providerConnection.get(CloudProviderConnection.PROVIDER_CONNECTION_ID);
          JSONObject result =
            cloudAPI.checkBankConnection(user.get(CloudDesktopUser.EMAIL), user.get(CloudDesktopUser.BV_TOKEN), providerConnectionId);

          String status = result.getString(CloudConstants.STATUS);
          switch (CloudRequestStatus.get(status)) {
            case OK:
              break;
            case NO_SUBSCRIPTION:
              callback.processSubscriptionError(getSubscriptionStatus(result));
              return;
            default:
              throw new UnexpectedValue(result.getString(CloudConstants.STATUS));
          }

          boolean initialized;
          JSONArray array = result.getJSONArray("connections");
          if (array.length() == 0) {
            initialized = false;
          }
          else {
            JSONObject connection = (JSONObject) array.get(0);
            initialized = Boolean.TRUE.equals(connection.optBoolean(CloudConstants.INITIALIZED));
          }

          repository.update(providerConnection, CloudProviderConnection.INITIALIZED, initialized);

          callback.processCompletion(initialized);
        }
        catch (Exception e) {
          Log.write("Error retrieving connections", e);
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
          JSONObject connections = cloudAPI.getBankConnections(user.get(CloudDesktopUser.EMAIL), user.get(CloudDesktopUser.BV_TOKEN));

          System.out.println("CloudService.updateBankConnections: " + connections.toString(2));

          repository.startChangeSet();
          repository.update(CloudDesktopUser.KEY, value(CloudDesktopUser.SUBSCRIPTION_END_DATE,
                                                        JsonGlobFormat.parseDate(connections.optString(CloudConstants.SUBSCRIPTION_END_DATE))));
          repository.deleteAll(CloudProviderConnection.TYPE);
          for (Object c : connections.getJSONArray("connections")) {
            JSONObject connection = (JSONObject) c;
            int providerId = connection.getInt(CloudConstants.PROVIDER_ID);
            repository.create(CloudProviderConnection.TYPE,
                              value(CloudProviderConnection.PROVIDER, providerId),
                              value(CloudProviderConnection.PROVIDER_CONNECTION_ID, connection.getInt(CloudConstants.PROVIDER_CONNECTION_ID)),
                              value(CloudProviderConnection.BANK, Bank.findIdByProviderId(providerId, connection.getInt(CloudConstants.PROVIDER_BANK_ID), repository)),
                              value(CloudProviderConnection.BANK_NAME, connection.getString(CloudConstants.BANK_NAME)),
                              value(CloudProviderConnection.INITIALIZED, connection.getBoolean(CloudConstants.INITIALIZED)));
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

  public void deleteBankConnection(final Glob connection, final GlobRepository repository, final Callback callback) {
    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {

          System.out.println("CloudService.deleteBankConnection");

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

  public void downloadStatement(final GlobRepository repository, final DownloadCallback callback) {
    Thread thread = new Thread(new Runnable() {
      public void run() {

        System.out.println("CloudService.downloadStatement");

        try {
          final GlobList importedRealAccounts = doDownloadStatement(repository);
          GuiUtils.runInSwingThread(new Runnable() {
            public void run() {
              callback.processCompletion(importedRealAccounts);
            }
          });
        }
        catch (final SubscriptionError e) {
          GuiUtils.runInSwingThread(new Runnable() {
            public void run() {
              callback.processSubscriptionError(e.status);
            }
          });
        }
        catch (final Exception e) {
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
    String status = result.getString(CloudConstants.STATUS);
    switch (CloudRequestStatus.get(status)) {
      case OK:
        break;
      case NO_SUBSCRIPTION:
        throw new SubscriptionError(getSubscriptionStatus(result));
      default:
        throw new UnexpectedValue(result.getString(CloudConstants.STATUS));
    }

    System.out.println("CloudService.doDownloadStatement\n" + result.toString(2));

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
      if (realAccount != null) {
        JSONArray transactions = account.optJSONArray("transactions");
        if (transactions == null || transactions.length() == 0) {
          continue;
        }
      }

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
//                            value(RealAccount.BANK_ENTITY_LABEL, ),
//                            value(RealAccount.BANK_ENTITY, bankEntityId),
                        value(RealAccount.FILE_NAME, null),
                        value(RealAccount.FILE_CONTENT, account.toString()));
      importedRealAccounts.add(realAccount);
    }

    if (!importedRealAccounts.isEmpty()) {
      int newUpdate = result.getInt("last_update");
      repository.update(CloudDesktopUser.KEY, CloudDesktopUser.LAST_UPDATE, newUpdate);
    }
    return importedRealAccounts;
  }

  public AccountType getAccountType(JSONObject account) {
    String type = account.optString("type");
    return Strings.isNullOrEmpty(type) ? AccountType.MAIN : AccountType.get(type);
  }

  public void deleteCloudAccount(GlobRepository repository, final UnsubscriptionCallback callback) {
    Glob user = repository.findOrCreate(CloudDesktopUser.KEY);
    try {
      cloudAPI.deleteCloudAccount(user.get(CloudDesktopUser.EMAIL),
                                  user.get(CloudDesktopUser.BV_TOKEN));
      GuiUtils.runInSwingThread(new Runnable() {
        public void run() {
          callback.processCompletion();
        }
      });
    }
    catch (final Exception e) {
      Log.write("Error deleting BV cloud account", e);
      GuiUtils.runInSwingThread(new Runnable() {
        public void run() {
          callback.processError(e);
        }
      });
    }
  }

  private Glob createMissingBank(int budgeaBankId, String bankName, GlobRepository repository) {
    return repository.create(Bank.TYPE,
                             value(Bank.NAME, bankName),
                             value(Bank.PROVIDER, Provider.BUDGEA.getId()),
                             value(Bank.PROVIDER_ID, budgeaBankId));

  }

  private JsonGlobParser createBankParser(GlobRepository repository) {
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
