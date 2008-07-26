package org.designup.picsoulicence;

import org.designup.picsoulicence.mail.Mailler;
import org.designup.picsoulicence.servlet.AskForMailServlet;
import org.designup.picsoulicence.servlet.RegisterServlet;
import org.designup.picsoulicence.servlet.RequestForConfigServlet;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcSqlService;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

public class LicenceServer {
  public static final String USE_SSHL = "picsou.server.useSsl";
  public static final String KEYSTORE = "picsou.server.keystore";
  public static final String HOST_PROPERTY = "picsou.server.host";
  private Server jetty;
  private boolean useSsl = true;
  private int port = 8443;
  private int mailPort;
  private String dabaseUrl;
  private String databaseUser = "sa";
  private String databasePassword = "";

  public LicenceServer() {
    jetty = new Server();
  }

  public void useSsl(boolean useSsl) {
    this.useSsl = useSsl;
  }

  public void usePort(int port) {
    this.port = port;
  }

  public void setMailPort(int mailPort) {
    this.mailPort = mailPort;
  }

  public void setDabaseUrl(String dabaseUrl) {

    this.dabaseUrl = dabaseUrl;
  }

  public void init() {
    if (useSsl) {
      SslSocketConnector connector = new SslSocketConnector();
      String keyStore = System.getProperty(KEYSTORE);
      if (keyStore == null) {
        keyStore = "picsou_licence/resources/.keystore";
      }
      connector.setKeystore(keyStore);
      String host = System.getProperty(HOST_PROPERTY);
      if (host == null) {
        host = "localhost";
      }
      connector.setHost(host);
      connector.setPassword("ninja600");
      connector.setPort(port);
      jetty.addConnector(connector);
    }
    else {
      SocketConnector connector = new SocketConnector();
      connector.setHost("localhost");
      connector.setPort(port);
      jetty.addConnector(connector);
    }
    Context context = new Context(jetty, "/", Context.SESSIONS);
    context.setResourceBase("classes");
    Directory directory = createDirectory();
    context.addServlet(new ServletHolder(new AskForMailServlet(directory)), "/mailTo");
    context.addServlet(new ServletHolder(new RequestForConfigServlet(directory)), "/requestForConfig");
    context.addServlet(new ServletHolder(new RegisterServlet(directory)), "/register");
  }

  private Directory createDirectory() {
    Directory directory = new DefaultDirectory();
    Mailler mailler = new Mailler();
    directory.add(mailler);
    mailler.setPort(mailPort);
    SqlService sqlService = new JdbcSqlService(dabaseUrl, databaseUser, databasePassword);
    directory.add(SqlService.class, sqlService);
    return directory;
  }


  public void start() throws Exception {
    init();
    jetty.start();
  }

  public void stop() throws Exception {
    jetty.stop();
  }
}
