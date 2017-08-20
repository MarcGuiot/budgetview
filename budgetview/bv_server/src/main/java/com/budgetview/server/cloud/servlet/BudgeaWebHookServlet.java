package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.budgea.Budgea;
import com.budgetview.server.cloud.budgea.BudgeaAccountTypeConverter;
import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.model.ProviderAccount;
import com.budgetview.server.cloud.model.ProviderTransaction;
import com.budgetview.server.cloud.model.ProviderUpdate;
import com.budgetview.server.cloud.services.CloudSerializationService;
import com.budgetview.server.cloud.services.WebhookNotificationService;
import com.budgetview.server.utils.DateConverter;
import com.budgetview.shared.cloud.budgea.BudgeaAPI;
import com.budgetview.shared.cloud.budgea.BudgeaSeriesConverter;
import com.budgetview.shared.http.Http;
import com.budgetview.shared.model.DefaultSeries;
import com.budgetview.shared.model.Provider;
import org.apache.log4j.Logger;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.sqlstreams.exceptions.GlobsSQLException;
import org.globsframework.utils.Dates;
import org.globsframework.utils.Files;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.TooManyItems;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.sqlstreams.constraints.Where.fieldEquals;

public class BudgeaWebHookServlet extends HttpCloudServlet {

  private static Logger logger = Logger.getLogger("BudgeaWebHookServlet");
  private static Pattern pattern = Pattern.compile("Bearer (.*)");

  private GlobsDatabase db;
  private CloudSerializationService serializer;
  private WebhookNotificationService webhookNotifications;

  public BudgeaWebHookServlet(Directory directory) throws Exception {
    super(directory);
    db = directory.get(GlobsDatabase.class);
    serializer = directory.get(CloudSerializationService.class);
    webhookNotifications = new WebhookNotificationService(directory);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");

    String authorization = request.getHeader("Authorization");
    if (Strings.isNullOrEmpty(authorization)) {
      logger.error("No credentials provided - request rejected");
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    Matcher tokenMatcher = pattern.matcher(authorization.trim());
    if (!tokenMatcher.matches()) {
      logger.error("Invalid credentials provided in '" + authorization + "' - request rejected");
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    Glob user = null;
    String token = "...";
    WebhookNotificationService.Notifications notifications = webhookNotifications.start();

    try {
      token = tokenMatcher.group(1);

      InputStream inputStream = request.getInputStream();
      String json = Files.loadStreamToString(inputStream, "UTF-8");
      JSONObject root = new JSONObject(json);

      if (logger.isDebugEnabled()) {
        logger.debug("Budgea webhook called with: " + root.toString(2));
      }

      JSONArray array = root.optJSONArray("connections");
      if (array == null) {
        logger.error("Missing array 'connections' in budgea call - content: " + root.toString(2));
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
      }
      for (Object c : array) {
        JSONObject budgeaConnection = (JSONObject) c;
        int connectionId = budgeaConnection.getInt("id");
        int budgeaUserId = budgeaConnection.getInt("id_user");
        user = getCloudUser(budgeaUserId, token);
        if (user == null) {
          response.setStatus(HttpServletResponse.SC_OK);
          logger.error("Received update for unknown budgea user " + budgeaUserId);
          runDeleteBudgeaUserCommand(budgeaUserId, token);
          return;
        }
        boolean passwordError = "wrongpass".equalsIgnoreCase(budgeaConnection.optString("error"));
        JSONObject bank = budgeaConnection.getJSONObject("bank");
        JSONArray accounts = budgeaConnection.optJSONArray("accounts");
        boolean containsAccounts = false;
        if (accounts != null && accounts.length() > 0) {
          DbUpdater updater = new DbUpdater(user.get(CloudUser.ID), connectionId);
          for (Object a : accounts) {
            JSONObject account = (JSONObject) a;
            containsAccounts |= updater.loadAccount(connectionId, bank, account);
          }
          updater.save();
        }
        else {
          logger.debug("No account for connection");
        }
        notifications.addConnection(connectionId,
                                    bank.optString("name"),
                                    containsAccounts,
                                    passwordError);
        logger.debug("Webhook saved");
      }
    }
    catch (ParseException e) {
      logger.error("Error parsing / storing update for user '" + user + "' with token '" + Strings.cut(token, 15) + "'", e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }
    catch (Exception e) {
      logger.error("Uncaught exception for user '" + user + "' with token '" + Strings.cut(token, 15) + "'", e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    try {
      if (user != null) {
        notifications.send(user);
        logger.info("Update processed for user " + user.get(CloudUser.ID));
        response.setStatus(HttpServletResponse.SC_OK);
      }
      else {
        logger.info("No user found in this update");
      }
    }
    catch (Exception e) {
      logger.error("Failed to process notification for user " + user, e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private void runDeleteBudgeaUserCommand(final int budgeaUserId, final String token) {
    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {
          BudgeaAPI budgeaAPI = new BudgeaAPI();
          budgeaAPI.setToken(token);
          budgeaAPI.deleteUser(budgeaUserId);
        }
        catch (IOException e) {
          logger.error("Failed to delete unmanaged user with budgea ID " + budgeaUserId, e);
        }
      }
    });
    thread.start();
  }

  private Glob getCloudUser(int budgeaUserId, String token) {
    SqlConnection connection = db.connect();
    try {
      return connection.selectUnique(CloudUser.TYPE, Where.and(fieldEquals(CloudUser.PROVIDER, Provider.BUDGEA.getId()),
                                                               fieldEquals(CloudUser.PROVIDER_USER_ID, budgeaUserId),
                                                               fieldEquals(CloudUser.PROVIDER_ACCESS_TOKEN, token)));
    }
    catch (ItemNotFound itemNotFound) {
      logger.error("User '" + budgeaUserId + "' with token '" + Strings.cut(token, 15) + "' not recognized");
    }
    catch (TooManyItems tooManyItems) {
      logger.error("Several entries found for user '" + budgeaUserId + "' with token '" + Strings.cut(token, 15) + "'");
    }
    return null;
  }

  private class DbUpdater {
    private Integer userId;
    private int connectionId;
    private GlobRepository repository = GlobRepositoryBuilder.createEmpty();

    public DbUpdater(Integer userId, int connectionId) {
      this.userId = userId;
      this.connectionId = connectionId;
    }

    public boolean loadAccount(int connectionId, JSONObject bank, JSONObject account) throws GlobsSQLException, ParseException {
      String lastUpdateValue = account.optString("last_update");
      JSONArray transactions = account.optJSONArray("transactions");
      Date lastUpdate;
      if (Strings.isNotEmpty(lastUpdateValue)) {
        lastUpdate = Budgea.parseTimestamp(lastUpdateValue);
      }
      else if (transactions != null && transactions.length() != 0) {
        lastUpdate = Dates.now();
      }
      else {
        return false;
      }

      int providerAccountId = account.getInt("id");
      repository.create(ProviderAccount.TYPE,
                        value(ProviderAccount.ID, providerAccountId),
                        value(ProviderAccount.PROVIDER, Provider.BUDGEA.getId()),
                        value(ProviderAccount.PROVIDER_CONNECTION, connectionId),
                        value(ProviderAccount.PROVIDER_BANK_ID, bank.getInt("id")),
                        value(ProviderAccount.PROVIDER_BANK_NAME, bank.getString("name")),
                        value(ProviderAccount.ACCOUNT_TYPE, BudgeaAccountTypeConverter.convertName(account.optString("type"))),
                        value(ProviderAccount.NAME, account.getString("name")),
                        value(ProviderAccount.NUMBER, account.getString("number")),
                        value(ProviderAccount.DELETED, Boolean.TRUE.equals(account.optBoolean("deleted"))),
                        value(ProviderAccount.POSITION, account.getDouble("balance")),
                        value(ProviderAccount.POSITION_MONTH, DateConverter.getMonthId(lastUpdate)),
                        value(ProviderAccount.POSITION_DAY, DateConverter.getDay(lastUpdate)));

      if (transactions != null) {
        for (Object t : transactions) {
          JSONObject transaction = (JSONObject) t;
          loadTransaction(providerAccountId, transaction);
        }
      }

      return true;
    }

    public void loadTransaction(Integer accountId, JSONObject transaction) throws GlobsSQLException, ParseException {
      int providerTransactionId = transaction.getInt("id");
      Date operationDate = Budgea.parseDate(transaction.getString("rdate"));
      Date bankDate = Budgea.parseDate(transaction.getString("date"));
      JSONObject category = transaction.getJSONObject("category");
      DefaultSeries defaultSeries = findDefaultSeries(category.getInt("id"));

      repository.create(ProviderTransaction.TYPE,
                        value(ProviderTransaction.ID, providerTransactionId),
                        value(ProviderTransaction.ACCOUNT, accountId),
                        value(ProviderTransaction.AMOUNT, transaction.getDouble("value")),
                        value(ProviderTransaction.BANK_DATE, bankDate),
                        value(ProviderTransaction.OPERATION_DATE, operationDate),
                        value(ProviderTransaction.LABEL, transaction.getString("wording")),
                        value(ProviderTransaction.ORIGINAL_LABEL, transaction.getString("original_wording")),
                        value(ProviderTransaction.DEFAULT_SERIES_ID, defaultSeries != null ? defaultSeries.getId() : null),
                        value(ProviderTransaction.PROVIDER_CATEGORY_NAME, category.getString("name")),
                        value(ProviderTransaction.DELETED, Boolean.TRUE.equals(transaction.optBoolean("deleted"))));
    }

    private DefaultSeries findDefaultSeries(Integer providerSeriesId) {
      BudgeaSeriesConverter converter = new BudgeaSeriesConverter();
      DefaultSeries defaultSeries = converter.convert(providerSeriesId);
      if (DefaultSeries.UNCATEGORIZED.equals(defaultSeries)) {
        return null;
      }
      return defaultSeries;
    }

    public void save() throws Exception {
      if (!repository.contains(ProviderAccount.TYPE)) {
        logger.debug("No accounts created");
        return;
      }

      SqlConnection sqlConnection = db.connect();
      sqlConnection
        .startCreate(ProviderUpdate.TYPE)
        .set(ProviderUpdate.PROVIDER, Provider.BUDGEA.getId())
        .set(ProviderUpdate.PROVIDER_CONNECTION, connectionId)
        .set(ProviderUpdate.USER, userId)
        .set(ProviderUpdate.DATE, new Date())
        .set(ProviderUpdate.DATA, serializer.toBlob(repository))
        .run();
      sqlConnection.commitAndClose();
    }
  }
}
