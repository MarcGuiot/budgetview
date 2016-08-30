package com.budgetview.server.cloud.functests.checkers;

import com.budgetview.server.cloud.stub.BudgeaStubServer;
import com.budgetview.shared.cloud.budgea.BudgeaConstants;
import com.budgetview.shared.cloud.CloudConstants;
import junit.framework.Assert;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;

public class BudgeaChecker {

  private Logger logger = Logger.getLogger("BudgeaChecker");

  private BudgeaStubServer stub;

  public BudgeaChecker() throws Exception {
    System.setProperty(BudgeaConstants.SERVER_URL_PROPERTY, BudgeaConstants.LOCAL_SERVER_URL);
    stub = new BudgeaStubServer("budgetview/bv_server/dev/config/budgea_stub.properties");
  }

  public void startServer() throws Exception {
    stub.start();
  }

  public void stopServer() throws Exception {
    stub.stop();
  }

  public void setPersistentToken(String persistentToken) {
    stub.setPersistentToken(persistentToken);
  }

  public void callWebhook(String budgeaToken, String json) throws IOException {

    logger.info("Sending\n" + new JSONObject(json).toString(2));

    Request request = Request.Post(CloudConstants.getServerUrl("/budgea"))
      .addHeader("Authorization", "Bearer " + budgeaToken)
      .bodyString(json.replaceAll("'", "\""), ContentType.APPLICATION_JSON);

    Response response = request.execute();
    Assert.assertEquals(200, response.returnResponse().getStatusLine().getStatusCode());
  }

  public void setNextStatement(String statement) {
    stub.setNextStatement(statement);
  }
}
