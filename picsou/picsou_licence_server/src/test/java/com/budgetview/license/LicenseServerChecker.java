package com.budgetview.license;

import com.budgetview.license.servlet.LicenseServer;
import org.mortbay.jetty.servlet.ServletHolder;

import java.io.IOException;

public class LicenseServerChecker {
  private LicenseServer server;
  private boolean started;

  public LicenseServerChecker(String databaseUrl, int port) throws IOException {
    server = new LicenseServer();
    server.usePort(null, port);
    server.setMailPort("localhost", 2500);
    server.setDatabaseUrl(databaseUrl);
  }

  public void add(ServletHolder holder, String name) {
    server.addServlet(holder, name);
  }

  public void init() {
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
