package com.budgetview.server.cloud.stub;

import com.budgetview.server.config.ConfigService;
import com.budgetview.server.utils.Log4J;
import com.budgetview.server.web.WebServer;
import com.budgetview.shared.cloud.budgea.BudgeaCategory;
import com.budgetview.shared.cloud.budgea.BudgeaConstants;
import org.apache.log4j.Logger;
import org.globsframework.utils.Utils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Stack;

public class BudgeaStubServer {

  private Logger logger = Logger.getLogger("BudgeaStubServer");

  private BudgeaWebhook webhook = new BudgeaWebhook();

  private WebServer webServer;
  private int userId = 123;
  private int temporaryTokenId =  0;
  private String lastTempToken;
  private String persistentToken = BudgeaWebhook.PERSISTEN_TOKEN;
  private BudgeaBankFieldSample bankFields = BudgeaBankFieldSample.BUDGEA_TEST_CONNECTOR;
  private Stack<String> statements = new Stack<String>();

  public static void main(String... args) throws Exception {
    BudgeaStubServer stub = new BudgeaStubServer(args);
    stub.pushStatement(BudgeaStatement.init()
                         .addConnection(1, 123, 40, "Connecteur de Test Budgea", "2016-08-10 17:44:26")
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
    webServer.add(new BanksFieldsServlet(), "/banks/*");
    webServer.add(new UsersMeServlet(), "/users/me");
    webServer.add(new UsersMeConnectionsServlet(), "/users/me/connections");
    webServer.add(new PingServlet(), "/ping");

    Log4J.init(config);
  }

  public void start() throws Exception {
    logger.info("starting server");
    webServer.start();
    logger.info("server started");
  }

  public void stop() throws Exception {
    logger.info("stopping server");
    webServer.stop();
    logger.info("server stopped");
  }

  public void callWebhook(String json) throws IOException {
    callWebhook(persistentToken, json);
  }

  public void callWebhook(String budgeaToken, String json) throws IOException {
    webhook.callWebhook(budgeaToken, json);
  }

  private void callWebhookWithCurrentStatements() throws IOException {
    if (statements.isEmpty()) {
      logger.info("No statement to send - next download will be empty");
      return;
    }

    final String statement = statements.pop();

    logger.info("Starting thread for webhook");
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
    this.bankFields = bankFields;
  }

  private String createTemporaryToken() {
    temporaryTokenId += 1;
    lastTempToken = "budgea-temp-token#" + temporaryTokenId;
    return lastTempToken;
  }

  private class AuthInitServlet extends HttpServlet {

    private Logger logger = Logger.getLogger("BudgeaStubServer - /auth/init");

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      logger.info("POST");
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

    private Logger logger = Logger.getLogger("BudgeaStubServer - /auth/token/code");

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      logger.info("GET");
      PrintWriter writer = response.getWriter();
      writer.append("{\n" +
                    "   \"auth_token\" : \"" + createTemporaryToken() + "\",\n" +
                    "   \"type\" : \"temporary\"\n" +
                    "}");
      writer.close();
      response.setStatus(HttpServletResponse.SC_OK);
    }
  }

  private class AuthTokenAccessServlet extends HttpServlet {

    private Logger logger = Logger.getLogger("BudgeaStubServer - /auth/token/access");

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      logger.info("POST");

      PrintWriter writer = response.getWriter();
      writer.append("{\n" +
                    "   \"access_token\":\"" + persistentToken + "\",\n" +
                    "   \"token_type\":\"Bearer\"\n" +
                    "}");
      writer.close();
      response.setStatus(HttpServletResponse.SC_OK);
    }
  }

  public class BanksFieldsServlet extends HttpServlet {

    private Logger logger = Logger.getLogger("BudgeaStubServer - /banks/{id}/fields");

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      logger.info("GET");

      if (!checkAuthorization(request, logger)) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }

      PrintWriter writer = response.getWriter();
      writer.print(bankFields.getJSON());
      writer.close();
      response.setStatus(HttpServletResponse.SC_OK);
    }
  }

  private class UsersMeServlet extends HttpServlet {

    private Logger logger = Logger.getLogger("BudgeaStubServer - /users/me");

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      logger.info("GET");
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

    private Logger logger = Logger.getLogger("BudgeaStubServer - /users/me/connections");

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      logger.info("POST");

      if (!checkAuthorization(request, logger)) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }

      PrintWriter writer = response.getWriter();
      writer.write("{\n" +
                   "   \"expire\" : null,\n" +
                   "   \"last_update\" : \"2016-04-03 18:51:07\",\n" +
                   "   \"id_user\" : 687,\n" +
                   "   \"id\" : " + userId + ",\n" +
                   "   \"id_bank\" : 40,\n" +
                   "   \"error\" : null,\n" +
                   "   \"accounts\" : [\n" +
                   "      {\n" +
                   "         \"display\" : true,\n" +
                   "         \"currency\" : {\n" +
                   "            \"prefix\" : false,\n" +
                   "            \"id\" : \"EUR\",\n" +
                   "            \"symbol\" : \"€\"\n" +
                   "         },\n" +
                   "         \"id_connection\" : 74,\n" +
                   "         \"iban\" : \"FR7613662074083300290000016\",\n" +
                   "         \"id\" : 211,\n" +
                   "         \"type\" : \"checking\",\n" +
                   "         \"balance\" : 4405.73,\n" +
                   "         \"number\" : \"3002900000\",\n" +
                   "         \"last_update\" : \"2015-04-23 18:51:06\",\n" +
                   "         \"name\" : \"Compte chèque\",\n" +
                   "         \"deleted\" : null,\n" +
                   "         \"formatted_balance\" : \"4405.73 €\"\n" +
                   "      },\n" +
                   "   ],\n" +
                   "}\n");
      writer.close();
      response.setStatus(HttpServletResponse.SC_OK);

      callWebhookWithCurrentStatements();
    }
  }

  private class PingServlet extends HttpServlet {

    private Logger logger = Logger.getLogger("BudgeaStubServer - /ping");

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      logger.info("GET");
      PrintWriter writer = resp.getWriter();
      writer.write("Pong");
      writer.close();
      resp.setStatus(HttpServletResponse.SC_OK);
    }
  }

  private boolean checkAuthorization(HttpServletRequest request, Logger logger) {
    String actual = request.getHeader(BudgeaConstants.AUTHORIZATION);
    String expected = "Bearer " + lastTempToken;
    if (!Utils.equal(actual, expected)) {
      logger.error("Unexpected token: " + actual+ " - expected: " + expected);
      return false;
    }
    return true;
  }
}
