package com.budgetview.server.license;

import com.budgetview.desktop.Application;
import com.budgetview.desktop.browsing.BrowsingService;
import com.budgetview.desktop.startup.components.SingleApplicationInstanceListener;
import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.server.license.checkers.LicenseDbChecker;
import com.budgetview.server.license.checkers.FtpServerChecker;
import com.budgetview.server.license.checkers.MailServerChecker;
import com.budgetview.server.mobile.MobileServer;
import com.budgetview.server.cloud.utils.CloudDb;
import com.budgetview.server.web.WebServer;
import com.budgetview.shared.license.LicenseConstants;
import com.budgetview.shared.mobile.MobileConstants;
import org.globsframework.utils.Files;
import org.uispec4j.UISpec4J;
import org.uispec4j.UISpecTestCase;

import java.io.File;
import java.util.Locale;

public abstract class ConnectedTestCase extends UISpecTestCase {
  protected MailServerChecker mailServer;
  protected FtpServerChecker ftpServer;
  protected LicenseDbChecker db;
  protected int httpPort = 5000;

  protected static final String PATH_TO_DATA = "tmp/localprevayler";
  protected static final File MOBILE_DATA_DIR = new File("/var/tmp/bv_mobile/");
  protected LicenseServerChecker licenseServer;

  protected void setUp() throws Exception {
    super.setUp();
    LoggedInFunctionalTestCase.resetWindow();
    BrowsingService.setDummyBrowser(true);

    System.setProperty(MobileServer.MOBILE_PATH_PROPERTY, MOBILE_DATA_DIR.getAbsolutePath());
    System.setProperty("budgetview.log.sout", "true");

    System.setProperty(SingleApplicationInstanceListener.SINGLE_INSTANCE_DISABLED, "true");
    System.setProperty(LicenseConstants.LICENSE_URL_PROPERTY, "http://localhost:" + httpPort);
    System.setProperty(MobileConstants.SERVER_URL_PROPERTY, "http://localhost:" + httpPort);
    System.setProperty(LicenseConstants.FTP_URL_PROPERTY, "ftp://localhost:12000");

    System.setProperty(WebServer.HTTP_PORT_PROPERTY, Integer.toString(httpPort));
    System.setProperty(CloudDb.DATABASE_URL, LicenseDbChecker.DATABASE_URL);
    System.setProperty(CloudDb.DATABASE_USER, LicenseDbChecker.DATABASE_USER);
    System.setProperty(CloudDb.DATABASE_PASSWORD, LicenseDbChecker.DATABASE_PASSWORD);

    System.setProperty(Application.LOCAL_PREVAYLER_PATH_PROPERTY, PATH_TO_DATA);
    System.setProperty(Application.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");

    Locale.setDefault(Locale.ENGLISH);
    Files.deleteSubtreeOnly(new File(PATH_TO_DATA));
    Files.deleteSubtreeOnly(MOBILE_DATA_DIR);
    MOBILE_DATA_DIR.mkdir();

    mailServer = new MailServerChecker();

    db =  new LicenseDbChecker();
    licenseServer = new LicenseServerChecker();

//    Protocol http = new Protocol("http", new DefaultProtocolSocketFactory(), httpPort);
//    Protocol.registerProtocol("http", http);

    ftpServer = new FtpServerChecker(12000);
    UISpec4J.setAssertionTimeLimit(10000);  //=> l'envoie de mail est parfois res long
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    stopServers();
    mailServer = null;
    db = null;
    ftpServer = null;
    licenseServer = null;
    System.setProperty(LicenseConstants.LICENSE_URL_PROPERTY, "");
  }

  protected void startServers() throws Exception {
    licenseServer.init();
    startServersWithoutLicence();
    Thread.sleep(1000); // wait for jetty to listen on port
  }

  protected void startServersWithoutLicence() throws Exception {
    db.start();
    mailServer.start();
    licenseServer.start();
    ftpServer.start();
  }

  protected void stopServers() throws Exception {
    mailServer.dispose();
    licenseServer.dispose();
    ftpServer.dispose();
    db.dispose();
  }
}
