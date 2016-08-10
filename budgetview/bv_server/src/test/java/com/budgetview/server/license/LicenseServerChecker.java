package com.budgetview.server.license;

import com.budgetview.server.config.ConfigService;

import javax.servlet.http.HttpServlet;
import java.io.IOException;

public class LicenseServerChecker {
  private LicenseServer server;
  private boolean started;

  public LicenseServerChecker() throws Exception {
    server = new LicenseServer("budgetview/bv_server/dev/config/bv_cloud_test.properties");
    server.setMailPort("localhost", 2500);
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
