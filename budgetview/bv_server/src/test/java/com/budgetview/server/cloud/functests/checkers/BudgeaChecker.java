package com.budgetview.server.cloud.functests.checkers;

import com.budgetview.server.cloud.stub.BudgeaBankFieldSample;
import com.budgetview.server.cloud.stub.BudgeaStubServer;
import com.budgetview.shared.cloud.budgea.BudgeaConstants;
import org.apache.log4j.Logger;

import java.io.IOException;

public class BudgeaChecker {

  private BudgeaStubServer stub;

  public BudgeaChecker() throws Exception {
    System.setProperty(BudgeaConstants.SERVER_URL_PROPERTY, BudgeaConstants.LOCAL_SERVER_URL);
    stub = new BudgeaStubServer("budgetview/bv_server/dev/config/budgea_stub_local.properties");
  }

  public void startServer() throws Exception {
    stub.start();
  }

  public void stopServer() throws Exception {
    stub.stop();
  }

  public void setBankFields(BudgeaBankFieldSample fields) {
    stub.setBankFields(fields);
  }

  public void pushStatement(String statement) {
    stub.pushStatement(statement);
  }

  public void callWebhook(String json) throws IOException {
    stub.callWebhook(json);
  }
}
