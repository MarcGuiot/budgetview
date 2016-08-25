package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.budgea.Budgea;
import com.budgetview.server.cloud.budgea.BudgeaAccountTypeConverter;
import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.model.ProviderAccount;
import com.budgetview.server.cloud.model.ProviderTransaction;
import com.budgetview.server.utils.DateConverter;
import com.budgetview.shared.model.Provider;
import org.apache.log4j.Logger;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.model.FieldValues;
import org.globsframework.model.Glob;
import org.globsframework.sqlstreams.*;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.sqlstreams.exceptions.GlobsSQLException;
import org.globsframework.streams.GlobStream;
import org.globsframework.streams.accessors.GlobAccessorBuilder;
import org.globsframework.streams.accessors.IntegerAccessor;
import org.globsframework.utils.Files;
import org.globsframework.utils.Ref;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.TooManyItems;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.globsframework.sqlstreams.constraints.Where.fieldEquals;

public class BudgeaWebHookServlet extends HttpServlet {

  private static Logger logger = Logger.getLogger("/budgea");
  private static Pattern pattern = Pattern.compile("Bearer (.*)");
  private GlobsDatabase db;

  public BudgeaWebHookServlet(Directory directory) {
    db = directory.get(GlobsDatabase.class);
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
      logger.error("Invalid credentials provided '" + authorization + "' - request rejected");
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    String token = tokenMatcher.group(1);

    InputStream inputStream = request.getInputStream();
    String json = Files.loadStreamToString(inputStream, "UTF-8");

    Integer userId = null;
    try {
      JSONObject root = new JSONObject(json);
      for (Object c : root.getJSONArray("connections")) {
        JSONObject budgeaConnection = (JSONObject) c;
        int budgeaUserId = budgeaConnection.getInt("id_user");
        userId = getCloudUserId(budgeaUserId, token);
        if (userId == null) {
          response.setStatus(HttpServletResponse.SC_FORBIDDEN);
          return;
        }
        JSONObject bank = budgeaConnection.getJSONObject("bank");
        DbUpdater updater = new DbUpdater(userId);
        try {
          for (Object a : budgeaConnection.getJSONArray("accounts")) {
            JSONObject account = (JSONObject) a;
            updater.saveAccount(userId, bank, account);
          }
        }
        finally {
          updater.commitAndClose();
        }
      }
    }
    catch (Exception e) {
      logger.error("Error parsing update for user '" + userId + "' with token '" + Strings.cut(token, 15) + "'", e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    response.setStatus(HttpServletResponse.SC_OK);
  }

  private Integer getCloudUserId(int budgeaUserId, String token) {
    SqlConnection connection = db.connect();
    try {
      Glob user = connection.selectUnique(CloudUser.TYPE, Where.and(fieldEquals(CloudUser.PROVIDER, Provider.BUDGEA.getId()),
                                                                    fieldEquals(CloudUser.PROVIDER_ID, budgeaUserId),
                                                                    fieldEquals(CloudUser.PROVIDER_ACCESS_TOKEN, token)));
      return user.get(CloudUser.ID);
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
    private SqlConnection sqlConnection;

    private Map<Integer, Integer> accountIds;
    private GlobAccessorBuilder providerAccount;
    private SqlCreateRequest providerAccountCreateRequest;
    private SqlRequest providerAccountUpdateRequest;

    private Map<Integer, Integer> transactionIds;
    private GlobAccessorBuilder providerTransaction;
    private SqlRequest providerTransactionCreateRequest;
    private SqlRequest providerTransactionUpdateRequest;

    public DbUpdater(Integer userId) {

      sqlConnection = db.connect();

      providerAccount = new GlobAccessorBuilder(ProviderAccount.TYPE);
      providerAccountCreateRequest = sqlConnection.startCreate(ProviderAccount.TYPE)
        .setAll(providerAccount.getAccessor())
        .getRequest();
      providerAccountUpdateRequest = sqlConnection.startUpdate(ProviderAccount.TYPE)
        .setAll(providerAccount.getAccessor())
        .getRequest();
      accountIds = loadProviderIds(userId, ProviderAccount.ID, ProviderAccount.PROVIDER_ACCOUNT_ID, ProviderAccount.USER, ProviderAccount.PROVIDER);

      providerTransaction = new GlobAccessorBuilder(ProviderTransaction.TYPE);
      providerTransactionCreateRequest = sqlConnection.startCreate(ProviderTransaction.TYPE)
        .setAll(providerTransaction.getAccessor())
        .getRequest();
      providerTransactionUpdateRequest = sqlConnection.startUpdate(ProviderTransaction.TYPE)
        .setAll(providerTransaction.getAccessor())
        .getRequest();
      transactionIds = loadProviderIds(userId, ProviderTransaction.ID, ProviderTransaction.PROVIDER_ID, ProviderTransaction.USER, ProviderTransaction.PROVIDER);
    }

    public Map<Integer, Integer> loadProviderIds(Integer userId, IntegerField idField, IntegerField providerIdField, LinkField userField, LinkField providerField) {
      Ref<IntegerAccessor> providerId = new Ref<IntegerAccessor>();
      Ref<IntegerAccessor> cloudId = new Ref<IntegerAccessor>();
      SqlSelect query = sqlConnection.startSelect(providerIdField.getGlobType(),
                                                  Where.and(fieldEquals(userField, userId),
                                                            fieldEquals(providerField, Provider.BUDGEA.getId())))
        .select(idField, cloudId)
        .select(providerIdField, providerId)
        .getQuery();
      Map<Integer, Integer> providerIds = new HashMap<Integer, Integer>();
      try {
        GlobStream stream = query.getStream();
        while (stream.next()) {
          providerIds.put(providerId.get().getInteger(),
                          cloudId.get().getInteger());
        }
      }
      finally {
        query.close();
      }
      return providerIds;
    }

    private void saveAccount(Integer userId, JSONObject bank, JSONObject account) throws GlobsSQLException, ParseException {

      Date lastUpdate = Budgea.parseTimestamp(account.getString("last_update"));
      int providerAccountId = account.getInt("id");

      providerAccount
        .set(ProviderAccount.USER, userId)
        .set(ProviderAccount.PROVIDER, Provider.BUDGEA.getId())
        .set(ProviderAccount.PROVIDER_ACCOUNT_ID, providerAccountId)
        .set(ProviderAccount.PROVIDER_BANK_ID, bank.getInt("id"))
        .set(ProviderAccount.PROVIDER_BANK_NAME, bank.getString("name"))
        .set(ProviderAccount.ACCOUNT_TYPE, BudgeaAccountTypeConverter.convertName(account.optString("type")))
        .set(ProviderAccount.NAME, account.getString("name"))
        .set(ProviderAccount.NUMBER, account.getString("number"))
        .set(ProviderAccount.DELETED, !account.isNull("deleted") && account.getBoolean("deleted"))
        .set(ProviderAccount.POSITION, account.getDouble("balance"))
        .set(ProviderAccount.POSITION_MONTH, DateConverter.getMonthId(lastUpdate))
        .set(ProviderAccount.POSITION_DAY, DateConverter.getDay(lastUpdate));

      Integer accountId = null;
      if (accountIds.containsKey(providerAccountId)) {
        accountId = accountIds.get(providerAccountId);
        providerAccount.set(ProviderAccount.ID, accountId);
        providerAccountUpdateRequest.execute();
      }
      else {
        providerAccount.clear(ProviderAccount.ID);
        providerAccountCreateRequest.execute();
        FieldValues keys = providerAccountCreateRequest.getLastGeneratedIds();
        accountId = keys.get(ProviderAccount.ID);
      }

      for (Object t : account.getJSONArray("transactions")) {
        JSONObject transaction = (JSONObject) t;
        saveTransaction(userId, accountId, transaction);
      }
    }

    private void saveTransaction(Integer userId, Integer accountId, JSONObject transaction) throws GlobsSQLException, ParseException {

      int providerTransactionId = transaction.getInt("id");
      Date operationDate = Budgea.parseDate(transaction.getString("rdate"));
      Date bankDate = Budgea.parseDate(transaction.getString("date"));
      JSONObject category = transaction.getJSONObject("category");

      providerTransaction
        .set(ProviderTransaction.USER, userId)
        .set(ProviderTransaction.ACCOUNT, accountId)
        .set(ProviderTransaction.AMOUNT, transaction.getDouble("value"))
        .set(ProviderTransaction.BANK_DATE, bankDate)
        .set(ProviderTransaction.OPERATION_DATE, operationDate)
        .set(ProviderTransaction.LABEL, transaction.getString("wording"))
        .set(ProviderTransaction.ORIGINAL_LABEL, transaction.getString("original_wording"))
        .set(ProviderTransaction.CATEGORY_ID, category.getInt("id"))
        .set(ProviderTransaction.CATEGORY_NAME, category.getString("name"))
        .set(ProviderTransaction.DELETED, !transaction.isNull("deleted") && transaction.getBoolean("deleted"));

      if (accountIds.containsKey(providerTransactionId)) {
        providerTransaction.set(ProviderTransaction.ID, accountIds.get(providerTransactionId));
        providerTransactionUpdateRequest.execute();
      }
      else {
        providerTransaction.clear(ProviderTransaction.ID);
        providerTransactionCreateRequest.execute();
      }
    }

    private void commitAndClose() {
      sqlConnection.commitAndClose();
    }
  }
}
