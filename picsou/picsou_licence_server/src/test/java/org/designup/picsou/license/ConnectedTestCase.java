package org.designup.picsou.license;

import org.apache.commons.httpclient.protocol.DefaultProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.Protocol;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.browsing.BrowsingService;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.startup.SingleApplicationInstanceListener;
import org.designup.picsou.license.checkers.DbChecker;
import org.designup.picsou.license.checkers.FtpServerChecker;
import org.designup.picsou.license.checkers.MailServerChecker;
import org.uispec4j.UISpec4J;
import org.uispec4j.UISpecTestCase;

import java.util.Locale;

public abstract class ConnectedTestCase extends UISpecTestCase {

  protected MailServerChecker mailServer;
  protected FtpServerChecker ftpServer;
  protected DbChecker db;

  protected static final String PATH_TO_DATA = "tmp/localprevayler";
  protected LicenseServerChecker licenseServer;

  protected void setUp() throws Exception {
    super.setUp();
    UISpec4J.setAssertionTimeLimit(1500);
    LoggedInFunctionalTestCase.resetWindow();
    BrowsingService.setDummyBrowser(true);
    Locale.setDefault(Locale.ENGLISH);
    System.setProperty(SingleApplicationInstanceListener.SINGLE_INSTANCE_DISABLED, "true");
    System.setProperty(ConfigService.COM_APP_LICENSE_URL, "http://localhost:5000");
    System.setProperty(ConfigService.COM_APP_LICENSE_FTP_URL, "ftp://localhost:12000");
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, PATH_TO_DATA);
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");

    mailServer = new MailServerChecker();

    db =  new DbChecker();
    licenseServer = new LicenseServerChecker(db.getUrl());

    Protocol http = new Protocol("http", new DefaultProtocolSocketFactory(), 5000);
    Protocol.registerProtocol("http", http);

    ftpServer = new FtpServerChecker(12000);
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
