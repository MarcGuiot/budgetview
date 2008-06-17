package org.designup.picsou.server;

import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;
import org.uispec4j.*;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.io.File;

public class LoginFuncTest extends ServerFuncTestCase {

  public void testLoginWithoutPassword() throws Exception {
    TextBox textBox = window.getTextBox("name");
    textBox.setText("user2");
    Button loginButton = window.getButton("login");
    loginButton.click();

    JLabel label = (JLabel)window.findSwingComponent(JLabel.class, "message");
    assertFalse(label.getText().equals(""));
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

    Button find = window.getButton("Browse");
    File file = new File(fileName);
    WindowInterceptor
      .init(find.triggerClick())
      .process(FileChooserHandler.init().select(new File[]{file}))
      .run();

    window.getButton("Import").click();
    window.getInputTextBox("number").setText("1111");
    window.getComboBox("accountBank").select("Societe Generale");
    window.getButton("OK").click();

    Table table = window.getTable("category");
    assertTrue(table.cellEquals(0, 2, "-100"));
  }

}
