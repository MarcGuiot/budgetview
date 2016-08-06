package com.budgetview.server.cloud;

import com.budgetview.server.cloud.servlet.UserServlet;
import com.budgetview.server.config.ConfigService;
import com.budgetview.server.license.mail.Mailer;
import com.budgetview.server.utils.Log4J;
import com.budgetview.server.license.servlet.PostDataServlet;
import com.budgetview.server.license.servlet.VersionService;
import com.budgetview.server.web.WebServer;
import com.budgetview.server.mobile.servlet.*;
import com.budgetview.shared.mobile.MobileConstants;
import org.apache.log4j.Logger;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.io.IOException;

public class CloudServer {

  private static Logger logger = Logger.getLogger("CloudServer");

  private Directory directory;
  private WebServer webServer;

  public static void main(String[] args) throws Exception {
    Log4J.init();

    CloudServer server = new CloudServer();
    server.init(args);
    server.start();
  }

  private CloudServer() throws IOException {
    logger.info("init server");
  }

  public void init(String[] args) throws Exception {
    directory = createDirectory(args);

    webServer = new WebServer(directory, "register.mybudgetview.fr", 8088, 1444);
    webServer.add(new UserServlet(), "/user");
  }

  private Directory createDirectory(String[] args) throws Exception {
    Directory directory = new DefaultDirectory();
    directory.add(new ConfigService(args));
    directory.add(new Mailer());
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
    logger.info("server stopped");
  }
}
