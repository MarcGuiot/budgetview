package com.budgetview.server.cloud;

import com.budgetview.server.cloud.servlet.BudgeaServlet;
import com.budgetview.server.cloud.servlet.ConnectionServlet;
import com.budgetview.server.cloud.servlet.PingServlet;
import com.budgetview.server.config.ConfigService;
import com.budgetview.server.license.mail.Mailer;
import com.budgetview.server.utils.Log4J;
import com.budgetview.server.web.WebServer;
import org.apache.log4j.Logger;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

public class CloudServer {

  private static Logger logger = Logger.getLogger("CloudServer");

  private Directory directory;
  private WebServer webServer;
  private ConfigService config;

  public static void main(String[] args) throws Exception {
    CloudServer server = new CloudServer(args);
    server.init();
    server.start();
  }

  public CloudServer(String... args) throws Exception {
    config = new ConfigService(args);
    Log4J.init(config);
  }

  public void init() throws Exception {
    directory = createDirectory();
    webServer = new WebServer(directory, "register.mybudgetview.fr", 8088, 1444);
    webServer.add(new ConnectionServlet(directory), "/connections");
    webServer.add(new BudgeaServlet(directory), "/budgea");
    webServer.add(new PingServlet(directory), "/ping");
  }

  private Directory createDirectory() throws Exception {
    Directory directory = new DefaultDirectory();
    directory.add(config);
    directory.add(new UserDataService());
    return directory;
  }

  public void start() throws Exception {
    logger.info("starting server");
    webServer.start();
  }

  public void stop() throws Exception {
    webServer.stop();
    logger.info("server stopped");
  }
}
