package com.budgetview.license;

import com.budgetview.license.servlet.LicenseServer;
import com.budgetview.license.servlet.WebServer;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.http.HttpServlet;
import java.io.IOException;

public class LicenseServerChecker {
  private LicenseServer server;
  private boolean started;

  public LicenseServerChecker(String databaseUrl, int port) throws IOException {

    System.setProperty(WebServer.HTTP_PORT_PROPERTY, Integer.toString(port));

    server = new LicenseServer();
    server.setMailPort("localhost", 2500);
    server.setDatabaseUrl(databaseUrl);
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
