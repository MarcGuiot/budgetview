package org.designup.picsou.functests.budget;

import org.designup.picsou.functests.checkers.SeriesEditionDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;
import org.uispec4j.Key;
import org.uispec4j.TextBox;

import javax.swing.*;

public class SeriesEditionTest extends LoggedInFunctionalTestCase {

  public void testStandardEdition() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000123", 1000.00, "2008/07/29")
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Free Telecom")
      .load();

    timeline.selectMonth("2008/07");
    transactions.initContent()
      .add("29/07/2008", "01/08/2008", TransactionType.PRELEVEMENT, "Free Telecom", "", -29.00)
      .check();

    categorization.checkTable(new Object[][]{
      {"29/07/2008", "", "Free Telecom", -29.00}
    });
    categorization.setNewRecurring("Free Telecom", "Internet");

    budgetView.recurring.editSeries("Internet")
      .checkTitle("Editing a series")
      .checkName("Internet")
      .setName("Free")
      .checkEditableTargetAccount("Main accounts")
      .checkAvailableTargetAccounts("Account n. 000123", "Main accounts")
      .checkChart(new Object[][]{
        {"2008", "July", 29.00, 29.00, true},
        {"2008", "August", 0.00, 29.00},
      })
      .checkMonthSelected(200807)
      .checkAmountLabel("for july 2008")
      .selectAllMonths()
      .checkAmountLabel("from july 2008")
      .validate();

    budgetView.recurring.checkContent("| Free | 29.00 | 29.00 |");
  }

  public void testSeriesNamesAreTrimmed() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Free")
      .load();

    timeline.selectMonth("2008/07");

    categorization.selectTableRow(0);
    categorization.selectRecurring().createSeries()
      .setNameAndValidate("    Internet   ");

    categorization.selectRecurring().selectSeries("Internet");

    budgetView.recurring.checkSeries("Internet", -29.00, -29.00);
  }

  public void testCurrentMonthsAreInitiallySelectedInBudgetTable() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/29", "2008/08/01", -29.00, "Free Telecom")
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Free Telecom")
      .addTransaction("2008/06/29", "2008/08/01", -29.00, "Free Telecom")
      .addTransaction("2008/05/29", "2008/08/01", -29.00, "Free Telecom")
      .load();

    timeline.selectAll();

    categorization.getTable().selectRowSpan(0, 3);
    categorization.setNewRecurring(0, "Internet");
    categorization.setRecurring(1, "Internet");
    categorization.setRecurring(2, "Internet");
    categorization.setRecurring(3, "Internet");
    timeline.selectMonths("2008/06", "2008/08");
    budgetView.recurring.checkSeries("Internet", -58.00, -58.00);

    budgetView.recurring.editSeries("Internet")
      .checkTitle("Editing a series")
      .checkName("Internet")
      .setName("Free")
      .checkAccountsComboAreHidden()
      .checkChart(new Object[][]{
        {"2008", "May", 29.00, 29.00},
        {"2008", "June", 29.00, 29.00, true},
        {"2008", "July", 29.00, 29.00},
        {"2008", "August", 29.00, 29.00, true},
      })
      .checkSelectedMonths(200808, 200806)
      .validate();

    timeline.checkSelection("2008/06", "2008/08");
    budgetView.recurring.checkSeries("Free", -58.00, -58.00);
  }

  public void testAllMonthsAreSelectedInBudgetTableIfCurrentMonthIsNotFound() throws Exception {

    operations.openPreferences().setFutureMonthsCount(6).validate();

    timeline.selectMonths("2008/10", "2008/11");

    budgetView.variable.createSeries()
      .setName("Groceries")
      .checkSelectedMonths(200808, 200809, 200810, 200811, 200812, 200901, 200902)
      .checkPropagationEnabled()
      .setAmount(200)
      .validate();

    timeline.selectMonth("2008/10");
    budgetView.variable.checkSeries("Groceries", 0, -200.00);

    timeline.selectMonth("2008/11");
    budgetView.variable.checkSeries("Groceries", 0, -200.00);
  }

  public void testCurrentlySelectedMonthsAreSelectedInBudgetTableIfCurrentMonthIsNotFoundForExtraSeries() throws Exception {

    operations.openPreferences().setFutureMonthsCount(6).validate();

    timeline.selectMonths("2008/10", "2008/11");

    budgetView.extras.createSeries()
      .setName("Plumber")
      .checkSelectedMonths(200810, 200811)
      .setAmount(200)
      .validate();

    timeline.selectMonth("2008/10");
    budgetView.extras.checkSeries("Plumber", 0, -200.00);

    timeline.selectMonth("2008/11");
    budgetView.extras.checkSeries("Plumber", 0, -200.00);
  }

  public void testChangingTheAmountForAMonth() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Free Telecom")
      .load();

    timeline.selectMonth("2008/07");
    categorization.setNewRecurring("Free Telecom", "Internet");

    budgetView.recurring.editSeries("Internet")
      .checkChart(new Object[][]{
        {"2008", "July", 29.00, 29.00, true},
        {"2008", "August", 0.00, 29.00},
      })
      .checkMonthSelected(200807)
      .checkAmount(29.00)
      .setAmount("40.00")
      .checkChart(new Object[][]{
        {"2008", "July", 29.00, 40.00, true},
        {"2008", "August", 0.00, 29.00},
      })
      .validate();

    budgetView.recurring.checkSeries("Internet", -29.00, -40.00);

    budgetView.recurring.editSeries("Internet")
      .checkChart(new Object[][]{
        {"2008", "July", 29.00, 40.00, true},
        {"2008", "August", 0.00, 29.00},
      })
      .checkAmountIsSelected()
      .checkMonthSelected(200807)
      .checkAmount(40.00)
      .setAmount("30.00")
      .validate();

    budgetView.recurring.checkSeries("Internet", -29.00, -30.00);
  }

  public void testActivatingMonths() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/29", "2008/08/01", -29.00, "Free Telecom")
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Free Telecom")
      .addTransaction("2008/06/29", "2008/08/01", -29.00, "Free Telecom")
      .addTransaction("2008/05/29", "2008/08/01", -29.00, "France Telecom")
      .load();

    timeline.selectAll();

    categorization.setNewRecurring("Free Telecom", "Internet");

    timeline.selectMonths("2008/08", "2008/06");
    SeriesEditionDialogChecker editionDialogChecker = budgetView.recurring.editSeries("Internet");
    editionDialogChecker
      .setRepeatCustom()
      .toggleMonth("May")
      .checkChart(new Object[][]{
        {"2008", "May", 0.00, 0.00},
        {"2008", "June", 29.00, 29.00, true},
        {"2008", "July", 29.00, 29.00},
        {"2008", "August", 29.00, 29.00, true},
      });
    editionDialogChecker
      .toggleMonth("May")
      .checkChart(new Object[][]{
        {"2008", "June", 29.00, 29.00, true},
        {"2008", "July", 29.00, 29.00},
        {"2008", "August", 29.00, 29.00, true},
      });
    editionDialogChecker
      .toggleMonth("Aug")
      .checkChart(new Object[][]{
        {"2008", "June", 29.00, 29.00, true},
        {"2008", "July", 29.00, 29.00},
        {"2008", "August", 29.00, 29.00, true},
      });
    editionDialogChecker
      .toggleMonth("Aug")
      .checkChart(new Object[][]{
        {"2008", "June", 29.00, 29.00, true},
        {"2008", "July", 29.00, 29.00},
        {"2008", "August", 29.00, 29.00, true},
      })
      .validate();

    budgetView.recurring.editSeries("Internet")
      .checkChart(new Object[][]{
        {"2008", "June", 29.00, 29.00, true},
        {"2008", "July", 29.00, 29.00},
        {"2008", "August", 29.00, 29.00, true},
      })
      .cancel();
  }

  public void testEditingAllTheSeriesForABudgetArea() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .addTransaction("2008/07/10", -50.00, "Monoprix")
      .addTransaction("2008/07/05", -29.00, "Free Telecom")
      .addTransaction("2008/07/04", -55.00, "EDF")
      .addTransaction("2008/07/03", -15.00, "McDo")
      .addTransaction("2008/07/02", 200.00, "WorldCo - Bonus")
      .addTransaction("2008/07/01", 3540.00, "WorldCo")
      .load();

    transactions.initContent()
      .add("12/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -95.00)
      .add("10/07/2008", TransactionType.PRELEVEMENT, "Monoprix", "", -50.00)
      .add("05/07/2008", TransactionType.PRELEVEMENT, "Free Telecom", "", -29.00)
      .add("04/07/2008", TransactionType.PRELEVEMENT, "EDF", "", -55.00)
      .add("03/07/2008", TransactionType.PRELEVEMENT, "McDo", "", -15.00)
      .add("02/07/2008", TransactionType.VIREMENT, "WorldCo - Bonus", "", 200.00)
      .add("01/07/2008", TransactionType.VIREMENT, "WorldCo", "", 3540.00)
      .check();

    categorization.setNewVariable("Auchan", "Groceries");
    categorization.setVariable("Monoprix", "Groceries");
    categorization.setNewRecurring("Free Telecom", "Internet");
    categorization.setNewRecurring("EDF", "Electricity");
    categorization.setExceptionalIncome("WorldCo - Bonus", "Exceptional Income", true);
    categorization.setNewIncome("WorldCo", "Salary");

    budgetView.recurring.checkSeriesList("Electricity", "Internet");
    budgetView.variable.checkSeriesList("Groceries");
    budgetView.income.checkSeriesList("Exceptional Income", "Salary");
  }

  public void testCreatingANewSeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Free Telecom")
      .load();

    timeline.selectMonth("2008/07");

    budgetView.recurring
      .createSeries()
      .checkTitle("Creating a series")
      .checkNameIsSelected()
      .checkName("New series")
      .setName("Free Telecom")
      .selectAllMonths()
      .setAmount("40")
      .checkChart(new Object[][]{
        {"2008", "July", 0.0, 40.00, true},
        {"2008", "August", 0.00, 40.00, true},
      })
      .validate();

    budgetView.recurring.checkSeries("Free Telecom", -0.0, -40.0);
  }

  public void testCancel() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .load();

    budgetView.variable.createSeries()
      .setName("A series")
      .cancel();

    budgetView.variable.checkNoSeriesShown();
  }

  public void testEsc() throws Exception {
    SeriesEditionDialogChecker edition = budgetView.variable.createSeries();
    edition.editStartDate().pressEscapeKey();
    Thread.sleep(50);
    edition.checkNoStartDate();
    edition.cancel();
  }

  public void testEditDate() throws Exception {
    budgetView.variable.createSeries()
      .setStartDate(200809)
      .setEndDate(200810)
      .checkStartDate("september 2008")
      .checkEndDate("october 2008")
      .clearStartDate()
      .clearEndDate()
      .checkNoStartDate()
      .checkNoEndDate()
      .cancel();
  }

  public void testStartEndCalendar() throws Exception {
    SeriesEditionDialogChecker edition = budgetView.variable.createSeries()
      .setStartDate(200809);
    edition
      .editStartDate()
      .checkIsEnabled(200806, 200810)
      .cancel();
    edition.editEndDate()
      .checkIsEnabled(200809, 200810)
      .checkIsDisabled(200808)
      .cancel();
    edition.setEndDate(200811);
    edition.editStartDate()
      .checkIsDisabled(200812)
      .checkIsEnabled(200811, 200805)
      .cancel();

    edition.editEndDate()
      .checkIsDisabled(200808)
      .checkIsEnabled(200809, 200901)
      .cancel();

    edition.clearStartDate();
    edition.editEndDate()
      .checkIsEnabled(200701, 200901)
      .cancel();

    edition.cancel();
  }

  public void testStartEndDateWithTransaction() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .load();

    categorization.setNewVariable("Auchan", "Courant");
    SeriesEditionDialogChecker dialog = budgetView.variable.editSeries("Courant");
    dialog.editStartDate()
      .checkIsEnabled(200805, 200806, 200807)
      .checkIsDisabled(200808, 200809)
      .selectMonth(200806);

    dialog.editEndDate()
      .checkIsDisabled(200805, 200806)
      .checkIsEnabled(200807, 200808)
      .selectMonth(200808);
    dialog.cancel();
  }

  public void testAStartAndEndDateIsAutomaticallySetWhenASingleMonthPeriodicityIsChosen() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/15", -95.00, "Auchan")
      .addTransaction("2008/08/15", -95.00, "Auchan")
      .load();

    timeline.selectMonth("2008/06");
    budgetView.variable.createSeries()
      .setName("groceries")
      .checkNoStartDate()
      .checkNoEndDate()
      .setRepeatSingleMonth()
      .checkSingleMonth("june 2008")
      .validate();

    timeline.selectAll();
    transactions.initContent()
      .add("15/08/2008", TransactionType.PRELEVEMENT, "Auchan", "", -95.00)
      .add("15/06/2008", TransactionType.PRELEVEMENT, "Auchan", "", -95.00)
      .check();
  }

  public void testSingleMonthPeriodicityCausesTheStartAndEndDateToRemainTheSame() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/15", -95.00, "Auchan")
      .addTransaction("2008/08/15", -95.00, "Auchan")
      .load();

    timeline.selectMonth("2008/07");
    budgetView.variable.createSeries()
      .setName("groceries")
      .setStartDate(200806)
      .setEndDate(200808)
      .setRepeatSingleMonth()
      .checkSingleMonth("june 2008")
      .checkMonthSelectorsVisible(false)
      .setSingleMonthDate(200808)
      .checkSingleMonth("august 2008")
      .setRepeatEveryMonth()
      .checkStartDate("august 2008")
      .checkEndDate("august 2008")
      .setRepeatSingleMonth()
      .setSingleMonthDate(200807)
      .checkSingleMonth("july 2008")
      .validate();
  }

  public void testUsingSingleMonthPeriodicityInManualMode() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/15", -95.00, "Auchan")
      .addTransaction("2008/08/15", -95.00, "Auchan")
      .load();

    timeline.selectMonth("2008/06");
    budgetView.variable.createSeries()
      .setName("manualThenSingleMonth")
      .setRepeatSingleMonth()
      .checkSingleMonth("june 2008")
      .checkChart(new Object[][]{
        {"2008", "June", 0.00, 0.00, true}
      })
      .validate();

    budgetView.variable.createSeries()
      .setName("singleMonthThenManual")
      .setRepeatSingleMonth()
      .checkSingleMonth("june 2008")
      .checkChart(new Object[][]{
        {"2008", "June", 0.00, 0.00, true}
      })
      .validate();
  }

  public void testDateAndBudgetSeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2007/02/10", -29.00, "Free Telecom")
      .load();

    SeriesEditionDialogChecker edition = budgetView.recurring.createSeries();
    edition.setRepeatCustom()
      .toggleMonth("Jan", "Mar", "Jul", "Sep", "Nov");

    edition.selectAllMonths()
      .setAmount("30");

    // L'histoChart ne montre qu'une partie des mois
    edition.setStartDate(200709)
      .checkChart(new Object[][]{
        {"2007", "October", 0.00, 30.00, true},
        {"2007", "December", 0.00, 30.00, true},
        {"2008", "February", 0.00, 30.00, true},
      });

    edition.setEndDate(200801)
      .checkChart(new Object[][]{
        {"2007", "October", 0.00, 30.00, true},
        {"2007", "December", 0.00, 30.00, true},
      });

    edition.setEndDate(200802)
      .checkChart(new Object[][]{
        {"2007", "October", 0.00, 30.00, true},
        {"2007", "December", 0.00, 30.00, true},
        {"2008", "February", 0.00, 0.00, true},
      });

    edition.setStartDate(200712)
      .checkChart(new Object[][]{
        {"2007", "December", 0.00, 30.00, true},
        {"2008", "February", 0.00, 0.00, true},
      });
    edition.selectAllMonths().setAmount("30")
      .toggleMonth("Dec", "Feb").toggleMonth("Dec", "Feb")
      .checkChart(new Object[][]{
        {"2007", "December", 0.00, 30.00, true},
        {"2008", "February", 0.00, 30.00, true},
      });
    edition.cancel();
  }

  public void testMonthIsHiddenIfLessThanOneMonthInDateRange() throws Exception {
    SeriesEditionDialogChecker edition = budgetView.variable.createSeries();
    edition.setStartDate(200705)
      .setEndDate(200709)
      .checkMonthsEnabled("May", "Jun", "Jul", "Aug", "Sep")
      .checkMonthsDisabled("Feb", "Mar", "Apr", "Oct")
      .setEndDate(200802)
      .checkMonthsEnabled("Feb", "May", "Jun", "Jul", "Aug", "Sep")
      .checkMonthsDisabled("Mar", "Apr")
      .cancel();
  }

  public void testRenamingASeries() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -60, "Free")
      .load();

    categorization.selectTableRow(0);
    categorization.selectRecurring().createSeries()
      .setName("Internet")
      .validate();

    timeline.selectMonths("2008/06", "2008/07");
    transactions
      .showPlannedTransactions()
      .initContent()
      .add("27/07/2008", TransactionType.PLANNED, "Planned: Internet", "", -60.00, "Internet")
      .add("30/06/2008", TransactionType.PRELEVEMENT, "Free", "", -60.00, "Internet")
      .check();

    categorization.editSeries("Internet")
      .setName("Leisures")
      .validate();
    categorization.getRecurring().checkContainsSeries("Leisures");

    transactions.initContent()
      .add("27/07/2008", TransactionType.PLANNED, "Planned: Leisures", "", -60.00, "Leisures")
      .add("30/06/2008", TransactionType.PRELEVEMENT, "Free", "", -60.00, "Leisures")
      .check();
  }

  public void testRenameRecurrentFromBudgetView() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -60, "Forfait Kro")
      .load();

    categorization.setNewRecurring(0, "Drinking");

    budgetView.recurring.editSeries("Drinking")
      .setName("Leisures")
      .validate();

    categorization.selectRecurring().checkContainsSeries("Leisures");
  }

  public void testFillNameAndAmountWithKeyPressed() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", 10, "Auchan")
      .load();

    SeriesEditionDialogChecker edition = budgetView.variable.createSeries();
    edition.setName(null)
      .setAmount(null)
      .getNameBox().pressKey(Key.shift(Key.A)).pressKey(Key.shift(Key.A));

    edition.selectAllMonths();

    TextBox amount = edition.getAmountTextBox();
    JTextField textAmount = (JTextField)amount.getAwtComponent();
    textAmount.select(0, textAmount.getText().length());
    amount.pressKey(Key.DELETE).pressKey(Key.d1).pressKey(Key.d3);

    edition
      .checkName("AA")
      .checkAmount("13")
      .checkChart(new Object[][]{
        {"2008", "June", 0.00, 13.00, true},
        {"2008", "July", 0.00, 13.00, true},
        {"2008", "August", 0.00, 13.00, true},
      })
      .cancel();
  }

  public void testMonthsAreShownOrNotDependingOnThePeriodicity() throws Exception {
    SeriesEditionDialogChecker edition = budgetView.variable.createSeries();
    edition.setRepeatIrregular()
      .checkMonthsAreHidden()
      .setRepeatEverySixMonths()
      .checkMonthsAreVisible()
      .setRepeatCustom()
      .checkMonthsAreVisible()
      .setRepeatIrregular()
      .checkMonthsAreHidden()
      .cancel();
  }

  public void testChangeMonthChangeOtherMonth() throws Exception {
    SeriesEditionDialogChecker edition = budgetView.variable.createSeries();
    edition.setName("S1");
    edition.setRepeatEverySixMonths()
      .checkMonthIsChecked(1, 7);

    edition.toggleMonth(1)
      .checkMonthIsChecked(1, 7)
      .checkMonthIsNotChecked(2, 5);

    edition.toggleMonth(2)
      .checkMonthIsChecked(2, 8)
      .checkMonthIsNotChecked(1, 5, 7);

    edition.setRepeatEveryTwoMonths()
      .checkMonthIsChecked(2, 4, 6, 8, 10)
      .toggleMonth(3)
      .checkMonthIsChecked(1, 3, 5, 7, 9, 11)
      .toggleMonth(4)
      .checkMonthIsChecked(2, 4, 6, 8, 10);
    edition.cancel();
  }

  public void testSwitchBetweenSeries() throws Exception {
    operations.openPreferences().setFutureMonthsCount(12).validate();

    budgetView.variable.createSeries()
      .setName("S1")
      .setRepeatEverySixMonths()
      .toggleMonth(3)
      .checkMonthIsChecked(3, 9)
      .validate();

    budgetView.variable.createSeries()
      .setName("S2")
      .setRepeatEveryTwoMonths()
      .checkMonthIsChecked(1, 3, 5, 7, 9, 11)
      .toggleMonth(2)
      .checkMonthIsChecked(2, 4, 6, 8, 10)
      .validate();

    timeline.selectAll();

    budgetView.variable.editSeries("S1")
      .checkMonthsAreVisible()
      .checkMonthIsChecked(3, 9)
      .cancel();

    budgetView.variable.editSeries("S2")
      .checkMonthsAreVisible()
      .checkMonthIsChecked(2, 6, 10)
      .cancel();
  }

  public void testPeriodOrder() throws Exception {
    SeriesEditionDialogChecker edition =
      budgetView.variable.createSeries()
        .setName("S1")
        .checkProfiles("Every month", "Every two months", "Every three months", "Every six months",
                       "Once a year", "Single month", "Custom", "Irregular");
    edition.cancel();
  }

  public void testEnteringPositiveOrNegativeValuesInAnExpensesBudgetArea() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -20.0, "McDo")
      .load();

    categorization.selectTransactions("McDo");
    SeriesEditionDialogChecker edition = categorization.selectVariable().createSeries();

    edition.setName("Diet Food")
      .checkChart(new Object[][]{
        {"2008", "June", 0.00, 0.00, true},
        {"2008", "July", 0.00, 0.00, true},
        {"2008", "August", 0.00, 0.00, true},
      })
      .setPropagationDisabled()
      .checkChart(new Object[][]{
        {"2008", "June", 0.00, 0.00, true},
        {"2008", "July", 0.00, 0.00},
        {"2008", "August", 0.00, 0.00},
      });

    edition.checkNegativeAmountsSelected();

    edition.selectMonth(200806)
      .checkNegativeAmountsSelected()
      .selectPositiveAmounts()
      .setAmount("35")
      .checkChart(new Object[][]{
        {"2008", "June", 0.00, 35.00, true},
        {"2008", "July", 0.00, 0.00},
        {"2008", "August", 0.00, 0.00},
      });

    edition.selectMonth(200807)
      .checkNegativeAmountsSelected()
      .setAmount("20")
      .checkChart(new Object[][]{
        {"2008", "June", 0.00, 35.00},
        {"2008", "July", 0.00, -20.00, true},
        {"2008", "August", 0.00, 0.00},
      });

    edition.selectMonth(200808)
      .checkNegativeAmountsSelected()
      .setAmount("10")
      .checkChart(new Object[][]{
        {"2008", "June", 0.00, 35.00},
        {"2008", "July", 0.00, -20.00},
        {"2008", "August", 0.00, -10.00, true},
      });

    edition.selectMonth(200806)
      .checkPositiveAmountsSelected()
      .checkAmount(35.00)
      .selectNegativeAmounts()
      .checkChart(new Object[][]{
        {"2008", "June", 0.00, 35.00, true},
        {"2008", "July", 0.00, 20.00},
        {"2008", "August", 0.00, 10.00},
      });

    edition.selectMonth(200807)
      .checkNegativeAmountsSelected()
      .checkAmount(20.00)
      .selectPositiveAmounts()
      .checkChart(new Object[][]{
        {"2008", "June", 0.00, -35.00},
        {"2008", "July", 0.00, 20.00, true},
        {"2008", "August", 0.00, -10.00},
      });

    edition.validate();

    timeline.selectMonth("2008/08");
    budgetView.variable.checkSeries("Diet Food", 0, -10.00);

    timeline.selectMonth("2008/07");
    budgetView.variable.checkSeries("Diet Food", 0, 20.00);
  }

  public void testEnteringPositiveOrNegativeValuesInAnIncomeBudgetArea() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", 1000.0, "WorldCo")
      .load();

    categorization.selectTransactions("WorldCo");
    SeriesEditionDialogChecker edition = categorization.selectIncome().createSeries();

    edition.setName("Salary")
      .checkChart(new Object[][]{
        {"2008", "June", 0.00, 0.00, true},
        {"2008", "July", 0.00, 0.00},
        {"2008", "August", 0.00, 0.00},
      });

    edition.checkPositiveAmountsSelected();

    edition.selectMonth(200806)
      .checkPositiveAmountsSelected()
      .setAmount("35")
      .checkChart(new Object[][]{
        {"2008", "June", 0.00, 35.00, true},
        {"2008", "July", 0.00, 0.00},
        {"2008", "August", 0.00, 0.00},
      });

    edition.selectMonth(200807)
      .checkPositiveAmountsSelected()
      .selectNegativeAmounts()
      .setAmount("20")
      .checkChart(new Object[][]{
        {"2008", "June", 0.00, 35.00},
        {"2008", "July", 0.00, -20.00, true},
        {"2008", "August", 0.00, 0.00},
      });

    edition.selectMonth(200808)
      .checkPositiveAmountsSelected()
      .setAmount("10")
      .selectNegativeAmounts()
      .checkChart(new Object[][]{
        {"2008", "June", 0.00, 35.00},
        {"2008", "July", 0.00, -20.00},
        {"2008", "August", 0.00, -10.00, true},
      });

    edition.selectMonth(200806)
      .checkPositiveAmountsSelected()
      .checkAmount(35.00)
      .setAmount("30")
      .checkChart(new Object[][]{
        {"2008", "June", 0.00, 30.00, true},
        {"2008", "July", 0.00, -20.00},
        {"2008", "August", 0.00, -10.00},
      });

//    edition.selectNoMonth()
//      .checkAmountIsDisabled();

    edition.selectMonth(200807)
      .checkNegativeAmountsSelected()
      .checkAmount(20.00)
      .selectPositiveAmounts()
      .checkChart(new Object[][]{
        {"2008", "June", 0.00, 30.00},
        {"2008", "July", 0.00, 20.00, true},
        {"2008", "August", 0.00, -10.00},
      });

    edition.validate();

    timeline.selectMonth("2008/08");
    budgetView.income.checkSeries("Salary", 0, -10.00);

    timeline.selectMonth("2008/07");
    budgetView.income.checkSeries("Salary", 0, 20.00);
  }

  public void testAutomaticAndManualModes() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/28", -30., "EAU")
      .addTransaction("2008/08/27", -20., "EAU")
      .load();

    timeline.selectMonth("2008/06");
    categorization.setNewRecurring("EAU", "EAU");

    timeline.selectMonth("2008/08");
    categorization.setRecurring("EAU", "EAU");
    categorization.selectRecurring()
      .editSeries("EAU")
      .setRepeatEveryTwoMonths()
      .toggleMonth(6)
      .validate();

    timeline.selectMonths("2008/06", "2008/07", "2008/08");
    transactions
      .showPlannedTransactions()
      .initContent()
      .add("27/08/2008", TransactionType.PLANNED, "Planned: EAU", "", -10.00, "EAU")
      .add("27/08/2008", TransactionType.PRELEVEMENT, "EAU", "", -20.00, "EAU")
      .add("28/06/2008", TransactionType.PRELEVEMENT, "EAU", "", -30.00, "EAU")
      .check();
  }

  public void testChangingPeriodicitySelectsCurrentMonth() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/28", -30., "EAU")
      .addTransaction("2008/08/27", -20., "EAU")
      .load();

    categorization.selectTransactions("EAU");
    categorization.selectVariable().createSeries().setName("Eau")
      .setRepeatEveryTwoMonths()
      .checkMonthSelectorsVisible(true)
      .checkMonthIsChecked(2, 4, 6, 8, 10, 12)
      .checkMonthIsNotChecked(1, 3, 5, 7, 9, 11)
      .validate();
  }

  public void testChangePeriodicityToPersonnalUseSelectedMonth() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/28", -30., "EAU")
      .addTransaction("2008/08/27", -20., "EAU")
      .load();

    categorization.selectTransactions("EAU");
    categorization.selectVariable().createSeries().setName("Eau")
      .setRepeatCustom()
      .checkMonthIsChecked(6, 8)
      .checkMonthIsNotChecked(1, 2, 3, 4, 5, 7, 9, 10, 11, 12)
      .setRepeatEveryMonth()
      .checkMonthIsChecked(1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 12)
      .setRepeatCustom()
      .checkMonthIsChecked(6, 8)
      .validate();
  }

  public void testUnknownPeriodicity() throws Exception {
    fail("Je ne suis pas d'accord avec le test et ce n'est pas conforme a la version precedente : alors pourquoi le test passe?");
    OfxBuilder.init(this)
      .addTransaction("2008/06/29", -100.00, "Virement")
      .load();

    categorization.selectTableRow(0);
    categorization.selectTransfers()
      .selectAndCreateTransferSeries("Epargne", "Account n. 00001123");

    budgetView.transfer.align("Epargne");

    timeline.selectMonths("2008/06", "2008/07");
    transactions
      .showPlannedTransactions()
      .initContent()
      .add("27/07/2008", TransactionType.PLANNED, "Planned: Epargne", "", -100.00, "Epargne")
      .add("29/06/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne")
      .check();

    categorization.selectTransfers().editSeries("Epargne")
      .setRepeatIrregular()
      .checkChart(new Object[][]{
        {"2008", "June", 100.00, 100.00, true},
        {"2008", "July", 0.00, 0.00, true},
        {"2008", "August", 0.00, 0.00},
      })
      .validate();

    timeline.selectMonths("2008/06", "2008/07");
    transactions.initContent()
      .add("29/06/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne")
      .check();
  }

  public void testUnkownPeriodicityAndPreverseAutomatic() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/29", -100.00, "Virement")
      .load();

    categorization.selectTableRow(0);
    categorization.selectTransfers()
      .selectAndCreateTransferSeries("Epargne", "Account n. 00001123");

    categorization.selectTransfers()
      .editSeries("Epargne")
      .setRepeatIrregular()
      .setRepeatEveryTwoMonths()
      .validate();

    categorization.selectTransfers().editSeries("Epargne")
      .setRepeatIrregular()
      .validate();

    categorization.selectTransfers().editSeries("Epargne")
      .setRepeatIrregular()
      .setRepeatEveryTwoMonths()
      .validate();
  }

  public void testChangeLastMonthInIrregular() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/29", -100.00, "Virement")
      .load();

    categorization.selectTableRow(0);
    categorization.selectTransfers()
      .selectAndCreateTransferSeries("Epargne", "Account n. 00001123");
    categorization.selectTransfers().editSeries("Epargne")
      .setRepeatIrregular()
      .setEndDate(200807)
      .validate();

    operations.openPreferences().setFutureMonthsCount(2).validate();

    categorization.selectTransfers().editSeries("Epargne")
      .setEndDate(200810)
      .validate();
    timeline.checkSpanEquals("2008/06", "2008/10");

    timeline.selectAll();
    transactions.initContent()
      .add("29/06/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne")
      .check();
  }

  public void testAddMonthUpdateBudgetWithLastValidBudget() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/04", -100.00, "Virement")
      .addTransaction("2008/07/04", -10.00, "McDo")
      .load();
    categorization.selectTransactions("Virement");
    categorization.selectTransfers()
      .selectAndCreateTransferSeries("Epargne", "Account n. 00001123");

    categorization.selectTransfers().editSeries("Epargne")
      .setRepeatEveryTwoMonths()
      .validate();
    timeline.selectMonth("2008/06");
    budgetView.transfer.alignAndPropagate("Epargne");

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initContent()
      .add("04/08/2008", TransactionType.PLANNED, "Planned: Epargne", "", -100.00, "Epargne")
      .add("04/07/2008", TransactionType.PRELEVEMENT, "McDo", "", -10.00)
      .add("04/06/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne")
      .check();
    operations.openPreferences().setFutureMonthsCount(2).validate();
    timeline.selectAll();
    timeline.checkSpanEquals("2008/06", "2008/10");
    transactions.initContent()
      .add("04/10/2008", TransactionType.PLANNED, "Planned: Epargne", "", -100.00, "Epargne")
      .add("04/08/2008", TransactionType.PLANNED, "Planned: Epargne", "", -100.00, "Epargne")
      .add("04/07/2008", TransactionType.PRELEVEMENT, "McDo", "", -10.00)
      .add("04/06/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne")
      .check();
  }

  public void testNoAutomaticAddMonth() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/04", -100.00, "Virement")
      .addTransaction("2008/07/04", -10.00, "McDo")
      .load();
    categorization.selectTransactions("Virement");
    categorization.selectTransfers()
      .selectAndCreateTransferSeries("Epargne", "Account n. 00001123");

    operations.openPreferences().setFutureMonthsCount(1).validate();
    categorization.selectTransfers().editSeries("Epargne")
      .selectMonth(200809)
      .setAmount("0")
      .setRepeatEveryTwoMonths()
      .validate();

    timeline.selectMonth("2008/06");
    budgetView.transfer.alignAndPropagate("Epargne");

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initContent()
      .add("04/08/2008", TransactionType.PLANNED, "Planned: Epargne", "", -100.00, "Epargne")
      .add("04/07/2008", TransactionType.PRELEVEMENT, "McDo", "", -10.00)
      .add("04/06/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne")
      .check();
    operations.openPreferences().setFutureMonthsCount(2).validate();
    timeline.selectAll();
    timeline.checkSpanEquals("2008/06", "2008/10");
    transactions.initContent()
      .add("04/10/2008", TransactionType.PLANNED, "Planned: Epargne", "", -100.00, "Epargne")
      .add("04/08/2008", TransactionType.PLANNED, "Planned: Epargne", "", -100.00, "Epargne")
      .add("04/07/2008", TransactionType.PRELEVEMENT, "McDo", "", -10.00)
      .add("04/06/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne")
      .check();
  }

  public void testChangeBudgetAmountWhileInOverrun() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/08/28", 5000., "Complement")
      .load();

    categorization.setExceptionalIncome("Complement", "Salaire sup", true);

    budgetView.income.editSeries("Salaire sup")
      .selectMonth(200808)
      .setAmount("6000")
      .validate();

    transactions
      .showPlannedTransactions()
      .initContent()
      .add("28/08/2008", TransactionType.PLANNED, "Planned: Salaire sup", "", 1000.00, "Salaire sup")
      .add("28/08/2008", TransactionType.VIREMENT, "Complement", "", 5000.00, "Salaire sup")
      .check();
  }

  public void testExtraWithOnlyOneMonth() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/05/04", -100.00, "Virement")
      .addTransaction("2008/06/04", -100.00, "CENTER PARC")
      .addTransaction("2008/07/04", -10.00, "McDo")
      .load();
    categorization.selectTransactions("CENTER PARC");
    categorization.selectExtras().createSeries()
      .setName("Center Parc")
      .checkSelectedProfile("Irregular")
      .checkNoStartDate()
      .checkNoEndDate()
      .checkMonthSelected(200807)
      .checkAmount(0.00)
      .selectMonth(200806)
      .checkAmount(100.00)
      .validate();
  }

  public void testExtraWithSeveralMonths() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2007/05/04", -100.00, "Virement")
      .addTransaction("2007/06/04", -100.00, "CENTER PARC")
      .addTransaction("2007/11/04", -100.00, "CENTER PARC")
      .addTransaction("2008/03/04", -100.00, "CENTER PARC")
      .addTransaction("2008/07/04", -10.00, "McDo")
      .load();
    categorization.selectTransactions("CENTER PARC");
    categorization.selectExtras().createSeries()
      .setName("Center Parc")
      .checkSelectedProfile("Irregular")
      .checkNoStartDate()
      .checkNoEndDate()
      .checkSelectedMonths(200807)
      .validate();
  }

  public void testExtraIsInitializedWithASingleMonthPeriodicityWhenOnlyOneMonthIsSelected() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/04", -10.00, "McDo")
      .load();

    timeline.selectMonth("2008/06");
    budgetView.extras.createSeries()
      .setName("Center Parc")
      .checkSelectedProfile("Irregular")
      .checkMonthSelected(200806)
      .validate();
  }

  public void testSelectionIsUpdatedWhenStartOrEndDatesAreChanged() throws Exception {
    operations.openPreferences().setFutureMonthsCount(4).validate();
    OfxBuilder.init(this)
      .addTransaction("2008/03/04", -10.00, "McDo")
      .addTransaction("2008/06/04", -10.00, "McDo")
      .load();

    timeline.selectMonth("2008/06");
    budgetView.extras.createSeries()
      .setName("Center Parc")
      .setAmount(500.00)
      .validate();

    budgetView.extras.editSeries("Center Parc")
      .setRepeatSingleMonth()
      .setSingleMonthDate(200807)
      .checkAmountEditionEnabled()
      .checkMonthSelected(200807)
      .validate();

    timeline.selectMonths(200806, 200807);
    budgetView.extras.editSeries("Center Parc")
      .setRepeatEveryMonth()
      .clearStartDate()
      .checkAmountEditionEnabled()
      .checkMonthSelected(200807)
      .setEndDate(200806)
      .checkMonthSelected(200806)
      .setEndDate(200805)
      .checkMonthSelected(200805)
      .validate();

    timeline.selectMonth(200805);
    budgetView.extras.editSeries("Center Parc")
      .checkMonthSelected(200805)
      .checkPropagationDisabled()
      .clearEndDate()
      .checkAmountEditionEnabled()
      .setStartDate(200808)
      .checkMonthSelected(200808)
      .setStartDate(200805)
      .checkMonthSelected(200805)
      .validate();
  }

  public void testOneTimeAYearCanBeChangedWhenATransactionWithDifferentMonthIsSelected() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/12/30", -20., "PointP")
      .load();

    categorization.selectTransactions("PointP");
    categorization.selectVariable().createSeries()
      .setName("Brico")
      .setRepeatOnceAYear()
      .toggleMonth(11)
      .checkMonthIsChecked(11)
      .cancel();
  }

  public void testCreatingAnAccountFromTheSeriesDialog() throws Exception {

    accounts.createMainAccount("Main", 0.0);

    SeriesEditionDialogChecker seriesEdition = budgetView.transfer.createSeries().setName("ING");

    seriesEdition.createAccount()
      .setName("Virt ING")
      .setAccountNumber("1234")
      .selectBank("ING Direct")
      .checkIsSavings()
      .validate();

    seriesEdition
      .setFromAccount("Main")
      .setToAccount("Virt ING")
      .validate();

    savingsAccounts.edit("Virt ING")
      .checkAccountName("Virt ING")
      .checkAccountNumber("1234")
      .checkSelectedBank("ING Direct")
      .checkIsSavings()
      .cancel();

    budgetView.transfer.editSeries("ING")
      .checkToAccount("Virt ING")
      .validate();
    budgetView.transfer.checkOrder("ING");
  }

  public void testCreateDeleteCreateBudget() throws Exception {
    operations.openPreferences().setFutureMonthsCount(12).validate();
    budgetView.variable.createSeries()
      .setName("Serie")
      .selectAllMonths()
      .setAmount(100)
      .validate();

    budgetView.variable.editSeries("Serie")
      .setEndDate(200808)
      .clearEndDate()
      .setStartDate(200808)
      .setEndDate(200812)
      .clearStartDate()
      .validate();
  }

  public void testAlignPlannedAndActualAmounts() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Free Telecom")
      .load();

    timeline.selectMonth("2008/07");
    categorization.checkTable(new Object[][]{
      {"29/07/2008", "", "Free Telecom", -29.00}
    });
    categorization.setNewRecurring("Free Telecom", "Internet");

    budgetView.recurring.editSeries("Internet")
      .checkChart(new Object[][]{
        {"2008", "July", 29.00, 29.00, true},
        {"2008", "August", 0.00, 29.00},
      })
      .checkMonthSelected(200807)
      .selectAllMonths()
      .setAmount(50.00)
      .validate();
    budgetView.recurring.checkSeries("Internet", -29.00, -50.00);

    budgetView.recurring.editSeries("Internet")
      .checkAmount(50.00)
      .checkAlignPlannedAndActualEnabled()
      .checkActualAmount("29.00")
      .alignPlannedAndActual()
      .checkAmount(29.00)
      .validate();
    budgetView.recurring.checkSeries("Internet", -29.00, -29.00);

    budgetView.recurring.editSeries("Internet")
      .checkAmount(29.00)
      .selectAllMonths()
      .checkActualAmount("Actual")
      .alignPlannedAndActual()
      .checkAmount("29.00")
      .validate();
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkSeries("Internet", -29.00, -29.00);
    timeline.selectMonth("2008/08");
    budgetView.recurring.checkSeries("Internet", 0.00, -29.00);
  }

  public void testAutomaticAdjustPlannedAndActualForFirstMonthWithActual() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder.init(this)
      .addTransaction("2008/05/15", -10.00, "Tel")
      .addTransaction("2008/06/15", -90.00, "Free")
      .addTransaction("2008/07/15", -95.00, "Free")
      .addTransaction("2008/08/15", -98.00, "Free")
      .load();

    categorization.selectTransactions("Free")
      .selectRecurring()
      .createSeries("internet");

    timeline.selectMonth("2008/06");
    budgetView.recurring.checkSeries("internet", -90, -90);
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkSeries("internet", -95, -90);
    timeline.selectMonth("2008/08");
    budgetView.recurring.checkSeries("internet", -98, -95);
    timeline.selectMonth("2008/09");
    budgetView.recurring.checkSeries("internet", 0, -98);

    categorization.showSelectedMonthsOnly();
    timeline.selectMonth("2008/07");
    categorization.selectTransaction("free")
      .setUncategorized();
    timeline.selectMonth("2008/06");
    budgetView.recurring.checkSeries("internet", -90, -90);
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkSeries("internet", 0, -90);
    timeline.selectMonth("2008/08");
    budgetView.recurring.checkSeries("internet", -98, 0);
    timeline.selectMonth("2008/09");
    budgetView.recurring.checkSeries("internet", 0, -98);

    categorization.showSelectedMonthsOnly();
    timeline.selectMonth("2008/06");
    categorization.selectTransaction("free")
      .setUncategorized();
    timeline.selectMonth("2008/06");
    budgetView.recurring.checkSeries("internet", 0, 0);
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkSeries("internet", 0, 0);
    timeline.selectMonth("2008/08");
    budgetView.recurring.checkSeries("internet", -98, -98);
    timeline.selectMonth("2008/09");
    budgetView.recurring.checkSeries("internet", 0, -98);
  }

  public void testCategorizeOutOfThePeriod() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder.init(this)
      .addTransaction("2008/03/15", -10.00, "Tel")
      .addTransaction("2008/06/1", -10.00, "Tel")
      .load();

    categorization.selectTableRow(0)
      .selectRecurring()
      .createSeries()
      .setName("Telephone")
      .setRepeatCustom()
      .setPeriodMonths(1, 3, 5)
      .validate();

    categorization.selectTableRow(1)
      .selectRecurring()
      .checkSeriesIsInactive("Telephone")
      .selectSeries("Telephone");

    timeline.selectMonth("2008/06");
    budgetView.recurring.checkSeries("Telephone", -10, 0);
  }

  public void testChangeBugdetArea() throws Exception {
    addOns.activateAnalysis();
    OfxBuilder.init(this)
      .addTransaction("2008/08/01", -29.00, "Free Telecom")
      .load();

    categorization.setNewRecurring("Free Telecom", "Internet");
    categorization.editSeries("Internet").editSubSeries().addSubSeries("Free").addSubSeries("sfr").validate();
    categorization.editSeries("Internet")
      .checkBudgetAreaContent()
      .checkBudgetArea("Recurring")
      .changeBudgetArea("Variable")
      .checkBudgetArea("Variable")
      .validate();
    categorization.selectTransaction("Free Telecom")
      .selectVariable()
      .checkNoSeriesMessageHidden();

    budgetView.recurring.checkTotalGauge(0, 0);
    budgetView.variable.checkTotalGauge(-29., -29.);
    budgetView.variable.checkSeries("Internet", -29., -29.);

    analysis.table().initContent()
      .add("Main accounts", "", "", "", "", "", "", "", "")
      .add("Balance", "", "-29.00", "", "", "", "", "", "")
      .add("Savings accounts", "", "", "", "", "", "", "", "")
      .add("To categorize", "", "", "", "", "", "", "", "")
      .add("Income", "", "", "", "", "", "", "", "")
      .add("Recurring", "", "", "", "", "", "", "", "")
      .add("Variable", "", "29.00", "", "", "", "", "", "")
      .add("Internet", "", "29.00", "", "", "", "", "", "")
      .add("Free", "", "", "", "", "", "", "", "")
      .add("sfr", "", "", "", "", "", "", "", "")
      .add("Extras", "", "", "", "", "", "", "", "")
      .add("Savings", "", "", "", "", "", "", "", "");
  }

  public void testNoBudgetAreaForSavingsAndIncome() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/01", 1000, "salaire")
      .load();
    categorization.setNewIncome("salaire", "salaire");
    budgetView.income.editSeries("salaire")
      .checkBudgetAreaIsHidden()
      .validate();
    categorization.selectTransaction("salaire")
      .selectUncategorized().setUncategorized();
    categorization.setNewTransfer("salaire", "complement", "external", "Account n. 00001123");
    budgetView.transfer.editSeries("complement")
      .checkBudgetAreaIsHidden()
      .validate();
  }

  public void testChangeTabAndReopenReturnsToMainTab() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/01", -29.00, "Auchan")
      .load();

    categorization.selectTableRow(0);
    categorization.selectVariable().createSeries()
      .setName("Groceries")
      .editSubSeries()
      .validate();

    categorization.editSeries("Groceries")
      .checkMainPanelShown()
      .cancel();
  }

  // En mode virement externe=>non importé on crée des operations automatiquement
  // et du coup on ne peut plus changer la date de debut
  // il faut autoriser le changement meme si il y a des operations
  public void testAllowChangeOfBeginOfDayOnSavingsEnvelope() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/01/01", -29.00, "Auchan") // pour creer des mois dans le passe
      .load();
    accounts.createSavingsAccount("ING", 1000.00);
    savingsAccounts.select("ING");
    budgetView.transfer.createSeries()
      .setName("Epargne")
      .setFromAccount("External account")
      .setToAccount("ING")
      .setDay("4")
      .selectAllMonths()
      .setAmount(100)
      .validate();

    budgetView.transfer.editSeries("Epargne")
      .setStartDate(200805)
      .validate();

    timeline.selectMonth("2008/03");

    transactions.checkEmpty();

    timeline.selectMonth("2008/08");
    budgetView.transfer.editSeries("Epargne")
      .setEndDate(200807)
      .validate();

    transactions.checkEmpty();
  }

  public void testCanSelectMainAccountBeforeAnyOperationIsAssigned() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000123", 1000.00, "2008/08/15")
      .addTransaction("2008/08/15", -30.00, "Auchan")
      .load();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000234", 2000.00, "2008/08/10")
      .addTransaction("2008/08/10", -50.00, "FNAC")
      .load();

    accounts.createNewAccount()
      .setAsSavings()
      .setName("Livret")
      .selectBank("ING Direct")
      .setPosition(2000.00)
      .validate();

    budgetView.variable.createSeries()
      .setName("Courses")
      .checkEditableTargetAccount("Main accounts")
      .checkAvailableTargetAccounts("Account n. 000123", "Account n. 000234", "Main accounts")
      .setTargetAccount("Account n. 000123")
      .setAmount(100.00)
      .validate();

    mainAccounts.getChart("Account n. 000123")
      .checkValue(200808, 1, 1030.00)
      .checkValue(200808, 15, 900.00);

    budgetView.variable.editSeries("Courses")
      .checkEditableTargetAccount("Account n. 000123")
      .checkAvailableTargetAccounts("Account n. 000123", "Account n. 000234", "Main accounts")
      .setTargetAccount("Main accounts")
      .checkAmount(100.00)
      .validate();

    mainAccounts.getChart("Account n. 000123")
      .checkValue(200808, 1, 1030.00)
      .checkValue(200808, 15, 950.00);
  }

  public void testClosedAccount() throws Exception {
    fail("MG: if the account is closed we must update the end date? (or not create planned operation) - \n" +
         "RM: je propose de positionner automatiquement Series.END_DATE dans ce cas là");
  }
}
