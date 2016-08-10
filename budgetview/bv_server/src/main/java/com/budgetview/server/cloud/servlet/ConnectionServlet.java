package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.Budgea;
import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.cloud.model.Provider;
import com.budgetview.shared.cloud.BudgeaConstants;
import com.budgetview.shared.cloud.CloudConstants;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.log4j.Logger;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.OperationFailed;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ConnectionServlet extends HttpServlet {

  private static Logger logger = Logger.getLogger("/connections");

  private final GlobsDatabase database;

  public ConnectionServlet(Directory directory) {
    this.database = directory.get(GlobsDatabase.class);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");

    String email = request.getParameter(CloudConstants.EMAIL);

    String token = request.getParameter(CloudConstants.BUDGEA_TOKEN);
    String userId = request.getParameter(CloudConstants.BUDGEA_USER_ID);
    if (Strings.isNullOrEmpty(token) || Strings.isNullOrEmpty(userId)) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    try {
      registerBudgeaToken(token);
    }
    catch (Exception e) {
      logger.error("Budgea registration failed", e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    saveCloudUser(email, userId, token);

    response.setStatus(HttpServletResponse.SC_OK);
  }

  private void registerBudgeaToken(String code) throws IOException {
    logger.info("Budgea temporary token: " + code);
    String serverUrl = BudgeaConstants.getServerUrl("/auth/token/access");
    logger.info("ConnectionServlet.registerBudgeaToken: " + serverUrl);
    Request request = Request.Post(serverUrl)
      .bodyForm(Form.form()
                  .add("code", code)
                  .add("client_id", Budgea.CLIENT_ID)
                  .add("client_secret", Budgea.CLIENT_SECRET)
                  .build());

    Response response = request.execute();
    int statusCode = response.returnResponse().getStatusLine().getStatusCode();
    if (statusCode != HttpServletResponse.SC_OK) {
      throw new OperationFailed("Budgea returned " + statusCode + " instead of " + HttpServletResponse.SC_OK);
    }
  }

  private void saveCloudUser(String email, String userId, String token) {
    SqlConnection connection = database.connect();
    try {
      connection.startCreate(CloudUser.TYPE)
        .set(CloudUser.EMAIL, email)
        .set(CloudUser.PROVIDER, Provider.BUDGEA.getId())
        .set(CloudUser.PROVIDER_ID, Integer.parseInt(userId))
        .set(CloudUser.PROVIDER_ACCESS_TOKEN, token)
        .run();

      GlobPrinter.print(connection.selectAll(CloudUser.TYPE));
    }
    finally {
      connection.commitAndClose();
    }
  }
}
