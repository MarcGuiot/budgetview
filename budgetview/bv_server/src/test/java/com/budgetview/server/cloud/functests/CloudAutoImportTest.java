package com.budgetview.server.cloud.functests;

import com.budgetview.server.cloud.functests.testcases.CloudDesktopTestCase;
import com.budgetview.server.cloud.stub.BudgeaStatement;
import com.budgetview.shared.cloud.budgea.BudgeaCategory;
import org.globsframework.utils.Dates;

public class CloudAutoImportTest extends CloudDesktopTestCase {

  public void setUp() throws Exception {
    resetWindow();
    setInMemory(false);
    setDeleteLocalPrevayler(true);
    super.setUp();
    setDeleteLocalPrevayler(false);
  }

  protected void tearDown() throws Exception {
    resetWindow();
    super.tearDown();
  }

  public void testAutoImportOnStartup() throws Exception {

    cloud.createSubscription("toto@example.com", Dates.tomorrow());
    budgea.pushNewConnectionResponse(1, 123, 40);
    budgea.pushStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de test", "2016-08-10 17:44:26")
                           .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-10 13:00:00")
                           .addTransaction(1, "2016-08-10 13:00:00", -100.00, "AUCHAN")
                           .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF")
                           .endAccount()
                           .addAccount(2, "Main account 2", "200300400", "checking", 2000.00, "2016-08-10 11:00:00")
                           .addTransaction(3, "2016-08-11 11:00:00", -200.00, "FNAC")
                           .endAccount()
                           .endConnection()
                           .get());

    operations.openImportDialog()
      .selectCloudForNewUser()
      .register("toto@example.com")
      .processEmailAndNextToBankSelection(mailbox.getDeviceVerificationCode("toto@example.com"))
      .selectBank("Connecteur de test")
      .next()
      .setChoice("Type de compte", "Particuliers")
      .setText("Identifiant", "1234")
      .setPassword("Code (1234)", "1234")
      .next()
      .waitForNotificationAndDownload(mailbox.checkStatementReady("toto@example.com"))
      .checkTransactions(new Object[][]{
        {"2016/08/12", "EDF", "-50.00"},
        {"2016/08/10", "AUCHAN", "-100.00"},
      })
      .importAccountAndOpenNext()
      .checkTransactions(new Object[][]{
        {"2016/08/11", "FNAC", "-200.00"},
      })
      .importAccountAndComplete();

    budgea.sendStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de test", "2016-08-15 17:44:26")
                           .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-15 13:00:00")
                           .addTransaction(4, "2016-08-13 13:00:00", -25.00, "TOTAL", BudgeaCategory.ESSENCE)
                           .addTransaction(5, "2016-08-15 15:00:00", -50.00, "FOUQUETS", BudgeaCategory.RESTAURANT)
                           .endAccount()
                           .addAccount(2, "Main account 2", "200300400", "checking", 1500.00, "2016-08-15 13:00:00")
                           .addTransaction(6, "2016-08-13 14:00:00", -500.00, "APPLE")
                           .endAccount()
                           .endConnection()
                           .get());

    restartApplication();

    autoImport.checkDisplayed()
    .waitForEndOfProgress()
    .performAction("Categorize operations");

    views.checkCategorizationSelected();

    categorization.checkShowsAllTransactions();
    categorization.initContent()
      .add("10/08/2016", "", "AUCHAN", -100.00)
      .add("12/08/2016", "", "EDF", -50.00)
      .add("11/08/2016", "", "FNAC", -200.00)
      .check();
  }

  public void testCancelAfterImport() throws Exception {
    fail("");
  }

  public void testNoNewOperations() throws Exception {
    fail("on envoie vers le dashboard");
  }

  public void testNewAccountNeedsManualImport() throws Exception {
  }

  public void testSubscriptionExpired() throws Exception {
    fail("on envoie vers le site");
  }

  public void testServerError() throws Exception {
    fail("on ouvre une fenetre pour nous envoyer l'exception");
  }
}
