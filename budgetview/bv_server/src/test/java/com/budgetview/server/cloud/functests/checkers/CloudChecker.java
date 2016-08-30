package com.budgetview.server.cloud.functests.checkers;

import com.budgetview.server.cloud.CloudServer;
import com.budgetview.shared.cloud.CloudAPI;
import com.budgetview.shared.cloud.CloudConstants;
import com.budgetview.shared.model.Provider;
import org.json.JSONObject;

public class CloudChecker {


  private CloudServer cloudServer;

  public void setUp() throws Exception {
    System.setProperty(CloudConstants.CLOUD_URL_PROPERTY, CloudConstants.LOCAL_SERVER_URL);
  }

  public void startServer() throws Exception {
    cloudServer = new CloudServer("budgetview/bv_server/dev/config/bv_cloud_test.properties");
    cloudServer.init();
    cloudServer.start();
  }

  public void register(String email, Integer budgeaUserId, String budgeaToken) throws Exception {
    CloudAPI api = new CloudAPI();
    api.addConnection(email, budgeaToken, budgeaUserId);
  }

  public void checkBankStatement(String email, int budgeaBankId, String expected) throws Exception {
    CloudAPI api = new CloudAPI();
    JSONObject object = api.getStatement(email, Provider.BUDGEA, budgeaBankId);
    System.out.println("CloudChecker.checkBankStatement: " + object.toString(2));
  }

  public void stopServer() throws Exception {
    cloudServer.stop();
  }
}
