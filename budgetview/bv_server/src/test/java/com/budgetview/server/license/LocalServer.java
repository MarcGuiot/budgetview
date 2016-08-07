package com.budgetview.server.license;

import com.budgetview.server.license.servlet.NewUserServlet;
import com.budgetview.server.web.WebServer;
import org.eclipse.jetty.servlet.DefaultServlet;

import javax.servlet.http.HttpServlet;

public class LocalServer {
  public static void main(String[] args) throws Exception {

    System.setProperty(NewUserServlet.PAYPAL_CONFIRM_URL_PROPERTY, "http://www.sandbox.paypal.com/fr/cgi-bin/webscr");
    System.setProperty(WebServer.HTTP_PORT_PROPERTY, "8080");
    System.setProperty(WebServer.HTTPS_PORT_PROPERTY, "8443");
    System.setProperty(LicenseServer.DATABASE_URL, "jdbc:hsqldb:.");

    LicenseServer server = new LicenseServer(args);
    server.init();

    AddUser.main("-d", "jdbc:hsqldb:.", "-u", "sa", "-p", "", "user@localhost");
    System.out.println("LocalServer.main user : user@localhost");

    HttpServlet servlet = new DefaultServlet();
    server.addServlet(servlet, "/");
    server.start();
  }
}
