package com.budgetview.server.cloud.functests;

import com.budgetview.server.cloud.functests.testcases.CloudDesktopTestCase;
import com.budgetview.server.cloud.stub.BudgeaConnections;
import com.budgetview.server.cloud.stub.BudgeaStatement;
import com.budgetview.server.cloud.utils.WebsiteUrls;
import com.budgetview.shared.cloud.budgea.BudgeaCategory;
import org.globsframework.utils.Dates;
import org.junit.Test;

import java.util.Date;

public class CloudSubscriptionTest extends CloudDesktopTestCase {

  @Test
  public void test() throws Exception {

    Date nextMonth = Dates.nextMonth();
    payments.setSubscriptionEndDate(nextMonth);
    subscriptions.submitStripeForm("toto@example.com", "abcdef012345", WebsiteUrls.emailSent());
    mailbox.clickSubscriptionValidationLink("toto@example.com");
    website.checkLastVisitedPage(WebsiteUrls.subscriptionCreated());
    payments.checkLastRequest("toto@example.com", "abcdef012345");

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
      .selectBank("Connecteur de test")
      .next()
      .setChoice("Type de compte", "Particuliers")
      .setText("Identifiant", "1234")
      .setPassword("Code (1234)", "1234")
      .next()
      .waitForNotificationAndDownload(mailbox.checkStatementReady("toto@example.com"))
      .importAccountAndComplete();

    budgea.pushConnectionList(BudgeaConnections.init()
                                .add(1, 123, 40, true, "2016-08-10 17:44:26")
                                .get());
    operations.openImportDialog()
      .editCloudConnections()
      .checkSubscriptionEndDate(nextMonth)
      .close();
    budgea.pushConnectionList(BudgeaConnections.init()
                                .add(1, 123, 40, true, "2016-08-10 17:44:26")
                                .get());
    operations.openImportDialog()
      .editCloudConnections()
      .checkSubscriptionEndDate(nextMonth)
      .close();
  }
}
