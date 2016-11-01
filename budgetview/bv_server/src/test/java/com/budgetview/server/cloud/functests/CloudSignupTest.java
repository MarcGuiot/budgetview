package com.budgetview.server.cloud.functests;

import com.budgetview.model.TransactionType;
import com.budgetview.server.cloud.functests.testcases.CloudDesktopTestCase;
import com.budgetview.server.cloud.stub.BudgeaStatement;
import com.budgetview.shared.cloud.budgea.BudgeaCategory;
import org.junit.Test;

public class CloudSignupTest extends CloudDesktopTestCase {

  @Test
  public void testNewUserSequence() throws Exception {
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

    transactions.initContent()
      .add("12/08/2016", TransactionType.PRELEVEMENT, "EDF", "", -50.00, "Electricité")
      .add("10/08/2016", TransactionType.PRELEVEMENT, "AUCHAN", "", -100.00)
      .check();
  }
}
