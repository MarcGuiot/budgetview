package com.budgetview.server.cloud.functests.checkers;

import com.budgetview.server.cloud.stub.BudgeaBankFieldSample;
import com.budgetview.server.cloud.stub.BudgeaStubServer;
import com.budgetview.shared.cloud.budgea.BudgeaConstants;
import org.globsframework.utils.TestUtils;

import java.io.IOException;

public class BudgeaChecker {

  private BudgeaStubServer stub;

  public BudgeaChecker() throws Exception {
    System.setProperty(BudgeaConstants.SERVER_URL_PROPERTY, BudgeaConstants.LOCAL_SERVER_URL);
    stub = new BudgeaStubServer("budgetview/bv_server/server_admin/config/budgea_stub_local.properties");
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

  public void sendStatement(String json) throws IOException {
    stub.callWebhook(json);
  }

  public void pushStatement(String json) {
    stub.pushStatement(json);
  }

  public void pushNewConnectionResponse(int connectionId, int userId, int bankId) {
    pushNewConnectionResponse(connectionId, userId, bankId, "null");
  }

  public void pushNewConnectionResponse(int connectionId, int userId, int bankId, String error) {
    stub.pushNewConnectionResponse("{\n" +
                                   "   \"id\" : " + connectionId + ",\n" +
                                   "   \"id_user\" : " + userId + ",\n" +
                                   "   \"id_bank\" : " + bankId + ",\n" +
                                   "   \"expire\" : null,\n" +
                                   "   \"last_update\" : \"2016-04-03 18:51:07\",\n" +
                                   "   \"error\" : " + error + ",\n" +
                                   "}\n");
  }

  public void pushConnectionList(String json) {
    stub.pushConnectionList(json);
  }

  public void pushAccountList(String json) {
    stub.pushAccountResponse(json);
  }

  public void checkLastLogin(String... fieldValues) {
    stub.checkLastLogin(fieldValues);
  }

  public void setLoginConstraint(String fieldEqualsValue) {
    stub.setLoginConstraint(fieldEqualsValue);
  }

  public void checkUserDeleted(int userId) {
    stub.checkUserDeleted(userId);
  }

  public void checkConnectionDeleted(int connectionId) {
    stub.checkConnectionDeleted(connectionId);
  }

  public void checkAccountUpdates(String... updates) {
    TestUtils.assertSetEquals(stub.getAccountUpdates(), updates);
  }

  public void clearAccountUpdates() {
    stub.clearAccountUpdates();
  }
}
