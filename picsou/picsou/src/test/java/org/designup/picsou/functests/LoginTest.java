package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.*;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.startup.SingleApplicationInstanceListener;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.model.initial.DefaultSeriesFactory;
import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;

public class LoginTest extends StartUpFunctionalTestCase {

  private Window window;
  private PicsouApplication picsouApplication;
  private LoginChecker login;
  private OperationChecker operations;
  private boolean firstLogin;

  protected void setUp() throws Exception {
    super.setUp();

    System.setProperty(PicsouApplication.DEFAULT_ADDRESS_PROPERTY, "");
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "");
    System.setProperty(PicsouApplication.IS_DATA_IN_MEMORY, "");
    System.setProperty(PicsouApplication.LOG_SOUT, "true");
    System.setProperty(SingleApplicationInstanceListener.SINGLE_INSTANCE_DISABLED, "true");

    final StartupChecker startupChecker = new StartupChecker();
    setAdapter(new UISpecAdapter() {
      public Window getMainWindow() {
        if (firstLogin){
        return startupChecker.enterMain();
        }else {
          return WindowInterceptor.run(new Trigger() {
            public void run() throws Exception {
              picsouApplication = new PicsouApplication();
              picsouApplication.run();
            }
          });
        }
      }
    });

    openNewLoginWindow(true);
    if (firstLogin){
      picsouApplication = startupChecker.getApplication();
    }
  }

  protected void tearDown() throws Exception {
    window.getAwtComponent().setVisible(false);
    window.dispose();
    window = null;
    login = null;
    picsouApplication.shutdown();
    picsouApplication = null;
    operations = null;
  }

  private void openNewLoginWindow(final boolean firstLogin) throws Exception {
    this.firstLogin = firstLogin;
    closeWindow();
    window = getMainWindow();

    if (firstLogin){
      new OperationChecker(window).logout();
    }
    login = new LoginChecker(window);
  }

  private void closeWindow() {
    if (window != null) {
      new OperationChecker(window).exit();
    }
  }

  public void testCreatingAUserAndLoggingInAgain() throws Exception {
    String filePath = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .addTransaction("2006/01/11", -12.0, "Cheque 12345")
      .save();

    login.logNewUser("toto", "p4ssw0rd");
    OperationChecker operationChecker = OperationChecker.init(window);
    operationChecker.importOfxFile(filePath);
    operationChecker.checkChangeUserName();
    getTransactionView()
      .initContent()
      .add("11/01/2006", TransactionType.CHECK, "CHEQUE N°12345", "", -12.00)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();

    openNewLoginWindow(false);
    login.logExistingUser("toto", "p4ssw0rd", false);
    getTransactionView()
      .initContent()
      .add("11/01/2006", TransactionType.CHECK, "CHEQUE N°12345", "", -12.00)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();
  }

  public void testCreatingAUserWithDefaultUserSeriesModeActivated() throws Exception {
    DefaultSeriesFactory.AUTO_CREATE_DEFAULT_SERIES = true;
    try {
      login.logNewUser("toto", "p4ssw0rd");

      {
        BudgetViewChecker budgetView = new BudgetViewChecker(window);
        budgetView.income.checkSeriesPresent("Income 1", "Income 2");
        budgetView.variable.checkSeriesPresent("Groceries", "Health", "Fuel");
        budgetView.variable.checkPlannedUset("Groceries");

        operations = new OperationChecker(window);
        String filePath = OfxBuilder
          .init(this)
          .addTransaction("2006/01/10", 1300, "WorldCo")
          .addTransaction("2006/01/11", -12.0, "Cheque 12345")
          .save();
        operations.importOfxFile(filePath);

        operations.logout();
      }

      login.logExistingUser("toto", "p4ssw0rd", false);

      {
        CategorizationChecker categorization = new CategorizationChecker(window);
        categorization.selectTransaction("WorldCo");
        categorization.selectIncome().checkSeriesListEquals("Income 1", "Income 2");
      }
    }
    finally {
      DefaultSeriesFactory.AUTO_CREATE_DEFAULT_SERIES = false;
    }
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

    openNewLoginWindow(false);
    login.logExistingUser("toto", "p4ssw0rd", false);
    checkBankOnImport(filePath);
  }

  public void testLoginFailsIfUserNotRegistered() throws Exception {
    login.enterUserAndPassword("toto", "titi")
      .checkNoErrorDisplayed()
      .clickEnter()
      .checkErrorMessage("login.invalid.credentials");

    login.setCreation()
      .confirmPassword("titi")
      .checkNoErrorDisplayed()
      .loginAndSkipSla()
      .waitForApplicationToLoad();
  }

  public void testCannotUseTheSameLoginTwice() throws Exception {
    String path = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();

    login.logNewUser("toto", "p4ssw0rd");
    OperationChecker.init(window).importOfxFile(path);

    openNewLoginWindow(false);

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

    getCategorizationView().setNewVariable("Menu K", "Food");

    openNewLoginWindow(false);
    login.logExistingUser("toto", "p4ssw0rd", false);

    login.checkLoggedIn();
    OfxBuilder
      .init(this, new OperationChecker(window))
      .addTransaction("2006/01/12", -2, "Menu A")
      .load();

    getTransactionView().initContent()
      .add("12/01/2006", TransactionType.PRELEVEMENT, "Menu A", "", -2)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1, "Food")
      .check();

    getCategorizationView().setVariable("Menu A", "Food");

    getTransactionView().initContent()
      .add("12/01/2006", TransactionType.PRELEVEMENT, "Menu A", "", -2, "Food")
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1, "Food")
      .check();
  }

  public void testLoginInDemoMode() throws Exception {
    login.clickDemoLink();
    checkDemoMode();
    LicenseMessageChecker licenseMessage = getLicenseMessageView();
    licenseMessage.checkMessage("demo account");
    licenseMessage.clickLink("logout");
    login.checkComponentsVisible();
  }

  public void testActivatingTheDemoModeSkipsAccountCreation() throws Exception {
    login.enterUserAndPassword("username", "pwd")
      .setCreation()
      .clickDemoLink();
    checkDemoMode();
  }

  public void testCannotImportInDemoMode() throws Exception {
    login.clickDemoLink();
    OperationChecker operations = new OperationChecker(window);
    MessageDialogChecker dialogChecker = MessageDialogChecker.init(operations.getImportTrigger());
    dialogChecker.checkMessageContains("You cannot import operations in the demo account");
    dialogChecker.close();
  }

  public void testCannotCreateOperationsInDemoMode() throws Exception {
    login.clickDemoLink();
    AccountViewChecker mainAccounts = new MainAccountViewChecker(window);
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
    messageChecker.checkMessage("demo account");
  }

  public void testAutoLogin() throws Exception {
    login.clickAutoLogin();
    String path = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();
    operations = new OperationChecker(window);
    operations.checkProtect();
    operations.importOfxFile(path);
    operations.logout();
    firstLogin = false;
    login.clickAutoLogin();
    closeWindow();
    window = getMainWindow();
    getTransactionView()
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();
  }

  public void testAutoLoginAndImportInNewUser() throws Exception {
    login.clickAutoLogin();
    LicenseActivationChecker.enterLicense(window, "admin", "1234");

    operations = new OperationChecker(window);
    String path = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();

    operations.importOfxFile(path);
    String fileName = operations.backup(this);
    operations.deleteAutoLoginUser();
    login.logNewUser("Alfred", "Alfred");
    operations.restore(fileName);
    getTransactionView()
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();
    operations.logout();
    login.clickFirstAutoLogin();
    TimeViewChecker timeViewChecker = new TimeViewChecker(window);
    timeViewChecker.selectAll();
    getTransactionView()
      .initContent()
      .check();
    operations.deleteAutoLoginUser();
    login.clickFirstAutoLogin();
    operations.deleteAutoLoginUser();
    login.checkFirstAutoLogin();
  }

  public void testDeleteWithBadPwd() throws Exception {
    login.logNewUser("toto", "p4ssw0rd");
    operations = new OperationChecker(window);
    MenuItem subMenu = window.getMenuBar().getMenu("File").getSubMenu("Delete");
    PasswordDialogChecker dialogChecker =
      new PasswordDialogChecker(WindowInterceptor.getModalDialog(subMenu.triggerClick()));
    dialogChecker.setPassword("otherpwd");
    WindowInterceptor.init(dialogChecker.getOkTrigger())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          MessageDialogChecker checker = new MessageDialogChecker(window);
          checker.checkMessageContains("Bad password");
          return checker.triggerClose();
        }
      })
      .run();
    UISpecAssert.waitUntil(window.containsSwingComponent(JPasswordField.class, "password"), 2000);
  }

  public void testLoginWithPasswordAndAutologin() throws Exception {
    login.clickAutoLogin();
    operations = new OperationChecker(window);
    operations.logout();
    login.logNewUser("Alfred", "Alfred");
    openNewLoginWindow(false);
    login.clickAutoLogin();
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

  private LicenseMessageChecker getLicenseMessageView() {
    ViewSelectionChecker views = new ViewSelectionChecker(window);
    views.selectHome();
    return new LicenseMessageChecker(window);
  }

  private void checkBankOnImport(final String path) {
    UISpecAssert.waitUntil(window.containsMenuBar(), 2000);
    OperationChecker operations = new OperationChecker(window);
    Trigger trigger = operations.getImportTrigger();
    WindowInterceptor.init(trigger)
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          ImportDialogChecker importDialog = new ImportDialogChecker(window, true);

          importDialog.setFilePath(path)
            .doImport();

          return window.getButton("Skip file").triggerClick();
        }
      }).run();
  }
}
