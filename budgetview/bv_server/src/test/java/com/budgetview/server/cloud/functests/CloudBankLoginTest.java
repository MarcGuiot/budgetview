package com.budgetview.server.cloud.functests;

import com.budgetview.functests.checkers.CloudBankConnectionChecker;
import com.budgetview.model.TransactionType;
import com.budgetview.server.cloud.functests.testcases.CloudDesktopTestCase;
import com.budgetview.server.cloud.stub.BudgeaAccounts;
import com.budgetview.server.cloud.stub.BudgeaBankFieldSample;
import com.budgetview.server.cloud.stub.BudgeaConnections;
import com.budgetview.server.cloud.stub.BudgeaStatement;
import com.budgetview.shared.cloud.budgea.BudgeaCategory;
import org.globsframework.utils.Dates;
import org.junit.Test;

public class CloudBankLoginTest extends CloudDesktopTestCase {

  @Test
  public void testTwoStepBankLogin() throws Exception {
    cloud.createSubscription("toto@example.com", Dates.tomorrow());

    budgea.pushNewConnectionResponse(1, 123, 40);
    budgea.setBankLoginFields(BudgeaBankFieldSample.BUDGEA_FIELDS_STEP_1, BudgeaBankFieldSample.BUDGEA_FIELDS_STEP_2);
    budgea.pushStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de test", "2016-08-10 17:44:26")
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
      .processEmailAndNextToBankSelection(mailbox.getDeviceVerificationCode("toto@example.com"))
      .selectBank("Connecteur de test")
      .next()
      .setChoice("Type de compte", "Professionnels")
      .setText("Identifiant", "1234")
      .setPassword("Code (1234)", "1234")
      .nextAndGetStep2()
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

    cloud.createSubscription("toto@example.com", Dates.tomorrow());

    budgea.pushNewConnectionResponse(1, 123, 40);
    budgea.pushStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de test", "2016-08-10 17:44:26")
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
      .processEmailAndNextToBankSelection(mailbox.getDeviceVerificationCode("toto@example.com"))
      .selectBank("Connecteur de test")
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
    cloud.createSubscription("toto@example.com", Dates.tomorrow());

    budgea.pushNewConnectionResponse(1, 123, 40);
    budgea.pushStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de test", "2016-08-10 17:44:26")
                           .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-10 13:00:00")
                           .addTransaction(1, "2016-08-10 13:00:00", -100.00, "AUCHAN")
                           .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
                           .endAccount()
                           .endConnection()
                           .get());

    budgea.setLoginConstraint("login=1234");

    operations.openImportDialog()
      .selectCloudForNewUser()
      .registerAndCheckError("a b c d", "")
      .register("toto@example.com")
      .processEmailAndNextToBankSelection(mailbox.getDeviceVerificationCode("toto@example.com"))
      .selectBank("Connecteur de test")
      .next()
      .checkNoErrorShown()
      .nextAndCheckError("You must set 'Type de compte'")
      .setChoice("Type de compte", "Professionnels")
      .nextAndCheckError("You must set 'Identifiant'")
      .setText("Identifiant", "a 1 $")
      .nextAndCheckError("Invalid value for 'Identifiant'")
      .setText("Identifiant", "6666666")
      .nextAndCheckError("You must set 'Code (1234)'")
      .setPassword("Code (1234)", "Ab4d5dS")
      .nextAndCheckError("Invalid value for 'Code (1234)'")
      .setPassword("Code (1234)", "1234")
      .nextAndCheckError("Login failed")
      .setText("Identifiant", "1234")
      .next()
      .waitForNotificationAndDownload(mailbox.checkStatementReady("toto@example.com"))
      .checkTransactions(new Object[][]{
        {"2016/08/12", "EDF", "-50.00"},
        {"2016/08/10", "AUCHAN", "-100.00"},
      })
      .importAccountAndComplete();

    budgea.checkLastLogin("website=pro", "password=1234", "login=1234");

    transactions.initContent()
      .add("12/08/2016", TransactionType.PRELEVEMENT, "EDF", "", -50.00, "Electricity")
      .add("10/08/2016", TransactionType.PRELEVEMENT, "AUCHAN", "", -100.00)
      .check();
  }

  @Test
  public void testExistingConnectionsNotShownInBankSelectionList() throws Exception {

    cloud.createSubscription("toto@example.com", Dates.tomorrow());

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
      .checkContainsConnection("Connecteur de test")
      .addConnection()
      .checkBankNotShown("Connecteur de test")
      .close();

    budgea.pushConnectionList(BudgeaConnections.init()
                                .add(1, 123, 40, true, "2016-08-10 17:44:26")
                                .get());
    budgea.pushAccountList(BudgeaAccounts.init()
                             .add(1, 1, "Main account 1", "100200300", true)
                             .get());
    operations.openImportDialog()
      .editCloudConnections()
      .deleteConnection("Connecteur de test")
      .checkNoConnectionsShown()
      .close();
    budgea.checkConnectionDeleted(1);

    budgea.pushNewConnectionResponse(1, 123, 40);
    budgea.pushConnectionList(BudgeaConnections.init().get());
    budgea.pushAccountList(BudgeaAccounts.init().get());
    budgea.pushStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de test", "2016-08-13 14:00:00")
                           .addAccount(1, "Main account 1", "100200300", "checking", 900.00, "2016-08-13 13:00:00")
                           .addTransaction(1, "2016-08-10 13:00:00", -100.00, "AUCHAN")
                           .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
                           .addTransaction(3, "2016-08-13 13:00:00", -100.00, "FNAC", BudgeaCategory.LOISIRS)
                           .endAccount()
                           .endConnection()
                           .get());
    operations.openImportDialog()
      .editCloudConnections()
      .addConnection()
      .selectBank("Connecteur de test")
      .next()
      .setChoice("Type de compte", "Particuliers")
      .setText("Identifiant", "1234")
      .setPassword("Code (1234)", "1234")
      .next()
      .waitForNotificationAndDownload(mailbox.checkStatementReady("toto@example.com"))
      .checkTransactions(new Object[][]{
        {"2016/08/13", "FNAC", "-100.00"},
        {"2016/08/12", "EDF", "-50.00"},
        {"2016/08/10", "AUCHAN", "-100.00"},
      })
      .importAccountAndComplete();

    transactions.initContent()
      .add("13/08/2016", TransactionType.PRELEVEMENT, "FNAC", "", -100.00, "Leisures")
      .add("12/08/2016", TransactionType.PRELEVEMENT, "EDF", "", -50.00, "Electricity")
      .add("10/08/2016", TransactionType.PRELEVEMENT, "AUCHAN", "", -100.00)
      .check();
  }

  @Test
  public void testBankLoginChange() throws Exception {
    cloud.createSubscription("toto@example.com", Dates.tomorrow());

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
      .checkTransactions(new Object[][]{
        {"2016/08/12", "EDF", "-50.00"},
        {"2016/08/10", "AUCHAN", "-100.00"},
      })
      .importAccountAndComplete();

    budgea.sendStatement(BudgeaStatement.init()
                         .addConnection(1, 123, 40, "Connecteur de test", "2016-08-10 17:44:26", "wrongpass")
                         .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-10 13:00:00")
                         .addTransaction(4, "2016-08-14 13:00:00", -75.00, "Auchan")
                         .addTransaction(5, "2016-08-15 10:00:00", -5.00, "CIC", BudgeaCategory.FRAIS_BANCAIRES)
                         .addTransaction(6, "2016-08-16 08:00:00", "2016-08-16 10:00:00", -30.00, "Vroom", "PRLV VROOM SARL", 123456789, "Karting", false)
                         .endAccount()
                         .endConnection()
                         .get());
    mailbox.checkConnectionPasswordAlert("toto@example.com", "Connecteur de test");

    budgea.pushNewConnectionResponse(1, 123, 40);
    budgea.pushConnectionList(BudgeaConnections.init()
                                .add(1, 123, 40, true, "2016-08-10 17:44:26", "wrongpass")
                                .get());
    budgea.pushAccountList(BudgeaAccounts.init()
                             .add(1, 1, "Main account 1", "100200300", true)
                             .get());
    operations.openImportDialog()
      .editCloudConnections()
      .checkConnectionWithPasswordError("Connecteur de test")
      .updatePassword("Connecteur de test")
      .setChoice("Type de compte", "Particuliers")
      .setText("Identifiant", "1234")
      .setPassword("Code (1234)", "6789")
      .next()
      .close();

    budgea.checkLastLogin("website=par", "login=1234", "password=6789");
  }

  public void testErrorDuringAPasswordUpdate() throws Exception {
    cloud.createSubscription("toto@example.com", Dates.tomorrow());

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
      .checkTransactions(new Object[][]{
        {"2016/08/12", "EDF", "-50.00"},
        {"2016/08/10", "AUCHAN", "-100.00"},
      })
      .importAccountAndComplete();

    budgea.pushConnectionList(BudgeaConnections.init()
                                .add(1, 123, 40, true, "2016-08-10 17:44:26", "wrongpass")
                                .get());
    budgea.pushAccountList(BudgeaAccounts.init()
                             .add(1, 1, "Main account 1", "100200300", true)
                             .get());
    budgea.pushNewConnectionResponse(1, 123, 40, "wrongpass");
    CloudBankConnectionChecker bankConnection = operations.openImportDialog()
      .editCloudConnections()
      .updatePassword("Connecteur de test")
      .setChoice("Type de compte", "Particuliers")
      .setText("Identifiant", "1234")
      .setPassword("Code (1234)", "6789")
      .nextAndCheckError("Login failed");

    budgea.pushNewConnectionResponse(1, 123, 40);
    bankConnection
      .setPassword("Code (1234)", "2345")
      .next()
      .close();

    budgea.checkLastLogin("website=par", "login=1234", "password=2345");
  }
}
