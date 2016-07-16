package com.budgetview.license;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.gui.PicsouApplication;
import com.budgetview.gui.browsing.BrowsingService;
import com.budgetview.gui.config.ConfigService;
import com.budgetview.gui.startup.components.SingleApplicationInstanceListener;
import com.budgetview.http.HttpBudgetViewConstants;
import com.budgetview.license.checkers.DbChecker;
import com.budgetview.license.checkers.FtpServerChecker;
import com.budgetview.license.checkers.MailServerChecker;
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
  protected LicenseServerChecker licenseServer;

  protected void setUp() throws Exception {
    super.setUp();
    LoggedInFunctionalTestCase.resetWindow();
    BrowsingService.setDummyBrowser(true);
    Locale.setDefault(Locale.ENGLISH);
    System.setProperty(SingleApplicationInstanceListener.SINGLE_INSTANCE_DISABLED, "true");
    System.setProperty(ConfigService.COM_APP_LICENSE_URL, "http://localhost:" + httpPort);
    System.setProperty(ConfigService.COM_APP_MOBILE_URL, "http://localhost:" + httpPort);
    System.setProperty(ConfigService.COM_APP_FTP_URL, "ftp://localhost:12000");
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, PATH_TO_DATA);
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    Files.deleteSubtreeOnly(new File(PATH_TO_DATA));

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
    System.setProperty(ConfigService.COM_APP_LICENSE_URL, "");
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
