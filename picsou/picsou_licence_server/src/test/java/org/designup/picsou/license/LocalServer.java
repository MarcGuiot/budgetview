package org.designup.picsou.license;

import org.designup.picsou.license.servlet.LicenseServer;

import java.io.IOException;

public class LocalServer {
  public static void main(String[] args) throws Exception {
    LicenseServer server = new LicenseServer();
    server.setMailPort(5000);
    server.useSsl(false);
    server.setDatabaseUrl("jdbc:hsqldb:hsql://localhost/picsou");
    server.start();
  }
}
