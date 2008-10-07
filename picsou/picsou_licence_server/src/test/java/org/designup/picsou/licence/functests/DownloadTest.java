package org.designup.picsou.licence.functests;

import org.designup.picsou.functests.checkers.*;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.licence.LicenseTestCase;
import org.designup.picsou.licence.model.SoftwareInfo;
import org.designup.picsou.model.TransactionType;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.utils.Dates;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class DownloadTest extends LicenseTestCase {
  private Window window;
  private PicsouApplication picsouApplication;

  protected void setUp() throws Exception {
    super.setUp();
    System.setProperty(PicsouApplication.IS_DATA_IN_MEMORY, "false");
    TimeService.setCurrentDate(Dates.parseMonth("2008/07"));
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    if (window != null) {
      window.dispose();
    }
    window = null;
    if (picsouApplication != null) {
      picsouApplication.shutdown();
    }
    picsouApplication = null;
  }

  private void startPicsou() {
    window = WindowInterceptor.run(new Trigger() {
      public void run() throws Exception {
        picsouApplication = new PicsouApplication();
        picsouApplication.run();
      }
    });
  }

  public void testJarIsSent() throws Exception {
    SqlConnection connection = getSqlConnection();
    connection.getCreateBuilder(SoftwareInfo.TYPE)
      .set(SoftwareInfo.LATEST_JAR_VERSION, PicsouApplication.JAR_VERSION + 1L)
      .set(SoftwareInfo.LATEST_CONFIG_VERSION, PicsouApplication.BANK_CONFIG_VERSION + 1L)
      .getRequest().run();
    connection.commitAndClose();
    start();
    final String jarName = ConfigService.generatePicsouJarName(PicsouApplication.JAR_VERSION + 1L);
    final String configJarName = ConfigService.generateConfigJarName(PicsouApplication.BANK_CONFIG_VERSION + 1L);
    byte[] content = generateConfigContent();
    LicenseTestCase.Retr retr = setFtpReply(jarName, "jar content", configJarName, content);
    startPicsou();
    retr.assertOk();
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("user", "passw@rd");
    InfoChecker checker = new InfoChecker(window);
    checker.checkNewVersion();

    OfxBuilder builder = OfxBuilder.init(this);
    builder.addBankAccount(4321, -2, "1111", 123.3, "10/09/2008");
    builder.addTransaction("2008/09/10", -100., "STUPID HEADER blabla");
    String ofxFile = builder.save();
    OperationChecker operation = new OperationChecker(window);
    operation.importOfxFile(ofxFile);
    ViewSelectionChecker views = new ViewSelectionChecker(window);
    views.selectData();
    TransactionChecker transaction = new TransactionChecker(window);
    transaction.initContent()
      .add("10/09/2008", TransactionType.VIREMENT, "GOOD HEADER", "", -100.00)
      .check();
    String path = PicsouApplication.getDataPath();
    File pathToJar = new File(path + "/jars");
    String[] jars = pathToJar.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.equals(jarName);
      }
    });
    assertTrue(jars.length == 1);
    File pathToConfig = new File(path + "/configs");
    String[] configs = pathToConfig.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.equals(configJarName);
      }
    });
    assertTrue(configs.length == 1);
  }


  private byte[] generateConfigContent() throws Exception {
    File tempFile = File.createTempFile("config", ".jar");
    tempFile.deleteOnExit();
    JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tempFile));
    jarOutputStream.putNextEntry(new ZipEntry("banks/bankList.txt"));
    jarOutputStream.write("picsouBank.xml\n".getBytes());
    jarOutputStream.putNextEntry(new ZipEntry("banks/picsouBank.xml"));
    jarOutputStream.write(("<globs>\n" +
                           "  <bank name=\"picsouBank\" downloadUrl=\"\" id='-2'>\n" +
                           "    <bankEntity id=\"4321\"/> \n" +
                           "    <transactionTypeMatcher regexp=\"STUPID HEADER .*\"\n" +
                           "                            transactionTypeName=\"virement\" " +
                           "                            label=\"GOOD HEADER\"/>" +
                           "  </bank>\n" +
                           "</globs>\n").getBytes());
    jarOutputStream.close();
    byte content[] = new byte[(int)tempFile.length()];
    FileInputStream inputStream = new FileInputStream(tempFile);
    inputStream.read(content);
    inputStream.close();
    return content;
  }
}
