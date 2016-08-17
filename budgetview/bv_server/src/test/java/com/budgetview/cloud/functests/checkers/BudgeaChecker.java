package com.budgetview.cloud.functests.checkers;

import com.budgetview.server.config.ConfigService;
import com.budgetview.server.web.WebServer;
import com.budgetview.shared.cloud.BudgeaConstants;
import com.budgetview.shared.cloud.CloudConstants;
import junit.framework.Assert;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class BudgeaChecker {

  private WebServer webServer;
  private String persistentToken = "";

  public void startServer() throws Exception {
    webServer = new WebServer(new ConfigService("budgetview/bv_server/dev/config/budgea_test.properties"));
    webServer.add(new BudgeaStubServlet(), "/auth/token/access");
    webServer.start();
  }

  public void stopServer() throws Exception {
    webServer.stop();
  }

  public void setPersistentToken(String persistentToken) {
    this.persistentToken = persistentToken;
  }

  public void callWebhook(String budgeaToken, String json) throws IOException {
    Request request = Request.Post(CloudConstants.getServerUrl("/budgea"))
      .addHeader("Authorization", "Bearer " + budgeaToken)
      .bodyString(json.replaceAll("'", "\""), ContentType.APPLICATION_JSON);

    Response response = request.execute();
    Assert.assertEquals(200, response.returnResponse().getStatusLine().getStatusCode());
  }

  private class BudgeaStubServlet extends HttpServlet {

    private Logger logger = Logger.getLogger("BudgeaStub");

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      logger.info("OK");
      PrintWriter writer = new PrintWriter(response.getOutputStream());
      writer.append("{\n" +
                    "   \"access_token\":\"" + persistentToken + "\",\n" +
                    "   \"token_type\":\"Bearer\"\n" +
                    "}");
      writer.close();
      response.setStatus(HttpServletResponse.SC_OK);
    }
  }
}
