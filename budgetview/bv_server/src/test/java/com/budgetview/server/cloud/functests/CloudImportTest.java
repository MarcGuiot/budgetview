package com.budgetview.server.cloud.functests;

import com.budgetview.model.TransactionType;
import com.budgetview.server.cloud.functests.testcases.CloudDesktopTestCase;
import com.budgetview.server.cloud.stub.BudgeaStatement;
import com.budgetview.shared.cloud.budgea.BudgeaCategory;
import org.junit.Test;

public class CloudImportTest extends CloudDesktopTestCase {

  @Test
  public void testCreateStandardConnection() throws Exception {
    budgea.setInitialStatement(BudgeaStatement.init()
                                 .addConnection(1, 123, 40, "Connecteur de Test Budgea", "2016-08-10 17:44:26")
                                 .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-10 13:00:00")
                                 .addTransaction(1, "2016-08-10 13:00:00", -100.00, "AUCHAN")
                                 .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
                                 .addTransaction(3, "2016-08-08 10:00:00", -10.00, "CIC", BudgeaCategory.FRAIS_BANCAIRES)
                                 .endAccount()
                                 .endConnection()
                                 .get());

    operations.openImportDialog()
      .selectCloudForNewUser()
      .register("toto@example.com")
      .processEmail(mailbox.getVerificationCode("toto@example.com"))
      .checkContainsBanks("BNP Paribas", "CIC", "Connecteur de Test Budgea", "Crédit Agricole", "LCL")
      .selectBank("Connecteur de Test Budgea")
      .next()
      .setChoice("Type de compte", "Particuliers")
      .setText("Identifiant", "1234")
      .setPassword("Code (1234)", "")
      .next()
      .checkTransactions(new Object[][]{
        {"2016/08/12", "EDF", "-50.00"},
        {"2016/08/10", "AUCHAN", "-100.00"},
        {"2016/08/08", "CIC", "-10.00"}
      })
      .importAccountWithAllSeriesAndComplete();

    mainAccounts.checkAccount("Main account 1", 1000.00, "2016/08/12");

    transactions.initContent()
      .add("12/08/2016", TransactionType.PRELEVEMENT, "EDF", "", -50.00, "Electricité")
      .add("10/08/2016", TransactionType.PRELEVEMENT, "AUCHAN", "", -100.00)
      .add("08/08/2016", TransactionType.PRELEVEMENT, "CIC", "", -10.00, "Frais bancaires")
      .check();

    budgetView.recurring.checkContent("| Electricité | 50.00 | 50.00 |");
    budgetView.variable.checkContent("| Frais bancaires | 10.00 | To define |");
  }

  @Test
  public void testRefreshDoesNotResendPreviousStatements() throws Exception {
    budgea.setInitialStatement(BudgeaStatement.init()
                                 .addConnection(1, 123, 40, "Connecteur de Test Budgea", "2016-08-10 17:44:26")
                                 .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-10 13:00:00")
                                 .addTransaction(1, "2016-08-10 13:00:00", -100.00, "AUCHAN")
                                 .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
                                 .endAccount()
                                 .endConnection()
                                 .get());

    operations.openImportDialog()
      .checkCloudRefreshNotVisible()
      .selectCloudForNewUser()
      .register("toto@example.com")
      .processEmail(mailbox.getVerificationCode("toto@example.com"))
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

    transactions.initAmountContent()
      .add("12/08/2016", "EDF", -50.00, "Electricité", 1000.00, 1000.00, "Main account 1")
      .add("10/08/2016", "AUCHAN", -100.00, "To categorize", 1050.00, 1050.00, "Main account 1")
      .check();

    budgea.callWebhook(BudgeaStatement.init()
                         .addConnection(1, 123, 40, "Connecteur de Test Budgea", "2016-08-10 17:44:26")
                         .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-10 13:00:00")
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
      .importAccountWithAllSeriesAndGetSummary()
      .checkSummaryAndValidate(1, 1, 1);

    budgetView.recurring.checkContent("| Electricité | 50.00 | 50.00 |");
    budgetView.variable.checkContent("| Frais bancaires | 10.00 | To define |");

    operations.openImportDialog()
      .selectCloudRefreshAndGetSummary()
      .checkSummaryAndValidate(0, 0, 0);
  }

  @Test
  public void testRequestSameUpdateAfterCancel() throws Exception {
    budgea.setInitialStatement(BudgeaStatement.init()
                                 .addConnection(1, 123, 40, "Connecteur de Test Budgea", "2016-08-10 17:44:26")
                                 .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-10 13:00:00")
                                 .addTransaction(1, "2016-08-10 13:00:00", -100.00, "AUCHAN")
                                 .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
                                 .endAccount()
                                 .endConnection()
                                 .get());

    operations.openImportDialog()
      .checkCloudRefreshNotVisible()
      .selectCloudForNewUser()
      .register("toto@example.com")
      .processEmail(mailbox.getVerificationCode("toto@example.com"))
      .selectBank("Connecteur de Test Budgea")
      .next()
      .setChoice("Type de compte", "Particuliers")
      .setText("Identifiant", "1234")
      .setPassword("Code (1234)", "")
      .next()
      .importAccountWithAllSeriesAndComplete();


    budgea.callWebhook(BudgeaStatement.init()
                         .addConnection(1, 123, 40, "Connecteur de Test Budgea", "2016-08-10 17:44:26")
                         .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-10 13:00:00")
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
      .importAccountWithAllSeriesAndGetSummary()
      .cancel();

    operations.openImportDialog()
      .selectCloudRefresh()
      .checkTransactions(new Object[][]{
        {"2016/08/12", "EDF", "-50.00"},
        {"2016/08/08", "CIC", "-10.00"}
      })
      .checkSelectedAccount("Main account 1")
      .importAccountWithAllSeriesAndGetSummary()
      .checkSummaryAndValidate(1, 1, 1);

    budgetView.recurring.checkContent("| Electricité | 50.00 | 50.00 |");
    budgetView.variable.checkContent("| Frais bancaires | 10.00 | To define |");
  }
}