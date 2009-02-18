package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.*;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.startup.SingleApplicationInstanceListener;
import org.designup.picsou.model.MasterCategory;
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
    if (window != null) {
      window.getAwtComponent().setVisible(false);
      window.dispose();
      picsouApplication.shutdown();
    }
    window = getMainWindow();
    login = new LoginChecker(window);
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
      .add("11/01/2006", TransactionType.CHECK, "CHEQUE N. 12345", "", -12.00, MasterCategory.NONE)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1, MasterCategory.NONE)
      .check();

    openNewLoginWindow();
    login.logExistingUser("toto", "p4ssw0rd");
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
    assertFalse(window.containsMenuBar());

    login.clickEnterAndGetSlaDialog()
      .checkTitle("End-User License Agreement")
      .checkNoErrorMessage()
      .checkValidationFailed()
      .checkErrorMessage("You must agree with these terms")
      .acceptTerms()
      .checkNoErrorMessage()
      .validate();

    assertTrue(window.containsMenuBar());
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

    getCategorizationView().setOccasional("Menu K", MasterCategory.FOOD);

    openNewLoginWindow();
    login.logExistingUser("toto", "p4ssw0rd");

    assertThat(window.containsMenuBar());
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
            .contentEquals("(Select a bank)", "Autre", "AXA Banque", "Banque Populaire", "BNP",
                           "Caisse d'épargne", "CIC",
                           "Crédit Agricole", "Crédit Mutuel", "ING Direct", "La Poste", "LCL",
                           "Société Générale"));
          return window.getButton("Skip file").triggerClick();
        }
      }).run();
  }
}
