package com.budgetview.functests.useraccounts;

import com.budgetview.functests.checkers.*;
import com.budgetview.functests.checkers.components.TimeViewChecker;
import com.budgetview.functests.checkers.license.LicenseActivationChecker;
import com.budgetview.functests.checkers.license.LicenseChecker;
import com.budgetview.functests.utils.OfxBuilder;
import com.budgetview.functests.utils.StartUpFunctionalTestCase;
import com.budgetview.desktop.Application;
import com.budgetview.desktop.startup.components.SingleApplicationInstanceListener;
import com.budgetview.model.TransactionType;
import com.budgetview.model.initial.DefaultSeriesFactory;
import org.junit.Test;
import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;

public class LoginTest extends StartUpFunctionalTestCase {

  private Window window;
  private LoginChecker login;
  private OperationChecker operations;
  private boolean firstLogin;
  private ApplicationChecker application;

  protected void setUp() throws Exception {
    super.setUp();

    System.setProperty(Application.DEFAULT_ADDRESS_PROPERTY, "");
    System.setProperty(Application.DELETE_LOCAL_PREVAYLER_PROPERTY, "");
    System.setProperty(Application.IS_DATA_IN_MEMORY, "");
    System.setProperty(Application.LOG_TO_SOUT, "true");
    System.setProperty(SingleApplicationInstanceListener.SINGLE_INSTANCE_DISABLED, "true");

    application = new ApplicationChecker();
    setAdapter(new UISpecAdapter() {
      public Window getMainWindow() {
        if (firstLogin) {
          return application.start();
        }
        else {
          return application.startWithoutSLA();
        }
      }
    });

    openNewLoginWindow(true);
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    window.getAwtComponent().setVisible(false);
    window.dispose();
    window = null;
    login = null;
    application.dispose();
    application = null;
    operations = null;
  }

  private void openNewLoginWindow(final boolean firstLogin) throws Exception {
    this.firstLogin = firstLogin;
    closeWindow();
    window = getMainWindow();

    if (firstLogin) {
      new OperationChecker(window).logout();
    }
    login = new LoginChecker(window);
  }

  private void closeWindow() {
    if (window != null) {
      new OperationChecker(window).exit();
    }
  }

  private void requestExit() {
    if (window != null) {
      new OperationChecker(window).requestExit();
    }
  }

  @Test
  public void testCreatingAUserAndLoggingInAgain() throws Exception {
    String filePath = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .addTransaction("2006/01/11", -12.0, "Cheque 12345")
      .save();

    login.logNewUser("toto", "p4ssw0rd");
    OperationChecker operations = OperationChecker.init(window);
    operations.importOfxFile(filePath);
    operations.checkSetPasswordIsChange();
    getTransactionView()
      .initContent()
      .add("11/01/2006", TransactionType.CHECK, "CHEQUE N°12345", "", -12.00)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();

    openNewLoginWindow(false);
    login.logExistingUser("toto", "p4ssw0rd");
    getTransactionView()
      .initContent()
      .add("11/01/2006", TransactionType.CHECK, "CHEQUE N°12345", "", -12.00)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();
  }

  @Test
  public void testSelectingTheUserInAList() throws Exception {
    String filePath = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .addTransaction("2006/01/11", -12.0, "Cheque 12345")
      .save();

    login.checkUserSelectionHidden();
    login.logNewUser("toto", "p4ssw0rd");
    OperationChecker operations = OperationChecker.init(window);
    operations.importOfxFile(filePath);

    openNewLoginWindow(false);

    login.checkUserName("");
    login.checkUserSelectionAvailable();
    login.openUserSelection()
      .checkNames("toto")
      .checkSelected("toto")
      .validate();
    login.checkUserName("toto");

    login.logNewUser("titi", "p4ssw0rd");
    openNewLoginWindow(false);

    login.checkUserName("");
    login.checkUserSelectionAvailable();
    login.openUserSelection()
      .checkNames("titi", "toto")
      .checkSelected("titi")
      .select("toto")
      .validate();
    login.checkUserName("toto")
      .enterPassword("p4ssw0rd")
      .clickEnter();

    getTransactionView()
      .initContent()
      .add("11/01/2006", TransactionType.CHECK, "CHEQUE N°12345", "", -12.00)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1)
      .check();
  }

  @Test
  public void testCreatingAUserWithDefaultUserSeriesModeActivated() throws Exception {
    DefaultSeriesFactory.AUTO_CREATE_DEFAULT_SERIES = true;
    try {
      login.logNewUser("toto", "p4ssw0rd");

      {
        BudgetViewChecker budgetView = new BudgetViewChecker(window);
        budgetView.income.checkSeriesPresent("Income 1", "Income 2");
        budgetView.variable.checkSeriesPresent("Groceries", "Health", "Fuel");
        budgetView.variable.checkPlannedUnset("Groceries");

        operations = new OperationChecker(window);
        String filePath = OfxBuilder
          .init(this)
          .addTransaction("2006/01/10", 1300, "WorldCo")
          .addTransaction("2006/01/11", -12.0, "Cheque 12345")
          .save();
        operations.importOfxFile(filePath);

        TimeViewChecker timeView = new TimeViewChecker(window);
        timeView.selectMonth("2006/01");
        budgetView.variable.checkPlannedUnset("Groceries");
        operations.logout();
      }

      login.logExistingUser("toto", "p4ssw0rd");

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

  @Test
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
    login.logExistingUser("toto", "p4ssw0rd");
    checkBankOnImport(filePath);
  }

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
  public void testDoubleExitProtection() throws Exception {
    login.logNewUser("toto", "p4ssw0rd");

    requestExit();
    requestExit();

    UISpecAssert.assertFalse(window.isVisible());
  }

  @Test
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

    getCategorizationView().setVariable("Menu A", "Food");

    getTransactionView().initContent()
      .add("12/01/2006", TransactionType.PRELEVEMENT, "Menu A", "", -2, "Food")
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1, "Food")
      .check();
  }

  @Test
  public void testLoginInDemoMode() throws Exception {
    login.clickDemoLink();
    getLicense().checkInfoMessageHidden();
    checkDemoMode().exit();
    login.checkComponentsVisible();
  }

  @Test
  public void testActivatingTheDemoModeSkipsAccountCreation() throws Exception {
    login.enterUserAndPassword("username", "pwd")
      .setCreation()
      .clickDemoLink();
    checkDemoMode();
  }

  @Test
  public void testCannotImportInDemoMode() throws Exception {
    login.clickDemoLink();
    OperationChecker operations = new OperationChecker(window);
    MessageDialogChecker dialogChecker = MessageDialogChecker.open(operations.getImportTrigger());
    dialogChecker.checkInfoMessageContains("You cannot import operations in the demo account");
    dialogChecker.close();
  }

  @Test
  public void testCannotCreateOperationsInDemoMode() throws Exception {
    login.clickDemoLink();

    ViewSelectionChecker views = new ViewSelectionChecker(window);
    views.selectCategorization();

    TransactionCreationChecker transactionCreation = new TransactionCreationChecker(window);
    transactionCreation.checkDemoMessage();
  }

  @Test
  public void testAutoLogin() throws Exception {
    login.clickAutoLogin();
    String path = OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .save();
    operations = new OperationChecker(window);
    operations.checkSetPasswordIsProtect();
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

  @Test
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
    operations.hideSignposts();
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

  @Test
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
          checker.checkErrorMessageContains("Bad password");
          return checker.triggerClose();
        }
      })
      .run();
    UISpecAssert.waitUntil(window.containsSwingComponent(JPasswordField.class, "password"), 2000);
  }

  @Test
  public void testLoginWithPasswordAndAutologin() throws Exception {
    login.clickAutoLogin();
    operations = new OperationChecker(window);
    operations.logout();
    login.logNewUser("Alfred", "Alfred");
    openNewLoginWindow(false);
    login.clickAutoLogin();
  }

  @Test
  public void testLoginWithBadPasswdAndAutoLog() throws Exception {
    login.logNewUser("Alfred", "Alfred");
    openNewLoginWindow(false);
    login.enterUserAndPassword("Alfred", "toto")
      .clickEnter()
      .checkNotLoggedIn()
      .checkErrorMessage("login.invalid.credentials");
    login.clickAutoLogin();
  }

  private DemoMessageChecker checkDemoMode() {
    login.checkLoggedIn();
    getTransactionView().checkNotEmpty();
    DemoMessageChecker demo = new DemoMessageChecker(window);
    demo.checkVisible();
    return demo;
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

  private LicenseChecker getLicense() {
    ViewSelectionChecker views = new ViewSelectionChecker(window);
    views.selectHome();
    return new LicenseChecker(window);
  }

  private void checkBankOnImport(final String path) {
    UISpecAssert.waitUntil(window.containsMenuBar(), 2000);
    OperationChecker operations = new OperationChecker(window);
    Trigger trigger = operations.getImportTrigger();
    WindowInterceptor.init(trigger)
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          ImportDialogChecker importDialog = new ImportDialogChecker(window);

          importDialog.setFilePath(path)
            .importFileAndPreview();

          window.getButton("Skip file").click();
          return window.getButton("OK").triggerClick();
        }
      })
      .run();
  }
}
