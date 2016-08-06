package com.budgetview.server.license;

import com.budgetview.server.config.ConfigService;
import com.budgetview.server.license.mail.Mailer;
import com.budgetview.server.license.model.License;
import com.budgetview.server.license.model.MailError;
import com.budgetview.server.license.model.RepoInfo;
import com.budgetview.server.license.model.SoftwareInfo;
import com.budgetview.server.license.servlet.*;
import com.budgetview.server.mobile.MobileServer;
import com.budgetview.server.mobile.servlet.*;
import com.budgetview.server.utils.Log4J;
import com.budgetview.server.web.WebServer;
import com.budgetview.shared.license.LicenseConstants;
import com.budgetview.shared.mobile.MobileConstants;
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

  public static final String DATABASE_URL = "budgetview.database.url";
  public static final String DATABASE_USER = "budgetview.database.user";
  public static final String DATABASE_PASSWORD = "budgetview.database.password";
  private static final String JDBC_HSQLDB = "jdbc:hsqldb:.";

  private WebServer webServer;
  private Timer timer;
  private Directory directory;
  private Mailer mailer;

  public static void main(String[] args) throws Exception {
    Log4J.init();

    LicenseServer server = new LicenseServer();
    server.init(args);
    server.start();
  }

  public LicenseServer() throws IOException {
    logger.info("init server");
    mailer = new Mailer();
  }

  public void setMailPort(String mailHost, int mailPort) {
    mailer.setPort(mailHost, mailPort);
  }

  public void init(String... args) throws Exception {
    directory = createDirectory(args);
    initDb(directory);

    QueryVersionTask queryVersionTask = new QueryVersionTask(directory.get(SqlService.class), directory.get(VersionService.class));
    queryVersionTask.run();
    timer = new Timer(true);
    timer.schedule(queryVersionTask, 5000, 5000);

    webServer = new WebServer(directory, "register.mybudgetview.fr", null, 443);
    webServer.add(new AskForCodeServlet(directory), LicenseConstants.REQUEST_FOR_MAIL);
    webServer.add(new RequestForConfigServlet(directory), LicenseConstants.REQUEST_FOR_CONFIG);
    webServer.add(new RegisterServlet(directory), LicenseConstants.REQUEST_FOR_REGISTER);
    webServer.add(new NewUserServlet(directory), LicenseConstants.NEW_USER);
    webServer.add(new SendMailServlet(directory), LicenseConstants.REQUEST_SEND_MAIL);
    webServer.add(new SendUseInfoServlet(), LicenseConstants.SEND_USE_INFO);

    String pathForMobileData = MobileServer.getDataDirectoryPath(directory);
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

  private void initDb(Directory directory) {
    String database = directory.get(ConfigService.class).get(DATABASE_URL);
    if (database.equals(JDBC_HSQLDB)) {
      this.directory.get(SqlService.class).getDb().createTable(License.TYPE, SoftwareInfo.TYPE, RepoInfo.TYPE, MailError.TYPE);
    }
  }

  private Directory createDirectory(String[] args) throws Exception {

    ConfigService config = new ConfigService(args);

    Directory directory = new DefaultDirectory();
    directory.add(config);
    directory.add(mailer);
    directory.add(SqlService.class, new JdbcSqlService(config.get(DATABASE_URL), config.get(DATABASE_USER), config.get(DATABASE_PASSWORD)));
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
