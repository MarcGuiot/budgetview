package com.budgetview.server.cloud.functests;

import com.budgetview.model.TransactionType;
import com.budgetview.server.cloud.functests.checkers.CloudChecker;
import com.budgetview.server.cloud.functests.testcases.CloudDesktopTestCase;
import com.budgetview.server.cloud.stub.BudgeaConnections;
import com.budgetview.server.cloud.stub.BudgeaStatement;
import com.budgetview.server.cloud.tools.AddCloudUser;
import com.budgetview.server.cloud.tools.DeleteCloudUser;
import com.budgetview.server.cloud.tools.ShowBudgeaUser;
import com.budgetview.server.cloud.tools.ShowCloudUser;
import com.budgetview.shared.cloud.budgea.BudgeaCategory;
import org.globsframework.utils.Dates;
import org.globsframework.utils.StringChecker;
import org.junit.Test;

public class CloudToolsTest extends CloudDesktopTestCase {

  @Test
  public void testCreateDeleteUser() throws Exception {

    AddCloudUser.main(CloudChecker.CONFIG_FILE_PATH, "toto@example.com", "5");

    budgea.pushConnectionResponse(1, 123, 40);
    budgea.pushStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de test", "2016-08-10 17:44:26")
                           .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-10 13:00:00")
                           .addTransaction(1, "2016-08-10 13:00:00", -100.00, "AUCHAN")
                           .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
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
      .importAccountAndComplete();

    transactions.initContent()
      .add("12/08/2016", TransactionType.PRELEVEMENT, "EDF", "", -50.00, "Electricity")
      .add("10/08/2016", TransactionType.PRELEVEMENT, "AUCHAN", "", -100.00)
      .check();

    budgea.pushConnectionList(BudgeaConnections.init()
                                .add(1, 123, 40, true, "2016-08-10 17:44:26")
                                .get());
    operations.openImportDialog()
      .editCloudConnections()
      .checkSubscriptionEndDate(Dates.monthsLater(5))
      .close();

    StringChecker info = new StringChecker(ShowCloudUser.dump(CloudChecker.CONFIG_FILE_PATH, "toto@example.com"));
    info.checkContains("email: toto@example.com");
    info.checkContains("emailVerified: false");
    info.checkContains("provider: 2");
    info.checkContains("providerUserId: 123");

    StringChecker infoForBudgeaUser = new StringChecker(ShowBudgeaUser.dump(CloudChecker.CONFIG_FILE_PATH, "123"));
    infoForBudgeaUser.checkEquals(info);

    StringChecker deleteOutput = new StringChecker(DeleteCloudUser.dump(CloudChecker.CONFIG_FILE_PATH, "toto@example.com", "skipConfirm"));
    deleteOutput.checkContains("Deleted account for user 0 with email toto@example.com");
    deleteOutput.checkContains("Deletion completed");

    mailbox.checkEmpty();

    StringChecker newInfo = new StringChecker(ShowCloudUser.dump(CloudChecker.CONFIG_FILE_PATH, "toto@example.com"));
    newInfo.checkEquals("User not found");
  }
}
