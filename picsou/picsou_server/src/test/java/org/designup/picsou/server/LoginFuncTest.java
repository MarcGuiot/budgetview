package org.designup.picsou.server;

import org.designup.picsou.functests.checkers.ImportChecker;
import org.designup.picsou.functests.checkers.OperationChecker;
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

    TextBox textBox = window.getTextBox("name");
    textBox.setText("user1");

    PasswordField password = window.getPasswordField("password");
    PasswordField confirmPassword = window.getPasswordField("confirmPassword");

    CheckBox createAccount = window.getCheckBox("createAccountCheckBox");
    password.setPassword("_user1");
    createAccount.click();
    confirmPassword.setPassword("_user1");

    window.getButton("login").click();

    OperationChecker operations = new OperationChecker(window);
    ImportChecker checker = operations.openImportDialog();
    checker.selectFiles(fileName);
    checker.acceptFile();
    checker.selectBank("Société Générale");
    checker.enterAccountNumber("1111");
    checker.doImport();

    assertTrue(getCategoryTable().cellEquals(0, 2, "-155"));
  }
}
