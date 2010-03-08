package org.designup.picsou.license.servlet;

import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.license.mail.Mailer;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcSqlService;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LicenseServer {
  static Logger logger = Logger.getLogger("LicenseServer");
  public static final String USE_SSHL = "picsou.server.useSsl";
  public static final String KEYSTORE = "picsou.server.keystore";
  public static final String HOST_PROPERTY = "picsou.server.host";
  public static final String DATABASE_URL = "picsou.server.database.url";
  public static final String DATABASE_USER = "picsou.server.database.user";
  public static final String DATABASE_PASSWD = "picsou.server.database.passwd";
  private Server jetty;
  private boolean useSsl = true;
  private int port = 8443;
  private int mailPort = 25;
  private String databaseUrl = "jdbc:hsqldb:.";
  private String databaseUser = "sa";
  private String databasePassword = "";
  private QueryVersionTask queryVersionTask;
  private Timer timer;
  private Directory directory;

  public LicenseServer() throws IOException {
    initLogger();
    jetty = new Server();
  }

  public void getParams() throws IOException {
    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
    System.out.print("database :");
    databaseUrl = input.readLine();
    System.out.print("user :");
    databaseUser = input.readLine();
    System.out.print("password :");
    databasePassword = input.readLine();
  }

  public void useSsl(boolean useSsl) {
    this.useSsl = useSsl;
  }

  public void usePort(int port) {
    this.port = port;
  }

  public static void main(String[] args) throws Exception {
    LicenseServer server = new LicenseServer();
    server.getParams();
    server.start();
  }

  private static void initLogger() throws IOException {
    InputStream stream = LicenseServer.class.getClassLoader().getResourceAsStream("loggingLicenceServer.properties");
    LogManager.getLogManager().readConfiguration(stream);
  }

  public void setMailPort(int mailPort) {
    this.mailPort = mailPort;
  }

  public void setDatabaseUrl(String databaseUrl) {
    this.databaseUrl = databaseUrl;
  }

  public void init() {
    if (useSsl) {
      SslSocketConnector connector = new SslSocketConnector();
      String keyStore = System.getProperty(KEYSTORE);
      if (keyStore == null) {
        keyStore = "resources/.keystore";
      }
      connector.setKeystore(keyStore);
      String host = System.getProperty(HOST_PROPERTY);
      if (host == null) {
        host = "localhost";
      }
      connector.setHost(host);
//      connector.setPassword();
      connector.setKeyPassword("ninja600");
      connector.setPort(port);
      jetty.addConnector(connector);
    }
    else {
      SocketConnector connector = new SocketConnector();
      connector.setHost("localhost");
      connector.setPort(port);
      jetty.addConnector(connector);
    }
    String database = System.getProperty(DATABASE_URL);
    if (database != null) {
      databaseUrl = database;
    }
    directory = createDirectory();

    timer = new Timer(true);
    queryVersionTask = new QueryVersionTask(directory.get(SqlService.class),
                                            directory.get(VersionService.class));
    queryVersionTask.run();
    timer.schedule(queryVersionTask, 1000, 5000);

    Context context = new Context(jetty, "/", Context.SESSIONS);
    context.setResourceBase("classes");
    context.addServlet(new ServletHolder(new AskForMailServlet(directory)), ConfigService.REQUEST_FOR_MAIL);
    context.addServlet(new ServletHolder(new RequestForConfigServlet(directory)), ConfigService.REQUEST_FOR_CONFIG);
    context.addServlet(new ServletHolder(new RegisterServlet(directory)), ConfigService.REQUEST_FOR_REGISTER);
  }

  private Directory createDirectory() {
    Directory directory = new DefaultDirectory();
    Mailer mailer = new Mailer();
    directory.add(mailer);
    mailer.setPort(mailPort);
    SqlService sqlService = new JdbcSqlService(databaseUrl, databaseUser, databasePassword);
    directory.add(SqlService.class, sqlService);
    directory.add(new VersionService());
    return directory;
  }

  public void start() throws Exception {
    init();
    logger.info("starting server");
    jetty.start();
  }

  public void stop() throws Exception {
    directory.get(Mailer.class).stop();
    jetty.stop();
    jetty.join();
    timer.cancel();
    logger.info("end server");
  }
}
