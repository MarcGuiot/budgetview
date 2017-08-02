package com.budgetview.server.cloud.functests;

import com.budgetview.model.CloudDesktopUser;
import com.budgetview.model.TransactionType;
import com.budgetview.server.cloud.functests.testcases.CloudDesktopTestCase;
import com.budgetview.server.cloud.stub.BudgeaStatement;
import com.budgetview.shared.cloud.budgea.BudgeaCategory;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.utils.Dates;
import org.junit.Test;

public class CloudRestartTest extends CloudDesktopTestCase {

  public void setUp() throws Exception {
    resetWindow();
    setInMemory(true);
    setDeleteLocalPrevayler(true);
    super.setUp();
    setDeleteLocalPrevayler(false);
  }

  public void restart() throws Exception {
    setNotRegistered();
    restartApplication(true);
  }

  protected void tearDown() throws Exception {
    resetWindow();
    super.tearDown();
  }

  @Test
  public void testConnectToAccountCreatedWithAnotherComputer() throws Exception {

    cloud.createSubscription("toto@example.com", Dates.tomorrow());

    budgea.pushConnectionResponse(1, 123, 40);
    budgea.pushStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de test", "2016-08-10 17:44:26")
                           .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-10 13:00:00")
                           .addTransaction(1, "2016-08-10 13:00:00", -100.00, "AUCHAN")
                           .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
                           .endAccount()
                           .endConnection()
                           .get());

    // -- Init --

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
      .importAccountAndComplete();

    transactions.initContent()
      .add("12/08/2016", TransactionType.PRELEVEMENT, "EDF", "", -50.00, "Electricity")
      .add("10/08/2016", TransactionType.PRELEVEMENT, "AUCHAN", "", -100.00)
      .check();

    budgea.callWebhook(BudgeaStatement.init()
                         .addConnection(1, 123, 40, "Connecteur de test", "2016-08-15 17:44:26")
                         .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-15 13:00:00")
                         .addTransaction(3, "2016-08-13 13:00:00", -25.00, "TOTAL", BudgeaCategory.ESSENCE)
                         .addTransaction(4, "2016-08-15 15:00:00", -50.00, "FOUQUETS", BudgeaCategory.RESTAURANT)
                         .endAccount()
                         .endConnection()
                         .get());

    String backup = operations.backup(this);

    // -- Restart --

    restart();

    transactions.checkEmpty();

    operations.openImportDialog()
      .checkCloudRefreshNotVisible()
      .selectCloudForNewUser()
      .register("toto@example.com")
      .processEmailAndNextToDownload(mailbox.getDeviceVerificationCode("toto@example.com"))
      .checkTransactions(new Object[][]{
        {"2016/08/15", "FOUQUETS", "-50.00"},
        {"2016/08/13", "TOTAL", "-25.00"},
        {"2016/08/12", "EDF", "-50.00"},
        {"2016/08/10", "AUCHAN", "-100.00"},
      })
      .importAccountAndComplete();

    transactions.initContent()
      .add("15/08/2016", TransactionType.PRELEVEMENT, "FOUQUETS", "", -50.00, "Restaurant")
      .add("13/08/2016", TransactionType.PRELEVEMENT, "TOTAL", "", -25.00, "Fuel")
      .add("12/08/2016", TransactionType.PRELEVEMENT, "EDF", "", -50.00, "Electricity")
      .add("10/08/2016", TransactionType.PRELEVEMENT, "AUCHAN", "", -100.00)
      .check();

    // -- Restore --

    operations.restore(backup);

    transactions.initContent()
      .add("12/08/2016", TransactionType.PRELEVEMENT, "EDF", "", -50.00, "Electricity")
      .add("10/08/2016", TransactionType.PRELEVEMENT, "AUCHAN", "", -100.00)
      .check();

    operations.openImportDialog()
      .checkCloudRefreshNotVisible()
      .selectCloudForNewUser()
      .register("toto@example.com")
      .processEmailAndNextToDownload(mailbox.getDeviceVerificationCode("toto@example.com"))
      .checkTransactions(new Object[][]{
        {"2016/08/15", "FOUQUETS", "-50.00"},
        {"2016/08/13", "TOTAL", "-25.00"},
      })
      .importAccountAndComplete();

    transactions.initContent()
      .add("15/08/2016", TransactionType.PRELEVEMENT, "FOUQUETS", "", -50.00, "Restaurant")
      .add("13/08/2016", TransactionType.PRELEVEMENT, "TOTAL", "", -25.00, "Fuel")
      .add("12/08/2016", TransactionType.PRELEVEMENT, "EDF", "", -50.00, "Electricity")
      .add("10/08/2016", TransactionType.PRELEVEMENT, "AUCHAN", "", -100.00)
      .check();
  }
}
