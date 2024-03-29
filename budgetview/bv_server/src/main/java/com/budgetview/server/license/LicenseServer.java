package com.budgetview.server.license;

import com.budgetview.server.cloud.servlet.DesktopVersionServlet;
import com.budgetview.server.cloud.utils.CloudDb;
import com.budgetview.server.common.ServerStatusServlet;
import com.budgetview.server.config.ConfigService;
import com.budgetview.server.license.mail.Mailer;
import com.budgetview.server.license.model.License;
import com.budgetview.server.license.model.MailError;
import com.budgetview.server.license.model.RepoInfo;
import com.budgetview.server.license.model.SoftwareInfo;
import com.budgetview.server.license.servlet.*;
import com.budgetview.server.license.utils.LicenseDb;
import com.budgetview.server.utils.Log4J;
import com.budgetview.server.web.WebServer;
import com.budgetview.shared.license.LicenseConstants;
import org.apache.log4j.Logger;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.servlet.http.HttpServlet;
import java.util.Timer;

public class LicenseServer {

  static Logger logger = Logger.getLogger("LicenseServer");

  private WebServer webServer;
  private Timer timer;
  private Directory directory;
  private ConfigService config;
  private Mailer mailer;

  public static void main(String[] args) throws Exception {
    ConfigService.checkCommandLine(args);
    LicenseServer server = new LicenseServer(args);
    server.init();
    server.start();
  }

  public LicenseServer(String... args) throws Exception {
    config = new ConfigService(args);
    Log4J.init(config);
    logger.info("init server");
    mailer = new Mailer(config);
  }

  public void init() throws Exception {
    directory = createDirectory();
    initDb(directory);

    QueryVersionTask queryVersionTask = new QueryVersionTask(directory.get(GlobsDatabase.class), directory.get(VersionService.class));
    queryVersionTask.run();
    timer = new Timer(true);
    timer.schedule(queryVersionTask, 5000, 5000);

    // Current: HTTP=null / HTTPS=443
    webServer = new WebServer(config);
    webServer.add(new AskForCodeServlet(directory), LicenseConstants.REQUEST_FOR_MAIL);
    webServer.add(new RequestForConfigServlet(directory), LicenseConstants.REQUEST_FOR_CONFIG);
    webServer.add(new RegisterServlet(directory), LicenseConstants.REQUEST_FOR_REGISTER);
    webServer.add(new NewUserServlet(directory), LicenseConstants.NEW_USER);
    webServer.add(new SendMailServlet(directory), LicenseConstants.SEND_MAIL_TO_US);
    webServer.add(new SendUseInfoServlet(), LicenseConstants.SEND_USE_INFO);
    webServer.add(new ServerStatusServlet(directory), LicenseConstants.SERVER_STATUS);
    webServer.add(new DesktopVersionServlet(directory), LicenseConstants.DESKTOP_VERSION);
  }

  public void addServlet(HttpServlet servlet, String name) {
    webServer.add(servlet, name);
  }

  private void initDb(Directory directory) {
    String databaseUrl = directory.get(ConfigService.class).get(CloudDb.DATABASE_URL);
    if (CloudDb.isJDBC(databaseUrl)) {
      this.directory.get(GlobsDatabase.class).connect().createTables(License.TYPE, SoftwareInfo.TYPE, RepoInfo.TYPE, MailError.TYPE);
    }
  }

  public Directory getDirectory() {
    return directory;
  }

  private Directory createDirectory() throws Exception {
    Directory directory = new DefaultDirectory();
    directory.add(config);
    directory.add(mailer);
    directory.add(GlobsDatabase.class, LicenseDb.create(config));
    directory.add(new VersionService());
    return directory;
  }

  public void start() throws Exception {
    logger.info("starting server");
    webServer.start();
    logger.info("server started - " + webServer.info());
  }

  public void stop() throws Exception {
    directory.get(Mailer.class).stop();
    webServer.stop();
    timer.cancel();
    logger.info("server stopped");
  }
}
