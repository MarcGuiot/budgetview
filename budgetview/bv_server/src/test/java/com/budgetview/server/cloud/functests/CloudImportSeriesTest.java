package com.budgetview.server.cloud.functests;

import com.budgetview.server.cloud.functests.testcases.CloudDesktopTestCase;
import com.budgetview.server.cloud.stub.BudgeaStatement;
import com.budgetview.shared.cloud.budgea.BudgeaCategory;
import org.globsframework.utils.Dates;
import org.junit.Test;

public class CloudImportSeriesTest extends CloudDesktopTestCase {

  public void setUp() throws Exception {
    setCurrentDate("2016/08/20");
    createDefaultSeries = true;
    super.setUp();
  }

  @Test
  public void testUsesLocalSeriesFirstAndIgnoresTheOthers() throws Exception {

    cloud.createSubscription("toto@example.com", Dates.tomorrow());

    budgea.pushNewConnectionResponse(1, 123, 40);
    budgea.pushStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de test", "2016-08-10 17:44:26")
                           .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-10 13:00:00")
                           .addTransaction(1, "2016-08-10 13:00:00", -100.00, "Auchan")
                           .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
                           .addTransaction(3, "2016-08-13 20:00:00", -15.00, "McDo", BudgeaCategory.RESTAURANT)
                           .endAccount()
                           .endConnection()
                           .get());

    operations.openImportDialog()
      .checkCloudRefreshNotVisible()
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
        {"2016/08/13", "McDo", "-15.00"},
        {"2016/08/12", "EDF", "-50.00"},
        {"2016/08/10", "Auchan", "-100.00"},
      })
      .importAccountAndComplete();

    transactions.initAmountContent()
      .add("13/08/2016", "MCDO", -15.00, "Restaurant", 1000.00, 1000.00, "Main account 1")
      .add("12/08/2016", "EDF", -50.00, "Electricity", 1015.00, 1015.00, "Main account 1")
      .add("10/08/2016", "AUCHAN", -100.00, "To categorize", 1065.00, 1065.00, "Main account 1")
      .check();

    budgetView.recurring.checkContent("| Electricity      | 50.00 | 50.00 |\n" +
                                      "| Car insurance    | 0.00  | 0.00  |\n" +
                                      "| Car loan         | 0.00  | 0.00  |\n" +
                                      "| Gas              | 0.00  | 0.00  |\n" +
                                      "| Health insurance | 0.00  | 0.00  |\n" +
                                      "| Housing taxes    | 0.00  | 0.00  |\n" +
                                      "| Income taxes     | 0.00  | 0.00  |\n" +
                                      "| Internet         | 0.00  | 0.00  |\n" +
                                      "| Phone            | 0.00  | 0.00  |\n" +
                                      "| Rent/Mortgage    | 0.00  | 0.00  |\n" +
                                      "| Water            | 0.00  | 0.00  |");

    categorization.setVariable("Auchan", "Groceries");

    budgetView.variable.checkContent("| Groceries      | 100.00 | To define |\n" +
                                     "| Restaurant     | 15.00  | To define |\n" +
                                     "| Animals        | 0.00   | To define |\n" +
                                     "| Bank fees      | 0.00   | To define |\n" +
                                     "| Beauty         | 0.00   | To define |\n" +
                                     "| Cash           | 0.00   | To define |\n" +
                                     "| Clothing       | 0.00   | To define |\n" +
                                     "| Drugstore      | 0.00   | To define |\n" +
                                     "| Fuel           | 0.00   | To define |\n" +
                                     "| Gifts          | 0.00   | To define |\n" +
                                     "| Health         | 0.00   | To define |\n" +
                                     "| Home           | 0.00   | To define |\n" +
                                     "| Leisures       | 0.00   | To define |\n" +
                                     "| Miscellaneous  | 0.00   | To define |\n" +
                                     "| Parking        | 0.00   | To define |\n" +
                                     "| Physician      | 0.00   | To define |\n" +
                                     "| Reimbursements | 0.00   | To define |\n" +
                                     "| Toll           | 0.00   | To define |\n" +
                                     "| Transport      | 0.00   | To define |");

    budgea.sendStatement(BudgeaStatement.init()
                         .addConnection(1, 123, 40, "Connecteur de test", "2016-08-10 17:44:26")
                         .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-10 13:00:00")
                         .addTransaction(4, "2016-08-14 13:00:00", -75.00, "Auchan")
                         .addTransaction(5, "2016-08-15 10:00:00", -5.00, "CIC", BudgeaCategory.FRAIS_BANCAIRES)
                         .addTransaction(6, "2016-08-16 08:00:00", "2016-08-16 10:00:00", -30.00, "Vroom", "PRLV VROOM SARL", 123456789, "Karting", false)
                         .endAccount()
                         .endConnection()
                         .get());

    operations.openImportDialog()
      .selectCloudRefresh()
      .checkTransactions(new Object[][]{
        {"2016/08/16", "Vroom", "-30.00"},
        {"2016/08/15", "CIC", "-5.00"},
        {"2016/08/14", "Auchan", "-75.00"}
      })
      .checkSelectedAccount("Main account 1")
      .importAccountAndGetSummary()
      .checkSummaryAndValidate(3, 0, 2);

    budgetView.variable.checkContent("| Groceries      | 175.00 | To define |\n" +
                                     "| Restaurant     | 15.00  | To define |\n" +
                                     "| Bank fees      | 5.00   | To define |\n" +
                                     "| Animals        | 0.00   | To define |\n" +
                                     "| Beauty         | 0.00   | To define |\n" +
                                     "| Cash           | 0.00   | To define |\n" +
                                     "| Clothing       | 0.00   | To define |\n" +
                                     "| Drugstore      | 0.00   | To define |\n" +
                                     "| Fuel           | 0.00   | To define |\n" +
                                     "| Gifts          | 0.00   | To define |\n" +
                                     "| Health         | 0.00   | To define |\n" +
                                     "| Home           | 0.00   | To define |\n" +
                                     "| Leisures       | 0.00   | To define |\n" +
                                     "| Miscellaneous  | 0.00   | To define |\n" +
                                     "| Parking        | 0.00   | To define |\n" +
                                     "| Physician      | 0.00   | To define |\n" +
                                     "| Reimbursements | 0.00   | To define |\n" +
                                     "| Toll           | 0.00   | To define |\n" +
                                     "| Transport      | 0.00   | To define |");

    operations.openImportDialog()
      .selectCloudRefreshAndGetSummary()
      .checkSummaryAndValidate(0, 0, 0);
  }

  @Test
  public void testUsesExistingSeriesOnlyWhenAvailableForSelectedAccount() throws Exception {

    cloud.createSubscription("toto@example.com", Dates.tomorrow());

    accounts.createMainAccount("Existing account", "123123123", 500.00);
    budgetView.recurring.editSeries("Electricity")
      .setTargetAccount("Existing account")
      .validate();

    budgea.pushNewConnectionResponse(1, 123, 40);
    budgea.pushStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de test", "2016-08-10 17:44:26")
                           .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-10 13:00:00")
                           .addTransaction(1, "2016-08-10 13:00:00", -100.00, "Auchan")
                           .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
                           .endAccount()
                           .endConnection()
                           .get());

    operations.openImportDialog()
      .checkCloudRefreshNotVisible()
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
        {"2016/08/10", "Auchan", "-100.00"},
      })
      .importAccountAndComplete();

    transactions.initAmountContent()
      .add("12/08/2016", "EDF", -50.00, "To categorize", 1000.00, 1500.00, "Main account 1")
      .add("10/08/2016", "AUCHAN", -100.00, "To categorize", 1050.00, 1550.00, "Main account 1")
      .check();


    budgetView.recurring.checkContent("| Car insurance    | 0.00 | 0.00 |\n" +
                                      "| Car loan         | 0.00 | 0.00 |\n" +
                                      "| Electricity      | 0.00 | 0.00 |\n" +
                                      "| Gas              | 0.00 | 0.00 |\n" +
                                      "| Health insurance | 0.00 | 0.00 |\n" +
                                      "| Housing taxes    | 0.00 | 0.00 |\n" +
                                      "| Income taxes     | 0.00 | 0.00 |\n" +
                                      "| Internet         | 0.00 | 0.00 |\n" +
                                      "| Phone            | 0.00 | 0.00 |\n" +
                                      "| Rent/Mortgage    | 0.00 | 0.00 |\n" +
                                      "| Water            | 0.00 | 0.00 |");
  }

  @Test
  public void testUsesExistingSeriesOnlyWhenActiveForTransactionMonth() throws Exception {
    cloud.createSubscription("toto@example.com", Dates.tomorrow());
    accounts.createMainAccount("Existing account", "123123123", 500.00);
    budgetView.recurring.editSeries("Electricity")
      .setEndDate(201607)
      .validate();

    budgea.pushNewConnectionResponse(1, 123, 40);
    budgea.pushStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de test", "2016-08-10 17:44:26")
                           .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-10 13:00:00")
                           .addTransaction(1, "2016-08-10 13:00:00", -100.00, "Auchan")
                           .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
                           .endAccount()
                           .endConnection()
                           .get());

    operations.openImportDialog()
      .checkCloudRefreshNotVisible()
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
        {"2016/08/10", "Auchan", "-100.00"},
      })
      .importAccountAndComplete();

    transactions.initAmountContent()
      .add("12/08/2016", "EDF", -50.00, "To categorize", 1000.00, 1500.00, "Main account 1")
      .add("10/08/2016", "AUCHAN", -100.00, "To categorize", 1050.00, 1550.00, "Main account 1")
      .check();

    budgetView.recurring.checkContent("| Car insurance    | 0.00 | 0.00 |\n" +
                                      "| Car loan         | 0.00 | 0.00 |\n" +
                                      "| Gas              | 0.00 | 0.00 |\n" +
                                      "| Health insurance | 0.00 | 0.00 |\n" +
                                      "| Housing taxes    | 0.00 | 0.00 |\n" +
                                      "| Income taxes     | 0.00 | 0.00 |\n" +
                                      "| Internet         | 0.00 | 0.00 |\n" +
                                      "| Phone            | 0.00 | 0.00 |\n" +
                                      "| Rent/Mortgage    | 0.00 | 0.00 |\n" +
                                      "| Water            | 0.00 | 0.00 |");
  }
}
