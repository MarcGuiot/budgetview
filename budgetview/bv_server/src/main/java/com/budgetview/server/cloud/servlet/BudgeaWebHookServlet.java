package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.Budgea;
import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.model.Provider;
import com.budgetview.server.cloud.model.ProviderAccount;
import com.budgetview.server.cloud.model.ProviderTransaction;
import com.budgetview.server.utils.DateConverter;
import org.apache.log4j.Logger;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.model.Glob;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SelectQuery;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.streams.GlobStream;
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

import static org.globsframework.sqlstreams.constraints.Constraints.and;
import static org.globsframework.sqlstreams.constraints.Constraints.equal;

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
    Matcher progressMatcher = pattern.matcher(authorization.trim());
    if (!progressMatcher.matches()) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    String token = progressMatcher.group(1);
    logger.info(token);

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

  private Integer getCloudUserId(int userId, String token) {
    SqlConnection connection = db.connect();
    try {
      Glob user = connection.selectUnique(CloudUser.TYPE, and(equal(CloudUser.PROVIDER, Provider.BUDGEA.getId()),
                                                              equal(CloudUser.PROVIDER_ID, userId),
                                                              equal(CloudUser.PROVIDER_ACCESS_TOKEN, token)));
      return user.get(CloudUser.ID);
    }
    catch (ItemNotFound itemNotFound) {
      logger.error("User '" + userId + "' with token '" + Strings.cut(token, 15) + "' not recognized");
    }
    catch (TooManyItems tooManyItems) {
      logger.error("Several entries found for user '" + userId + "' with token '" + Strings.cut(token, 15) + "'");
    }
    return null;
  }

  private class DbUpdater {
    private SqlConnection sqlConnection;
    private Map<Integer, Integer> accountIds;
    private Map<Integer, Integer> transactionIds;

    public DbUpdater(Integer userId) {
      sqlConnection = db.connect();
      accountIds = loadProviderIds(userId, ProviderAccount.ID, ProviderAccount.PROVIDER_ID, ProviderAccount.USER, ProviderAccount.PROVIDER);
      transactionIds = loadProviderIds(userId, ProviderTransaction.ID, ProviderTransaction.PROVIDER_ID, ProviderTransaction.USER, ProviderTransaction.PROVIDER);
    }

    public Map<Integer, Integer> loadProviderIds(Integer userId, IntegerField idField, IntegerField providerIdField, LinkField userField, LinkField providerField) {
      Ref<IntegerAccessor> providerId = new Ref<IntegerAccessor>();
      Ref<IntegerAccessor> cloudId = new Ref<IntegerAccessor>();
      SelectQuery query = sqlConnection.startSelect(providerIdField.getGlobType(),
                                                    and(equal(userField, userId),
                                                        equal(providerField, Provider.BUDGEA.getId())))
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

    private void saveAccount(Integer userId, JSONObject bank, JSONObject account) throws ParseException {

      Date lastUpdate = Budgea.parseTimestamp(account.getString("last_update"));
      int accountId = account.getInt("id");

      GlobBuilder providerAccount =
        GlobBuilder.init(ProviderAccount.TYPE)
          .set(ProviderAccount.USER, userId)
          .set(ProviderAccount.PROVIDER, Provider.BUDGEA.getId())
          .set(ProviderAccount.PROVIDER_ID, accountId)
          .set(ProviderAccount.PROVIDER_BANK_ID, bank.getInt("id"))
          .set(ProviderAccount.PROVIDER_BANK_NAME, bank.getString("name"))
          .set(ProviderAccount.ACCOUNT_TYPE, account.getString("type"))
          .set(ProviderAccount.NAME, account.getString("name"))
          .set(ProviderAccount.NUMBER, account.getString("number"))
          .set(ProviderAccount.DELETED, !account.isNull("deleted") && account.getBoolean("deleted"))
          .set(ProviderAccount.POSITION, account.getDouble("balance"))
          .set(ProviderAccount.POSITION_MONTH, DateConverter.getMonthId(lastUpdate))
          .set(ProviderAccount.POSITION_DAY, DateConverter.getDay(lastUpdate));

      if (accountIds.containsKey(accountId)) {
        providerAccount.set(ProviderAccount.ID, accountIds.get(accountId));
        sqlConnection.update(providerAccount.get());
      }
      else {
        sqlConnection.create(providerAccount.get());
      }
    }

    private void commitAndClose() {

      GlobPrinter.print(sqlConnection.selectAll(ProviderAccount.TYPE));

      sqlConnection.commitAndClose();
    }
  }
}
