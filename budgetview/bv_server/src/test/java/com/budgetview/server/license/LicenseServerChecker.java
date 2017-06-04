package com.budgetview.server.license;

import javax.servlet.http.HttpServlet;

public class LicenseServerChecker {
  private LicenseServer server;
  private boolean started;

  public LicenseServerChecker() throws Exception {
    server = new LicenseServer("budgetview/bv_server/server_admin/config/bv_license_test.properties");
  }

  public void add(HttpServlet holder, String name) {
    server.addServlet(holder, name);
  }

  public void init() throws Exception {
    server.init();
  }

  public void start() throws Exception {
    server.start();
    started = true;
  }

  public void stop() throws Exception {
    if (started) {
      server.stop();
    }
    started = false;
  }

  public void dispose() throws Exception {
    stop();
    server = null;
  }
}
