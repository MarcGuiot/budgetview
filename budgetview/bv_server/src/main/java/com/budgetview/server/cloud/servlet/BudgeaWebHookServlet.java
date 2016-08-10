package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.model.Provider;
import org.apache.log4j.Logger;
import org.globsframework.model.Glob;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.utils.Files;
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
    logger.info("JSON:\n" + json);
    JSONObject root = new JSONObject(json);
    for (Object c : root.getJSONArray("connections")) {
      JSONObject budgeaConnection = (JSONObject) c;
      int budgeaUserId = budgeaConnection.getInt("id_user");
      Integer userId = getCloudUserId(budgeaUserId, token);
      if (userId == null) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return;
      }
      saveBudgeaConnection(userId, budgeaConnection);
    }

    response.setStatus(HttpServletResponse.SC_OK);
  }

  private void saveBudgeaConnection(Integer userId, JSONObject budgeaConnection) {
    for (Object c : budgeaConnection.getJSONArray("accounts")) {
      JSONObject account = (JSONObject) c;
      saveAccount(userId, account);
    }
  }

  private void saveAccount(Integer userId, JSONObject account) {
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
}
