package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public class SavingsTest extends LoggedInFunctionalTestCase {

  public void testCreateSavingsInMainAccountCreateSeriesInSavingAccount() throws Exception {
    operations.getPreferences().changeFutureMonth(2).validate();
    OfxBuilder.init(this)
      .addTransaction("2008/06/10", -100.00, "Virement")
      .addTransaction("2008/07/10", -100.00, "Virement")
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();
    timeline.selectAll();
    views.selectHome();
    accounts.createMain()
      .setAccountName("Epargne LCL")
      .setAccountNumber("1234")
      .selectBank("LCL")
      .setAsSavings()
      .setBalance(1000)
      .setAsNotImported()
      .checkNotImported()
      .validate();
    views.selectCategorization();
    categorization
      .selectTableRows("Virement")
      .selectSavings()
      .createSavingsSeries()
      .setName("Epargne")
      .setCategories(MasterCategory.SAVINGS)
      .selectSavingsAccount("Epargne LCL")
      .validate();
    views.selectBudget();
    budgetView.savings
      .checkSeries("Epargne", 300, 3000)
      .checkSeries("Savings:Epargne", 300, 300);
    views.selectData();
    transactions.initContent()
      .add("10/10/2008", TransactionType.PLANNED, "Planned: Savings:Epargne", "", 100.00, "Savings:Epargne", MasterCategory.SAVINGS)
      .add("10/10/2008", TransactionType.PLANNED, "Planned: Epargne", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/09/2008", TransactionType.PLANNED, "Planned: Savings:Epargne", "", 100.00, "Savings:Epargne", MasterCategory.SAVINGS)
      .add("10/09/2008", TransactionType.PLANNED, "Planned: Epargne", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.VIREMENT, "Virement", "", 100.00, "Savings:Epargne", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/07/2008", TransactionType.VIREMENT, "Virement", "", 100.00, "Savings:Epargne", MasterCategory.SAVINGS)
      .add("10/07/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/06/2008", TransactionType.VIREMENT, "Virement", "", 100.00, "Savings:Epargne", MasterCategory.SAVINGS)
      .add("10/06/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .check();
  }

  public void testCreateSavingsAndReEdit() throws Exception {
    fail();
  }


  public void testSwitchingToManualSwitchPendingSeriesAndViceVersa() throws Exception {
    fail();
  }

  public void testInManualCreateSavingsInMainAccountCreateSeriesInSavingAccount() throws Exception {
    fail();
  }

  public void testCreateManySavingsSeriesAndSwitchPreserveAccountAndSeries() throws Exception {
    fail("codé");
  }

  public void testOnlyNotAssociatedSeriesAreShown() throws Exception {
    fail("codé");
  }

  public void testChangeInMainAccountSavingsSeriesAreReportedToSavingsAccountSeries() throws Exception {
    fail("codé");
  }

  public void testChangeInSavingsAccountSeriesAreReportedToMainAccountSeries() throws Exception {
    fail("codé");
  }

  public void testSplitPreviouslyCategorizedInSavings() throws Exception {
    fail("codé");
  }

  public void testSavingSeriesNotAssociatedToMainAccountSeriesOfNotImportedAccountCreateTransaction() throws Exception {
    fail("non codé");
  }


  public void testVirementFromSavingsToMainAccount() throws Exception {
    fail("non codé");
  }

  public void testSavingsAccountBalance() throws Exception {
    fail("non codé");
  }

  public void testPositiveBudgetInEnveloppe() throws Exception {
    fail("vieux code, il y a peut-etre deja un test");
  }
}
