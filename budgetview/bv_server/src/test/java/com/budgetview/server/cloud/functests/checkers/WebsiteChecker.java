package com.budgetview.server.cloud.functests.checkers;

public class WebsiteChecker {

  private WebsiteStubServer server;

  public WebsiteChecker() {
  }

  public void startServer() throws Exception {
    server = new WebsiteStubServer("budgetview/bv_server/server_admin/config/bv_website_test.properties");
    server.start();
    server.init();
  }

  public void stopServer() throws Exception {
    server.stop();
  }

  public void checkLastVisitedPage(String path) {
    server.checkLastVisitedPage(path);
  }
}

