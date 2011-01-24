package org.designup.picsou.server;

import org.designup.picsou.functests.checkers.LoginChecker;
import org.designup.picsou.functests.checkers.OperationChecker;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;
import org.uispec4j.Button;
import org.uispec4j.TextBox;
import org.uispec4j.assertion.UISpecAssert;

public class LoginFuncTest extends ServerFuncTestCase {

  public void testLoginWithoutPassword() throws Exception {
    TextBox textBox = window.getTextBox("name");
    textBox.setText("user2");
    Button loginButton = window.getButton("userlogin");
    loginButton.click();

    TextBox label = window.findUIComponent(TextBox.class, "message");
    UISpecAssert.assertFalse(label.textIsEmpty());
  }

  public void testCreateAccount() throws Exception {
    String fileName = TestUtils.getFileName(this, ".qif");

    Files.copyStreamTofile(LoginFuncTest.class.getResourceAsStream(PICSOU_DEV_TESTFILES_SG1_QIF), fileName);

    final LoginChecker login = new LoginChecker(window);
    login.enterUserAndPassword("user1", "_user1")
      .setCreation()
      .confirmPassword("_user1")
      .loginAndSkipSla()
      .waitForApplicationToLoad();

    OperationChecker.init(window)
      .openImportDialog()
      .selectFiles(fileName)
      .acceptFile()
      .defineAccount("Société Générale", "Main account", "1111")
      .completeImport(10.00);

  }
}
