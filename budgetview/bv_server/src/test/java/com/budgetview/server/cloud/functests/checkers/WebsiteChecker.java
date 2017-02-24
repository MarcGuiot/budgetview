package com.budgetview.server.cloud.functests.checkers;

public class WebsiteChecker {

  private WebsiteStubServer server;

  public WebsiteChecker() {
  }

  public void startServer() throws Exception {
    server = new WebsiteStubServer("budgetview/bv_server/dev/config/bv_website_test.properties");
    server.start();
    server.init();
  }

  public void stopServer() throws Exception {
    server.stop();
  }
}

