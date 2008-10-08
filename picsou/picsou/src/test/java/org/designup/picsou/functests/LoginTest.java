package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.CategorizationChecker;
import org.designup.picsou.functests.checkers.OperationChecker;
import org.designup.picsou.functests.checkers.TransactionChecker;
import org.designup.picsou.functests.checkers.ViewSelectionChecker;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.startup.SingleApplicationInstanceListener;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.utils.Lang;
import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

public class LoginTest extends StartUpFunctionalTestCase {

  private Window window;
  private TextBox userField;
  private PasswordField passwordField;
  private CheckBox createUserCheckbox;
  private Button loginButton;
  private PicsouApplication picsouApplication;

  protected void setUp() throws Exception {
    super.setUp();

    System.setProperty(PicsouApplication.DEFAULT_ADDRESS_PROPERTY, "");
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "");
    System.setProperty(PicsouApplication.IS_DATA_IN_MEMORY, "");
    System.setProperty(SingleApplicationInstanceListener.SINGLE_INSTANCE_DISABLED, "true");

    setAdapter(new UISpecAdapter() {
      public Window getMainWindow() {
        return WindowInterceptor.run(new Trigger() {
          public void run() throws Exception {
            picsouApplication = new PicsouApplication();
            picsouApplication.run();
          }
        });
      }
    });

    openNewLoginWindow();
  }

  protected void tearDown() throws Exception {
    window.getAwtComponent().setVisible(false);
    window.dispose();
    window = null;
    userField = null;
    passwordField = null;
    createUserCheckbox = null;
    loginButton = null;
    picsouApplication.shutdown();
    picsouApplication = null;
  }

  private void openNewLoginWindow() throws Exception {
    if (window != null) {
      window.getAwtComponent().setVisible(false);
      window.dispose();
      picsouApplication.shutdown();
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
    getTransactionView()
      .initContent()
      .add("11/01/2006", TransactionType.CHECK, "CHEQUE N. 12345", "", -12.00, MasterCategory.NONE)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1, MasterCategory.NONE)
      .check();

    openNewLoginWindow();
    login("toto", "p4ssw0rd");
    getTransactionView()
      .initContent()
      .add("11/01/2006", TransactionType.CHECK, "CHEQUE N. 12345", "", -12.00, MasterCategory.NONE)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1, MasterCategory.NONE)
      .check();
  }

  public void testBanksAreCorrectlyReImported() throws Exception {
    final String filePath = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .addTransaction("2006/01/11", -12.0, "Cheque 12345")
      .save();

    createUser("toto", "p4ssw0rd", filePath);
    checkBankOnImport(filePath);

    openNewLoginWindow();
    login("toto", "p4ssw0rd");
    checkBankOnImport(filePath);
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
    checkNoErrorDisplayed();
    loginButton.click();
    checkErrorMessage("login.user.too.short");

    userField.setText("toto");
    checkNoErrorDisplayed();
    loginButton.click();
    checkErrorMessage("login.password.required");

    passwordField.setPassword("pwd");
    checkNoErrorDisplayed();
    loginButton.click();
    checkErrorMessage("login.password.too.short");
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
    getTransactionView()
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();

    getCategorizationView().setOccasional("Menu K", MasterCategory.FOOD);

    openNewLoginWindow();
    enterUserPassword("toto", "p4ssw0rd", false);
    loginButton.click();
    
    UISpecAssert.assertTrue(window.containsMenuBar());
    OfxBuilder
      .init(this, new OperationChecker(window))
      .addTransaction("2006/01/12", -2, "Menu A")
      .load();

    getTransactionView().initContent()
      .add("12/01/2006", TransactionType.PRELEVEMENT, "Menu A", "", -2, MasterCategory.NONE)
      .addOccasional("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1, MasterCategory.FOOD)
      .check();

    getCategorizationView().setOccasional("Menu A", MasterCategory.FOOD);

    getTransactionView().initContent()
      .addOccasional("12/01/2006", TransactionType.PRELEVEMENT, "Menu A", "", -2, MasterCategory.FOOD)
      .addOccasional("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1, MasterCategory.FOOD)
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

    UISpecAssert.waitUntil(window.containsMenuBar(), 10000);

    OperationChecker operations = new OperationChecker(window);
    operations.importOfxFile(filePath);
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

  private TransactionChecker getTransactionView() {
    UISpecAssert.waitUntil(window.containsUIComponent(ToggleButton.class, "dataCardToggle"), 10000);
    ViewSelectionChecker views = new ViewSelectionChecker(window);
    views.selectData();
    return new TransactionChecker(window);
  }

  private CategorizationChecker getCategorizationView() {
    ViewSelectionChecker views = new ViewSelectionChecker(window);
    views.selectCategorization();
    return new CategorizationChecker(window);
  }

  private void checkBankOnImport(final String path) {
    UISpecAssert.waitUntil(window.containsMenuBar(), 2000);
    OperationChecker operations = new OperationChecker(window);
    Trigger trigger = operations.getImportTrigger();
    WindowInterceptor.init(trigger)
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          window.getInputTextBox("fileField").setText(path);
          window.getButton("Import").click();
          assertTrue(window.getComboBox("accountBank")
            .contentEquals("Autre", "BNP", "CIC", "Caisse d'épargne",
                           "Crédit Agricole", "Crédit Mutuel", "LCL", "La Poste",
                           "Société Générale"));
          return window.getButton("Skip file").triggerClick();
        }
      }).run();
  }
}
