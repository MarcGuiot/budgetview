package org.designup.picsoulicence;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import org.apache.commons.httpclient.protocol.DefaultProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.Protocol;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.startup.SingleApplicationInstanceListener;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcSqlService;
import org.uispec4j.UISpecTestCase;

import java.util.Iterator;

public abstract class LicenseTestCase extends UISpecTestCase {
  protected SimpleSmtpServer mailServer;
  private LicenceServer server;
  private Thread mailThread;
  private static final String databaseUrl = "jdbc:hsqldb:.";
  private SqlService sqlService = null;
  protected static final String PATH_TO_DATA = "tmp/localprevayler";

  protected void setUp() throws Exception {
    super.setUp();
    System.setProperty(SingleApplicationInstanceListener.SINGLE_INSTANCE_DISABLED, "true");
    System.setProperty("com.picsou.licence.url", "http://localhost:5000");
//    System.setProperty(PicsouApplication.DEFAULT_ADDRESS_PROPERTY, "http://localhost:8443");
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, PATH_TO_DATA);
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    mailServer = new SimpleSmtpServer(2500);
    server = new LicenceServer();
    server.useSsl(false);
    server.usePort(5000);
    server.setMailPort(2500);
    server.setDabaseUrl(databaseUrl);
    Protocol http = new Protocol("http", new DefaultProtocolSocketFactory(), 5000);
    Protocol.registerProtocol("http", http);
  }

  public SqlConnection getSqlConnection() {
    if (sqlService == null) {
      sqlService = new JdbcSqlService(databaseUrl, "sa", "");
    }
    return sqlService.getDb();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    stop();
  }

  protected void start() throws Exception {
    mailThread = new Thread() {
      public void run() {
        mailServer.run();
      }
    };
    mailThread.setDaemon(true);
    mailThread.start();
    server.start();
  }

  protected void stop() throws Exception {
    if (mailServer != null) {
      mailServer.stop();
      mailServer = null;
    }
    if (mailThread != null) {
      mailThread.join();
      mailThread = null;
    }
    if (server != null) {
      server.stop();
      server = null;
    }
    sqlService = null;
    System.setProperty("com.picsou.licence.url", "");
  }

  protected String checkReceive(String mailTo) throws InterruptedException {
    long end = System.currentTimeMillis() + 1000;
    synchronized (mailServer) {
      Iterator receivedEmail = mailServer.getReceivedEmail();
      while (!receivedEmail.hasNext()) {
        mailServer.wait(800);
        if (System.currentTimeMillis() > end) {
          fail("no mail received");
        }
      }
      if (receivedEmail.hasNext()) {
        SmtpMessage message = (SmtpMessage)receivedEmail.next();
        assertEquals(mailTo, message.getHeaderValue("To"));
        return message.getBody();
      }
      else {
        fail("no mail received");
      }
    }
    return null;
  }

}
