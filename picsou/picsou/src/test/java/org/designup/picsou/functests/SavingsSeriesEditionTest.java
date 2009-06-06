package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.SeriesEditionDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.Bank;

import javax.swing.*;
import java.awt.*;

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
      .setBalance(1000)
      .validate();

    views.selectBudget();
    budgetView.savings
      .createSeries()
      .setFromAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setToAccount("Epargne LCL")
      .setName("Epargne")
      .validate();

    budgetView.savings
      .editSeries("Epargne")
      .checkToAccount("Epargne LCL")
      .switchToManual()
      .setToAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setFromAccount("Epargne LCL")
      .checkAmountsRadioAreNotVisible()
      .selectMonth(200808)
      .setAmount("100")
      .checkAmount("100")
      .setFromAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setToAccount("Epargne LCL")
      .checkAmount("100")
      .validate();
  }

  public void testSwitchBetweenSavingsSeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/04", -10.00, "McDo")
      .load();

    operations.openPreferences().setFutureMonthsCount(3).validate();
    timeline.selectLast();
    views.selectHome();
    savingsAccounts.createNewAccount().setAsSavings()
      .setAccountName("Epargne LCL")
      .selectBank("LCL")
      .setBalance(1000)
      .validate();

    savingsAccounts.createNewAccount().setAsSavings()
      .setAccountName("Epargne CA")
      .selectBank("CA")
      .setBalance(1000)
      .validate();

    views.selectBudget();
    budgetView.savings
      .createSeries()
      .setName("Epargne")
      .setFromAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setToAccount("Epargne LCL")
      .checkOkEnabled(true)
      .validate();

    budgetView.savings
      .createSeries()
      .setName("Veranda")
      .setToAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setFromAccount("Epargne CA")
      .switchToManual()
      .setSingleMonth()
      .setSingleMonthDate(200810)
      .selectMonth(200810)
      .setAmount(10000)
      .validate();

    budgetView.savings.editSeriesList()
      .selectSeries("Veranda")
      .checkSingleMonthSelected()
      .checkSingleMonthDate("Oct 2008")
      .checkFromAccount("Epargne CA")
      .checkToAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .checkSingleMonthSelected()
      .selectSeries("Epargne")
      .checkFromAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .checkToAccount("Epargne LCL")
      .checkAutomaticModeSelected()
      .cancel();

    views.selectCategorization();
  }


  public void testSwitchBetweenManualAndAutomatic() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/04", -10.00, "McDo")
      .load();

    operations.openPreferences().setFutureMonthsCount(3).validate();
    timeline.selectLast();
    views.selectHome();
    savingsAccounts.createNewAccount().setAsSavings()
      .setAccountName("Epargne LCL")
      .selectBank("LCL")
      .setBalance(1000)
      .validate();

    views.selectBudget();
    budgetView.savings
      .createSeries()
      .setName("Epargne")
      .setFromAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setToAccount("Epargne LCL")
      .validate();

    views.selectCategorization();

    categorization.selectTransactions("McDo")
      .selectSavings()
      .editSeries("Epargne", true)
      .setToAccount("External account")
      .switchToManual()
      .checkToAccount("External account")
      .cancel();

  }

  public void testMirrorSeriesAreNotVisibleInSeriesList() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(Bank.GENERIC_BANK_ID, 111, "111", 1000., "2008/08/10")
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
      .setFromAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setToAccount("Account n. 111")
      .validate();

    budgetView.savings.editSeriesList()
      .checkSeriesListEquals("CA")
      .validate();
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
      .setBalance(1000)
      .validate();
    views.selectBudget();

    budgetView.savings
      .createSeries()
      .setFromAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setToAccount("Epargne LCL")
      .setName("Epargne")
      .setSixMonths()
      .setSingleMonth()
      .setSingleMonthDate(200810)
      .switchToManual()
      .checkTable(new Object[][]{
        {"2008", "October", "", "0"}})
      .validate();
  }

  public void testEditingMirrorSeriesRedirectToMainEdit() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(Bank.GENERIC_BANK_ID, 111, "111", 1000., "2008/08/10")
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
      .setFromAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setToAccount("Account n. 111")
      .validate();
    views.selectCategorization();
    categorization.selectTableRow(0)
      .selectSavings()
//      .selectSavingsSeries("CA")
      .editSeries("CA", true)
      .setName("Autre")
      .validate();

//    views.selectSavings();
//    savingsView.checkAmount("Account n. 111", "Autre", 0, 0);

    views.selectBudget();
    budgetView.savings.checkSeriesPresent("Autre");

    SeriesEditionDialogChecker firstSeriesChecker =
      budgetView.savings.editSeries("Autre");
    firstSeriesChecker.switchToManual().selectAllMonths().setAmount(50).validate();
    views.selectSavings();
    SeriesEditionDialogChecker secondSeriesChecker = savingsView.editSavingsSeries("Account n. 111", "CA");
    secondSeriesChecker.checkManualModeSelected()
      .checkName("Autre")
      .switchToAutomatic()
      .validate();
    views.selectBudget();
    firstSeriesChecker = budgetView.savings.editSeries("Autre");
    firstSeriesChecker.checkAutomaticModeSelected().validate();
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
      .setBalance(1000)
      .validate();

    views.selectBudget();
    budgetView.savings
      .createSeries()
      .setName("Epargne")
      .checkFromAccount("External account")
      .checkToAccount("External account")
      .checkSavingsMessageVisibility(true)
      .checkOkEnabled(false)
      .setFromAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .checkOkEnabled(true)
      .validate();
  }


  private SeriesEditionDialogChecker getSeriesChecker(Component component) {
    final org.uispec4j.Button button = new org.uispec4j.Button((JButton)component);
    return SeriesEditionDialogChecker.open(button);
  }

}
