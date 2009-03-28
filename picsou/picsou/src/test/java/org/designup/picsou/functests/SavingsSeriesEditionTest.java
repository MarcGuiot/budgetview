package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.SeriesEditionDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.MasterCategory;
import org.uispec4j.interception.WindowInterceptor;
import org.uispec4j.*;

import javax.swing.*;
import java.awt.*;
import java.awt.Button;

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
      .setFromAccount("Main accounts")
      .setToAccount("Epargne LCL")
      .setName("Epargne")
      .setCategory(MasterCategory.SAVINGS)
      .checkOkEnabled(true)
      .validate();

    budgetView.savings
      .editSeries("Epargne")
      .checkToAccount("Epargne LCL")
      .switchToManual()
      .setToAccount("Main accounts")
      .setFromAccount("Epargne LCL")
      .checkAmountsRadioAreNotVisible()
      .selectMonth(200808)
      .setAmount("100")
      .checkAmount("100")
      .setFromAccount("Main accounts")
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
      .setFromAccount("Main accounts")
      .setToAccount("Epargne LCL")
      .setCategory(MasterCategory.SAVINGS)
      .checkOkEnabled(true)
      .validate();

    budgetView.savings
      .createSeries()
      .setName("Veranda")
      .setToAccount("Main accounts")
      .setFromAccount("Epargne CA")
      .setCategory(MasterCategory.HOUSE)
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
      .checkToAccount("Main accounts")
      .checkSingleMonthSelected()
      .selectSeries("Epargne")
      .checkFromAccount("Main accounts")
      .checkToAccount("Epargne LCL")
      .checkInAutomatic()
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
      .setFromAccount("Main accounts")
      .setToAccount("Epargne LCL")
      .setCategory(MasterCategory.SAVINGS)
      .checkOkEnabled(true)
      .validate();

    views.selectCategorization();

    categorization.selectTableRows("McDo")
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
      .setCategory(MasterCategory.SAVINGS)
      .setFromAccount("Main account")
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
      .setFromAccount("Main accounts")
      .setToAccount("Epargne LCL")
      .setName("Epargne")
      .setCategory(MasterCategory.SAVINGS)
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
      .setCategory(MasterCategory.SAVINGS)
      .setFromAccount("Main account")
      .setToAccount("Account n. 111")
      .validate();
    views.selectCategorization();
    categorization.selectTableRow(1)
      .selectSavings()
      .editSeries("CA", true)
      .setName("Autre")
      .validate();

    views.selectBudget();
    Component[] seriesButtons = budgetView.savings.getPanel().getSwingComponents(JButton.class, "Autre");
    assertEquals(2, seriesButtons.length);

    SeriesEditionDialogChecker firstSeriesChecker = getSeriesChecker(seriesButtons[0]);
    firstSeriesChecker.switchToManual().selectAllMonths().setAmount(50).validate();
    SeriesEditionDialogChecker secondSeriesChecker = getSeriesChecker(seriesButtons[1]);
    secondSeriesChecker.checkInManual()
      .switchToAutomatic()
      .validate();
    firstSeriesChecker = getSeriesChecker(seriesButtons[0]);
    firstSeriesChecker.checkInAutomatic().validate();
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
      .setCategory(MasterCategory.SAVINGS)
      .checkFromAccount("Main accounts")
      .checkToAccount("External account")
      .setFromAccount("External account")
      .checkSavingsMessageVisibility(true)
      .checkOkEnabled(false)
      .setFromAccount("Main accounts")
      .checkOkEnabled(true)
      .validate();
  }


  private SeriesEditionDialogChecker getSeriesChecker(Component component) {
    final org.uispec4j.Button button = new org.uispec4j.Button((JButton)component);
    return SeriesEditionDialogChecker.open(button, true);
  }

}
