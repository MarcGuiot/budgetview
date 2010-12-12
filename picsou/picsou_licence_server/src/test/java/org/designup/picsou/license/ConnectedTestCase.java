package org.designup.picsou.license;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import org.apache.commons.httpclient.protocol.DefaultProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.Protocol;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.startup.SingleApplicationInstanceListener;
import org.designup.picsou.license.model.License;
import org.designup.picsou.license.model.MailError;
import org.designup.picsou.license.model.RepoInfo;
import org.designup.picsou.license.model.SoftwareInfo;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.constraints.Constraints;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcSqlService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.EmptyGlobList;
import org.globsframework.metamodel.Field;
import org.globsframework.utils.Dates;
import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.command.InvocationRecord;
import org.mockftpserver.core.session.Session;
import org.mockftpserver.stub.StubFtpServer;
import org.mockftpserver.stub.command.AbstractStubDataCommandHandler;
import org.uispec4j.UISpec4J;
import org.uispec4j.UISpecTestCase;

import java.util.Iterator;
import java.util.Locale;
import java.util.Date;

public abstract class ConnectedTestCase extends UISpecTestCase {
  protected SimpleSmtpServer mailServer;
  protected Thread mailThread;
  private static final String databaseUrl = "jdbc:hsqldb:.";
  private SqlService sqlService = null;
  protected static final String PATH_TO_DATA = "tmp/localprevayler";
  private StubFtpServer ftpServer;
  private String actualFilename;
  private boolean started;
  protected LicenseServerChercker licenseServer;

  protected void setUp() throws Exception {
    super.setUp();
    UISpec4J.setAssertionTimeLimit(1500);
    LoggedInFunctionalTestCase.resetWindow();
    Locale.setDefault(Locale.ENGLISH);
    System.setProperty(SingleApplicationInstanceListener.SINGLE_INSTANCE_DISABLED, "true");
    System.setProperty(ConfigService.COM_APP_LICENSE_URL, "http://localhost:5000");
    System.setProperty(ConfigService.COM_APP_LICENSE_FTP_URL, "ftp://localhost:12000");
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, PATH_TO_DATA);
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    mailServer = new SimpleSmtpServer(2500);
    licenseServer = new LicenseServerChercker(databaseUrl);
    Protocol http = new Protocol("http", new DefaultProtocolSocketFactory(), 5000);
    Protocol.registerProtocol("http", http);
    ftpServer = new StubFtpServer();
    ftpServer.setServerControlPort(12000);

    SqlConnection connection = getSqlConnection();
    connection.createTable(License.TYPE, RepoInfo.TYPE, MailError.TYPE, SoftwareInfo.TYPE);
    connection.emptyTable(License.TYPE, RepoInfo.TYPE, MailError.TYPE, SoftwareInfo.TYPE);
    connection.commitAndClose();
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
    mailServer = null;
    mailThread = null;
    sqlService = null;
    ftpServer = null;
    licenseServer = null;
    System.setProperty(ConfigService.COM_APP_LICENSE_URL, "");
  }

  protected void startServers() throws Exception {
    mailThread = new Thread() {
      public void run() {
        mailServer.run();
      }
    };
    mailThread.setDaemon(true);
    mailThread.start();
    licenseServer.start();
    ftpServer.start();
    started = true;
  }

  protected void stop() throws Exception {
    if (mailServer != null) {
      if (started) {
        mailServer.stop();
      }
    }
    mailServer = null;
    if (mailThread != null) {
      mailThread.join();
    }
    mailThread = null;
    if (licenseServer != null) {
      if (started) {
        licenseServer.stop();
      }
      licenseServer = null;
    }
    if (ftpServer != null) {
      if (started) {
        ftpServer.stop();
      }
      ftpServer = null;
    }
    sqlService = null;
    started = false;
  }

  public interface Retr {
    void assertOk();
  }

  public Retr setFtpReply(final String firstExpectedFile, final String firstContent,
                          final String secondExpectedFile, final byte[] secondContent) {

    final CompositeRetrHandler commandHandler =
      new CompositeRetrHandler(firstExpectedFile, firstContent, secondExpectedFile, secondContent);
    ftpServer.setCommandHandler("RETR", commandHandler);
    return new Retr() {
      public void assertOk() {
        commandHandler.assertOk();
      }
    };
  }

  protected String checkReceivedMail(String mailTo) throws InterruptedException {
    long end = System.currentTimeMillis() + 4000;
    synchronized (mailServer) {
      Iterator receivedEmail = mailServer.getReceivedEmail();
      while (!receivedEmail.hasNext()) {
        mailServer.wait(800);
        if (System.currentTimeMillis() > end) {
          fail("no mail received");
        }
        receivedEmail = mailServer.getReceivedEmail();
      }
      if (receivedEmail.hasNext()) {
        SmtpMessage message = (SmtpMessage)receivedEmail.next();
        assertEquals(mailTo, message.getHeaderValue("To"));
        receivedEmail.remove();
        return message.getBody();
      }
      else {
        fail("no mail received");
      }
    }
    return null;
  }

  private static class CompositeRetrHandler extends AbstractStubDataCommandHandler {
    int result;
    private final String firstExpectedFile;
    private final String firstContent;
    private final String secondExpectedFile;
    private final byte[] secondContent;
    private int exptectedResult;

    public CompositeRetrHandler(String firstExpectedFile, String firstContent,
                                String secondExpectedFile, byte[] secondContent) {
      this.firstExpectedFile = firstExpectedFile;
      this.firstContent = firstContent;
      this.secondExpectedFile = secondExpectedFile;
      this.secondContent = secondContent;
      result = 0;
      exptectedResult = 0;
      if (firstExpectedFile != null){
        exptectedResult++;
      }
      if (secondExpectedFile != null){
        exptectedResult++;
      }
    }

    protected void processData(Command command, Session session,
                               InvocationRecord invocationRecord) throws Exception {
      String filename = command.getRequiredString(0);
      if (filename.equals(firstExpectedFile)) {
        byte[] data = firstContent.getBytes();
        session.sendData(data, data.length);
        result++;
      }
      else if (filename.equals(secondExpectedFile)) {
        session.sendData(secondContent, secondContent.length);
        result++;
      }
      else {
        result = -1;
        session.close();
      }
    }

    public void assertOk() {
      long end = System.currentTimeMillis() + 30000;
      while (result != exptectedResult && System.currentTimeMillis() < end) {
        try {
          Thread.sleep(100);
        }
        catch (InterruptedException e) {
        }
      }
      assertEquals(exptectedResult, result);
    }
  }

  public class DbChecker {
    SqlConnection connection;

    public DbChecker() {
      connection = getSqlConnection();
    }

    public void registerMail(String email, String code) {
      connection.getCreateBuilder(License.TYPE)
        .set(License.MAIL, email)
        .set(License.ACTIVATION_CODE, code)
        .getRequest()
        .run();
      connection.commit();

    }

    private Glob getGlob(Field field, Object expected,
                         Constraint constraint) throws InterruptedException {
      long end = System.currentTimeMillis() + 3000;
      GlobList glob = new EmptyGlobList();
      while (end > System.currentTimeMillis()) {
        glob = connection.getQueryBuilder(field.getGlobType(), constraint)
          .selectAll()
          .getQuery().executeAsGlobs();
        connection.commit();
        if (glob.size() == 1) {
          Object actual = glob.get(0).getValue(field);
          if (actual != null && (expected == null || actual.equals(expected))) {
            break;
          }
        }
        Thread.sleep(50);
      }
      assertEquals(1, glob.size());
      return glob.get(0);
    }

    public Glob getLicense(String email, Field field, Object expected) throws InterruptedException {
      return getGlob(field, expected, Constraints.equal(License.MAIL, email));
    }

    public String checkRepoIdIsUpdated(long repoCount, Constraint constraint) throws InterruptedException {
      Glob repoInfo = getGlob(RepoInfo.COUNT, repoCount, constraint);
      Date target = repoInfo.get(RepoInfo.LAST_ACCESS_DATE);
      assertTrue(Dates.isNear(new Date(), target, 10000));
      assertEquals(repoCount, repoInfo.get(RepoInfo.COUNT).longValue());
      return repoInfo.get(RepoInfo.REPO_ID);
    }

    public void checkLicenseCount(String email, long count) throws InterruptedException {
      Glob license = getLicense(email, License.ACCESS_COUNT, count);
      assertEquals(count, license.get(License.ACCESS_COUNT).longValue());
      assertTrue(license.get(License.SIGNATURE).length > 1);
    }
  }

}
