package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public class SavingsTest extends LoggedInFunctionalTestCase {

  public void testCreateSavingsInMainAccount() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder.init(this)
      .addTransaction("2008/06/10", -100.00, "Virement")
      .addTransaction("2008/07/10", -100.00, "Virement")
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();
    timeline.selectAll();
    views.selectHome();
    createSavingsAccount("Epargne LCL", 1000);
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
      .checkSeries("Epargne", -300, -500);
    views.selectData();
    transactions.initContent()
      .add("10/10/2008", TransactionType.PLANNED, "Planned: Epargne", "", 100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/10/2008", TransactionType.PLANNED, "Planned: Epargne", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/09/2008", TransactionType.PLANNED, "Planned: Epargne", "", 100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/09/2008", TransactionType.PLANNED, "Planned: Epargne", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.VIREMENT, "Virement", "", 100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/07/2008", TransactionType.VIREMENT, "Virement", "", 100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/07/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/06/2008", TransactionType.VIREMENT, "Virement", "", 100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/06/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .check();
    views.selectHome();
    timeline.selectMonth("2008/08");
    savingsAccountView.checkPosition("Epargne", 1000);
    timeline.selectMonth("2008/09");
    savingsAccountView.checkPosition("Epargne", 1100);
    timeline.selectMonth("2008/10");
    savingsAccountView.checkPosition("Epargne", 1200);
  }

  private void createSavingsAccount(final String name, final int balance) {
    savingsAccounts.createNewAccount()
      .setAccountName(name)
      .setAccountNumber("1234")
      .selectBank("LCL")
      .setAsSavings()
      .checkIsSavings()
      .setBalance(balance)
      .validate();
  }

  public void testCreateSavingsSeriesAndPayFromSavings() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder.init(this)
      .addTransaction("2008/06/10", -100.00, "Virement")
      .addTransaction("2008/07/10", -100.00, "Virement")
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();
    timeline.selectAll();
    views.selectHome();
    createSavingsAccount("Epargne LCL", 1000);
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
    budgetView.savings.createSeries()
      .setName("Achat Tele")
      .setCategories(MasterCategory.EQUIPMENT)
      .selectSavingsAccount("Epargne LCL")
      .setSavingsToMain()
      .switchToManual()
      .selectMonth(200810)
      .setAmount(300)
      .validate();
    timeline.selectMonth("2008/10");
    views.selectHome();
    savingsAccountView.checkPosition("Epargne", 900);
  }

  public void testSavingsAccountBalance() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectHome();
    savingsAccounts.createNewAccount()
      .setAsSavings()
      .setAccountName("Epargne LCL")
      .selectBank("LCL")
      .setBalance(1000)
      .validate();

    views.selectBudget();
    budgetView.savings
      .createSeries()
      .setMainToSavings()
      .setName("Epargne")
      .setCategory(MasterCategory.SAVINGS)
      .selectSavingsAccount("Epargne LCL")
      .switchToManual()
      .selectAllMonths()
      .setAmount("200")
      .validate();

    budgetView.savings
      .createSeries()
      .setCategories(MasterCategory.HOUSE)
      .setName("Travaux")
      .switchToManual()
      .setSavingsToMain()
      .checkPositiveAmountsSelected()
      .checkAmountsRadioAreNotVisible()
      .selectSavingsAccount("Epargne LCL")
      .selectMonth(200810)
      .setAmount("400")
      .validate();

    views.selectHome();
    timeline.selectMonth("2008/10");
    savingsAccountView.checkPosition("Epargne LCL", 1200);
  }

  public void testCreateSavingsSeriesAndAssociateLaterToAccount() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/10", -100.00, "Virement")
      .addTransaction("2008/07/10", -100.00, "Virement")
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();
    timeline.selectAll();
    views.selectHome();

    views.selectCategorization();
    categorization
      .selectTableRows("Virement")
      .selectSavings()
      .createSavingsSeries()
      .setName("Epargne")
      .setCategories(MasterCategory.SAVINGS)
      .validate();

    views.selectData();
    transactions.initContent()
      .add("10/10/2008", TransactionType.PLANNED, "Planned: Epargne", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/09/2008", TransactionType.PLANNED, "Planned: Epargne", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/07/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/06/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .check();

    views.selectHome();
    savingsAccounts.createNewAccount()
      .setAsSavings()
      .setAccountName("Epargne LCL")
      .selectBank("LCL")
      .setBalance(1000)
      .validate();

    views.selectBudget();
    budgetView.savings.editSeries("Epargne")
      .selectSavingsAccount("Epargne LCL")
      .validate();
    views.selectData();
    transactions.initContent()
      .add("10/10/2008", TransactionType.PLANNED, "Planned: Epargne", "", 100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/10/2008", TransactionType.PLANNED, "Planned: Epargne", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/09/2008", TransactionType.PLANNED, "Planned: Epargne", "", 100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/09/2008", TransactionType.PLANNED, "Planned: Epargne", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.VIREMENT, "Virement", "", 100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/07/2008", TransactionType.VIREMENT, "Virement", "", 100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/07/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/06/2008", TransactionType.VIREMENT, "Virement", "", 100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/06/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .check();
  }

  public void testSplitPreviouslyCategorizedInSavings() throws Exception {
    fail("codé");
  }

  public void testPositiveBudgetInEnveloppe() throws Exception {
    fail("vieux code, il y a peut-etre deja un test");
  }
}
