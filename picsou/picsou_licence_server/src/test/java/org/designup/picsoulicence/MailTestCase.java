package org.designup.picsoulicence;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import junit.framework.TestCase;
import org.apache.commons.httpclient.protocol.DefaultProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.Protocol;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcSqlService;

import java.util.Iterator;

public abstract class MailTestCase extends TestCase {
  protected SimpleSmtpServer mailServer;
  private LicenceServer server;
  private Thread mailThread;
  private static final String databaseUrl = "jdbc:hsqldb:.";

  protected void setUp() throws Exception {
    super.setUp();
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
    SqlService sqlService = new JdbcSqlService(databaseUrl, "sa", "");
    return sqlService.getDb();
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
    mailServer.stop();
    mailThread.join();
    server.stop();
  }

  protected void checkReceive(String mailTo) throws InterruptedException {
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
      }
    }
  }

}
