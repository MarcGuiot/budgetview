package org.designup.picsou.license;

import org.designup.picsou.license.servlet.LicenseServer;
import org.designup.picsou.license.servlet.NewUserServlet;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.DefaultServlet;

public class LocalServer {
  public static void main(String[] args) throws Exception {
    System.setProperty(NewUserServlet.PAYPAL_CONFIRM_URL_PROPERTY, "http://www.sandbox.paypal.com/fr/cgi-bin/webscr");

    LicenseServer server = new LicenseServer();
    server.setMailPort(5000);
    server.useSsl(true);
    server.usePort(8443);
    server.setDatabaseUrl("jdbc:hsqldb:.");
    server.init();

    AddUser.main("-d", "jdbc:hsqldb:.", "-u", "sa", "-p", "", "user@localhost");

    server.addServlet(new ServletHolder(new DefaultServlet()), "/");
    server.start();
  }
}
