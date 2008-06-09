package org.designup.picsou.server;

import org.designup.picsou.PicsouServer;
import org.designup.picsou.gui.PicsouApplication;
import org.uispec4j.*;
import org.uispec4j.finder.ComponentMatchers;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowInterceptor;

public abstract class ServerFuncTestCase extends UISpecTestCase {
  protected Window window;
  protected PicsouServer picsouServer;
  protected static final String PICSOU_DEV_TESTFILES_SG1_QIF = "/testfiles/sg1.qif";
  protected static final String PICSOU_DEV_TESTFILES_CIC1_OFX = "/testfiles/cic1.ofx";

  protected void setUp() throws Exception {
    super.setUp();
    System.setProperty("SINGLE_INSTANCE_DISABLED", "true");
    System.setProperty(PicsouApplication.DEFAULT_ADDRESS_PROPERTY, "http://localhost:8443");
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, "tmp/localprevayler");
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    System.setProperty(PicsouServer.SERVER_PREVAYLER_PATH_PROPERTY, "tmp/serverprevayler");
    System.setProperty(PicsouServer.DELETE_SERVER_PROPERTY, "true");
    System.setProperty(PicsouServer.USE_SSHL, "false");
    picsouServer = new PicsouServer();
    picsouServer.start();
    window = WindowInterceptor.run(new Trigger() {
      public void run() throws Exception {
        PicsouApplication.main();
      }
    });
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    picsouServer.stop();
    PicsouApplication.shutdown();
  }

  public void createAndLogUser(String user, String userPassword, final String fileName) {
    TextBox textBox = window.getTextBox("name");
    textBox.setText(user);

    PasswordField password = window.getPasswordField("password");
    PasswordField confirmPassword = window.getPasswordField("confirmPassword");

    CheckBox createAccount = window.getCheckBox("createAccountCheckBox");
    password.setPassword(userPassword);
    createAccount.click();
    confirmPassword.setPassword(userPassword);
    window.getButton("Enter").click();

    WindowInterceptor.init(window.getButton("Browse").triggerClick())
      .process(FileChooserHandler.init().select(new String[]{fileName}))
      .run();

    window.getButton("Import").click();
    TextBox message = (TextBox)window.findUIComponent(ComponentMatchers.innerNameIdentity("message"));
    if (message != null) {
      assertTrue(message.textIsEmpty());
    }

    window.getButton("OK").click();
  }
}
