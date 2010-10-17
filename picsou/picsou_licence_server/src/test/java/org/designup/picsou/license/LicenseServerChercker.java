package org.designup.picsou.license;

import org.designup.picsou.license.servlet.LicenseServer;
import org.mortbay.jetty.servlet.ServletHolder;

import java.io.IOException;

public class LicenseServerChercker {
  private LicenseServer server;

  public LicenseServerChercker(String databaseUrl) throws IOException {
    server = new LicenseServer();
    server.useSsl(false);
    server.usePort(5000);
    server.setMailPort(2500);
    server.setDatabaseUrl(databaseUrl);
  }

  public void add(ServletHolder holder, String name){
    server.addServlet(holder, name);
  }

  public void stop() throws Exception {
    server.stop();
  }

  public void start() throws Exception {
    server.start();
  }

  public void init(){
    server.init();
  }
}
