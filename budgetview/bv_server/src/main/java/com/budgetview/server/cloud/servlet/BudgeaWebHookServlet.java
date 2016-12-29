package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.budgea.Budgea;
import com.budgetview.server.cloud.budgea.BudgeaAccountTypeConverter;
import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.model.ProviderAccount;
import com.budgetview.server.cloud.model.ProviderTransaction;
import com.budgetview.server.cloud.model.ProviderUpdate;
import com.budgetview.server.cloud.persistence.CloudSerializer;
import com.budgetview.server.utils.DateConverter;
import com.budgetview.shared.model.Provider;
import org.apache.log4j.Logger;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.sqlstreams.exceptions.GlobsSQLException;
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
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.sqlstreams.constraints.Where.fieldEquals;

public class BudgeaWebHookServlet extends HttpCloudServlet {

  private static Logger logger = Logger.getLogger("/budgea");
  private static Pattern pattern = Pattern.compile("Bearer (.*)");

  private GlobsDatabase db;
  private CloudSerializer serializer;

  public BudgeaWebHookServlet(Directory directory) throws Exception {
    super(directory);
    db = directory.get(GlobsDatabase.class);
    serializer = new CloudSerializer(directory);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");

    String authorization = request.getHeader("Authorization");
    if (Strings.isNullOrEmpty(authorization)) {
      logger.error("No credentials provided - request rejected");
      setUnauthorized(response);
      return;
    }
    Matcher tokenMatcher = pattern.matcher(authorization.trim());
    if (!tokenMatcher.matches()) {
      logger.error("Invalid credentials provided in '" + authorization + "' - request rejected");
      setUnauthorized(response);
      return;
    }

    String token = tokenMatcher.group(1);

    InputStream inputStream = request.getInputStream();
    String json = Files.loadStreamToString(inputStream, "UTF-8");

    Integer userId = null;
    try {
      JSONObject root = new JSONObject(json);

      logger.info("Budgea webhook called with: " + root.toString(2));

      JSONArray array = root.optJSONArray("connections");
      if (array == null) {
        logger.error("Missing array 'connections' in budgea call - content: " + root.toString(2));
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
      }
      for (Object c : array) {
        JSONObject budgeaConnection = (JSONObject) c;
        int budgeaUserId = budgeaConnection.getInt("id_user");
        userId = getCloudUserId(budgeaUserId, token);
        if (userId == null) {
          response.setStatus(HttpServletResponse.SC_FORBIDDEN);
          return;
        }
        JSONObject bank = budgeaConnection.getJSONObject("bank");
        DbUpdater updater = new DbUpdater(userId);
        JSONArray accounts = budgeaConnection.optJSONArray("accounts");
        if (accounts != null) {
          for (Object a : accounts) {
            JSONObject account = (JSONObject) a;
            updater.loadAccount(bank, account);
          }
        }
        else {
          logger.info("No account for connection");
        }
        updater.save();
      }
    }
    catch (ParseException e) {
      logger.error("Error parsing / storing update for user '" + userId + "' with token '" + Strings.cut(token, 15) + "'", e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }
    catch (GeneralSecurityException e) {
      logger.error("Error cyphering update for user '" + userId + "' with token '" + Strings.cut(token, 15) + "'", e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    logger.info("Webhook successfully saved");
    response.setStatus(HttpServletResponse.SC_OK);
  }

  private Integer getCloudUserId(int budgeaUserId, String token) {
    SqlConnection connection = db.connect();
    try {
      Glob user = connection.selectUnique(CloudUser.TYPE, Where.and(fieldEquals(CloudUser.PROVIDER, Provider.BUDGEA.getId()),
                                                                    fieldEquals(CloudUser.PROVIDER_USER_ID, budgeaUserId),
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
    private Integer userId;
    private GlobRepository repository = GlobRepositoryBuilder.createEmpty();
    private SqlConnection sqlConnection;

    public DbUpdater(Integer userId) {
      this.userId = userId;
      this.sqlConnection = db.connect();
    }

    public void loadAccount(JSONObject bank, JSONObject account) throws GlobsSQLException, ParseException {

      Date lastUpdate = Budgea.parseTimestamp(account.getString("last_update"));
      int providerAccountId = account.getInt("id");

      repository.create(ProviderAccount.TYPE,
                        value(ProviderAccount.ID, providerAccountId),
                        value(ProviderAccount.PROVIDER_BANK_ID, bank.getInt("id")),
                        value(ProviderAccount.PROVIDER_BANK_NAME, bank.getString("name")),
                        value(ProviderAccount.ACCOUNT_TYPE, BudgeaAccountTypeConverter.convertName(account.optString("type"))),
                        value(ProviderAccount.NAME, account.getString("name")),
                        value(ProviderAccount.NUMBER, account.getString("number")),
                        value(ProviderAccount.DELETED, !account.isNull("deleted") && account.getBoolean("deleted")),
                        value(ProviderAccount.POSITION, account.getDouble("balance")),
                        value(ProviderAccount.POSITION_MONTH, DateConverter.getMonthId(lastUpdate)),
                        value(ProviderAccount.POSITION_DAY, DateConverter.getDay(lastUpdate)));

      for (Object t : account.getJSONArray("transactions")) {
        JSONObject transaction = (JSONObject) t;
        loadTransaction(providerAccountId, transaction);
      }
    }

    public void loadTransaction(Integer accountId, JSONObject transaction) throws GlobsSQLException, ParseException {
      int providerTransactionId = transaction.getInt("id");
      Date operationDate = Budgea.parseDate(transaction.getString("rdate"));
      Date bankDate = Budgea.parseDate(transaction.getString("date"));
      JSONObject category = transaction.getJSONObject("category");

      repository.create(ProviderTransaction.TYPE,
                        value(ProviderTransaction.ID, providerTransactionId),
                        value(ProviderTransaction.ACCOUNT, accountId),
                        value(ProviderTransaction.AMOUNT, transaction.getDouble("value")),
                        value(ProviderTransaction.BANK_DATE, bankDate),
                        value(ProviderTransaction.OPERATION_DATE, operationDate),
                        value(ProviderTransaction.LABEL, transaction.getString("wording")),
                        value(ProviderTransaction.ORIGINAL_LABEL, transaction.getString("original_wording")),
                        value(ProviderTransaction.PROVIDER_CATEGORY_ID, category.getInt("id")),
                        value(ProviderTransaction.PROVIDER_CATEGORY_NAME, category.getString("name")),
                        value(ProviderTransaction.DELETED, !transaction.isNull("deleted") && transaction.getBoolean("deleted")));
    }

    public void save() throws IOException, GeneralSecurityException {
      sqlConnection
        .startCreate(ProviderUpdate.TYPE)
        .set(ProviderUpdate.PROVIDER, Provider.BUDGEA.getId())
        .set(ProviderUpdate.USER, userId)
        .set(ProviderUpdate.DATE, new Date())
        .set(ProviderUpdate.DATA, serializer.toBlob(repository))
        .run();
      sqlConnection.commitAndClose();
    }

  }
}
