package com.budgetview.license.servlet;

import com.budgetview.http.HttpBudgetViewConstants;
import com.budgetview.license.mail.Mailer;
import com.budgetview.shared.utils.MobileConstants;
import org.apache.log4j.Logger;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.io.IOException;

public class MobileServer {

  public static final String MOBILE_PATH_PROPERTY = "bv.mobile.path";

  private static Logger logger = Logger.getLogger("MobileServer");

  private Directory directory;
  private WebServer webServer;

  public static void main(String[] args) throws Exception {
    Log4J.init();

    MobileServer server = new MobileServer();
    server.init();
    server.start();
  }

  public static String getDataDirectoryPath() {
    String path = System.getProperty(MOBILE_PATH_PROPERTY);
    if (Strings.isNullOrEmpty(path)) {
      throw new InvalidParameter("Mobile data directory must be set with -D" + MOBILE_PATH_PROPERTY);
    }
    return path;
  }

  private MobileServer() throws IOException {
    logger.info("init server");
  }

  public void init() throws Exception {
    directory = createDirectory();

    String pathForMobileData = getDataDirectoryPath();
    webServer = new WebServer("register.mybudgetview.fr", 8080, 1443);
    webServer.add(new ReceiveDataServlet(pathForMobileData), HttpBudgetViewConstants.REQUEST_CLIENT_TO_SERVER_DATA);
    webServer.add(new RetrieveDataServlet(pathForMobileData, directory), MobileConstants.GET_MOBILE_DATA);
    webServer.add(new SendMailCreateMobileUserServlet(pathForMobileData, directory, webServer.getHttpPort()), MobileConstants.SEND_MAIL_TO_CONFIRM_MOBILE);
    webServer.add(new DeleteMobileUserServlet(pathForMobileData, directory), MobileConstants.DELETE_MOBILE_ACCOUNT);
    webServer.add(new CreateMobileUserServlet(pathForMobileData, directory), MobileConstants.CREATE_MOBILE_USER);
    webServer.add(new SendMailFromMobileServlet(directory), MobileConstants.SEND_MAIL_REMINDER_FROM_MOBILE);
  }

  private Directory createDirectory() {
    Directory directory = new DefaultDirectory();
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
