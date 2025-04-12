package com.budgetview.server.license.functests;

import com.budgetview.desktop.Application;
import com.budgetview.functests.checkers.ApplicationChecker;
import com.budgetview.server.license.ConnectedTestCase;
import org.globsframework.utils.Files;

import java.io.File;

public class NewVersionTest extends ConnectedTestCase {

  private ApplicationChecker application = new ApplicationChecker();
  private static final File DESKTOP_PROPERTIES = new File("budgetview/bv_server/tmp/desktop.properties");

  public void setUp() throws Exception {
    super.setUp();
    System.setProperty("budgetview.desktop.version.path", DESKTOP_PROPERTIES.getAbsolutePath());
  }

  protected void tearDown() throws Exception {
    application.dispose();
    super.tearDown();
  }

  public void testNoDesktopFile() throws Exception {

    DESKTOP_PROPERTIES.delete();

    startServers();

    application.start();
    application.getNewVersionFooter()
      .checkHidden();

    stopServers();
  }

  public void testLicenseServerUnavailable() throws Exception {
    application.start();
    application.getNewVersionFooter()
      .checkHidden();
  }

  public void testCurrentVersion() throws Exception {

    Files.dumpStringToFile(DESKTOP_PROPERTIES,
                           "budgetview.desktop.jar=" + Application.JAR_VERSION + "\n" +
                           "budgetview.desktop.version=" + Application.APPLICATION_VERSION +"\n");

    startServers();

    application.start();
    application.getNewVersionFooter()
      .checkHidden();

    stopServers();
  }

  public void testNewVersionAvailable() throws Exception {

    long newJar = Application.JAR_VERSION + 1;
    String newVersion = Application.APPLICATION_VERSION + ".1";
    Files.dumpStringToFile(DESKTOP_PROPERTIES,
                           "budgetview.desktop.jar=" + newJar + "\n" +
                           "budgetview.desktop.version=" + newVersion + "\n");

    startServers();

    application.start();
    application.getNewVersionFooter()
      .checkNewVersionShown("Switch from version " + Application.APPLICATION_VERSION + " to " + newVersion)
      .checkLink("https://www.budgetview.fr/support/derniers-changements")
      .hide()
      .checkHidden();

    stopServers();
  }

}
