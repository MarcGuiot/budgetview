package com.budgetview.server.cloud.functests;

import com.budgetview.model.TransactionType;
import com.budgetview.server.cloud.functests.checkers.CloudChecker;
import com.budgetview.server.cloud.functests.testcases.CloudDesktopTestCase;
import com.budgetview.server.cloud.stub.BudgeaAccounts;
import com.budgetview.server.cloud.stub.BudgeaConnections;
import com.budgetview.server.cloud.stub.BudgeaStatement;
import com.budgetview.server.cloud.tools.*;
import com.budgetview.shared.cloud.budgea.BudgeaCategory;
import org.globsframework.utils.Dates;
import org.globsframework.utils.StringChecker;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static com.com.budgetview.server.utils.TestDates.monthsLater;
import static com.com.budgetview.server.utils.TestDates.today;

public class CloudToolsTest extends CloudDesktopTestCase {

  @Test
  public void testCreateDeleteUser() throws Exception {

    AddCloudUser.main(CloudChecker.CONFIG_FILE_PATH, "toto@example.com", "5");
    AddCloudUser.main(CloudChecker.CONFIG_FILE_PATH, "another@email.com", "1");

    budgea.pushNewConnectionResponse(1, 123, 40);
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
    budgea.pushAccountList(BudgeaAccounts.init()
                             .add(1, 1, "Main account 1", "100200300", true)
                             .get());
    operations.openImportDialog()
      .editCloudConnections()
      .checkSubscriptionEndDate(Dates.monthsLater(5))
      .close();

    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    ListCloudUsers.dump(new PrintStream(stream), CloudChecker.CONFIG_FILE_PATH, "toto@example.com");
    StringChecker.init(stream.toString())
      .with("today", today())
      .with("+1", monthsLater(1))
      .with("+5", monthsLater(5))
      .checkMatches("id\temail\tcreation\tsubscriptionEnd\tlastSeen\nlastUpdate" +
                    "[0-9]+\ttoto@example.com\t{{today}}\t{{+5}}\t{{today}}\t{{today}}\n" +
                    "[0-9]+\tanother@email.com\t{{today}}\t{{+1}}\t-\t-\n");

    StringChecker info = StringChecker.init(ShowCloudUser.dump(CloudChecker.CONFIG_FILE_PATH, "toto@example.com"))
      .checkContains("email: toto@example.com")
      .checkContains("emailVerified: false")
      .checkContains("provider: 2")
      .checkContains("providerUserId: 123");

    StringChecker infoForBudgeaUser = new StringChecker(ShowBudgeaUser.dump(CloudChecker.CONFIG_FILE_PATH, "123"));
    infoForBudgeaUser.checkEquals(info);

    StringChecker.init(DeleteCloudUser.dump(CloudChecker.CONFIG_FILE_PATH, "toto@example.com", "skipConfirm"))
      .checkLineMatches("Deleted account for user [0-9]+ with email toto@example.com")
      .checkContains("Deletion completed");

    mailbox.checkEmpty();

    StringChecker.init(ShowCloudUser.dump(CloudChecker.CONFIG_FILE_PATH, "toto@example.com"))
      .checkEquals("User not found");
  }
}
