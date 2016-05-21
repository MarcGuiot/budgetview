package com.budgetview.license.servlet;

import com.budgetview.gui.config.ConfigService;
import com.budgetview.license.model.License;
import com.budgetview.license.model.MailError;
import com.budgetview.license.model.RepoInfo;
import com.budgetview.shared.utils.ComCst;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import com.budgetview.license.mail.Mailer;
import com.budgetview.license.model.SoftwareInfo;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcSqlService;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import java.io.IOException;
import java.util.Timer;

public class LicenseServer {
  static Logger logger = Logger.getLogger("LicenseServer");
  public static final String CREATE_MOBILE_USER = "/createMobileUser";
  public static final String USE_SSHL = "picsou.server.useSsl";
  public static final String KEYSTORE = "picsou.server.keystore";
  public static final String HOST_PROPERTY = "picsou.server.host";
  public static final String DATABASE_URL = "picsou.server.database.url";
  public static final String DATABASE_USER = "picsou.server.database.user";
  public static final String DATABASE_PASSWD = "picsou.server.database.passwd";
  public static final String NEW_USER = "/newUser";
  private Server jetty;
  private Integer sslPort = null;
  private Integer port = null;
  private String mailHost = "ns0.ovh.net";
  private int mailPort = 587;
  private String databaseUrl = JDBC_HSQLDB;
  private String databaseUser = "sa";
  private String databasePassword = "";
  private QueryVersionTask queryVersionTask;
  private Timer timer;
  private Directory directory;
  private static final String JDBC_HSQLDB = "jdbc:hsqldb:.";
  private Context context;
  private String pathForMobileData = System.getProperty("data.mobile.path", "/tmp/data");
  private boolean onlyMobile = false;

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

  public void usePort(Integer sslPort, Integer port) {
    this.sslPort = sslPort;
    this.port = port;
  }

  public static void main(String[] args) throws Exception {
    LicenseServer server = new LicenseServer();
    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      if (arg.equalsIgnoreCase("-data")) {
        server.usePort(1443, 8080);
        server.onlyMobile();
      }
    }
    server.readParam(args);
    if (!server.onlyMobile){
      server.usePort(443, null);
    }
    server.init();
    server.start();
  }

  private void readParam(String[] args) {
    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      if (arg.equals("-url")) {
        databaseUrl = args[i + 1];
      }
      else if (arg.equals("-user")){
        databaseUser = args[i + 1];
      }
      else if (arg.equals("-pass")){
        databasePassword = args[i + 1];
      }
    }
  }

  private void onlyMobile() {
    this.onlyMobile = true;
  }

  private static void initLogger() throws IOException {
  }

  public void setMailPort(String mailHost, int mailPort) {
    this.mailHost = mailHost;
    this.mailPort = mailPort;
  }

  public void setDatabaseUrl(String databaseUrl) {
    this.databaseUrl = databaseUrl;
  }

  public void init() {
    String host = System.getProperty(HOST_PROPERTY);
    if (host == null) {
      host = "0.0.0.0";
    }
    if (sslPort != null) {
      SslSocketConnector sslConnector = new SslSocketConnector();
      sslConnector.setHeaderBufferSize(1024 * 1024);
      sslConnector.setRequestBufferSize(1024 * 1024);
      String keyStore = System.getProperty(KEYSTORE);
      if (keyStore == null) {
        keyStore = "resources/.keystore";
      }
      sslConnector.setKeystore(keyStore);
      sslConnector.setHost(host);
      sslConnector.setPassword("ninja600");
      sslConnector.setKeyPassword("ninja600");
      sslConnector.setPort(sslPort);
      jetty.addConnector(sslConnector);
    }

    if (port != null) {
      SocketConnector connector = new SocketConnector();
      connector.setHeaderBufferSize(1024 * 1024);
      connector.setRequestBufferSize(1024 * 1024);
      connector.setHost(host);
      connector.setPort(port);
      jetty.addConnector(connector);
    }

    String database = System.getProperty(DATABASE_URL);
    if (database != null) {
      databaseUrl = database;
    }
    directory = createDirectory();

    initHslqDb(directory.get(SqlService.class));
    context = new Context(jetty, "/", Context.SESSIONS);
    context.setResourceBase("resources");
    if (!onlyMobile) {
      timer = new Timer(true);
      queryVersionTask = new QueryVersionTask(directory.get(SqlService.class),
                                              directory.get(VersionService.class));
      queryVersionTask.run();
      timer.schedule(queryVersionTask, 5000, 5000);
      context.addServlet(new ServletHolder(new AskForCodeServlet(directory)), ConfigService.REQUEST_FOR_MAIL);
      context.addServlet(new ServletHolder(new RequestForConfigServlet(directory)), ConfigService.REQUEST_FOR_CONFIG);
      context.addServlet(new ServletHolder(new RegisterServlet(directory)), ConfigService.REQUEST_FOR_REGISTER);
      context.addServlet(new ServletHolder(new NewUserServlet(directory)), NEW_USER);
      context.addServlet(new ServletHolder(new SendMailServlet(directory)), ConfigService.REQUEST_SEND_MAIL);
      context.addServlet(new ServletHolder(new SendUseInfo()), ConfigService.SEND_USE_INFO);
    }

    context.addServlet(new ServletHolder(new ReceiveDataServlet(pathForMobileData, directory)), ConfigService.REQUEST_CLIENT_TO_SERVER_DATA);
    context.addServlet(new ServletHolder(new RetrieveDataServlet(pathForMobileData, directory)), ComCst.GET_MOBILE_DATA);
    context.addServlet(new ServletHolder(new SendMailCreateMobileUserServlet(pathForMobileData, directory, sslPort, port)),
                       ComCst.SEND_MAIL_TO_CONFIRM_MOBILE);
    context.addServlet(new ServletHolder(new DeleteMobileUserServlet(pathForMobileData, directory)),
                       ComCst.DELETE_MOBILE_ACCOUNT);
    context.addServlet(new ServletHolder(new CreateMobileUserServlet(pathForMobileData, directory)), CREATE_MOBILE_USER);

    context.addServlet(new ServletHolder(new SendMailFromMobileServlet(directory)), ComCst.SEND_MAIL_REMINDER_FROM_MOBILE);
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
    mailer.setPort(mailHost, mailPort);
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
