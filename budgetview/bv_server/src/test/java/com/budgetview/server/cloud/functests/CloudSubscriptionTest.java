package com.budgetview.server.cloud.functests;

import com.budgetview.model.TransactionType;
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
  public void testASubscriptionLifecycle() throws Exception {

    Date nextMonth = Dates.nextMonth();
    payments.setSubscriptionEndDate(nextMonth);
    subscriptions.submitStripeForm("toto@example.com", "abcdef012345", WebsiteUrls.emailSent());
    mailbox.clickSubscriptionValidationLink("toto@example.com");
    website.checkLastVisitedPage(WebsiteUrls.subscriptionCreated());
    String subscriptionId = payments.checkLastRequest("toto@example.com", "abcdef012345");

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


    // UPDATES

    Date newEndDate = Dates.monthsLater(2);
    payments.notifyInvoice(subscriptionId, newEndDate, "000111222333");
    mailbox.checkInvoice("toto@example.com", "000111222333");
    budgea.pushConnectionList(BudgeaConnections.init()
                                .add(1, 123, 40, true, "2016-08-10 17:44:26")
                                .get());
    operations.openImportDialog()
      .editCloudConnections()
      .checkSubscriptionEndDate(newEndDate)
      .close();


    // SUBSCRIPTION PAYMENT FAILED

    payments.notifyInvoicePaymentFailed(subscriptionId, newEndDate, "000111222333");
    mailbox.checkInvoicePaymentFailed("toto@example.com", "000111222333");


    // UPDATE CARD

    payments.setSubscriptionEndDate(Dates.monthsLater(3));
    subscriptions.submitStripeForm("toto@example.com", "xyz98765", WebsiteUrls.emailSent());
    mailbox.clickSubscriptionValidationLink("toto@example.com");
    website.checkLastVisitedPage(WebsiteUrls.cardUpdated());
    payments.checkLastUpdate("xyz98765");


    // DELETION

    budgea.pushConnectionList(BudgeaConnections.init()
                                .add(1, 123, 40, true, "2016-08-10 17:44:26")
                                .get());

    operations.openImportDialog()
      .editCloudConnections()
      .checkContainsConnection("Connecteur de test")
      .unsubscribe()
      .checkUnsubscribeButtonShown()
      .checkIntroMessageShown()
      .unsubscribeAndCancel()
      .checkUnsubscribeButtonShown()
      .checkIntroMessageShown()
      .unsubscribeAndConfirm()
      .checkUnsubscribeButtonHidden()
      .checkCompletionMessageShown()
      .close();

    budgea.checkUserDeletions(123);
    payments.checkSubscriptionDeleted(subscriptionId);
    mailbox.checkAccountDeleted("toto@example.com");

    budgea.pushNewConnectionResponse(1, 123, 40);
    budgea.pushStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de test", "2016-08-10 17:44:26")
                           .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-10 13:00:00")
                           .addTransaction(1, "2016-08-10 13:00:00", -100.00, "AUCHAN")
                           .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
                           .endAccount()
                           .endConnection()
                           .get());

    // Should be retrieved from PaymentService
    cloud.createSubscription("toto@example.com", Dates.tomorrow());

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
  }
}
