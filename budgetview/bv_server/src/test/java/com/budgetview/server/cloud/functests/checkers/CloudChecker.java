package com.budgetview.server.cloud.functests.checkers;

import com.budgetview.server.cloud.CloudServer;
import com.budgetview.shared.cloud.CloudAPI;
import com.budgetview.shared.cloud.CloudConstants;
import org.json.JSONObject;
import org.junit.Assert;

import java.util.Date;

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

  public void stopServer() throws Exception {
    cloudServer.resetDatabase();
    cloudServer.stop();
  }
}
