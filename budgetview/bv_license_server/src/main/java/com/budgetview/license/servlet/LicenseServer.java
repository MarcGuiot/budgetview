package com.budgetview.license.servlet;

import com.budgetview.license.mail.Mailer;
import com.budgetview.license.model.License;
import com.budgetview.license.model.MailError;
import com.budgetview.license.model.RepoInfo;
import com.budgetview.license.model.SoftwareInfo;
import com.budgetview.shared.http.HttpBudgetViewConstants;
import com.budgetview.shared.utils.MobileConstants;
import org.apache.log4j.Logger;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcSqlService;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.util.Timer;

public class LicenseServer {

  static Logger logger = Logger.getLogger("LicenseServer");

  public static final String NEW_USER = "/newUser";

  public static final String DATABASE_URL = "bv.server.database.url";
  public static final String DATABASE_USER = "bv.server.database.user";
  public static final String DATABASE_PASSWD = "bv.server.database.passwd";

  private static final String JDBC_HSQLDB = "jdbc:hsqldb:.";
  private WebServer webServer;
  private String databaseUrl = JDBC_HSQLDB;
  private String databaseUser = "sa";
  private String databasePassword = "";
  private Timer timer;
  private Directory directory;
  private Mailer mailer;

  public static void main(String[] args) throws Exception {
    Log4J.init();

    LicenseServer server = new LicenseServer();
    server.readDbParams(args);
    server.init();
    server.start();
  }

  public LicenseServer() throws IOException {
    logger.info("init server");
    mailer = new Mailer();
  }

  private void readDbParams(String[] args) {
    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      if (arg.equals("-url")) {
        databaseUrl = args[i + 1];
      }
      else if (arg.equals("-user")) {
        databaseUser = args[i + 1];
      }
      else if (arg.equals("-pass")) {
        databasePassword = args[i + 1];
      }
    }
  }

  public void setMailPort(String mailHost, int mailPort) {
    mailer.setPort(mailHost, mailPort);
  }

  public void setDatabaseUrl(String databaseUrl) {
    this.databaseUrl = databaseUrl;
  }

  public void init() throws Exception {
    directory = createDirectory();
    initDb();

    QueryVersionTask queryVersionTask = new QueryVersionTask(directory.get(SqlService.class), directory.get(VersionService.class));
    queryVersionTask.run();
    timer = new Timer(true);
    timer.schedule(queryVersionTask, 5000, 5000);

    webServer = new WebServer("register.mybudgetview.fr", null, 443);
    webServer.add(new AskForCodeServlet(directory), HttpBudgetViewConstants.REQUEST_FOR_MAIL);
    webServer.add(new RequestForConfigServlet(directory), HttpBudgetViewConstants.REQUEST_FOR_CONFIG);
    webServer.add(new RegisterServlet(directory), HttpBudgetViewConstants.REQUEST_FOR_REGISTER);
    webServer.add(new NewUserServlet(directory), NEW_USER);
    webServer.add(new SendMailServlet(directory), HttpBudgetViewConstants.REQUEST_SEND_MAIL);
    webServer.add(new SendUseInfoServlet(), HttpBudgetViewConstants.SEND_USE_INFO);

    String pathForMobileData = MobileServer.getDataDirectoryPath();
    webServer.add(new PostDataServlet(pathForMobileData), MobileConstants.POST_MOBILE_DATA);
    webServer.add(new GetMobileDataServlet(pathForMobileData, directory), MobileConstants.GET_MOBILE_DATA);
    webServer.add(new SendMailCreateMobileUserServlet(pathForMobileData, directory, webServer.getHttpPort()), MobileConstants.SEND_MAIL_TO_CONFIRM_MOBILE);
    webServer.add(new DeleteMobileUserServlet(pathForMobileData, directory), MobileConstants.DELETE_MOBILE_ACCOUNT);
    webServer.add(new CreateMobileUserServlet(pathForMobileData, directory), MobileConstants.CREATE_MOBILE_USER);
    webServer.add(new SendMailFromMobileServlet(directory), MobileConstants.SEND_MAIL_REMINDER_FROM_MOBILE);
  }

  public void addServlet(HttpServlet servlet, String name) {
    webServer.add(servlet, name);
  }

  private void initDb() {
    String database = System.getProperty(DATABASE_URL);
    if (database != null) {
      databaseUrl = database;
    }
    if (databaseUrl.equals(JDBC_HSQLDB)) {
      directory.get(SqlService.class).getDb().createTable(License.TYPE, SoftwareInfo.TYPE, RepoInfo.TYPE, MailError.TYPE);
    }
  }

  private Directory createDirectory() {
    Directory directory = new DefaultDirectory();
    directory.add(mailer);
    directory.add(SqlService.class, new JdbcSqlService(databaseUrl, databaseUser, databasePassword));
    directory.add(new VersionService());
    return directory;
  }

  public void start() throws Exception {
    logger.info("starting server");
    webServer.start();
  }

  public void stop() throws Exception {
    directory.get(Mailer.class).stop();
    webServer.stop();
    timer.cancel();
    logger.info("server stopped");
  }
}
