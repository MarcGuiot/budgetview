package com.budgetview.server.cloud;

import com.budgetview.server.cloud.servlet.*;
import com.budgetview.server.config.ConfigService;
import com.budgetview.server.utils.DbInit;
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
    directory = createDirectory();
    webServer = new WebServer(config);
    webServer.add(new ConnectionServlet(directory), "/connections");
    webServer.add(new BudgeaWebHookServlet(directory), "/budgea");
    webServer.add(new StatementServlet(directory), "/statement");

    if (config.isTrue("budgetview.ping.available")) {
      webServer.add(new PingServlet(directory), "/ping");
    }
  }

  private Directory createDirectory() throws Exception {
    Directory directory = new DefaultDirectory();
    directory.add(config);
    DbInit.create(config, directory);
    directory.add(new AuthenticationService(directory));
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
