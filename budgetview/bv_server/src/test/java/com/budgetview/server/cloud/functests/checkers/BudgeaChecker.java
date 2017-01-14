package com.budgetview.server.cloud.functests.checkers;

import com.budgetview.server.cloud.stub.BudgeaBankFieldSample;
import com.budgetview.server.cloud.stub.BudgeaStubServer;
import com.budgetview.shared.cloud.budgea.BudgeaConstants;

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

  public void setBankLoginFields(BudgeaBankFieldSample fields) {
    stub.setBankFields(fields);
  }

  public void setBankLoginFields(BudgeaBankFieldSample step1JSON, BudgeaBankFieldSample step2JSON) {
    stub.setBankFields(step1JSON, step2JSON);
  }

  public void pushStatement(String json) {
    stub.pushStatement(json);
  }

  public void pushConnections(String json) {
    stub.pushConnections(json);
  }

  public void pushNewConnection(int connectionId, int userId, int bankId) {
    stub.pushConnections("{\n" +
                         "   \"id\" : " + connectionId + ",\n" +
                         "   \"id_user\" : " +  userId + ",\n" +
                         "   \"id_bank\" : "+ bankId+",\n" +
                         "   \"expire\" : null,\n" +
                         "   \"last_update\" : \"2016-04-03 18:51:07\",\n" +
                         "   \"error\" : null,\n" +
                         "}\n");
  }

  public void callWebhook(String json) throws IOException {
    stub.callWebhook(json);
  }

  public void checkLastLogin(String... fieldValues) {
    stub.checkLastLogin(fieldValues);
  }

  public void setLoginConstraint(String fieldEqualsValue) {
    stub.setLoginConstraint(fieldEqualsValue);
  }
}
