package org.designup.picsou.server;

import org.designup.picsou.functests.checkers.OperationChecker;
import org.designup.picsou.functests.checkers.LoginChecker;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;
import org.uispec4j.Button;
import org.uispec4j.CheckBox;
import org.uispec4j.PasswordField;
import org.uispec4j.TextBox;
import org.uispec4j.assertion.UISpecAssert;

public class LoginFuncTest extends ServerFuncTestCase {

  public void testLoginWithoutPassword() throws Exception {
    TextBox textBox = window.getTextBox("name");
    textBox.setText("user2");
    Button loginButton = window.getButton("login");
    loginButton.click();

    TextBox label = window.findUIComponent(TextBox.class, "message");
    UISpecAssert.assertFalse(label.textIsEmpty());
  }

  public void testCreateAccount() throws Exception {
    String fileName = TestUtils.getFileName(this, ".qif");

    Files.copyStreamTofile(LoginFuncTest.class.getResourceAsStream(PICSOU_DEV_TESTFILES_SG1_QIF), fileName);

    LoginChecker login = new LoginChecker(window);
    login.enterUserAndPassword("user1", "_user1")
      .setCreation()
      .confirmPassword("_user1")
      .loginAndSkipSla()
      .waitForApplicationToLoad();

    OperationChecker.init(window)
      .openImportDialog()
      .selectFiles(fileName)
      .acceptFile()
      .selectBank("Société Générale")
      .enterAccountNumber("1111")
      .doImportWithBalance()
      .validate();

    fail("Marc: tester dans la vue home que le compte existe ?");
    assertTrue(getCategoryTable().cellEquals(0, 2, "-155"));
  }
}
