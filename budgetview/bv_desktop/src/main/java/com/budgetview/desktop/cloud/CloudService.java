package com.budgetview.desktop.cloud;

import com.budgetview.budgea.model.*;
import com.budgetview.model.*;
import com.budgetview.shared.cloud.*;
import com.budgetview.shared.cloud.budgea.BudgeaAPI;
import com.budgetview.shared.model.AccountType;
import com.budgetview.shared.model.Provider;
import com.budgetview.utils.Lang;
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

    void processCompletionAndModifyEmail(String email);

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
                            value(CloudDesktopUser.DEVICE_TOKEN, null),
                            value(CloudDesktopUser.REGISTERED, false));
          JSONObject result = cloudAPI.signup(email, Lang.getLang());
          checkAPIVersion(result);
          switch (CloudRequestStatus.get(result.getString(CloudConstants.STATUS))) {
            case OK:
              Log.write("[Cloud] Signup successful - email: " + email);
              callback.processCompletion();
              break;
            case NO_SUBSCRIPTION:
              Log.write("[Cloud] Signup completed with subscription error");
              callback.processSubscriptionError(getSubscriptionStatus(result));
              break;
            default:
              throw new UnexpectedValue(result.getString(CloudConstants.STATUS));
          }
        }
        catch (Exception e) {
          Log.write("[Cloud] Error during signup", e);
          callback.processError(e);
        }
      }
    });
    thread.start();
  }

  public void validateSignup(final String email, final String code, final GlobRepository repository, final ValidationCallback callback) {
    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {
          JSONObject result = cloudAPI.validateSignup(email, code);
          Log.debug("[Cloud] validateSignup received:" + result.toString(2));
          checkAPIVersion(result);
          switch (CloudValidationStatus.get(result.getString(CloudConstants.STATUS))) {
            case OK:
              repository.update(CloudDesktopUser.KEY,
                                value(CloudDesktopUser.CLOUD_USER_ID, result.getInt(CloudConstants.CLOUD_USER_ID)),
                                value(CloudDesktopUser.DEVICE_ID, result.getInt(CloudConstants.DEVICE_ID)),
                                value(CloudDesktopUser.DEVICE_TOKEN, result.getString(CloudConstants.DEVICE_TOKEN)),
                                value(CloudDesktopUser.REGISTERED, true));
              if (Boolean.TRUE.equals(result.optBoolean(CloudConstants.EXISTING_STATEMENTS))) {
                Log.write("[Cloud] Email validation completed, existing statements");
                callback.processCompletionAndDownload();
              }
              else {
                Log.write("[Cloud] Email validation completed, proceeding to first download");
                callback.processCompletionAndSelectBank();
              }
              break;
            case UNKNOWN_VALIDATION_CODE:
              Log.write("[Cloud] Email validation: unknown code");
              callback.processInvalidCode();
              break;
            case TEMP_VALIDATION_CODE_EXPIRED:
              Log.write("[Cloud] Email validation: temp code expired");
              callback.processTempTokenExpired();
              break;
            case NO_SUBSCRIPTION:
              Log.write("[Cloud] Email validation: subscription error");
              callback.processSubscriptionError(getSubscriptionStatus(result));
              break;
            default:
              Log.write("[Cloud] Email validation: error " + result.toString(2));
              callback.processError(null);
          }
        }
        catch (Exception e) {
          Log.write("[Cloud] Error validating email", e);
          callback.processError(e);
        }
      }
    });
    thread.start();
  }

  public void modifyEmailAddress(final String newEmail, final GlobRepository repository, final Callback callback) {
    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {
          Glob user = repository.get(CloudDesktopUser.KEY);
          int cloudUserId = user.get(CloudDesktopUser.CLOUD_USER_ID);
          int deviceId = user.get(CloudDesktopUser.DEVICE_ID);
          String deviceToken = user.get(CloudDesktopUser.DEVICE_TOKEN);
          String currentEmail = repository.get(CloudDesktopUser.KEY).get(CloudDesktopUser.EMAIL);
          JSONObject result = cloudAPI.modifyEmailAddress(cloudUserId, deviceId, deviceToken, currentEmail, newEmail);
          checkAPIVersion(result);
          switch (CloudRequestStatus.get(result.getString(CloudConstants.STATUS))) {
            case OK:
              Log.write("[Cloud] Email address change completed");
              callback.processCompletion();
              break;
            case NO_SUBSCRIPTION:
              Log.write("[Cloud] Email address change - subscription error");
              callback.processSubscriptionError(getSubscriptionStatus(result));
              break;
            default:
              Log.write("[Cloud] Email address change - unexpected case " + result.toString(2));
              throw new UnexpectedValue(result.getString(CloudConstants.STATUS));
          }
        }
        catch (Exception e) {
          Log.write("[Cloud] Error when modifying email address", e);
          callback.processError(e);
        }
      }
    });
    thread.start();
  }

  public void validateEmailModification(final String email, final String code, final GlobRepository repository, final ValidationCallback callback) {
    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {
          Glob user = repository.get(CloudDesktopUser.KEY);
          int cloudUserId = user.get(CloudDesktopUser.CLOUD_USER_ID);
          int deviceId = user.get(CloudDesktopUser.DEVICE_ID);
          String deviceToken = user.get(CloudDesktopUser.DEVICE_TOKEN);
          JSONObject result = cloudAPI.validateEmailModification(cloudUserId, deviceId, deviceToken, email, code);
          Log.debug("[Cloud] validateEmailModification received:" + result.toString(2));
          checkAPIVersion(result);
          switch (CloudValidationStatus.get(result.getString(CloudConstants.STATUS))) {
            case OK:
              Log.write("[Cloud] Email modication validation completed");
              String newEmail = result.getString(CloudConstants.NEW_EMAIL);
              repository.update(CloudDesktopUser.KEY,
                                value(CloudDesktopUser.EMAIL, newEmail));
              callback.processCompletionAndModifyEmail(newEmail);
              break;
            case UNKNOWN_VALIDATION_CODE:
              Log.write("[Cloud] Email modication validation - subscription error");
              callback.processInvalidCode();
              break;
            case TEMP_VALIDATION_CODE_EXPIRED:
              Log.write("[Cloud] Email modication validation - temp validation code expired");
              callback.processTempTokenExpired();
              break;
            case NO_SUBSCRIPTION:
              Log.write("[Cloud] Email modication validation - no subscription found");
              callback.processSubscriptionError(getSubscriptionStatus(result));
              break;
            default:
              Log.write("[Cloud] Email modication validation - error " + result.toString(2));
              callback.processError(null);
          }
        }
        catch (Exception e) {
          Log.write("[Cloud] Error validating email", e);
          callback.processError(e);
        }
      }
    });
    thread.start();
  }

  public void updateBankFields(Key bankKey, GlobRepository repository, Callback callback) {
    try {
      Glob user = repository.get(CloudDesktopUser.KEY);

      int cloudUserId = user.get(CloudDesktopUser.CLOUD_USER_ID);
      int deviceId = user.get(CloudDesktopUser.DEVICE_ID);
      String deviceToken = user.get(CloudDesktopUser.DEVICE_TOKEN);
      JSONObject result = cloudAPI.getTemporaryBudgeaToken(cloudUserId, deviceId, deviceToken);
      switch (CloudRequestStatus.get(result.getString(CloudConstants.STATUS))) {
        case OK:
          Log.write("[Cloud] Updated bank fields");
          String token = result.getString(CloudConstants.PROVIDER_TOKEN);
          budgeaAPI.setToken(token);
          break;
        case NO_SUBSCRIPTION:
          Log.write("[Cloud] Bank fields update subscription error");
          callback.processSubscriptionError(getSubscriptionStatus(result));
          return;
        default:
          Log.write("[Cloud] Bank fields update error");
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
      Log.write("[Cloud] Error retrieving bank fields", e);
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
    Log.debug("[Cloud] addBankConnection: " + bank);
    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {
          Map<String, String> params = getParametersMap(repository, bankConnection);

          Glob user = repository.get(CloudDesktopUser.KEY);
          int cloudUserId = user.get(CloudDesktopUser.CLOUD_USER_ID);
          int deviceId = user.get(CloudDesktopUser.DEVICE_ID);
          String deviceToken = user.get(CloudDesktopUser.DEVICE_TOKEN);
          if (!cloudAPI.isProviderAccessRegistered(cloudUserId, deviceId, deviceToken)) {
            cloudAPI.addProviderAccess(cloudUserId, deviceId, deviceToken, budgeaAPI.getToken(), budgeaAPI.getUserId());
          }

          JSONObject result = cloudAPI.getTemporaryBudgeaToken(cloudUserId, deviceId, deviceToken);
          checkAPIVersion(result);
          switch (CloudRequestStatus.get(result.getString(CloudConstants.STATUS))) {
            case OK:
              Log.write("[Cloud] Bank connection added");
              budgeaAPI.setToken(result.getString(CloudConstants.PROVIDER_TOKEN));
              break;
            case NO_SUBSCRIPTION:
              Log.write("[Cloud] Bank connection could not be added - subscription error");
              callback.processSubscriptionError(getSubscriptionStatus(result));
              return;
            default:
              Log.write("[Cloud] Bank connection could not be added - error");
              throw new UnexpectedValue(result.getString(CloudConstants.STATUS));
          }

          Log.debug("[Cloud] addBankConnection - starting step1");

          BudgeaAPI.LoginResult connectionResult = budgeaAPI.addBankConnectionStep1(bankConnection.get(BudgeaConnection.BANK), params);
          switch (connectionResult.status) {
            case ACCEPTED:
              Log.debug("[Cloud] addBankConnection - login OK");
              processLoginOk(connectionResult.json, repository, callback);
              break;
            case SECOND_STEP_NEEDED:
              Log.debug("[Cloud] addBankConnection - needs a second step \n" + connectionResult.json.toString(2));
              int connectionId = connectionResult.json.getInt("id");
              resetBankFields(bank, connectionResult.json, repository);
              callback.processSecondStepResponse(connectionId);
              break;
            case CREDENTIALS_REJECTED:
              Log.write("[Cloud] addBankConnection - credentials rejected");
              callback.processCredentialsRejected();
              break;
            case OTHER_ERROR:
              Log.debug("[Cloud] addBankConnection - other error" + connectionResult.json.toString(2));
              callback.processError(null);
              break;
          }
        }
        catch (Exception e) {
          Log.write("[Cloud] Error creating connection", e);
          callback.processError(e);
        }
      }
    });
    thread.start();
  }

  public void addBankConnectionStep2(final int connectionId, final Glob bankConnection, final GlobRepository repository, final BankConnectionCallback callback) {
    Log.debug("[Cloud] addBankConnectionStep2");
    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {
          JSONObject connectionResult =
            budgeaAPI.addBankConnectionStep2(connectionId, getParametersMap(repository, bankConnection));
          processLoginOk(connectionResult, repository, callback);
        }
        catch (Exception e) {
          Log.write("[Cloud] Error adding bank connection / step 2", e);
          callback.processError(e);
        }
      }
    });
    thread.start();
  }

  public void updatePassword(final int connectionId, final Glob bankConnection, final GlobRepository repository, final BankConnectionCallback callback) {
    Log.debug("[Cloud] updatePassword");
    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {

          BudgeaAPI.LoginResult connectionResult =
            budgeaAPI.updateBankPassword(connectionId, getParametersMap(repository, bankConnection));
          switch (connectionResult.status) {
            case ACCEPTED:
              Log.debug("[Cloud] Password updated");
              processLoginOk(connectionResult.json, repository, callback);
              break;
            case SECOND_STEP_NEEDED:
              Log.debug("[Cloud] Password update - needs a second step");
              int connectionId = connectionResult.json.getInt("id");
              resetBankFields(repository.findLinkTarget(bankConnection, BudgeaConnection.BANK), connectionResult.json, repository);
              callback.processSecondStepResponse(connectionId);
              break;
            case CREDENTIALS_REJECTED:
              Log.debug("[Cloud] Password update - credentials rejected");
              callback.processCredentialsRejected();
              break;
            case OTHER_ERROR:
              Log.debug("[Cloud] Password update - other error" + connectionResult.json.toString(2));
              callback.processError(null);
              break;
          }
        }
        catch (Exception e) {
          Log.write("[Cloud] Error updatingn password", e);
          callback.processError(e);
        }
      }
    });
    thread.start();
  }

  public static Map<String, String> getParametersMap(GlobRepository repository, Glob bankConnection) {
    Map<String, String> params = new HashMap<String, String>();
    for (Glob value : repository.findLinkedTo(bankConnection.getKey(), BudgeaConnectionValue.CONNECTION)) {
      Glob field = repository.findLinkTarget(value, BudgeaConnectionValue.FIELD);
      String name = field.get(BudgeaBankField.NAME);
      params.put(name, value.get(BudgeaConnectionValue.VALUE));
    }
    return params;
  }

  private void processLoginOk(JSONObject connectionResult, GlobRepository repository, BankConnectionCallback callback) throws IOException {

    Log.debug("[Cloud] processLoginOk");

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
                        value(CloudProviderConnection.INITIALIZED, false),
                        value(CloudProviderConnection.PASSWORD_ERROR, false));

    Glob user = repository.get(CloudDesktopUser.KEY);
    int cloudUserId = user.get(CloudDesktopUser.CLOUD_USER_ID);
    int deviceId = user.get(CloudDesktopUser.DEVICE_ID);
    String deviceToken = user.get(CloudDesktopUser.DEVICE_TOKEN);
    cloudAPI.addBankConnection(cloudUserId, deviceId, deviceToken, providerConnectionId);

    Log.write("[Cloud] Login successfully completed for bank " + bankName);
    callback.processCompletion(connection);
  }

  public void checkBankConnectionReady(final Glob providerConnection, final GlobRepository repository, final BankConnectionCheckCallback callback) {
    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {

          Log.debug("[Cloud] checkBankConnectionReady");

          Glob user = repository.get(CloudDesktopUser.KEY);
          Integer providerConnectionId = providerConnection.get(CloudProviderConnection.PROVIDER_CONNECTION_ID);
          int cloudUserId = user.get(CloudDesktopUser.CLOUD_USER_ID);
          int deviceId = user.get(CloudDesktopUser.DEVICE_ID);
          String deviceToken = user.get(CloudDesktopUser.DEVICE_TOKEN);
          JSONObject result = cloudAPI.checkBankConnection(cloudUserId, deviceId, deviceToken, providerConnectionId);

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

          boolean initialized = false;
          boolean passwordError = false;
          JSONArray array = result.getJSONArray("connections");
          if (array.length() > 0) {
            JSONObject connection = (JSONObject) array.get(0);
            initialized = Boolean.TRUE.equals(connection.optBoolean(CloudConstants.INITIALIZED));
            passwordError = Boolean.TRUE.equals(connection.optBoolean(CloudConstants.PASSWORD_ERROR));
          }

          repository.update(providerConnection,
                            value(CloudProviderConnection.INITIALIZED, initialized),
                            value(CloudProviderConnection.PASSWORD_ERROR, passwordError));

          callback.processCompletion(initialized);
        }
        catch (Exception e) {
          Log.write("[Cloud] Error retrieving connections", e);
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
          int cloudUserId = user.get(CloudDesktopUser.CLOUD_USER_ID);
          int deviceId = user.get(CloudDesktopUser.DEVICE_ID);
          String deviceToken = user.get(CloudDesktopUser.DEVICE_TOKEN);
          JSONObject connections = cloudAPI.getBankConnections(cloudUserId, deviceId, deviceToken);

          Log.debug("[Cloud] updateBankConnections: " + connections.toString(2));

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
                              value(CloudProviderConnection.INITIALIZED, connection.getBoolean(CloudConstants.INITIALIZED)),
                              value(CloudProviderConnection.PASSWORD_ERROR, connection.getBoolean(CloudConstants.PASSWORD_ERROR)));
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

          Log.debug("[Cloud] deleteBankConnection");

          Glob user = repository.get(CloudDesktopUser.KEY);

          int cloudUserId = user.get(CloudDesktopUser.CLOUD_USER_ID);
          int deviceId = user.get(CloudDesktopUser.DEVICE_ID);
          String deviceToken = user.get(CloudDesktopUser.DEVICE_TOKEN);
          cloudAPI.deleteConnection(cloudUserId, deviceId, deviceToken,
                                    connection.get(CloudProviderConnection.PROVIDER),
                                    connection.get(CloudProviderConnection.PROVIDER_CONNECTION_ID));

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

  public GlobList doDownloadStatement(GlobRepository repository) throws SubscriptionError, IOException, InvalidCloudAPIVersion {
    Glob user = repository.findOrCreate(CloudDesktopUser.KEY);
    Integer lastUpdate = user.get(CloudDesktopUser.LAST_UPDATE);
    int cloudUserId = user.get(CloudDesktopUser.CLOUD_USER_ID);
    int deviceId = user.get(CloudDesktopUser.DEVICE_ID);
    String deviceToken = user.get(CloudDesktopUser.DEVICE_TOKEN);
    JSONObject result = cloudAPI.getStatement(cloudUserId, deviceId, deviceToken, lastUpdate);
    checkAPIVersion(result);
    String status = result.getString(CloudConstants.STATUS);
    switch (CloudRequestStatus.get(status)) {
      case OK:
        break;
      case NO_SUBSCRIPTION:
        throw new SubscriptionError(getSubscriptionStatus(result));
      default:
        throw new UnexpectedValue(result.getString(CloudConstants.STATUS));
    }

    Log.debug("[Cloud] doDownloadStatement\n" + result.toString(2));

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

  private void checkAPIVersion(JSONObject result) throws InvalidCloudAPIVersion {
    int apiVersion = result.optInt(CloudConstants.API_VERSION, -1);
    if (apiVersion != CloudConstants.CURRENT_API_VERSION) {
      throw new InvalidCloudAPIVersion();
    }
  }

  public AccountType getAccountType(JSONObject account) {
    String type = account.optString("type");
    return Strings.isNullOrEmpty(type) ? AccountType.MAIN : AccountType.get(type);
  }

  public void deleteCloudAccount(final GlobRepository repository, final UnsubscriptionCallback callback) {
    Log.debug("[Cloud] deleteCloudAccount");
    Thread thread = new Thread(new Runnable() {
      public void run() {
        Glob user = repository.findOrCreate(CloudDesktopUser.KEY);
        try {
          int cloudUserId = user.get(CloudDesktopUser.CLOUD_USER_ID);
          int deviceId = user.get(CloudDesktopUser.DEVICE_ID);
          String deviceToken = user.get(CloudDesktopUser.DEVICE_TOKEN);
          cloudAPI.deleteCloudAccount(cloudUserId, deviceId, deviceToken);
          GuiUtils.runInSwingThread(new Runnable() {
            public void run() {
              callback.processCompletion();
            }
          });
        }
        catch (final Exception e) {
          Log.write("[Cloud] Error deleting BV cloud account", e);
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
