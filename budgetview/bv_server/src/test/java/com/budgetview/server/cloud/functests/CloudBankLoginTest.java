package com.budgetview.server.cloud.functests;

import com.budgetview.model.TransactionType;
import com.budgetview.server.cloud.functests.testcases.CloudDesktopTestCase;
import com.budgetview.server.cloud.stub.BudgeaBankFieldSample;
import com.budgetview.server.cloud.stub.BudgeaStatement;
import com.budgetview.shared.cloud.budgea.BudgeaCategory;
import org.globsframework.utils.Dates;
import org.junit.Test;

public class CloudBankLoginTest extends CloudDesktopTestCase {

  @Test
  public void testTwoStepBankLogin() throws Exception {
    cloudLicense.purchaseLicence("toto@example.com", Dates.tomorrow());

    budgea.pushNewConnection(1, 123, 40);
    budgea.setBankLoginFields(BudgeaBankFieldSample.BUDGEA_FIELDS_STEP_1, BudgeaBankFieldSample.BUDGEA_FIELDS_STEP_2);
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
      .selectBank("Connecteur de Test Budgea")
      .next()
      .setChoice("Type de compte", "Professionnels")
      .setText("Identifiant", "1234")
      .setPassword("Code (1234)", "")
      .enterAndGetStep2()
      .setText("Please enter the PIN code", "6789")
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
  public void testLoginWithDateField() throws Exception {

    cloudLicense.purchaseLicence("toto@example.com", Dates.tomorrow());

    budgea.pushNewConnection(1, 123, 40);
    budgea.pushStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de Test Budgea", "2016-08-10 17:44:26")
                           .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-10 13:00:00")
                           .addTransaction(1, "2016-08-10 13:00:00", -100.00, "AUCHAN")
                           .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
                           .endAccount()
                           .endConnection()
                           .get());

    budgea.setBankLoginFields(BudgeaBankFieldSample.ING_DIRECT);

    operations.openImportDialog()
      .selectCloudForNewUser()
      .register("toto@example.com")
      .processEmailAndNextToBankSelection(mailbox.getVerificationCode("toto@example.com"))
      .checkNoBankSelected()
      .checkNextDisabled()
      .selectBank("Connecteur de Test Budgea")
      .next()
      .setText("Numero client", "43214321")
      .setDate("Date de naissance", 25, "June", 2001)
      .setPassword("Code secret", "1234")
      .next()
      .waitForNotificationAndDownload(mailbox.checkStatementReady("toto@example.com"))
      .checkTransactions(new Object[][]{
        {"2016/08/12", "EDF", "-50.00"},
        {"2016/08/10", "AUCHAN", "-100.00"},
      })
      .importAccountAndComplete();

    budgea.checkLastLogin("login=43214321", "birthday=25/06/2001");

    transactions.initContent()
      .add("12/08/2016", TransactionType.PRELEVEMENT, "EDF", "", -50.00, "Electricity")
      .add("10/08/2016", TransactionType.PRELEVEMENT, "AUCHAN", "", -100.00)
      .check();
  }

  @Test
  public void testLoginErrors() throws Exception {
    fail("tbd");
  }
}
