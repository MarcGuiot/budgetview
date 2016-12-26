package com.budgetview.server.cloud.functests;

import com.budgetview.model.TransactionType;
import com.budgetview.server.cloud.functests.testcases.CloudDesktopTestCase;
import com.budgetview.server.cloud.stub.BudgeaStatement;
import com.budgetview.shared.cloud.budgea.BudgeaCategory;
import org.globsframework.utils.Dates;
import org.junit.Test;

public class CloudSignupTest extends CloudDesktopTestCase {

  @Test
  public void testNewUserSequence() throws Exception {

    cloudLicense.purchaseLicence("toto@example.com", Dates.tomorrow());

    budgea.pushStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de Test Budgea", "2016-08-10 17:44:26")
                           .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-10 13:00:00")
                           .addTransaction(1, "2016-08-10 13:00:00", -100.00, "AUCHAN")
                           .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
                           .endAccount()
                           .endConnection()
                           .get());

    operations.openImportDialog()
      .selectCloudForNewUser()
      .register("toto@example.com")
      .processEmailAndNextToBankSelection(mailbox.getVerificationCode("toto@example.com"))
      .checkNoBankSelected()
      .checkNextDisabled()
      .selectBank("Connecteur de Test Budgea")
      .next()
      .setChoice("Type de compte", "Particuliers")
      .setText("Identifiant", "1234")
      .setPassword("Code (1234)", "")
      .next()
      .checkTransactions(new Object[][]{
        {"2016/08/12", "EDF", "-50.00"},
        {"2016/08/10", "AUCHAN", "-100.00"},
      })
      .importAccountWithAllSeriesAndComplete();

    transactions.initContent()
      .add("12/08/2016", TransactionType.PRELEVEMENT, "EDF", "", -50.00, "Electricité")
      .add("10/08/2016", TransactionType.PRELEVEMENT, "AUCHAN", "", -100.00)
      .check();
  }

  @Test
  public void testNoSubscriptionError() throws Exception {
    operations.openImportDialog()
      .selectCloudForNewUser()
      .register("toto@example.com")
      .processEmailAndCheckSubscriptionError(mailbox.getVerificationCode("toto@example.com"))
      .checkNoSubscriptionFound("toto@example.com")
      .close();
  }

  @Test
  public void testTokenExpired() throws Exception {

    cloudLicense.purchaseLicence("toto@example.com", Dates.tomorrow());

    budgea.pushStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de Test Budgea", "2016-08-10 17:44:26")
                           .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-10 13:00:00")
                           .addTransaction(1, "2016-08-10 13:00:00", -100.00, "AUCHAN")
                           .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
                           .endAccount()
                           .endConnection()
                           .get());

    operations.openImportDialog()
      .selectCloudForNewUser()
      .register("toto@example.com")
      .processEmailAndCheckError("--this cannot be a valid code--")
      .checkInvalidTokenError()
      .processEmailAndNextToBankSelection(mailbox.getVerificationCode("toto@example.com"))
      .selectBank("Connecteur de Test Budgea")
      .next()
      .setChoice("Type de compte", "Particuliers")
      .setText("Identifiant", "1234")
      .setPassword("Code (1234)", "")
      .next()
      .checkTransactions(new Object[][]{
        {"2016/08/12", "EDF", "-50.00"},
        {"2016/08/10", "AUCHAN", "-100.00"},
      })
      .importAccountWithAllSeriesAndComplete();

    transactions.initContent()
      .add("12/08/2016", TransactionType.PRELEVEMENT, "EDF", "", -50.00, "Electricité")
      .add("10/08/2016", TransactionType.PRELEVEMENT, "AUCHAN", "", -100.00)
      .check();
  }

  @Test
  public void testErrorWhenTokenExpired() throws Exception {

    cloudLicense.purchaseLicence("toto@example.com", Dates.tomorrow());
    cloud.forceTokenExpirationDate(Dates.hoursAgo(1));

    budgea.pushStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de Test Budgea", "2016-08-10 17:44:26")
                           .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-10 13:00:00")
                           .addTransaction(1, "2016-08-10 13:00:00", -100.00, "AUCHAN")
                           .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
                           .endAccount()
                           .endConnection()
                           .get());

    operations.openImportDialog()
      .selectCloudForNewUser()
      .register("toto@example.com")
      .processEmailAndCheckError(mailbox.getVerificationCode("toto@example.com"))
      .checkTempTokenExpiredError()
      .back()
      .checkEmail("toto@example.com")
      .next()
      .processEmailAndNextToBankSelection(mailbox.getVerificationCode("toto@example.com"))
      .selectBank("Connecteur de Test Budgea")
      .next()
      .setChoice("Type de compte", "Particuliers")
      .setText("Identifiant", "1234")
      .setPassword("Code (1234)", "")
      .next()
      .checkTransactions(new Object[][]{
        {"2016/08/12", "EDF", "-50.00"},
        {"2016/08/10", "AUCHAN", "-100.00"},
      })
      .importAccountWithAllSeriesAndComplete();

    transactions.initContent()
      .add("12/08/2016", TransactionType.PRELEVEMENT, "EDF", "", -50.00, "Electricité")
      .add("10/08/2016", TransactionType.PRELEVEMENT, "AUCHAN", "", -100.00)
      .check();
  }

  @Test
  public void testSignupFromCleanInstanceWithExistingStatements() throws Exception {

    cloudLicense.purchaseLicence("toto@example.com", Dates.tomorrow());

    budgea.pushStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de Test Budgea", "2016-08-10 17:44:26")
                           .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-10 13:00:00")
                           .addTransaction(1, "2016-08-10 13:00:00", -100.00, "AUCHAN")
                           .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
                           .endAccount()
                           .endConnection()
                           .get());

    String backupPath = operations.backup(this);

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
      .checkTransactions(new Object[][]{
        {"2016/08/12", "EDF", "-50.00"},
        {"2016/08/10", "AUCHAN", "-100.00"},
      })
      .importAccountWithAllSeriesAndComplete();

    transactions.initContent()
      .add("12/08/2016", TransactionType.PRELEVEMENT, "EDF", "", -50.00, "Electricité")
      .add("10/08/2016", TransactionType.PRELEVEMENT, "AUCHAN", "", -100.00)
      .check();

    budgea.callWebhook(BudgeaStatement.init()
                         .addConnection(1, 123, 40, "Connecteur de Test Budgea", "2016-08-15 17:44:26")
                         .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-15 13:00:00")
                         .addTransaction(3, "2016-08-13 13:00:00", -25.00, "TOTAL", BudgeaCategory.FUEL1)
                         .addTransaction(4, "2016-08-15 15:00:00", -50.00, "FOUQUETS", BudgeaCategory.RESTAURANTS1)
                         .endAccount()
                         .endConnection()
                         .get());

    operations.restore(backupPath);
    transactions.checkEmpty();

    operations.openImportDialog()
      .checkCloudRefreshNotVisible()
      .selectCloudForNewUser()
      .register("toto@example.com")
      .processEmailAndNextToDownload(mailbox.getVerificationCode("toto@example.com"))
      .checkTransactions(new Object[][]{
        {"2016/08/15", "FOUQUETS", "-50.00"},
        {"2016/08/13", "TOTAL", "-25.00"},
        {"2016/08/12", "EDF", "-50.00"},
        {"2016/08/10", "AUCHAN", "-100.00"},
      })
      .importAccountWithAllSeriesAndComplete();

    transactions.initContent()
      .add("15/08/2016", TransactionType.PRELEVEMENT, "FOUQUETS", "", -50.00, "Restaurants")
      .add("13/08/2016", TransactionType.PRELEVEMENT, "TOTAL", "", -25.00, "Fuel")
      .add("12/08/2016", TransactionType.PRELEVEMENT, "EDF", "", -50.00, "Electricité")
      .add("10/08/2016", TransactionType.PRELEVEMENT, "AUCHAN", "", -100.00)
      .check();
  }

  @Test
  public void testSignupFromRestoredBackupWithExistingStatements() throws Exception {
    cloudLicense.purchaseLicence("toto@example.com", Dates.tomorrow());

    budgea.pushStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de Test Budgea", "2016-08-10 17:44:26")
                           .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-10 13:00:00")
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
      .checkTransactions(new Object[][]{
        {"2016/08/12", "EDF", "-50.00"},
        {"2016/08/10", "AUCHAN", "-100.00"},
      })
      .importAccountWithAllSeriesAndComplete();

    transactions.initContent()
      .add("12/08/2016", TransactionType.PRELEVEMENT, "EDF", "", -50.00, "Electricité")
      .add("10/08/2016", TransactionType.PRELEVEMENT, "AUCHAN", "", -100.00)
      .check();

    budgea.callWebhook(BudgeaStatement.init()
                         .addConnection(1, 123, 40, "Connecteur de Test Budgea", "2016-08-15 17:44:26")
                         .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-15 13:00:00")
                         .addTransaction(3, "2016-08-13 13:00:00", -25.00, "TOTAL", BudgeaCategory.FUEL1)
                         .addTransaction(4, "2016-08-15 15:00:00", -50.00, "FOUQUETS", BudgeaCategory.RESTAURANTS1)
                         .endAccount()
                         .endConnection()
                         .get());

    String backupPath = operations.backup(this);
    operations.restore(backupPath);

    operations.openImportDialog()
      .checkCloudRefreshNotVisible()
      .selectCloudForNewUser()
      .register("toto@example.com")
      .processEmailAndNextToDownload(mailbox.getVerificationCode("toto@example.com"))
      .checkTransactions(new Object[][]{
        {"2016/08/15", "FOUQUETS", "-50.00"},
        {"2016/08/13", "TOTAL", "-25.00"},
      })
      .importAccountWithAllSeriesAndComplete();

    transactions.initContent()
      .add("15/08/2016", TransactionType.PRELEVEMENT, "FOUQUETS", "", -50.00, "Restaurants")
      .add("13/08/2016", TransactionType.PRELEVEMENT, "TOTAL", "", -25.00, "Fuel")
      .add("12/08/2016", TransactionType.PRELEVEMENT, "EDF", "", -50.00, "Electricité")
      .add("10/08/2016", TransactionType.PRELEVEMENT, "AUCHAN", "", -100.00)
      .check();
  }
}
