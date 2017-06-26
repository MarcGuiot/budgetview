package com.budgetview.server.cloud.stub;

import com.budgetview.server.config.ConfigService;
import com.budgetview.server.utils.Log4J;
import com.budgetview.server.web.WebServer;
import com.budgetview.shared.cloud.budgea.BudgeaCategory;
import com.budgetview.shared.cloud.budgea.BudgeaConstants;
import org.apache.log4j.Logger;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class BudgeaStubServer {

  private Logger logger = Logger.getLogger("BudgeaStubServer");

  private BudgeaWebhook webhook = new BudgeaWebhook();

  private WebServer webServer;
  private int userId = 123;
  private int temporaryTokenId = 0;
  private String lastTempToken;
  private String persistentToken = BudgeaWebhook.PERSISTEN_TOKEN;
  private BudgeaBankFieldSample bankFieldsStep1 = BudgeaBankFieldSample.BUDGEA_FIELDS_STEP_1;
  private BudgeaBankFieldSample bankFieldsForUpdate = null;
  private Stack<String> statements = new Stack<String>();
  private Stack<String> connectionLists = new Stack<String>();
  private Stack<String> connectionResponses = new Stack<String>();
  private List<String> lastLoginFields = new ArrayList<String>();
  private String loginConstraint;
  private Integer lastDeletedUserId;

  public static void main(String... args) throws Exception {
    BudgeaStubServer stub = new BudgeaStubServer(args);
    stub.pushStatement(BudgeaStatement.init()
                         .addConnection(1, 123, 40, "Connecteur de test", "2016-08-10 17:44:26")
                         .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-10 13:00:00")
                         .addTransaction(1, "2016-08-10 13:00:00", -100.00, "AUCHAN")
                         .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
                         .addTransaction(3, "2016-08-08 10:00:00", -10.00, "CIC", BudgeaCategory.FRAIS_BANCAIRES)
                         .endAccount()
                         .endConnection()
                         .get());
    stub.start();
  }

  public BudgeaStubServer(String... args) throws Exception {
    ConfigService config = new ConfigService(args);
    webServer = new WebServer(config);
    webServer.add(new AuthInitServlet(), "/auth/init");
    webServer.add(new AuthTokenCodeServlet(), "/auth/token/code");
    webServer.add(new AuthTokenAccessServlet(), "/auth/token/access");
    webServer.add(new BanksServlet(), "/banks");
    webServer.add(new BanksFieldsServlet(), "/banks/*");
    webServer.add(new UsersMeServlet(), "/users/me");
    webServer.add(new UsersMeConnectionsServlet(), "/users/me/connections/*");
    webServer.add(new UserConnectionsServlet(), "/users/*");
    webServer.add(new PingServlet(), "/ping");

    Log4J.init(config);
  }

  public void start() throws Exception {
    logger.debug("starting server");
    webServer.start();
    logger.info("server started - " + webServer.info());
  }

  public void stop() throws Exception {
    logger.debug("stopping server");
    webServer.stop();
    logger.debug("server stopped");
  }

  public void callWebhook(String json) throws IOException {
    callWebhook(persistentToken, json);
  }

  public void callWebhook(String budgeaToken, String json) throws IOException {
    webhook.callWebhook(budgeaToken, json);
  }

  private void callWebhookWithCurrentStatements() throws IOException {
    if (statements.isEmpty()) {
      logger.debug("No statement to send - next download will be empty");
      return;
    }

    final String statement = statements.pop();

    logger.debug("Starting thread for webhook");
    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {
          Thread.sleep(100);
          callWebhook(persistentToken, statement);
        }
        catch (IOException e) {
          logger.error("Webhook call failed", e);
        }
        catch (InterruptedException e) {
        }
      }
    });
    thread.start();
  }

  public void pushStatement(String nextStatement) {
    this.statements.push(nextStatement);
  }

  public void setBankFields(BudgeaBankFieldSample bankFields) {
    this.bankFieldsStep1 = bankFields;
  }

  public void setBankFields(BudgeaBankFieldSample step1, BudgeaBankFieldSample step2) {
    this.bankFieldsStep1 = step1;
    this.bankFieldsForUpdate = step2;
  }

  private String createTemporaryToken() {
    temporaryTokenId += 1;
    lastTempToken = "budgea-temp-token#" + temporaryTokenId;
    return lastTempToken;
  }

  public void pushConnectionList(String json) {
    this.connectionLists.push(json);
  }

  public void pushConnectionResponse(String json) {
    this.connectionResponses.push(json);
  }

  public void checkLastLogin(String... fieldValues) {
    for (String fieldValue : fieldValues) {
      if (!lastLoginFields.contains(fieldValue)) {
        throw new RuntimeException("Couldn't find: " + fieldValue + " - login fields were: " + lastLoginFields);
      }
    }
  }

  public void setLoginConstraint(String loginConstraint) {
    this.loginConstraint = loginConstraint;
  }

  public void checkUserDeletion(int userId) {
    if (!Utils.equal(lastDeletedUserId, userId)) {
      throw new RuntimeException("Last deleted user ID: " + lastDeletedUserId + " but expected: " + userId);
    }
  }

  private class AuthInitServlet extends HttpServlet {

    private Logger logger = Logger.getLogger("BudgeaStubServer:AuthInitServlet");

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      logger.debug("POST");
      PrintWriter writer = response.getWriter();
      writer.append("{\n" +
                    "   \"auth_token\" : \"" + createTemporaryToken() + "\",\n" +
                    "   \"type\" : \"temporary\"\n" +
                    "}");
      writer.close();
      response.setStatus(HttpServletResponse.SC_OK);
    }
  }

  private class AuthTokenCodeServlet extends HttpServlet {

    private Logger logger = Logger.getLogger("BudgeaStubServer:AuthTokenCodeServlet");

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      logger.debug("GET");
      PrintWriter writer = response.getWriter();
      writer.print("{\n" +
                   "   \"code\" : \"" + createTemporaryToken() + "\",\n" +
                   "   \"type\" : \"temporary\"\n" +
                   "}");
      writer.close();
      response.setStatus(HttpServletResponse.SC_OK);
    }
  }

  private class AuthTokenAccessServlet extends HttpServlet {

    private Logger logger = Logger.getLogger("BudgeaStubServer:AuthTokenAccessServlet");

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      logger.debug("POST");

      PrintWriter writer = response.getWriter();
      writer.print("{\n" +
                   "   \"access_token\":\"" + persistentToken + "\",\n" +
                   "   \"token_type\":\"Bearer\"\n" +
                   "}");
      writer.close();
      response.setStatus(HttpServletResponse.SC_OK);
    }
  }

  public class BanksServlet extends HttpServlet {

    private Logger logger = Logger.getLogger("BudgeaStubServer:BanksServlet");

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      logger.debug("GET");

      if (!checkAuthorization(request, logger)) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }

      PrintWriter writer = response.getWriter();
      writer.print("{\"banks\": [" +
                   "  {\n" +
                   "    \"code\": null,\n" +
                   "    \"capabilities\": [\n" +
                   "      \"banktransfer\",\n" +
                   "      \"document\",\n" +
                   "      \"bank\"\n" +
                   "    ],\n" +
                   "    \"color\": \"5c2963\",\n" +
                   "    \"hidden\": false,\n" +
                   "    \"id_category\": null,\n" +
                   "    \"name\": \"Connecteur de test\",\n" +
                   "    \"id\": 40,\n" +
                   "    \"charged\": false,\n" +
                   "    \"slug\": \"EXA\",\n" +
                   "    \"beta\": false\n" +
                   "  },\n" +
                   "  {\n" +
                   "    \"code\": \"14559\",\n" +
                   "    \"capabilities\": [\n" +
                   "      \"document\",\n" +
                   "      \"bank\"\n" +
                   "    ],\n" +
                   "    \"color\": \"ff6600\",\n" +
                   "    \"hidden\": false,\n" +
                   "    \"id_category\": 8,\n" +
                   "    \"name\": \"ING Direct\",\n" +
                   "    \"id\": 7,\n" +
                   "    \"charged\": true,\n" +
                   "    \"slug\": \"ING\",\n" +
                   "    \"beta\": false\n" +
                   "  },\n" +
                   "  {\n" +
                   "    \"code\": \"30066\",\n" +
                   "    \"capabilities\": [\n" +
                   "      \"banktransfer\",\n" +
                   "      \"bank\",\n" +
                   "      \"contact\"\n" +
                   "    ],\n" +
                   "    \"color\": \"298381\",\n" +
                   "    \"hidden\": false,\n" +
                   "    \"id_category\": null,\n" +
                   "    \"name\": \"CIC\",\n" +
                   "    \"id\": 10,\n" +
                   "    \"charged\": true,\n" +
                   "    \"slug\": \"CIC\",\n" +
                   "    \"beta\": false\n" +
                   "  }," +
                   "]}");
      writer.close();
      response.setStatus(HttpServletResponse.SC_OK);
    }
  }

  public class BanksFieldsServlet extends HttpServlet {

    private Logger logger = Logger.getLogger("BudgeaStubServer:BanksFieldsServlet");

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      logger.debug("GET");

      if (!checkAuthorization(request, logger)) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }

      PrintWriter writer = response.getWriter();
      writer.print(bankFieldsStep1.getJSON());
      writer.close();
      response.setStatus(HttpServletResponse.SC_OK);
    }
  }

  private class UsersMeServlet extends HttpServlet {

    private Logger logger = Logger.getLogger("BudgeaStubServer:UsersMeServlet");

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      logger.debug("GET");

      PrintWriter writer = resp.getWriter();
      writer.write("{\n" +
                   "  \"signin\": \"datetime\",\n" +
                   "  \"platform\": \"unicode\",\n" +
                   "  \"id\": \"" + userId + "\"\n" +
                   "}");
      writer.close();
      resp.setStatus(HttpServletResponse.SC_OK);
    }
  }

  private class UsersMeConnectionsServlet extends HttpServlet {

    private Logger logger = Logger.getLogger("BudgeaStubServer:UsersMeConnectionsServlet");

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      logger.debug("POST");

      if (!checkAuthorization(request, logger)) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }

      BufferedReader reader = request.getReader();
      String body = reader.readLine();
      if (body == null) {
        logger.error("No login data provided");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      body = java.net.URLDecoder.decode(body, "UTF-8");
      lastLoginFields.clear();
      for (String field : body.split("&")) {
        lastLoginFields.add(field);
      }
      if (Strings.isNotEmpty(loginConstraint) && !lastLoginFields.contains(loginConstraint)) {
        logger.error("Login rejected - expected constraint: " + loginConstraint + " not found in :" + lastLoginFields);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      boolean update = Strings.isNotEmpty(request.getPathInfo());
      if (update) {
        logger.debug("Processing step2");
      }

      if (bankFieldsForUpdate == null || update) {
        logger.debug(update ? "Completing connection for step2" : "Completing connection for step1");
        if (connectionResponses.isEmpty()) {
          logger.error("No connection response provided - you must push one");
          response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          return;
        }
        String connectionResponse = connectionResponses.pop();
        if (connectionResponse.contains("wrongpass")) {
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          PrintWriter writer = response.getWriter();
          writer.write(connectionResponse);
          writer.close();
        }
        else {
          response.setStatus(HttpServletResponse.SC_OK);
          PrintWriter writer = response.getWriter();
          writer.write(connectionResponse);
          writer.close();
          callWebhookWithCurrentStatements();
        }
      }
      else {
        logger.debug("Two-step login : returning new fields with code " + HttpServletResponse.SC_ACCEPTED);
        response.setStatus(HttpServletResponse.SC_ACCEPTED);
        PrintWriter writer = response.getWriter();
        writer.write(bankFieldsForUpdate.getJSON());
        writer.close();
      }
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      logger.debug("DELETE");
      response.setStatus(HttpServletResponse.SC_OK);
    }
  }

  private class UserConnectionsServlet extends HttpServlet {

    private Logger logger = Logger.getLogger("BudgeaStubServer:UserConnectionsServlet");

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      logger.debug("GET");

      if (!checkAuthorization(request, logger)) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }

      if (connectionLists.isEmpty()) {
        logger.error("No connection response provided - you may have to push a connection JSON before making this call");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
      }

      PrintWriter writer = response.getWriter();
      writer.write(connectionLists.pop());
      writer.close();

      response.setStatus(HttpServletResponse.SC_OK);
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      logger.debug("GET");

      if (!checkAuthorization(request, logger)) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }

      lastDeletedUserId = Integer.parseInt(request.getPathInfo().substring(1));
      response.setStatus(HttpServletResponse.SC_OK);
    }
  }

  private class PingServlet extends HttpServlet {

    private Logger logger = Logger.getLogger("BudgeaStubServer:PingServlet");

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      logger.debug("GET");
      PrintWriter writer = resp.getWriter();
      writer.write("Pong");
      writer.close();
      resp.setStatus(HttpServletResponse.SC_OK);
    }
  }

  private boolean checkAuthorization(HttpServletRequest request, Logger logger) {
    String actual = request.getHeader(BudgeaConstants.AUTHORIZATION);
    String expectedTemp = "Bearer " + lastTempToken;
    String expectedPersistent = "Bearer " + persistentToken;
    if (!Utils.equal(actual, expectedTemp) && !Utils.equal(actual, expectedPersistent)) {
      logger.error("Unexpected token: " + actual + " - expected: " + expectedTemp + " or " + expectedPersistent);
      return false;
    }
    return true;
  }
}
