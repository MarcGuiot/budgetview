package com.budgetview.license;

import com.budgetview.license.servlet.LicenseServer;
import com.budgetview.license.servlet.NewUserServlet;
import com.budgetview.license.servlet.WebServer;
import org.eclipse.jetty.servlet.DefaultServlet;

import javax.servlet.http.HttpServlet;

public class LocalServer {
  public static void main(String[] args) throws Exception {

    System.setProperty(NewUserServlet.PAYPAL_CONFIRM_URL_PROPERTY, "http://www.sandbox.paypal.com/fr/cgi-bin/webscr");
    System.setProperty(WebServer.HTTP_PORT_PROPERTY, "8080");
    System.setProperty(WebServer.HTTPS_PORT_PROPERTY, "8443");

    LicenseServer server = new LicenseServer();
//    server.setMailPort(5000);
    server.setDatabaseUrl("jdbc:hsqldb:.");
    server.init();

    AddUser.main("-d", "jdbc:hsqldb:.", "-u", "sa", "-p", "", "user@localhost");
    System.out.println("LocalServer.main user : user@localhost");

    HttpServlet servlet = new DefaultServlet();
    server.addServlet(servlet, "/");
    server.start();
  }
}
