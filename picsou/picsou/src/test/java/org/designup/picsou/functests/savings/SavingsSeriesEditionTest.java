package org.designup.picsou.functests.savings;

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
    accounts.createNewAccount()
      .setAsSavings()
      .setName("Epargne LCL")
      .selectBank("LCL")
      .setPosition(1000)
      .validate();

    views.selectBudget();
    budgetView.savings
      .createSeries()
      .checkTargetAccountNotShown()
      .checkNoTargetAccountWarningHidden()
      .setFromAccount("Account n. 00001123")
      .setToAccount("Epargne LCL")
      .setName("Epargne")
      .validate();

    budgetView.savings
      .editSeries("Epargne")
      .checkTargetAccountNotShown()
      .checkNoTargetAccountWarningHidden()
      .checkToAccount("Epargne LCL")
      .setToAccount("Account n. 00001123")
      .setFromAccount("Epargne LCL")
      .checkAmountTogglesAreNotVisible()
      .selectMonth(200808)
      .setAmount("100")
      .checkAmount("100")
      .setFromAccount("Account n. 00001123")
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
    accounts.createNewAccount()
      .setAsSavings()
      .setName("Epargne LCL")
      .selectBank("LCL")
      .setPosition(1000)
      .validate();

    accounts.createNewAccount()
      .setAsSavings()
      .setName("Epargne CA")
      .selectBank("Crédit Agricole")
      .setPosition(1000)
      .validate();

    views.selectBudget();
    budgetView.savings
      .createSeries()
      .setName("Epargne")
      .setFromAccount("Account n. 00001123")
      .setToAccount("Epargne LCL")
      .checkOkEnabled(true)
      .validate();

    budgetView.savings
      .createSeries()
      .setName("Veranda")
      .setToAccount("Account n. 00001123")
      .setFromAccount("Epargne CA")
      .setRepeatSingleMonth()
      .setSingleMonthDate(200810)
      .selectMonth(200810)
      .setAmount(10000)
      .validate();

    timeline.selectMonth("2008/10");
    budgetView.savings.editSeries("Veranda")
      .checkRepeatsASingleMonth()
      .checkSingleMonthDate("Oct 2008")
      .checkFromAccount("Epargne CA")
      .checkToAccount("Account n. 00001123")
      .checkRepeatsASingleMonth()
      .cancel();

    budgetView.savings.editSeries("Epargne")
      .checkFromAccount("Account n. 00001123")
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
    accounts.createNewAccount()
      .setAsSavings()
      .setName("Epargne LCL")
      .selectBank("LCL")
      .setPosition(1000)
      .validate();

    views.selectBudget();
    budgetView.savings
      .createSeries()
      .setName("Epargne")
      .setFromAccount("Account n. 00001123")
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
      .setFromAccount("Account n. 00001123")
      .setToAccount("Account n. 111")
      .validate();

    budgetView.savings
      .checkSeriesPresent("CA");
  }

  public void testUseSingleMonthCreateSeriesBudget() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/04", -10.00, "McDo")
      .load();

    operations.openPreferences().setFutureMonthsCount(3).validate();
    timeline.selectLast();
    views.selectHome();
    accounts.createNewAccount().setAsSavings()
      .setName("Epargne LCL")
      .selectBank("LCL")
      .setPosition(1000)
      .validate();
    views.selectBudget();

    budgetView.savings
      .createSeries()
      .setFromAccount("Account n. 00001123")
      .setToAccount("Epargne LCL")
      .setName("Epargne")
      .setRepeatEverySixMonths()
      .setRepeatSingleMonth()
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
      .setFromAccount("Account n. 00001123")
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

    budgetView.savings.editSeries("Other")
      .selectAllMonths().setAmount(50).validate();
    fail("Pourquoi il y a avait CA et non Other?");
    savingsView.editSeries("Account n. 111", "Other")
      .checkName("Other")
      .validate();

    mainAccounts.select("Account n. 00001123");
    views.checkBudgetSelected();
    budgetView.savings.editSeries("Other")
      .validate();
  }

  public void testToAndFromInExternalIsNotPossible() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/04", -10.00, "McDo")
      .load();

    timeline.selectLast();
    views.selectHome();
    accounts.createNewAccount().setAsSavings()
      .setName("Epargne LCL")
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
      .setFromAccount("Account n. 00001123")
      .setToAccount("External account")
      .checkOkEnabled(true)
      .validate();
  }

  public void testSavingsSeriesDescriptionsAreShownInTooltips() throws Exception {
    views.selectHome();
    accounts.createNewAccount()
      .setAsSavings()
      .setName("Epargne LCL")
      .selectBank("LCL")
      .setPosition(1000)
      .validate();

    savingsAccounts.select("Epargne LCL");
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
