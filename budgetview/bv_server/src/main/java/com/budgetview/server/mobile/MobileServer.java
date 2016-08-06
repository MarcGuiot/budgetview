package com.budgetview.server.mobile;

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

public class MobileServer {

  public static final String MOBILE_PATH_PROPERTY = "budgetview.mobile.path";
  private static Logger logger = Logger.getLogger("MobileServer");

  private Directory directory;
  private WebServer webServer;

  public static void main(String[] args) throws Exception {
    Log4J.init();

    MobileServer server = new MobileServer();
    server.init(args);
    server.start();
  }

  public static String getDataDirectoryPath(Directory directory) {
    String path = directory.get(ConfigService.class).get(MOBILE_PATH_PROPERTY);
    if (Strings.isNullOrEmpty(path)) {
      throw new InvalidParameter("Mobile data directory must be set with: " + MOBILE_PATH_PROPERTY);
    }
    return path;
  }

  private MobileServer() throws IOException {
    logger.info("init server");
  }

  public void init(String[] args) throws Exception {
    directory = createDirectory(args);

    String pathForMobileData = getDataDirectoryPath(directory);
    webServer = new WebServer(directory, "register.mybudgetview.fr", 8080, 1443);
    webServer.add(new PostDataServlet(pathForMobileData), MobileConstants.POST_MOBILE_DATA);
    webServer.add(new GetMobileDataServlet(pathForMobileData, directory), MobileConstants.GET_MOBILE_DATA);
    webServer.add(new SendMailCreateMobileUserServlet(pathForMobileData, directory, webServer.getHttpPort()), MobileConstants.SEND_MAIL_TO_CONFIRM_MOBILE);
    webServer.add(new DeleteMobileUserServlet(pathForMobileData, directory), MobileConstants.DELETE_MOBILE_ACCOUNT);
    webServer.add(new CreateMobileUserServlet(pathForMobileData, directory), MobileConstants.CREATE_MOBILE_USER);
    webServer.add(new SendMailFromMobileServlet(directory), MobileConstants.SEND_MAIL_REMINDER_FROM_MOBILE);
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
