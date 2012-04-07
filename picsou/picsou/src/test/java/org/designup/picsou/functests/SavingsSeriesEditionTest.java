package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.SeriesEditionDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.BankEntity;

public class SavingsSeriesEditionTest extends LoggedInFunctionalTestCase {

  public void testSavings() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/04", -10.00, "McDo")
      .load();

    timeline.selectLast();
    views.selectHome();
    savingsAccounts.createNewAccount().setAsSavings()
      .setAccountName("Epargne LCL")
      .selectBank("LCL")
      .setPosition(1000)
      .validate();

    views.selectBudget();
    budgetView.savings
      .createSeries()
      .setFromAccount("Main accounts")
      .setToAccount("Epargne LCL")
      .setName("Epargne")
      .validate();

    budgetView.savings
      .editSeries("Epargne")
      .checkToAccount("Epargne LCL")
      .setToAccount("Main accounts")
      .setFromAccount("Epargne LCL")
      .checkAmountTogglesAreNotVisible()
      .selectMonth(200808)
      .setAmount("100")
      .checkAmount("100")
      .setFromAccount("Main accounts")
      .setToAccount("Epargne LCL")
      .checkAmount("100")
      .validate();
  }

  public void testSwitchingBetweenSavingsSeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/04", -10.00, "McDo")
      .load();

    operations.openPreferences().setFutureMonthsCount(3).validate();
    timeline.selectLast();
    views.selectHome();
    savingsAccounts.createNewAccount().setAsSavings()
      .setAccountName("Epargne LCL")
      .selectBank("LCL")
      .setPosition(1000)
      .validate();

    savingsAccounts.createNewAccount().setAsSavings()
      .setAccountName("Epargne CA")
      .selectBank("Crédit Agricole")
      .setPosition(1000)
      .validate();

    views.selectBudget();
    budgetView.savings
      .createSeries()
      .setName("Epargne")
      .setFromAccount("Main accounts")
      .setToAccount("Epargne LCL")
      .checkOkEnabled(true)
      .validate();

    budgetView.savings
      .createSeries()
      .setName("Veranda")
      .setToAccount("Main accounts")
      .setFromAccount("Epargne CA")
      .setSingleMonth()
      .setSingleMonthDate(200810)
      .selectMonth(200810)
      .setAmount(10000)
      .validate();

    timeline.selectMonth("2008/10");
    budgetView.savings.editSeries("Veranda")
      .checkSingleMonthSelected()
      .checkSingleMonthDate("Oct 2008")
      .checkFromAccount("Epargne CA")
      .checkToAccount("Main accounts")
      .checkSingleMonthSelected()
      .cancel();

    budgetView.savings.editSeries("Epargne")
      .checkFromAccount("Main accounts")
      .checkToAccount("Epargne LCL")
      .cancel();
  }

  public void testSwitchingBetweenManualAndAutomatic() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/04", -10.00, "McDo")
      .load();

    operations.openPreferences().setFutureMonthsCount(3).validate();
    timeline.selectLast();
    views.selectHome();
    savingsAccounts.createNewAccount().setAsSavings()
      .setAccountName("Epargne LCL")
      .selectBank("LCL")
      .setPosition(1000)
      .validate();

    views.selectBudget();
    budgetView.savings
      .createSeries()
      .setName("Epargne")
      .setFromAccount("Main accounts")
      .setToAccount("Epargne LCL")
      .validate();

    views.selectCategorization();

    categorization.selectTransactions("McDo")
      .selectSavings()
      .editSeries("Epargne")
      .setToAccount("External account")
      .checkToAccount("External account")
      .cancel();

  }

  public void testMirrorSeriesAreNotVisibleInSeriesList() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 1000., "2008/08/10")
      .addTransaction("2008/08/10", 100.00, "Virement")
      .load();
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();

    views.selectHome();
    this.mainAccounts.edit("Account n. 111")
      .setAsSavings()
      .validate();

    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("CA")
      .setFromAccount("Main accounts")
      .setToAccount("Account n. 111")
      .validate();

    budgetView.savings
      .checkSeriesPresent("CA", "From Account n. 111", "To Account n. 111");
  }

  public void testUseSingleMonthCreateSeriesBudget() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/04", -10.00, "McDo")
      .load();

    operations.openPreferences().setFutureMonthsCount(3).validate();
    timeline.selectLast();
    views.selectHome();
    savingsAccounts.createNewAccount().setAsSavings()
      .setAccountName("Epargne LCL")
      .selectBank("LCL")
      .setPosition(1000)
      .validate();
    views.selectBudget();

    budgetView.savings
      .createSeries()
      .setFromAccount("Main accounts")
      .setToAccount("Epargne LCL")
      .setName("Epargne")
      .setSixMonths()
      .setSingleMonth()
      .setSingleMonthDate(200810)
      .checkChart(new Object[][]{
        {"2008", "October", 0.00, 0.00, true}})
      .validate();
  }

  public void testEditingMirrorSeriesRedirectToMainEdit() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 1000., "2008/08/10")
      .addTransaction("2008/08/10", 100.00, "Virement")
      .load();
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();

    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectHome();
    this.mainAccounts.edit("Account n. 111")
      .setAsSavings()
      .validate();
    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("CA")
      .setFromAccount("Main accounts")
      .setToAccount("Account n. 111")
      .validate();
    views.selectCategorization();
    categorization.selectTableRow(0)
      .selectSavings()
      .editSeries("CA")
      .setName("Other")
      .validate();

    views.selectBudget();
    budgetView.savings.checkSeriesPresent("Other");

    SeriesEditionDialogChecker firstSeriesChecker =
      budgetView.savings.editSeries("Other");
    firstSeriesChecker.selectAllMonths().setAmount(50).validate();
    savingsView.editSeries("Account n. 111", "CA")
      .checkName("Other")
      .validate();
    views.selectBudget();
    firstSeriesChecker = budgetView.savings.editSeries("Other");
    firstSeriesChecker.validate();
  }

  public void testToAndFromInExternalIsNotPossible() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/04", -10.00, "McDo")
      .load();

    timeline.selectLast();
    views.selectHome();
    savingsAccounts.createNewAccount().setAsSavings()
      .setAccountName("Epargne LCL")
      .selectBank("LCL")
      .setPosition(1000)
      .validate();

    views.selectBudget();
    budgetView.savings
      .createSeries()
      .setName("Epargne")
      .checkFromAccount("")
      .checkToAccount("")
      .checkSavingsMessageVisibility(true)
      .checkOkEnabled(false)
      .setFromAccount("Main accounts")
      .setToAccount("External account")
      .checkOkEnabled(true)
      .validate();
  }

  public void testSavingsSeriesDescriptionsAreShownInTooltips() throws Exception {
    views.selectHome();
    savingsAccounts.createNewAccount().setAsSavings()
      .setAccountName("Epargne LCL")
      .selectBank("LCL")
      .setPosition(1000)
      .validate();

    savingsView.createSeries()
      .setName("Savings Plan")
      .setFromAccount("Epargne LCL")
      .setToAccount("External account")
      .setDescription("Savings for the kids")
      .validate();
    savingsView.checkSeriesTooltip("Epargne LCL", "Savings Plan", "Savings for the kids");

    savingsView.editSeries("Epargne LCL", "Savings Plan")
      .checkDescription("Savings for the kids")
      .setDescription("Savings for the Porsche")
      .validate();
    savingsView.checkSeriesTooltip("Epargne LCL", "Savings Plan", "Savings for the Porsche");

    savingsView.editSeries("Epargne LCL", "Savings Plan")
      .setDescription("")
      .validate();
    savingsView.checkSeriesTooltip("Epargne LCL", "Savings Plan", "");
  }
}
