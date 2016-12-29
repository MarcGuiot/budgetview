package com.budgetview.server.cloud;

import com.budgetview.server.cloud.services.AuthenticationService;
import com.budgetview.server.cloud.services.EmailValidationService;
import com.budgetview.server.cloud.servlet.*;
import com.budgetview.server.config.ConfigService;
import com.budgetview.server.license.mail.Mailer;
import com.budgetview.server.cloud.utils.CloudDb;
import com.budgetview.server.utils.Log4J;
import com.budgetview.server.web.WebServer;
import com.budgetview.shared.cloud.budgea.BudgeaConstants;
import com.budgetview.shared.license.LicenseConstants;
import org.apache.log4j.Logger;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

public class CloudServer {

  private static Logger logger = Logger.getLogger("CloudServer");

  private WebServer webServer;
  private ConfigService config;
  private Directory directory;

  public static void main(String[] args) throws Exception {
    ConfigService.checkCommandLine(args);
    CloudServer server = new CloudServer(args);
    server.init();
    server.start();
  }

  public CloudServer(String... args) throws Exception {
    config = new ConfigService(args);
    Log4J.init(config);
  }

  public void init() throws Exception {

    config.init(LicenseConstants.LICENSE_URL_PROPERTY);
    config.init(BudgeaConstants.SERVER_URL_PROPERTY);

    directory = createDirectory();
    webServer = new WebServer(config);
    webServer.add(new SignupServlet(directory), "/signup");
    webServer.add(new ValidateServlet(directory), "/validate");
    webServer.add(new BudgeaTokenServlet(directory), "/budgea/token");
    webServer.add(new ProviderAccessServlet(directory), "/provider/access");
    webServer.add(new BankConnectionsServlet(directory), "/banks/connections");
    webServer.add(new BudgeaWebHookServlet(directory), "/budgea");
    webServer.add(new StatementServlet(directory), "/statement/*");

    if (config.isTrue("budgetview.ping.available")) {
      webServer.add(new PingServlet(directory), "/ping");
    }
  }

  private Directory createDirectory() throws Exception {
    Directory directory = new DefaultDirectory();
    directory.add(config);
    directory.add(GlobsDatabase.class, CloudDb.create(config));
    directory.add(new Mailer(config));
    directory.add(new AuthenticationService(directory));
    directory.add(new EmailValidationService(directory));
    return directory;
  }

  public void start() throws Exception {
    logger.info("starting server");
    webServer.start();
    logger.info("server started");
  }

  public void resetDatabase() {
    logger.info("cleaning up database");
    CloudDb.cleanAllTables(directory);
  }

  public void stop() throws Exception {
    logger.info("stopping server");
    webServer.stop();
    logger.info("server stopped");
  }
}
