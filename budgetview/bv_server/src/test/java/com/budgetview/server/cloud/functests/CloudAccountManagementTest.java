package com.budgetview.server.cloud.functests;

import com.budgetview.functests.checkers.CloudAccountsChecker;
import com.budgetview.model.TransactionType;
import com.budgetview.server.cloud.functests.testcases.CloudDesktopTestCase;
import com.budgetview.server.cloud.stub.BudgeaAccounts;
import com.budgetview.server.cloud.stub.BudgeaConnections;
import com.budgetview.server.cloud.stub.BudgeaStatement;
import com.budgetview.shared.cloud.budgea.BudgeaCategory;
import org.globsframework.utils.Dates;
import org.junit.Test;

public class CloudAccountManagementTest extends CloudDesktopTestCase {

  public void setUp() throws Exception {
    setCurrentDate("2016/08/20");
    super.setUp();
  }

  @Test
  public void testEmptyAccountsAreAutomaticallySkipped() throws Exception {
    cloud.createSubscription("toto@example.com", Dates.tomorrow());

    budgea.pushNewConnectionResponse(1, 123, 40);
    budgea.pushStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de test", "2016-08-10 17:44:26")
                           .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-10 13:00:00")
                           .addTransaction(1, "2016-08-10 13:00:00", -100.00, "AUCHAN")
                           .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
                           .endAccount()
                           .addAccount(2, "Main account 2", "200300400", "checking", 2000.00, "2016-08-10 11:00:00")
                           .endAccount()
                           .endConnection()
                           .get());

    operations.openImportDialog()
      .selectCloudForNewUser()
      .register("toto@example.com")
      .processEmailAndNextToBankSelection(mailbox.getDeviceVerificationCode("toto@example.com"))
      .checkNoBankSelected()
      .checkNextDisabled()
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
      .checkNewAccountSelected()
      .importAndPreviewNextAccount()
      .checkNoTransactions()
      .checkSkipFileSelected()
      .checkSkippedFileMessage()
      .importAccountAndComplete();

    mainAccounts.checkAccounts("Main account 1");

    transactions.initContent()
      .add("12/08/2016", TransactionType.PRELEVEMENT, "EDF", "", -50.00, "Electricity")
      .add("10/08/2016", TransactionType.PRELEVEMENT, "AUCHAN", "", -100.00)
      .check();

    budgea.checkAccountUpdates("account:2 => disabled:1");
    budgea.clearAccountUpdates();

    // --- Set "Main account 2" to enabled again : the server is updated ---

    budgea.pushConnectionList(BudgeaConnections.init()
                                .add(1, 123, 40, true, "2016-08-10 17:44:26")
                                .get());
    budgea.pushAccountList(BudgeaAccounts.init()
                             .add(1, 1, "Main account 1", "100200300", true)
                             .add(2, 1, "Main account 2", "200300400", false)
                             .get());
    CloudAccountsChecker dialog = operations.openImportDialog()
      .editCloudConnections()
      .editAccounts("Connecteur de test")
      .checkAccounts("Main account 1 / 100200300 / true",
                     "Main account 2 / 200300400 / false")
      .checkApplyDisabled()
      .enableAccount("Main account 2")
      .checkAccounts("Main account 1 / 100200300 / true",
                     "Main account 2 / 200300400 / true")
      .apply()
      .checkApplyMessage("Your accounts have been updated.");

    budgea.checkAccountUpdates("account:2 => disabled:null");

    budgea.pushConnectionList(BudgeaConnections.init()
                                .add(1, 123, 40, true, "2016-08-10 17:44:26")
                                .get());
    budgea.pushAccountList(BudgeaAccounts.init()
                             .add(1, 1, "Main account 1", "100200300", true)
                             .add(2, 1, "Main account 2 - renamed", "200300400", true)
                             .get());
    dialog
      .back()
      .editAccounts("Connecteur de test")
      .checkAccounts("Main account 1 / 100200300 / true",
                     "Main account 2 - renamed / 200300400 / true")
      .checkApplyDisabled()
      .close();

    // --- New download : Main account 2 is now shown ---

    budgea.sendStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de test", "2016-08-14 17:44:26")
                           .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-14 13:00:00")
                           .addTransaction(3, "2016-08-14 13:00:00", -100.00, "FNAC")
                           .endAccount()
                           .addAccount(2, "Main account 2", "200300400", "checking", 1850.00, "2016-08-13 12:00:00")
                           .addTransaction(4, "2016-08-13 11:00:00", -150.00, "DECATHLON")
                           .endAccount()
                           .endConnection()
                           .get());
    operations.openImportDialog()
      .selectCloudRefresh()
      .checkTransactions(new Object[][]{
        {"2016/08/14", "FNAC", "-100.00"}
      })
      .checkSelectedAccount("Main account 1")
      .importAccountAndOpenNext()
      .checkTransactions(new Object[][]{
        {"2016/08/13", "DECATHLON", "-150.00"}
      })
      .checkNewAccountSelected()
      .checkAccount("Main account 2")
      .importAccountAndComplete();

    mainAccounts.checkAccounts("Main account 1", "Main account 2");

    transactions.initAmountContent()
      .add("14/08/2016", "FNAC", -100.00, "To categorize", 900.00, 2750.00, "Main account 1")
      .add("13/08/2016", "DECATHLON", -150.00, "To categorize", 1850.00, 2850.00, "Main account 2")
      .add("12/08/2016", "EDF", -50.00, "Electricity", 1000.00, 3000.00, "Main account 1")
      .add("10/08/2016", "AUCHAN", -100.00, "To categorize", 1050.00, 3050.00, "Main account 1")
      .check();
  }

  @Test
  public void testDisablingAndReenablingAccountsFromBudgea() throws Exception {
    cloud.createSubscription("toto@example.com", Dates.tomorrow());

    budgea.pushNewConnectionResponse(1, 123, 40);
    budgea.pushStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de test", "2016-08-10 17:44:26")
                           .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-10 13:00:00")
                           .addTransaction(1, "2016-08-10 13:00:00", -100.00, "AUCHAN")
                           .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
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
      .checkNoBankSelected()
      .checkNextDisabled()
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
      .checkNewAccountSelected()
      .importAndPreviewNextAccount()
      .checkTransactions(new Object[][]{
        {"2016/08/11", "FNAC", "-200.00"},
      })
      .checkNewAccountSelected()
      .importAccountAndComplete();

    // --- Disable main account 2 from Budgea : it is not imported anymore but the account remains in the application ---

    budgea.sendStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de test", "2016-08-14 17:44:26")
                           .addAccount(1, "Main account 1", "100200300", "checking", 900.00, "2016-08-14 13:00:00")
                           .addTransaction(3, "2016-08-13 13:00:00", -100.00, "FNAC")
                           .endAccount()
                           .addAccount(2, "Main account 2", "200300400", "checking", 1950.00, "2016-08-13 12:00:00", "2016-08-12 11:30:00")
                           .endAccount()
                           .endConnection()
                           .get());

    operations.openImportDialog()
      .selectCloudRefresh()
      .checkTransactions(new Object[][]{
        {"2016/08/13", "FNAC", "-100.00"},
      })
      .importAccountAndComplete();

    mainAccounts.checkAccounts("Main account 1", "Main account 2");

    // --- Reenable main account 2 from Budgea : it is proposed in the next download ---

    budgea.sendStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de test", "2016-08-15 15:00:00")
                           .addAccount(1, "Main account 1", "100200300", "checking", 600.00, "2016-08-15 13:00:00")
                           .addTransaction(3, "2016-08-14 13:00:00", -300.00, "DARTY")
                           .endAccount()
                           .addAccount(2, "Main account 2", "200300400", "checking", 1800.00, "2016-08-15 12:00:00")
                           .addTransaction(4, "2016-08-13 13:00:00", -150.00, "DECATHLON")
                           .endAccount()
                           .endConnection()
                           .get());

    operations.openImportDialog()
      .selectCloudRefresh()
      .checkTransactions(new Object[][]{
        {"2016/08/14", "DARTY", "-300.00"},
      })
      .importAccountAndOpenNext()
      .checkTransactions(new Object[][]{
        {"2016/08/13", "DECATHLON", "-150.00"},
      })
      .importAccountAndComplete();

    mainAccounts.checkAccounts("Main account 1", "Main account 2");
  }

  @Test
  public void testAccountsLinkedToACloudDownloadAreAutomaticallyDisabledWhenDeleted() throws Exception {
    cloud.createSubscription("toto@example.com", Dates.tomorrow());

    budgea.pushNewConnectionResponse(1, 123, 40);
    budgea.pushStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de test", "2016-08-10 17:44:26")
                           .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-10 13:00:00")
                           .addTransaction(1, "2016-08-10 13:00:00", -100.00, "AUCHAN")
                           .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
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
      .checkNoBankSelected()
      .checkNextDisabled()
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
      .checkNewAccountSelected()
      .importAndPreviewNextAccount()
      .checkTransactions(new Object[][]{
        {"2016/08/11", "FNAC", "-200.00"},
      })
      .checkNewAccountSelected()
      .importAccountAndComplete();

    mainAccounts.checkAccounts("Main account 1", "Main account 2");

    mainAccounts.openDelete("Main account 1")
      .validate();

    budgea.checkAccountUpdates("account:1 => disabled:1");

    mainAccounts.checkAccounts("Main account 2");

    budgea.pushConnectionList(BudgeaConnections.init()
                                .add(1, 123, 40, true, "2016-08-10 17:44:26")
                                .get());
    budgea.pushAccountList(BudgeaAccounts.init()
                             .add(1, 1, "Main account 1", "100200300", true)
                             .add(2, 1, "Main account 2", "200300400", false)
                             .get());
    operations.openImportDialog()
      .editCloudConnections()
      .editAccounts("Connecteur de test")
      .checkAccounts("Main account 1 / 100200300 / true",
                     "Main account 2 / 200300400 / false")
      .close();
  }

  @Test
  public void testUndoAfterDeletingAnAccountsRestoresTheSync() throws Exception {

    cloud.createSubscription("toto@example.com", Dates.tomorrow());

    budgea.pushNewConnectionResponse(1, 123, 40);
    budgea.pushStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de test", "2016-08-10 17:44:26")
                           .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-10 13:00:00")
                           .addTransaction(1, "2016-08-10 13:00:00", -100.00, "AUCHAN")
                           .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
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
      .checkNewAccountSelected()
      .importAndPreviewNextAccount()
      .checkTransactions(new Object[][]{
        {"2016/08/11", "FNAC", "-200.00"},
      })
      .checkNewAccountSelected()
      .importAccountAndComplete();

    mainAccounts.checkAccounts("Main account 1", "Main account 2");

    mainAccounts.openDelete("Main account 1")
      .validate();

    budgea.checkAccountUpdates("account:1 => disabled:1");
    budgea.clearAccountUpdates();

    operations.undo();

    budgea.checkAccountUpdates("account:1 => disabled:null");

    mainAccounts.checkAccounts("Main account 1", "Main account 2");
  }
}
