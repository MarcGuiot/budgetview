package com.budgetview.server.cloud.stub;

import com.budgetview.shared.cloud.CloudConstants;
import com.budgetview.shared.cloud.budgea.BudgeaCategory;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;

import java.io.IOException;

public class BudgeaWebhook {

  public static final String PERSISTEN_TOKEN = "~~~TOKEN~~~";

  private Logger logger = Logger.getLogger("BudgeaWebhook");

  public static void main(String[] args) throws Exception {
    BudgeaWebhook webhook = new BudgeaWebhook();
    webhook.callWebhook(PERSISTEN_TOKEN,
                        BudgeaStatement.init()
                          .addConnection(1, 123, 40, "Connecteur de Test Budgea", "2016-08-11 10:20:30")
                          .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-11 10:20:40")
                          .addTransaction(4, "2016-08-15 15:00:00", -150.00, "FNAC", BudgeaCategory.UNCATEGORIZED)
                          .endAccount()
                          .endConnection()
                          .get());
  }

  public void callWebhook(String budgeaToken, String json) throws IOException {
    logger.info("Calling webhook with token " + budgeaToken);
    Request request = Request.Post(CloudConstants.getServerUrl("/budgea"))
      .addHeader("Authorization", "Bearer " + budgeaToken)
      .bodyString(json.replaceAll("'", "\""), ContentType.APPLICATION_JSON);

    Response response = request.execute();
    int statusCode = response.returnResponse().getStatusLine().getStatusCode();
    if (statusCode != 200) {
      throw new IOException("Unexpected return value: " + statusCode);
    }
  }
}
