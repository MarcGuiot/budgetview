package com.budgetview.server.license;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.gui.PicsouApplication;
import com.budgetview.gui.browsing.BrowsingService;
import com.budgetview.gui.startup.components.SingleApplicationInstanceListener;
import com.budgetview.server.license.checkers.DbChecker;
import com.budgetview.server.license.checkers.FtpServerChecker;
import com.budgetview.server.license.checkers.MailServerChecker;
import com.budgetview.server.mobile.MobileServer;
import com.budgetview.server.license.servlet.WebServer;
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
  protected DbChecker db;
  protected int httpPort = 5000;

  protected static final String PATH_TO_DATA = "tmp/localprevayler";
  protected static final File MOBILE_DATA_DIR = new File("/var/tmp/bv_mobile/");
  protected LicenseServerChecker licenseServer;

  protected void setUp() throws Exception {
    super.setUp();
    LoggedInFunctionalTestCase.resetWindow();
    BrowsingService.setDummyBrowser(true);

    Locale.setDefault(Locale.ENGLISH);

    System.setProperty(WebServer.KEYSTORE_PATH, "./budgetview/bv_license_server/ssl/keystore");
    System.setProperty(WebServer.KEYSTORE_PWD, "bvpwd1");
    System.setProperty(WebServer.HTTPS_PORT_PROPERTY, "1443");
    System.setProperty(MobileServer.MOBILE_PATH_PROPERTY, MOBILE_DATA_DIR.getAbsolutePath());
    System.setProperty("budgetview.log.sout", "true");

    System.setProperty(SingleApplicationInstanceListener.SINGLE_INSTANCE_DISABLED, "true");
    System.setProperty(LicenseConstants.COM_APP_LICENSE_URL, "http://localhost:" + httpPort);
    System.setProperty(MobileConstants.COM_APP_MOBILE_URL, "http://localhost:" + httpPort);
    System.setProperty(LicenseConstants.COM_APP_FTP_URL, "ftp://localhost:12000");

    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, PATH_TO_DATA);
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");

    Files.deleteSubtreeOnly(new File(PATH_TO_DATA));

    Files.deleteSubtreeOnly(MOBILE_DATA_DIR);
    MOBILE_DATA_DIR.mkdir();

    mailServer = new MailServerChecker();

    db =  new DbChecker();
    licenseServer = new LicenseServerChecker(db.getUrl(), httpPort);

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
    System.setProperty(LicenseConstants.COM_APP_LICENSE_URL, "");
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
