package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.OperationChecker;
import org.designup.picsou.functests.checkers.TransactionChecker;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.SingleApplicationInstanceListener;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.Files;
import org.uispec4j.*;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

import java.io.File;

public class LoginTest extends ServerFunctionalTestCase {

  private Window window;
  private TextBox userField;
  private PasswordField passwordField;
  private CheckBox createUserCheckbox;
  private Button loginButton;

  protected void setUp() throws Exception {
    super.setUp();

    System.setProperty("SINGLE_INSTANCE_DISABLED", "");
    System.setProperty(PicsouApplication.DEFAULT_ADDRESS_PROPERTY, "");
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, "");
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "");

    Files.deleteSubtree(new File(ServerFunctionalTestCase.getUrl()));
    System.setProperty(SingleApplicationInstanceListener.SINGLE_INSTANCE_DISABLED, "true");
    setAdapter(new UISpecAdapter() {
      public Window getMainWindow() {
        return WindowInterceptor.run(new Trigger() {
          public void run() throws Exception {
            System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, ServerFunctionalTestCase.getUrl());
            System.setProperty(PicsouApplication.DEFAULT_ADDRESS_PROPERTY, "");
            PicsouApplication.main();
          }
        });
      }
    });

    openNewLoginWindow();
  }

  protected void tearDown() throws Exception {
    PicsouApplication.shutdown();
    System.setProperty(SingleApplicationInstanceListener.SINGLE_INSTANCE_DISABLED, "false");
  }

  private void openNewLoginWindow() throws Exception {
    if (window != null) {
      PicsouApplication.shutdown();
    }
    window = getMainWindow();
    userField = window.getInputTextBox("name");
    passwordField = window.getPasswordField("password");
    createUserCheckbox = window.getCheckBox();
    loginButton = window.getButton("Enter");
  }

  public void testCreatingAUserAndLoggingInAgain() throws Exception {
    String filePath = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .addTransaction("2006/01/11", -12.0, "Cheque 12345")
      .save();

    createUser("toto", "p4ssw0rd", filePath);
    getTransactionChecker()
      .initContent()
      .add("11/01/2006", TransactionType.CHECK, "12345", "", -12.00, MasterCategory.NONE)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1, MasterCategory.NONE)
      .check();

    openNewLoginWindow();
    login("toto", "p4ssw0rd");
    getTransactionChecker()
      .initContent()
      .add("11/01/2006", TransactionType.CHECK, "12345", "", -12.00, MasterCategory.NONE)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1, MasterCategory.NONE)
      .check();
  }

  public void testBankAreCorrectlyReImported() throws Exception {
    final String filePath = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .addTransaction("2006/01/11", -12.0, "Cheque 12345")
      .save();

    createUser("toto", "p4ssw0rd", filePath);
    checkBankOnImport();
    openNewLoginWindow();
    login("toto", "p4ssw0rd");
    checkBankOnImport();
  }

  private void checkBankOnImport() {
    OperationChecker operations = new OperationChecker(window);
    Trigger trigger = operations.getImportTrigger();
    WindowInterceptor.init(trigger)
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          assertTrue(window.getComboBox("bankCombo")
            .contentEquals("", "Autre", "CIC", "Credit Agricole", "La Poste", "Societe Generale"));
          return window.getButton("close").triggerClick();
        }
      }).run();
  }

  public void testLoginFailsIfUserNotRegistered() throws Exception {
    enterUserPassword("toto", "titi", false);
    checkNoErrorDisplayed();
    loginButton.click();
    checkErrorMessage("login.invalid.credentials");
  }

  public void testCannotUseTheSameLoginTwice() throws Exception {
    String path = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();

    createUser("toto", "p4ssw0rd", path);

    openNewLoginWindow();
    createUserCheckbox.select();
    enterUserPassword("toto", "an0th3rPwd", true);
    loginButton.click();

    checkErrorMessage("login.user.exists");
  }

  public void testUserAndPasswordRules() throws Exception {
    createUserCheckbox.select();

    loginButton.click();
    checkErrorMessage("login.user.required");

    userField.setText("t");
    loginButton.click();
    checkErrorMessage("login.user.too.short");

    userField.setText("toto");
    loginButton.click();
    checkErrorMessage("login.password.required");

    passwordField.setPassword("pwd");
    loginButton.click();
    checkErrorMessage("login.password.too.short");

    passwordField.setPassword("password");
    loginButton.click();
    checkErrorMessage("login.password.special.chars");
  }

  public void testPasswordMustBeConfirmedWhenCreatingAnAccount() throws Exception {
    enterUserPassword("toto", "p4ssw0rd", false);
    checkConfirmPasswordVisible(false);

    createUserCheckbox.select();
    checkConfirmPasswordVisible(true);

    loginButton.click();
    checkErrorMessage("login.confirm.required");

    setConfirmPassword("somethingElse");
    loginButton.click();
    checkErrorMessage("login.confirm.error");
  }

  public void testDataFileMustBeSelectedWhenCreatingAnAccount() throws Exception {
    createNewUser();

    ComboBox bankCombo = window.getComboBox("bankCombo");
    bankCombo.select("CIC");

    assertNotNull(window.getTextBox("http://www.cic.fr/telechargements.cgi"));

    TextBox fileField = window.getInputTextBox("fileField");
    Button importButton = window.getButton("Import");

    final String path = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();
    WindowInterceptor.init(window.getButton("Browse").triggerClick())
      .process(FileChooserHandler.init().select(new String[]{path}))
      .run();

    assertTrue(fileField.textEquals(path));
    importButton.click();

    Table table = window.getTable();
    assertTrue(table.contentEquals(new Object[][]{
      {"10/01/2006", "Menu K", "-1.10"}
    }));

    window.getButton("OK").click();

    getTransactionChecker()
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();
  }

  public void testImportSeveralFiles() throws Exception {
    final String path1 = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();
    final String path2 = OfxBuilder
      .init(this)
      .addTransaction("2006/01/20", -2.2, "Menu K")
      .save();
    createNewUser();
    window.getInputTextBox("fileField").setText(path1 + ";" + path2);
    window.getButton("Import").click();
    window.getButton("OK").click();
    window.getButton("OK").click();
    getTransactionChecker()
      .initContent()
      .add("20/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -2.2)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();
  }

  private void createNewUser() {
    createUserCheckbox.select();
    enterUserPassword("toto", "p4ssw0rd", true);
    loginButton.click();
  }

  public void testTransactionAndCategorisationWorkAfterReload() throws Exception {
    String path = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();
    createUser("toto", "p4ssw0rd", path);
    getTransactionChecker()
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();

    TransactionChecker checker = getTransactionChecker();
    checker.assignCategory(MasterCategory.FOOD, 0);

    openNewLoginWindow();
    enterUserPassword("toto", "p4ssw0rd", false);
    loginButton.click();
    OfxBuilder
      .init(this, new OperationChecker(window))
      .addTransaction("2006/01/12", -2, "Menu K")
      .load();
    getTransactionChecker()
      .initContent()
      .add("12/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -2, MasterCategory.FOOD)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1, MasterCategory.FOOD)
      .check();
  }

  private void login(String user, String password) {
    enterUserPassword(user, password, false);
    loginButton.click();
  }

  private void createUser(String user, String password, String filePath) {
    createUserCheckbox.select();
    enterUserPassword(user, password, true);
    loginButton.click();

    window.getInputTextBox("fileField").setText(filePath);
    TextBox messageBox = window.getTextBox("message");
    window.getButton("Import").click();
    assertTrue(messageBox.textIsEmpty());
    window.getButton("OK").click();
  }

  private void enterUserPassword(String user, String password, boolean confirm) {
    userField.setText(user);
    passwordField.setPassword(password);
    if (confirm) {
      setConfirmPassword(password);
    }
  }

  private void checkNoErrorDisplayed() {
    assertTrue(window.getTextBox("message").textIsEmpty());
  }

  private void checkErrorMessage(String message) {
    assertTrue(window.getTextBox("message").textContains(Lang.get(message)));
  }

  private void setConfirmPassword(String text) {
    getConfirmPassword().setPassword(text);
  }

  private PasswordField getConfirmPassword() {
    return window.getPasswordField("confirmPassword");
  }

  private void checkConfirmPasswordVisible(boolean visible) {
    if (visible) {
      assertNotNull(getConfirmPassword());
    }
    else {
      window.containsUIComponent(TextBox.class, "confirmPassword");
    }
  }

  private TransactionChecker getTransactionChecker() {
    return new TransactionChecker(window);
  }
}