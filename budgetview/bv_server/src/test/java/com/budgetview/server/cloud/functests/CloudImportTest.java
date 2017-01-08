package com.budgetview.server.cloud.functests;

import com.budgetview.functests.checkers.CloudFirstDownloadChecker;
import com.budgetview.functests.utils.OfxBuilder;
import com.budgetview.model.TransactionType;
import com.budgetview.server.cloud.functests.testcases.CloudDesktopTestCase;
import com.budgetview.server.cloud.stub.BudgeaBankFieldSample;
import com.budgetview.server.cloud.stub.BudgeaConnections;
import com.budgetview.server.cloud.stub.BudgeaStatement;
import com.budgetview.shared.cloud.budgea.BudgeaCategory;
import org.globsframework.utils.Dates;
import org.junit.Test;

public class CloudImportTest extends CloudDesktopTestCase {

  @Test
  public void testCreateStandardConnection() throws Exception {
    cloudLicense.purchaseLicence("toto@example.com", Dates.tomorrow());

    budgea.pushNewConnection(1, 123, 40);
    budgea.pushStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de Test Budgea", "2016-08-10 17:44:26")
                           .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-12 13:00:00")
                           .addTransaction(1, "2016-08-10 13:00:00", -100.00, "AUCHAN")
                           .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
                           .addTransaction(3, "2016-08-08 10:00:00", -10.00, "CIC", BudgeaCategory.FRAIS_BANCAIRES)
                           .endAccount()
                           .endConnection()
                           .get());

    operations.openImportDialog()
      .selectCloudForNewUser()
      .register("toto@example.com")
      .processEmailAndNextToBankSelection(mailbox.getVerificationCode("toto@example.com"))
      .checkContainsBanks("BNP Paribas", "CIC", "Connecteur de Test Budgea", "Crédit Agricole", "LCL")
      .selectBank("Connecteur de Test Budgea")
      .next()
      .setChoice("Type de compte", "Particuliers")
      .setText("Identifiant", "1234")
      .setPassword("Code (1234)", "")
      .next()
      .waitForNotificationAndDownload(mailbox.checkStatementReady("toto@example.com"))
      .checkTransactions(new Object[][]{
        {"2016/08/12", "EDF", "-50.00"},
        {"2016/08/10", "AUCHAN", "-100.00"},
        {"2016/08/08", "CIC", "-10.00"}
      })
      .importAccountAndComplete();

    mainAccounts.checkAccount("Main account 1", 1000.00, "2016/08/12");

    transactions.initContent()
      .add("12/08/2016", TransactionType.PRELEVEMENT, "EDF", "", -50.00, "Electricity")
      .add("10/08/2016", TransactionType.PRELEVEMENT, "AUCHAN", "", -100.00)
      .add("08/08/2016", TransactionType.PRELEVEMENT, "CIC", "", -10.00, "Bank fees")
      .check();

    budgetView.recurring.checkSeries("Electricity", "50.00", "50.00");
    budgetView.variable.checkSeries("Bank fees", "10.00", "To define");
  }

  @Test
  public void testWaitingForTheInitialStatement() throws Exception {
    cloudLicense.purchaseLicence("toto@example.com", Dates.tomorrow());

    budgea.pushNewConnection(1, 123, 40);

    CloudFirstDownloadChecker firstDownloadPanel = operations.openImportDialog()
      .selectCloudForNewUser()
      .register("toto@example.com")
      .processEmailAndNextToBankSelection(mailbox.getVerificationCode("toto@example.com"))
      .checkContainsBanks("BNP Paribas", "CIC", "Connecteur de Test Budgea", "Crédit Agricole", "LCL")
      .selectBank("Connecteur de Test Budgea")
      .next()
      .setChoice("Type de compte", "Particuliers")
      .setText("Identifiant", "1234")
      .setPassword("Code (1234)", "")
      .next()
      .checkNextDisabled()
      .checkNoDataMessageHidden()
      .download()
      .checkNoDataMessageShown();

    mailbox.checkEmpty();

    budgea.callWebhook(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de Test Budgea", "2016-08-10 17:44:26")
                           .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-12 13:00:00")
                           .addTransaction(1, "2016-08-10 13:00:00", -100.00, "AUCHAN")
                           .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
                           .addTransaction(3, "2016-08-08 10:00:00", -10.00, "CIC", BudgeaCategory.FRAIS_BANCAIRES)
                           .endAccount()
                           .endConnection()
                           .get());

    firstDownloadPanel
      .waitForNotificationAndDownload(mailbox.checkStatementReady("toto@example.com"))
      .checkTransactions(new Object[][]{
        {"2016/08/12", "EDF", "-50.00"},
        {"2016/08/10", "AUCHAN", "-100.00"},
        {"2016/08/08", "CIC", "-10.00"}
      })
      .importAccountAndComplete();

    mainAccounts.checkAccount("Main account 1", 1000.00, "2016/08/12");

    transactions.initContent()
      .add("12/08/2016", TransactionType.PRELEVEMENT, "EDF", "", -50.00, "Electricity")
      .add("10/08/2016", TransactionType.PRELEVEMENT, "AUCHAN", "", -100.00)
      .add("08/08/2016", TransactionType.PRELEVEMENT, "CIC", "", -10.00, "Bank fees")
      .check();

    budgetView.recurring.checkSeries("Electricity", "50.00", "50.00");
    budgetView.variable.checkSeries("Bank fees", "10.00", "To define");
  }

  @Test
  public void testClosingAndReopeningTheImportDialogWhileWaitingForTheInitialStatement() throws Exception {
    cloudLicense.purchaseLicence("toto@example.com", Dates.tomorrow());

    budgea.pushNewConnection(1, 123, 40);

    operations.openImportDialog()
      .selectCloudForNewUser()
      .register("toto@example.com")
      .processEmailAndNextToBankSelection(mailbox.getVerificationCode("toto@example.com"))
      .checkContainsBanks("BNP Paribas", "CIC", "Connecteur de Test Budgea", "Crédit Agricole", "LCL")
      .selectBank("Connecteur de Test Budgea")
      .next()
      .setChoice("Type de compte", "Particuliers")
      .setText("Identifiant", "1234")
      .setPassword("Code (1234)", "")
      .next()
      .checkNextDisabled()
      .close();

    mailbox.checkEmpty();
    budgea.callWebhook(BudgeaStatement.init()
                         .addConnection(1, 123, 40, "Connecteur de Test Budgea", "2016-08-10 17:44:26")
                         .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-12 13:00:00")
                         .addTransaction(1, "2016-08-10 13:00:00", -100.00, "AUCHAN")
                         .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
                         .addTransaction(3, "2016-08-08 10:00:00", -10.00, "CIC", BudgeaCategory.FRAIS_BANCAIRES)
                         .endAccount()
                         .endConnection()
                         .get());

    budgea.pushConnections(BudgeaConnections.init()
                             .add(1, 123, 40, true, "2016-08-10 17:44:26")
                             .get());

    operations.openImportDialog()
      .selectCloudRefreshAndGetFirstDowload()
      .waitForNotificationAndDownload(mailbox.checkStatementReady("toto@example.com"))
      .checkTransactions(new Object[][]{
        {"2016/08/12", "EDF", "-50.00"},
        {"2016/08/10", "AUCHAN", "-100.00"},
        {"2016/08/08", "CIC", "-10.00"}
      })
      .importAccountAndComplete();

    mainAccounts.checkAccount("Main account 1", 1000.00, "2016/08/12");

    transactions.initContent()
      .add("12/08/2016", TransactionType.PRELEVEMENT, "EDF", "", -50.00, "Electricity")
      .add("10/08/2016", TransactionType.PRELEVEMENT, "AUCHAN", "", -100.00)
      .add("08/08/2016", TransactionType.PRELEVEMENT, "CIC", "", -10.00, "Bank fees")
      .check();

    budgetView.recurring.checkSeries("Electricity", "50.00", "50.00");
    budgetView.variable.checkSeries("Bank fees", "10.00", "To define");
  }

  @Test
  public void testRefreshDoesNotResendPreviousStatements() throws Exception {
    cloudLicense.purchaseLicence("toto@example.com", Dates.tomorrow());
    budgea.pushNewConnection(1, 123, 40);
    budgea.pushStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de Test Budgea", "2016-08-10 17:44:26")
                           .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-12 13:00:00")
                           .addTransaction(1, "2016-08-10 13:00:00", -100.00, "AUCHAN")
                           .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
                           .endAccount()
                           .endConnection()
                           .get());

    operations.openImportDialog()
      .checkCloudRefreshNotVisible()
      .selectCloudForNewUser()
      .register("toto@example.com")
      .processEmailAndNextToBankSelection(mailbox.getVerificationCode("toto@example.com"))
      .selectBank("Connecteur de Test Budgea")
      .next()
      .setChoice("Type de compte", "Particuliers")
      .setText("Identifiant", "1234")
      .setPassword("Code (1234)", "")
      .next()
      .waitForNotificationAndDownload(mailbox.checkStatementReady("toto@example.com"))
      .checkTransactions(new Object[][]{
        {"2016/08/12", "EDF", "-50.00"},
        {"2016/08/10", "AUCHAN", "-100.00"},
      })
      .importAccountAndComplete();

    transactions.initAmountContent()
      .add("12/08/2016", "EDF", -50.00, "Electricity", 1000.00, 1000.00, "Main account 1")
      .add("10/08/2016", "AUCHAN", -100.00, "To categorize", 1050.00, 1050.00, "Main account 1")
      .check();

    budgea.callWebhook(BudgeaStatement.init()
                         .addConnection(1, 123, 40, "Connecteur de Test Budgea", "2016-08-10 17:44:26")
                         .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-12 13:00:00")
                         .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
                         .addTransaction(3, "2016-08-08 10:00:00", -10.00, "CIC", BudgeaCategory.FRAIS_BANCAIRES)
                         .endAccount()
                         .endConnection()
                         .get());

    operations.openImportDialog()
      .selectCloudRefresh()
      .checkTransactions(new Object[][]{
        {"2016/08/12", "EDF", "-50.00"},
        {"2016/08/08", "CIC", "-10.00"}
      })
      .checkSelectedAccount("Main account 1")
      .importAccountAndGetSummary()
      .checkSummaryAndValidate(1, 1, 1);

    budgetView.recurring.checkSeries("Electricity", "50.00", "50.00");
    budgetView.variable.checkSeries("Bank fees", "10.00", "To define");

    operations.openImportDialog()
      .selectCloudRefreshAndGetSummary()
      .checkSummaryAndValidate(0, 0, 0);
  }

  @Test
  public void testRequestSameUpdateAfterCancel() throws Exception {
    cloudLicense.purchaseLicence("toto@example.com", Dates.tomorrow());
    budgea.pushNewConnection(1, 123, 40);
    budgea.pushStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de Test Budgea", "2016-08-10 17:44:26")
                           .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-12 13:00:00")
                           .addTransaction(1, "2016-08-10 13:00:00", -100.00, "AUCHAN")
                           .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
                           .endAccount()
                           .endConnection()
                           .get());

    operations.openImportDialog()
      .checkCloudRefreshNotVisible()
      .selectCloudForNewUser()
      .register("toto@example.com")
      .processEmailAndNextToBankSelection(mailbox.getVerificationCode("toto@example.com"))
      .selectBank("Connecteur de Test Budgea")
      .next()
      .setChoice("Type de compte", "Particuliers")
      .setText("Identifiant", "1234")
      .setPassword("Code (1234)", "")
      .next()
      .waitForNotificationAndDownload(mailbox.checkStatementReady("toto@example.com"))
      .importAccountAndComplete();

    budgea.callWebhook(BudgeaStatement.init()
                         .addConnection(1, 123, 40, "Connecteur de Test Budgea", "2016-08-10 17:44:26")
                         .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-12 13:00:00")
                         .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
                         .addTransaction(3, "2016-08-08 10:00:00", -10.00, "CIC", BudgeaCategory.FRAIS_BANCAIRES)
                         .endAccount()
                         .endConnection()
                         .get());

    operations.openImportDialog()
      .selectCloudRefresh()
      .checkTransactions(new Object[][]{
        {"2016/08/12", "EDF", "-50.00"},
        {"2016/08/08", "CIC", "-10.00"}
      })
      .checkSelectedAccount("Main account 1")
      .importAccountAndGetSummary()
      .cancel();

    operations.openImportDialog()
      .selectCloudRefresh()
      .checkTransactions(new Object[][]{
        {"2016/08/12", "EDF", "-50.00"},
        {"2016/08/08", "CIC", "-10.00"}
      })
      .checkSelectedAccount("Main account 1")
      .importAccountAndGetSummary()
      .checkSummaryAndValidate(1, 1, 1);

    budgetView.recurring.checkSeries("Electricity", "50.00", "50.00");
    budgetView.variable.checkSeries("Bank fees", "10.00", "To define");
  }

  @Test
  public void testIgnoresOperationsThatWereAlreadyImportedWithAFile() throws Exception {
    cloudLicense.purchaseLicence("toto@example.com", Dates.tomorrow());
    OfxBuilder.init(this)
      .addBankAccount(-999, 1234, "100200300", 1000.00, "2016/08/10")
      .addTransaction("2016/08/08", "2016/08/08", -10.00, "PRLVT FRAIS CIC FILBANQUE")
      .addTransaction("2016/08/10", "2016/08/10", -100.00, "CB AUCHAN SA")
      .load();

    budgea.pushNewConnection(1, 123, 40);
    budgea.pushStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de Test Budgea", "2016-08-12 17:44:26")
                           .addAccount(1, "Main account 1", "100200300", "checking", 950.00, "2016-08-12 13:00:00")
                           .addTransaction(1, "2016-08-08 10:00:00", "2016-08-08 10:00:00", -10.00, "CIC", "PRLVT FRAIS CIC FILBANQUE", BudgeaCategory.FRAIS_BANCAIRES.getId(), "Frais bancaires", false)
                           .addTransaction(2, "2016-08-10 13:00:00", "2016-08-10 10:00:00", -100.00, "AUCHAN", "CB AUCHAN SA", BudgeaCategory.INDEFINI.getId(), "A classer", false)
                           .addTransaction(3, "2016-08-12 17:00:00", "2016-08-12 10:00:00", -50.00, "EDF", "CB E.D.F", BudgeaCategory.ELECTRICITE.getId(), "Electricité", false)
                           .endAccount()
                           .endConnection()
                           .get());

    operations.openImportDialog()
      .selectCloudForNewUser()
      .register("toto@example.com")
      .processEmailAndNextToBankSelection(mailbox.getVerificationCode("toto@example.com"))
      .checkContainsBanks("BNP Paribas", "CIC", "Connecteur de Test Budgea", "Crédit Agricole", "LCL")
      .selectBank("Connecteur de Test Budgea")
      .next()
      .setChoice("Type de compte", "Particuliers")
      .setText("Identifiant", "1234")
      .setPassword("Code (1234)", "1234")
      .next()
      .waitForNotificationAndDownload(mailbox.checkStatementReady("toto@example.com"))
      .checkTransactions(new Object[][]{
        {"2016/08/12", "EDF", "-50.00"},
        {"2016/08/10", "AUCHAN", "-100.00"},
        {"2016/08/08", "CIC", "-10.00"}
      })
      .importAccountAndComplete();

    transactions.initContent()
      .add("12/08/2016", TransactionType.PRELEVEMENT, "EDF", "", -50.00, "Electricity")
      .add("10/08/2016", TransactionType.PRELEVEMENT, "CB AUCHAN SA", "", -100.00)
      .add("08/08/2016", TransactionType.PRELEVEMENT, "PRLVT FRAIS CIC FILBANQUE", "", -10.00)
      .check();

    mainAccounts.checkAccount("Account n. 100200300", 950.00, "2016/08/12");

    budgetView.recurring.checkSeries("Electricity", "50.00", "50.00");
  }

  @Test
  public void testCanManageConnexionWithTwoBanks() throws Exception {
    cloudLicense.purchaseLicence("toto@example.com", Dates.tomorrow());
    budgea.pushNewConnection(1, 123, 40);
    budgea.pushStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de Test Budgea", "2016-08-10 17:44:26")
                           .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-12 13:00:00")
                           .addTransaction(1, "2016-08-10 13:00:00", -100.00, "AUCHAN")
                           .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
                           .endAccount()
                           .endConnection()
                           .get());

    operations.openImportDialog()
      .selectCloudForNewUser()
      .register("toto@example.com")
      .processEmailAndNextToBankSelection(mailbox.getVerificationCode("toto@example.com"))
      .selectBank("Connecteur de Test Budgea")
      .next()
      .setChoice("Type de compte", "Particuliers")
      .setText("Identifiant", "1234")
      .setPassword("Code (1234)", "")
      .next()
      .waitForNotificationAndDownload(mailbox.checkStatementReady("toto@example.com"))
      .checkTransactions(new Object[][]{
        {"2016/08/12", "EDF", "-50.00"},
        {"2016/08/10", "AUCHAN", "-100.00"},
      })
      .importAccountAndComplete();

    transactions.initAmountContent()
      .add("12/08/2016", "EDF", -50.00, "Electricity", 1000.00, 1000.00, "Main account 1")
      .add("10/08/2016", "AUCHAN", -100.00, "To categorize", 1050.00, 1050.00, "Main account 1")
      .check();

    budgea.setBankLoginFields(BudgeaBankFieldSample.CIC);
    budgea.pushNewConnection(2, 123, 10);
    budgea.pushStatement(BudgeaStatement.init()
                           .addConnection(2, 123, 10, "CIC", "2016-08-14 16:18:44")
                           .addAccount(2, "Joint account", "987654321", "checking", 500.00, "2016-08-14 17:00:00")
                           .addTransaction(1, "2016-08-14 10:00:00", -250.00, "FNAC", BudgeaCategory.INDEFINI)
                           .endAccount()
                           .endConnection()
                           .get());

    budgea.pushConnections(BudgeaConnections.init()
                             .add(1, 123, 40, true, "2016-08-10 17:44:26")
                             .get());

    operations.openImportDialog()
      .editCloudConnections()
      .checkConnections("Connecteur de Test Budgea")
      .addConnection()
      .selectBank("CIC")
      .next()
      .setText("Identifiant", "1234")
      .setPassword("Mot de passe", "abcd")
      .next()
      .waitForNotificationAndDownload(mailbox.checkStatementReady("toto@example.com"))
      .checkTransactions(new Object[][]{
        {"2016/08/14", "FNAC", "-250.00"},
      })
      .checkSelectedAccount("a new account")
      .importAccountAndGetSummary()
      .checkSummaryAndValidate(1, 0, 0);

    budgetView.recurring.checkSeries("Electricity", "50.00", "50.00");

    mainAccounts.checkContent("| Main account 1* | 1000.00 on 2016/08/12 | sunny |\n" +
                              "| Joint account*  | 500.00 on 2016/08/14  | sunny |");

    budgea.pushConnections(BudgeaConnections.init()
                             .add(1, 123, 40, true, "2016-08-10 17:44:26")
                             .add(2, 123, 10, true, "2016-08-14 16:18:44")
                             .get());

    operations.openImportDialog()
      .editCloudConnections()
      .checkConnections("Connecteur de Test Budgea", "CIC")
      .close();
  }

  @Test
  public void testDownloadAccountWithoutTransactions() throws Exception {
    cloudLicense.purchaseLicence("toto@example.com", Dates.tomorrow());
    budgea.pushNewConnection(1, 123, 40);
    budgea.pushStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de Test Budgea", "2016-08-10 17:44:26")
                           .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-10 13:00:00")
                           .endAccount()
                           .endConnection()
                           .get());

    operations.openImportDialog()
      .selectCloudForNewUser()
      .register("toto@example.com")
      .processEmailAndNextToBankSelection(mailbox.getVerificationCode("toto@example.com"))
      .checkContainsBanks("BNP Paribas", "CIC", "Connecteur de Test Budgea", "Crédit Agricole", "LCL")
      .selectBank("Connecteur de Test Budgea")
      .next()
      .setChoice("Type de compte", "Particuliers")
      .setText("Identifiant", "1234")
      .setPassword("Code (1234)", "")
      .next()
      .waitForNotificationAndDownload(mailbox.checkStatementReady("toto@example.com"))
      .checkNoTransactions()
      .importAccountAndGetSummary()
      .checkSummaryAndValidate(0, 0, 0);

    mainAccounts.checkAccount("Main account 1", 1000.00, "2016/08/01");
    transactions.checkEmpty();
  }
}
