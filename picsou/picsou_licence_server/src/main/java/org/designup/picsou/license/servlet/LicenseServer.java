package org.designup.picsou.license.servlet;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.license.mail.Mailer;
import org.designup.picsou.license.model.License;
import org.designup.picsou.license.model.MailError;
import org.designup.picsou.license.model.RepoInfo;
import org.designup.picsou.license.model.SoftwareInfo;
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
import java.io.InputStreamReader;
import java.util.Timer;

public class LicenseServer {
  static Logger logger = Logger.getLogger("LicenseServer");
  public static final String USE_SSHL = "picsou.server.useSsl";
  public static final String KEYSTORE = "picsou.server.keystore";
  public static final String HOST_PROPERTY = "picsou.server.host";
  public static final String DATABASE_URL = "picsou.server.database.url";
  public static final String DATABASE_USER = "picsou.server.database.user";
  public static final String DATABASE_PASSWD = "picsou.server.database.passwd";
  public static final String NEW_USER = "/newUser";
  private Server jetty;
  private boolean useSsl = true;
  private int port = 443;
  private int mailPort = 25;
  private String databaseUrl = JDBC_HSQLDB;
  private String databaseUser = "sa";
  private String databasePassword = "";
  private QueryVersionTask queryVersionTask;
  private Timer timer;
  private Directory directory;
  private static final String JDBC_HSQLDB = "jdbc:hsqldb:.";
  private Context context;

  public LicenseServer() throws IOException {
    try {
      PropertyConfigurator.configure("log4j.properties");
    }
    catch (Exception e) {
      System.err.println("Fail to init log4j");
    }
    logger.info("init server");
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
    server.init();
    server.start();
  }

  private static void initLogger() throws IOException {
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
      connector.setHeaderBufferSize(1024 * 1024);
      connector.setRequestBufferSize(1024 * 1024);
      String keyStore = System.getProperty(KEYSTORE);
      if (keyStore == null) {
        keyStore = "resources/.keystore";
      }
      connector.setKeystore(keyStore);
      String host = System.getProperty(HOST_PROPERTY);
      if (host == null) {
        host = "0.0.0.0";
      }
      connector.setHost(host);
//      connector.setPassword();
      connector.setKeyPassword("ninja600");
      connector.setPort(port);
      jetty.addConnector(connector);
    }
    else {
      SocketConnector connector = new SocketConnector();
      connector.setHeaderBufferSize(1024 * 1024);
      connector.setRequestBufferSize(1024 * 1024);
      connector.setHost("0.0.0.0");
      connector.setPort(port);
      jetty.addConnector(connector);
    }
    String database = System.getProperty(DATABASE_URL);
    if (database != null) {
      databaseUrl = database;
    }
    directory = createDirectory();

    initHslqDb(directory.get(SqlService.class));
    timer = new Timer(true);
    queryVersionTask = new QueryVersionTask(directory.get(SqlService.class),
                                            directory.get(VersionService.class));
    queryVersionTask.run();
    timer.schedule(queryVersionTask, 5000, 5000);

    context = new Context(jetty, "/", Context.SESSIONS);
    context.setResourceBase("resources");
    context.addServlet(new ServletHolder(new AskForCodeServlet(directory)), ConfigService.REQUEST_FOR_MAIL);
    context.addServlet(new ServletHolder(new RequestForConfigServlet(directory)), ConfigService.REQUEST_FOR_CONFIG);
    context.addServlet(new ServletHolder(new RegisterServlet(directory)), ConfigService.REQUEST_FOR_REGISTER);
    context.addServlet(new ServletHolder(new NewUserServlet(directory)), NEW_USER);
    context.addServlet(new ServletHolder(new SendMailServlet(directory)), ConfigService.REQUEST_SEND_MAIL);
    context.addServlet(new ServletHolder(new SendUseInfo()), ConfigService.SEND_USE_INFO);

    context.addServlet(new ServletHolder(new ReceiveDataServlet("/tmp/data", directory)), ConfigService.REQUEST_REGISTER_DATA);
    context.addServlet(new ServletHolder(new RetrieveDataServlet("/tmp/data", directory)), ConfigService.REQUEST_RETRIEVE_DATA);
    context.addServlet(new ServletHolder(new SendMailCreateMobileUserServlet("/tmp/data", directory)), "/sendMailToCreateMobileUser");
    context.addServlet(new ServletHolder(new CreateMobileUserServlet("/tmp/data", directory)), "/createMobileUser");
  }

  public void addServlet(ServletHolder holder, String name) {
    context.addServlet(holder, name);
  }

  private void initHslqDb(SqlService sqlService) {
    if (databaseUrl.equals(JDBC_HSQLDB)) {
      sqlService.getDb().createTable(License.TYPE, SoftwareInfo.TYPE, RepoInfo.TYPE, MailError.TYPE);
    }
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
