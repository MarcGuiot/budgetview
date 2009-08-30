package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.*;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.startup.SingleApplicationInstanceListener;
import org.designup.picsou.model.TransactionType;
import org.uispec4j.ToggleButton;
import org.uispec4j.Trigger;
import org.uispec4j.UISpecAdapter;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

public class LoginTest extends StartUpFunctionalTestCase {

  private Window window;
  private PicsouApplication picsouApplication;
  private LoginChecker login;
  private OperationChecker operationChecker;

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
    login = null;
    picsouApplication.shutdown();
    picsouApplication = null;
  }

  private void openNewLoginWindow() throws Exception {
    closeWindow();
    window = getMainWindow();
    login = new LoginChecker(window);
  }

  private void closeWindow() {
    if (window != null) {
      window.getAwtComponent().setVisible(false);
      window.dispose();
      picsouApplication.shutdown();
    }
  }

  public void testCreatingAUserAndLoggingInAgain() throws Exception {
    String filePath = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .addTransaction("2006/01/11", -12.0, "Cheque 12345")
      .save();

    login.logNewUser("toto", "p4ssw0rd");
    OperationChecker.init(window).importOfxFile(filePath);
    getTransactionView()
      .initContent()
      .add("11/01/2006", TransactionType.CHECK, "CHEQUE N°12345", "", -12.00)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();

    openNewLoginWindow();
    login.logExistingUser("toto", "p4ssw0rd");
    getTransactionView()
      .initContent()
      .add("11/01/2006", TransactionType.CHECK, "CHEQUE N°12345", "", -12.00)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();
  }

  public void testBanksAreCorrectlyReImported() throws Exception {
    final String filePath = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .addTransaction("2006/01/11", -12.0, "Cheque 12345")
      .save();

    login.logNewUser("toto", "p4ssw0rd");
    OperationChecker.init(window).importOfxFile(filePath);
    checkBankOnImport(filePath);

    openNewLoginWindow();
    login.logExistingUser("toto", "p4ssw0rd");
    checkBankOnImport(filePath);
  }

  public void testLoginFailsIfUserNotRegistered() throws Exception {
    login.enterUserAndPassword("toto", "titi");
    login.checkNoErrorDisplayed();
    login.clickEnter();
    login.checkErrorMessage("login.invalid.credentials");
  }

  public void testCannotUseTheSameLoginTwice() throws Exception {
    String path = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();

    login.logNewUser("toto", "p4ssw0rd");
    OperationChecker.init(window).importOfxFile(path);

    openNewLoginWindow();

    login.enterUserName("toto")
      .setCreation()
      .enterPassword("an0th3rPwd")
      .confirmPassword("an0th3rPwd")
      .loginAndSkipSla();

    login.checkErrorMessage("login.user.exists");
  }

  public void testUserAndPasswordRules() throws Exception {
    login.setCreation();

    login.clickEnter()
      .checkErrorMessage("login.user.required");

    login.enterUserName("t")
      .checkNoErrorDisplayed()
      .clickEnter()
      .checkErrorMessage("login.user.too.short");

    login.enterUserName("toto")
      .checkNoErrorDisplayed()
      .clickEnter()
      .checkErrorMessage("login.password.required");

    login.enterPassword("pwd")
      .checkNoErrorDisplayed()
      .clickEnter()
      .checkErrorMessage("login.password.too.short");
  }

  public void testPasswordMustBeConfirmedWhenCreatingAnAccount() throws Exception {
    login.enterUserAndPassword("toto", "p4ssw0rd");
    login.checkConfirmPasswordVisible(false);

    login.setCreation();
    login.checkConfirmPasswordVisible(true);

    login.clickEnter();
    login.checkErrorMessage("login.confirm.required");

    login.confirmPassword("somethingElse");
    login.clickEnter();
    login.checkErrorMessage("login.confirm.error");
  }

  public void testValidatingTheLicenseAgreement() throws Exception {
    login.enterUserAndPassword("toto", "p4assw0rd")
      .setCreation()
      .confirmPassword("p4assw0rd");

    login.clickEnterAndGetSlaDialog()
      .checkTitle("End-User License Agreement")
      .cancel();

    login.checkComponentsVisible();
    login.checkNotLoggedIn();
    login.clickEnterAndGetSlaDialog()
      .checkTitle("End-User License Agreement")
      .checkNoErrorMessage()
      .checkValidationFailed()
      .checkErrorMessage("You must agree with these terms")
      .acceptTerms()
      .checkNoErrorMessage()
      .validate();

    login.checkLoggedIn();
  }

  public void testTransactionAndCategorisationWorkAfterReload() throws Exception {
    String path = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();

    login.logNewUser("toto", "p4ssw0rd");
    OperationChecker.init(window).importOfxFile(path);
    getTransactionView()
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();

    getCategorizationView().setNewEnvelope("Menu K", "Food");

    openNewLoginWindow();
    login.logExistingUser("toto", "p4ssw0rd");

    login.checkLoggedIn();
    OfxBuilder
      .init(this, new OperationChecker(window))
      .addTransaction("2006/01/12", -2, "Menu A")
      .load();

    getTransactionView().initContent()
      .add("12/01/2006", TransactionType.PRELEVEMENT, "Menu A", "", -2)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1, "Food")
      .check();

    getCategorizationView().setEnvelope("Menu A", "Food");

    getTransactionView().initContent()
      .add("12/01/2006", TransactionType.PRELEVEMENT, "Menu A", "", -2, "Food")
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1, "Food")
      .check();
  }

  public void testLoginInDemoMode() throws Exception {
    login.clickDemoLink();
    checkDemoMode();
  }

  public void testActivatingTheDemoModeSkipsAccountCreation() throws Exception {
    login.enterUserAndPassword("username", "pwd")
      .setCreation()
      .clickDemoLink();
    checkDemoMode();
  }

  public void testCanNotImportInDemoMode() throws Exception {
    login.clickDemoLink();
    OperationChecker operations = new OperationChecker(window);
    MessageDialogChecker dialogChecker = MessageDialogChecker.init(operations.getImportTrigger());
    dialogChecker.checkMessageContains("You cannot import operations in demo");
    dialogChecker.close();
  }

  public void testCanNotCreateOperationInDemoMode() throws Exception {
    login.clickDemoLink();
    MainAccountViewChecker mainAccounts = new MainAccountViewChecker(window);
    mainAccounts.createNewAccount()
      .setAccountName("Cash")
      .setAccountNumber("012345")
      .setUpdateModeToManualInput()
      .selectBank("CIC")
      .validate();

    ViewSelectionChecker views = new ViewSelectionChecker(window);

    views.selectCategorization();
    TransactionCreationChecker transactionCreation = new TransactionCreationChecker(window);
    transactionCreation
      .checkDemoMessage();
  }

  public void testLicenseInfoInDemoMode() throws Exception {
    login.clickDemoLink();

    LicenseMessageChecker messageChecker = new LicenseMessageChecker(window);
    messageChecker.checkMessage("Demo account");
  }

  public void testAutolog() throws Exception {
    login.clickFirstAutologin();
    String path = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();
    operationChecker = new OperationChecker(window);
    operationChecker.importOfxFile(path);
    operationChecker.logout();
    login.clickAutologgin();
    operationChecker.logout();
    closeWindow();
    window = getMainWindow();
    getTransactionView()
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();
  }

  public void testAutoLogAndImportInNewUser() throws Exception {
    login.clickFirstAutologin();

    operationChecker = new OperationChecker(window);
    String path = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();

    operationChecker.importOfxFile(path);
    String fileName = operationChecker.backup(this);
    operationChecker.deleteAutologUser();
    login.logNewUser("Alfred", "Alfred");
    operationChecker.restore(fileName);
    getTransactionView()
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();
    operationChecker.logout();
    login.clickFirstAutologin();
    TimeViewChecker timeViewChecker = new TimeViewChecker(window);
    timeViewChecker.selectAll();
    getTransactionView()
      .initContent()
      .check();
  }

  public void testLoginWithPwdAndAutolog() throws Exception {
    login.clickFirstAutologin();
    operationChecker = new OperationChecker(window);
    operationChecker.logout();
    login.logNewUser("Alfred", "Alfred");
    operationChecker.logout();
    openNewLoginWindow();
    login.clickAutologgin();
  }

  private void checkDemoMode() {
    login.checkLoggedIn();
    getTransactionView().checkNotEmpty();
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
            .contentEquals("(Select a bank)", "Autre", "AXA Banque", "Banque Populaire", "BNP Paribas",
                           "Caisse d'épargne", "CIC",
                           "Crédit Agricole", "Crédit Mutuel", "ING Direct", "La Poste", "LCL",
                           "Société Générale"));
          return window.getButton("Skip file").triggerClick();
        }
      }).run();
  }

}
