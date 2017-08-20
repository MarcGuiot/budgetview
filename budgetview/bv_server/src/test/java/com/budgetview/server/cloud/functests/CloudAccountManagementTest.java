package com.budgetview.server.cloud.functests;

import com.budgetview.model.TransactionType;
import com.budgetview.server.cloud.functests.testcases.CloudDesktopTestCase;
import com.budgetview.server.cloud.stub.BudgeaStatement;
import com.budgetview.shared.cloud.budgea.BudgeaCategory;
import org.globsframework.utils.Dates;
import org.junit.Test;

public class CloudAccountManagementTest extends CloudDesktopTestCase {

  @Test
  public void testEmptyAccountsAreAutomaticallySkipped() throws Exception {
    cloud.createSubscription("toto@example.com", Dates.tomorrow());

    budgea.pushConnectionResponse(1, 123, 40);
    budgea.pushStatement(BudgeaStatement.init()
                           .addConnection(1, 123, 40, "Connecteur de test", "2016-08-10 17:44:26")
                           .addAccount(1, "Main account 1", "100200300", "checking", 1000.00, "2016-08-10 13:00:00")
                           .addTransaction(1, "2016-08-10 13:00:00", -100.00, "AUCHAN")
                           .addTransaction(2, "2016-08-12 17:00:00", -50.00, "EDF", BudgeaCategory.ELECTRICITE)
                           .endAccount()
                           .addAccount(2, "Main account 2", "200300400", "checking", 2000.00, "2016-08-10 11:00:00")
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
      .checkNewAccountSelected()
      .importAndPreviewNextAccount()
      .checkNoTransactions()
      .checkSkipFileSelected()
      .checkSkippedFileMessage()
      .importAccountAndComplete();

    mainAccounts.checkAccounts("Main account 1");

    transactions.initContent()
      .add("12/08/2016", TransactionType.PRELEVEMENT, "EDF", "", -50.00, "Electricity")
      .add("10/08/2016", TransactionType.PRELEVEMENT, "AUCHAN", "", -100.00)
      .check();

    budgea.checkAccountUpdates("account:2 => disabled:1");

    fail("TODO : on peut voir et réactiver les comptes à partir de la vue d'édition de la connexion cloud");
    fail("TODO: montrer les comptes disabled puis les réselectionner, refaire l'import et les retrouver " +
         "- cela signifie de refaire une passe sur tous les updates côté serveur en filtrant les comptes");
  }

  @Test
  public void testDeletedAccountsAreAutomaticallySetToDisabledDuringDownload() throws Exception {
    fail("TODO: si un compte (nouveau ou existant) est taggé deleted il est affiché dans la liste des comptes disabled dans la vue Completion");
  }

  @Test
  public void testDeletedAccountsLinkedToACLoudDownloadAreAutomaticallyDisabled() throws Exception {
    fail("Que se passe-t-il si undo ? Le griser, ou pousser l'action inverse ?");
  }
}
