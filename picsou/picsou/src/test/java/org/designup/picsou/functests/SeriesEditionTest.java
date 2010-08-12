package org.designup.picsou.functests;

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
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Free Telecom")
      .load();

    timeline.selectMonth("2008/07");
    views.selectData();
    transactions.initContent()
      .add("29/07/2008", "01/08/2008", TransactionType.PRELEVEMENT, "Free Telecom", "", -29.00)
      .check();

    views.selectCategorization();
    categorization.checkTable(new Object[][]{
      {"29/07/2008", "", "Free Telecom", -29.00}
    });
    categorization.setNewRecurring("Free Telecom", "Internet");

    views.selectBudget();

    budgetView.recurring.editSeries("Internet")
      .checkTitle("Editing a series")
      .checkName("Internet")
      .setName("Free")
      .checkChart(new Object[][]{
        {"2008", "July", 29.00, 29.00, true},
        {"2008", "August", 0.00, 29.00},
      })
      .checkMonthSelected(200807)
      .checkAmountLabel("Planned amount for july 2008")
      .selectAllMonths()
      .checkAmountLabel("Planned amount for july - august 2008")
      .validate();

    budgetView.recurring.checkSeries("Free", -29.00, -29.00);
  }

  public void testSeriesNamesAreTrimmed() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Free")
      .load();

    timeline.selectMonth("2008/07");

    views.selectCategorization();
    categorization.selectTableRow(0);
    categorization.selectRecurring().createSeries()
      .setNameAndValidate("    Internet   ");

    categorization.selectRecurring().selectSeries("Internet");

    views.selectBudget();
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

    views.selectCategorization();
    categorization.getTable().selectRowSpan(0, 3);
    categorization.setNewRecurring(0, "Internet");
    categorization.setRecurring(1, "Internet");
    categorization.setRecurring(2, "Internet");
    categorization.setRecurring(3, "Internet");
    views.selectBudget();
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
      .checkMonthsSelected(200808, 200806)
      .validate();

    timeline.checkSelection("2008/06", "2008/08");
    budgetView.recurring.checkSeries("Free", -58.00, -58.00);
  }

  public void testAllMonthsAreSelectedInBudgetTableIfCurrentMonthIsNotFound() throws Exception {

    operations.openPreferences().setFutureMonthsCount(4).validate();

    views.selectBudget();
    timeline.selectMonths("2008/10", "2008/11");
    budgetView.extras.createSeries()
      .setName("Plumber")
      .checkMonthsSelected(200810, 200811)
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
    views.selectCategorization();
    categorization.setNewRecurring("Free Telecom", "Internet");

    views.selectBudget();
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

    views.selectCategorization();
    categorization.setNewRecurring("Free Telecom", "Internet");

    views.selectBudget();
    timeline.selectMonths("2008/08", "2008/06");
    SeriesEditionDialogChecker editionDialogChecker = budgetView.recurring.editSeries("Internet");
    editionDialogChecker
      .setCustom()
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

  public void testEditAllSeriesIsInitiallyDisabled() throws Exception {
    views.selectBudget();
    budgetView.recurring.checkEditAllSeriesIsEnabled(false);
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

    views.selectData();
    transactions.initContent()
      .add("12/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -95.00)
      .add("10/07/2008", TransactionType.PRELEVEMENT, "Monoprix", "", -50.00)
      .add("05/07/2008", TransactionType.PRELEVEMENT, "Free Telecom", "", -29.00)
      .add("04/07/2008", TransactionType.PRELEVEMENT, "EDF", "", -55.00)
      .add("03/07/2008", TransactionType.PRELEVEMENT, "McDo", "", -15.00)
      .add("02/07/2008", TransactionType.VIREMENT, "WorldCo - Bonus", "", 200.00)
      .add("01/07/2008", TransactionType.VIREMENT, "WorldCo", "", 3540.00)
      .check();

    views.selectCategorization();
    categorization.setNewVariable("Auchan", "Groceries");
    categorization.setVariable("Monoprix", "Groceries");
    categorization.setNewRecurring("Free Telecom", "Internet");
    categorization.setNewRecurring("EDF", "Electricity");
    categorization.setExceptionalIncome("WorldCo - Bonus", "Exceptional Income", true);
    categorization.setNewIncome("WorldCo", "Salary");

    views.selectBudget();

    budgetView.recurring.editSeriesList()
      .checkSeriesListEquals("Electricity", "Internet")
      .validate();

    budgetView.variable.editSeriesList()
      .checkSeriesListEquals("Groceries")
      .validate();

    budgetView.income.editSeriesList()
      .checkSeriesListEquals("Exceptional Income", "Salary")
      .validate();
  }

  public void testSwitchingBetweenSeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/15", -29.00, "Free Telecom")
      .addTransaction("2008/07/15", -55.00, "EDF")
      .load();

    timeline.selectAll();
    views.selectData();
    transactions.initContent()
      .add("15/08/2008", TransactionType.PRELEVEMENT, "Free Telecom", "", -29.00)
      .add("15/07/2008", TransactionType.PRELEVEMENT, "EDF", "", -55.00)
      .check();

    views.selectCategorization();
    categorization.setNewRecurring("Free Telecom", "Internet");
    categorization.setNewRecurring("EDF", "Electricity");

    views.selectBudget();
    SeriesEditionDialogChecker editionDialog = budgetView.recurring.editSeriesList();

    editionDialog
      .checkSeriesListEquals("Electricity", "Internet")
      .checkSeriesSelected("Electricity")
      .setCustom()
      .selectAllMonths()
      .setAmount("70")
      .checkChart(new Object[][]{
        {"2008", "July", 55.00, 70.00, true},
        {"2008", "August", 0.00, 70.00, true},
      })
      .toggleMonth("Aug")
      .checkChart(new Object[][]{
        {"2008", "July", 55.00, 70.00, true},
      });

    editionDialog
      .selectSeries("Internet")
      .checkMonthIsChecked("Aug")
      .checkChart(new Object[][]{
        {"2008", "July", 0.00, 0.00, true},
        {"2008", "August", 29.00, 29.00},
      })
      .toggleMonth("Jul")
      .selectSeries("Electricity")
      .checkMonthIsChecked("Jul")
      .checkMonthIsNotChecked("Aug")
      .checkChart(new Object[][]{
        {"2008", "July", 55.00, 70.00, true},
      })
      .validate();
  }

  public void testNoSeriesSelected() throws Exception {
    fail("Gerer le unselect...");
    views.selectBudget();
    budgetView.recurring.createSeries()
      .setName("series")
      .validate();

    budgetView.recurring.editSeriesList()
      .unselectSeries()
      .checkAllMonthsDisabled()
      .checkCalendarsAreDisabled()
      .cancel();
  }

  public void testCreatingANewSeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Free Telecom")
      .load();

    timeline.selectMonth("2008/07");

    views.selectBudget();
    budgetView.recurring
      .createSeries()
      .checkTitle("Creating a series")
      .checkNameIsSelected()
      .checkSeriesListEquals("New series")
      .checkSeriesSelected("New series")
      .checkName("New series")
      .setName("Free Telecom")
      .selectAllMonths()
      .setAmount("40")
      .checkSeriesListEquals("Free Telecom")
      .checkChart(new Object[][]{
        {"2008", "July", 0.0, 40.00, true},
        {"2008", "August", 0.00, 40.00, true},
      })
      .validate();

    budgetView.recurring.checkSeries("Free Telecom", -0.0, -40.0);
  }

  public void testExistingSeriesAreVisibleWhenCreatingANewSeries() throws Exception {
    views.selectBudget();

    budgetView.recurring.createSeries()
      .checkTitle("Creating a series")
      .checkBudgetArea("Recurring")
      .setName("My recurrence")
      .validate();

    budgetView.variable.createSeries()
      .checkTitle("Creating a series")
      .checkBudgetArea("Variable")
      .setName("My envelope")
      .validate();

    budgetView.variable.createSeries()
      .checkSeriesListEquals("My envelope", "New series")
      .checkSeriesSelected("New series")
      .setName("My new envelope")
      .validate();

    budgetView.variable.createSeries()
      .checkSeriesListEquals("My envelope", "My new envelope", "New series")
      .cancel();

    budgetView.recurring.createSeries()
      .checkSeriesListEquals("My recurrence", "New series")
      .cancel();
  }

  public void testSwitchingBetweenTwoNewSeriesWithTheSameName() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/15", -29.00, "Free Telecom")
      .addTransaction("2008/07/15", -55.00, "EDF")
      .load();

    views.selectBudget();
    SeriesEditionDialogChecker seriesEdition = budgetView.recurring
      .createSeries();
    seriesEdition
      .setCustom()
      .createSeries()
      .setCustom()
      .checkSeriesListEquals("New series", "New series");

    seriesEdition
      .selectSeries(0)
      .selectAllMonths()
      .setAmount("70")
      .toggleMonth("Aug")
      .checkChart(new Object[][]{
        {"2008", "July", 0.00, 70.00, true},
      });

    seriesEdition
      .selectSeries(1)
      .checkMonthIsChecked("Aug")
      .toggleMonth("Jul")
      .checkChart(new Object[][]{
        {"2008", "August", 0.00, 0.00, true},
      })

      .selectSeries(0)
      .checkMonthIsChecked("Jul")
      .checkMonthIsNotChecked("Aug")
      .checkChart(new Object[][]{
        {"2008", "July", 0.00, 70.00, true},
      })
      .cancel();
  }

  public void testCancel() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .load();

    views.selectBudget();
    budgetView.variable.createSeries()
      .setName("A series")
      .cancel();
    budgetView.variable.createSeries()
      .checkSeriesListEquals("New series")
      .cancel();
  }

  public void testEsc() throws Exception {
    views.selectBudget();
    SeriesEditionDialogChecker edition = budgetView.variable.createSeries();
    edition.editStartDate().pressEscapeKey();
    Thread.sleep(50);
    edition.checkNoStartDate();
    edition.cancel();
  }

  public void testEditDate() throws Exception {
    views.selectBudget();
    budgetView.variable.createSeries()
      .setStartDate(200809)
      .setEndDate(200810)
      .checkStartDate("Sep 2008")
      .checkEndDate("Oct 2008")
      .removeStartDate()
      .removeEndDate()
      .checkNoStartDate()
      .checkNoEndDate()
      .cancel();

    budgetView.variable.createSeries()
      .unselectSeries()
      .checkCalendarsAreDisabled()
      .cancel();
  }

  public void testStartEndCalendar() throws Exception {
    views.selectBudget();
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

    edition.removeStartDate();
    edition.editEndDate()
      .checkIsEnabled(200701, 200901)
      .cancel();
    edition.cancel();
  }

  public void testStartEndDateWithTransaction() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .load();

    views.selectCategorization();
    categorization.setNewVariable("Auchan", "Courant");
    views.selectBudget();
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
    views.selectBudget();
    budgetView.variable.createSeries()
      .setName("groceries")
      .checkNoStartDate()
      .checkNoEndDate()
      .setSingleMonth()
      .checkSingleMonthDate("June 2008")
      .validate();

    views.selectData();
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
    views.selectBudget();
    budgetView.variable.createSeries()
      .setName("groceries")
      .setStartDate(200806)
      .setEndDate(200808)
      .setSingleMonth()
      .checkSingleMonthDate("June 2008")
      .checkMonthSelectorsVisible(false)
      .setSingleMonthDate(200808)
      .checkSingleMonthDate("Aug 2008")
      .setEveryMonth()
      .checkStartDate("Aug 2008")
      .checkEndDate("Aug 2008")
      .setSingleMonth()
      .setSingleMonthDate(200807)
      .checkSingleMonthDate("Jul 2008")
      .validate();
  }

  public void testUsingSingleMonthPeriodicityInManualMode() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/15", -95.00, "Auchan")
      .addTransaction("2008/08/15", -95.00, "Auchan")
      .load();

    timeline.selectMonth("2008/06");
    views.selectBudget();
    budgetView.variable.createSeries()
      .setName("manualThenSingleMonth")
      .setSingleMonth()
      .checkSingleMonthDate("June 2008")
      .checkChart(new Object[][]{
        {"2008", "June", 0.00, 0.00, true}
      })
      .validate();

    budgetView.variable.createSeries()
      .setName("singleMonthThenManual")
      .setSingleMonth()
      .checkSingleMonthDate("June 2008")
      .checkChart(new Object[][]{
        {"2008", "June", 0.00, 0.00, true}
      })
      .validate();
  }

  public void testDateAndBudgetSeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2007/02/10", -29.00, "Free Telecom")
      .load();
    
    views.selectBudget();
    SeriesEditionDialogChecker edition = budgetView.recurring.createSeries();
    edition.setCustom()
      .toggleMonth("Jan", "Mar", "Jul", "Sep", "Nov");

    edition.selectAllMonths()
      .setAmount("30");

    // L'histoChart ne montre qu'une partie des mois
    edition.setStartDate(200709)
      .checkChart(new Object[][]{
        {"2007", "October", 0.00, 30.00},
        {"2007", "December", 0.00, 30.00},
        {"2008", "February", 0.00, 30.00},
      });

    edition.setEndDate(200801)
      .checkChart(new Object[][]{
        {"2007", "October", 0.00, 30.00},
        {"2007", "December", 0.00, 30.00},
      });

    edition.setEndDate(200802)
      .checkChart(new Object[][]{
        {"2007", "October", 0.00, 30.00},
        {"2007", "December", 0.00, 30.00},
        {"2008", "February", 0.00, 0.00},
      });

    edition.setStartDate(200712)
      .checkChart(new Object[][]{
        {"2007", "December", 0.00, 30.00},
        {"2008", "February", 0.00, 0.00},
      });
    edition.selectAllMonths().setAmount("30")
      .toggleMonth("Dec", "Feb").toggleMonth("Dec", "Feb")
      .checkChart(new Object[][]{
        {"2007", "December", 0.00, 30.00},
        {"2008", "February", 0.00, 30.00},
      });
    edition.cancel();
  }

  public void testMonthIsHiddenIfLessThanOneMonthInDateRange() throws Exception {
    views.selectBudget();
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

    views.selectCategorization();
    categorization.selectTableRow(0);
    categorization.selectRecurring().createSeries()
      .setName("Internet")
      .validate();

    views.selectData();
    timeline.selectMonths("2008/06", "2008/07");
    transactions.initContent()
      .add("30/07/2008", TransactionType.PLANNED, "Planned: Internet", "", -60.00, "Internet")
      .add("30/06/2008", TransactionType.PRELEVEMENT, "Free", "", -60.00, "Internet")
      .check();

    views.selectCategorization();
    categorization.editSeries()
      .selectSeries("Internet")
      .setName("Leisures")
      .validate();
    categorization.getRecurring().checkContainsSeries("Leisures");

    views.selectData();
    transactions.initContent()
      .add("30/07/2008", TransactionType.PLANNED, "Planned: Leisures", "", -60.00, "Leisures")
      .add("30/06/2008", TransactionType.PRELEVEMENT, "Free", "", -60.00, "Leisures")
      .check();
  }

  public void testRenameRecurrentFromBudgetView() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -60, "Forfait Kro")
      .load();

    views.selectCategorization();
    categorization.setNewRecurring(0, "Drinking");

    views.selectBudget();
    budgetView.recurring.editSeriesList()
      .selectSeries("Drinking")
      .setName("Leisures")
      .validate();

    views.selectCategorization();
    categorization.selectRecurring().checkContainsSeries("Leisures");
  }

  public void testFillNameAndAmountWithKeyPressed() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", 10, "Auchan")
      .load();

    views.selectBudget();
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
      .checkSeriesListEquals("AA");
    edition.checkChart(new Object[][]{
      {"2008", "June", 0.00, 13.00, true},
      {"2008", "July", 0.00, 13.00, true},
      {"2008", "August", 0.00, 13.00, true},
    });
    edition.cancel();
  }

  public void testMonthsAreShownOrNotDependingOnThePeriodicity() throws Exception {
    views.selectBudget();
    SeriesEditionDialogChecker edition = budgetView.variable.createSeries();
    edition.setIrregular()
      .checkMonthsAreHidden()
      .setSixMonths()
      .checkMonthsAreVisible()
      .setCustom()
      .checkMonthsAreVisible()
      .setIrregular()
      .checkMonthsAreHidden()
      .cancel();
  }

  public void testChangeMonthChangeOtherMonth() throws Exception {
    views.selectBudget();
    SeriesEditionDialogChecker edition = budgetView.variable.createSeries();
    edition.setName("S1");
    edition.setSixMonths()
      .checkMonthIsChecked(1, 7);

    edition.toggleMonth(1)
      .checkMonthIsChecked(1, 7)
      .checkMonthIsNotChecked(2, 5);

    edition.toggleMonth(2)
      .checkMonthIsChecked(2, 8)
      .checkMonthIsNotChecked(1, 5, 7);

    edition.setTwoMonths()
      .checkMonthIsChecked(2, 4, 6, 8, 10)
      .toggleMonth(3)
      .checkMonthIsChecked(1, 3, 5, 7, 9, 11)
      .toggleMonth(4)
      .checkMonthIsChecked(2, 4, 6, 8, 10);
    edition.cancel();
  }

  public void testSwitchBetweenSeries() throws Exception {
    views.selectBudget();
    SeriesEditionDialogChecker edition = budgetView.variable.createSeries()
      .setName("S1");
    edition.setSixMonths()
      .toggleMonth(3)
      .checkMonthIsChecked(3, 9);

    edition.createSeries().setName("S2")
      .setTwoMonths()
      .checkMonthIsChecked(1, 3, 5, 7, 9, 11)
      .toggleMonth(2)
      .checkMonthIsChecked(2, 4, 6, 8, 10);

    edition.selectSeries("S1")
      .checkMonthsAreVisible()
      .checkMonthIsChecked(3, 9);
    edition
      .selectSeries("S2")
      .checkMonthsAreVisible()
      .checkMonthIsChecked(2, 6, 10)
      .cancel();
    edition.cancel();
  }

  public void testPeriodOrder() throws Exception {
    views.selectBudget();
    SeriesEditionDialogChecker edition =
      budgetView.variable.createSeries()
        .setName("S1")
        .checkProfiles("Every month", "Every two months", "Every six months",
                       "Once a year", "Single month", "Custom", "Irregular");
    edition.cancel();
  }

  public void testSeriesListVisibility() throws Exception {
    views.selectBudget();
    budgetView.variable.createSeries()
      .checkSeriesListIsHidden()
      .setName("")
      .validate();

    budgetView.variable.editSeriesList()
      .checkSeriesListIsVisible()
      .cancel();
  }

  public void testEnteringPositiveOrNegativeValuesInAnExpensesBudgetArea() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -20.0, "McDo")
      .load();

    views.selectCategorization();
    categorization.selectTransactions("McDo");
    SeriesEditionDialogChecker edition = categorization.selectVariable().createSeries();

    edition.setName("Diet Food")
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
        {"2008", "July", 0.00, 20.00},
        {"2008", "August", 0.00, 0.00},
      });

    edition.selectMonth(200808)
      .checkNegativeAmountsSelected()
      .setAmount("10")
      .checkChart(new Object[][]{
        {"2008", "June", 0.00, 35.00},
        {"2008", "July", 0.00, 20.00},
        {"2008", "August", 0.00, 10.00, true},
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

    edition.selectNoMonth()
      .checkAmountIsDisabled();

    edition.selectMonth(200807)
      .checkNegativeAmountsSelected()
      .checkAmount(20.00)
      .selectPositiveAmounts()
      .checkChart(new Object[][]{
        {"2008", "June", 0.00, 35.00},
        {"2008", "July", 0.00, 20.00, true},
        {"2008", "August", 0.00, 10.00},
      });

    edition.validate();

    views.selectBudget();

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

    views.selectCategorization();
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
        {"2008", "June", 0.00, 35.00, true},
        {"2008", "July", 0.00, -20.00},
        {"2008", "August", 0.00, 0.00},
      });

    edition.selectMonth(200808)
      .checkPositiveAmountsSelected()
      .setAmount("10")
      .selectNegativeAmounts()
      .checkChart(new Object[][]{
        {"2008", "June", 0.00, 35.00, true},
        {"2008", "July", 0.00, -20.00},
        {"2008", "August", 0.00, -10.00},
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

    edition.selectNoMonth()
      .checkAmountIsDisabled();

    edition.selectMonth(200807)
      .checkNegativeAmountsSelected()
      .checkAmount(20.00)
      .selectPositiveAmounts()
      .checkChart(new Object[][]{
        {"2008", "June", 0.00, 30.00, true},
        {"2008", "July", 0.00, 20.00},
        {"2008", "August", 0.00, -10.00},
      });

    edition.validate();

    views.selectBudget();

    timeline.selectMonth("2008/08");
    budgetView.income.checkSeries("Salary", 0, -10.00);

    timeline.selectMonth("2008/07");
    budgetView.income.checkSeries("Salary", 0, 20.00);
  }

  public void testInManualDoNotSelectHiddenSeriesBudget() throws Exception {
    views.selectBudget();
    budgetView.variable.createSeries()
      .setName("S1")
      .setSixMonths()
      .checkMonthSelectorsVisible(true)
      .toggleMonth(3)
      .checkMonthIsChecked(3, 9)
      .validate();
    timeline.selectMonth("2008/08");
    budgetView.variable.editSeriesList().selectSeries("S1").checkAmountIsDisabled()
      .cancel();
  }

  public void testAutomaticAndManualModes() throws Exception {

    System.out.println("SeriesEditionTest.testAutomaticAndManualModes ---  A VALIDER");

    OfxBuilder
      .init(this)
      .addTransaction("2008/06/28", -30., "EAU")
      .addTransaction("2008/08/27", -20., "EAU")
      .load();

    views.selectCategorization();
    timeline.selectMonth("2008/06");
    categorization.setNewRecurring("EAU", "EAU");

    views.selectCategorization();
    timeline.selectMonth("2008/08");
    categorization.setRecurring("EAU", "EAU");
    categorization.selectRecurring()
      .editSeries()
      .selectSeries("EAU")
      .setTwoMonths()
      .toggleMonth(6)
      .validate();
    views.selectData();
    timeline.selectMonths("2008/06", "2008/07", "2008/08");
    transactions.initContent()
      .add("28/08/2008", TransactionType.PLANNED, "Planned: EAU", "", -10.00, "EAU")
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

    views.selectCategorization();
    categorization.selectTransactions("EAU");
    categorization.selectVariable().createSeries().setName("Eau")
      .setTwoMonths()
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

    views.selectCategorization();
    categorization.selectTransactions("EAU");
    categorization.selectVariable().createSeries().setName("Eau")
      .setCustom()
      .checkMonthIsChecked(6, 8)
      .checkMonthIsNotChecked(1, 2, 3, 4, 5, 7, 9, 10, 11, 12)
      .setEveryMonth()
      .checkMonthIsChecked(1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 12)
      .setCustom()
      .checkMonthIsChecked(6, 8)
      .validate();
  }

  public void testUnknownPeriodicity() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/29", -100.00, "Virement")
      .load();

    views.selectCategorization();
    categorization.selectTableRow(0);
    categorization.selectSavings()
      .selectAndCreateSavingsSeries("epargne", "Main accounts");

    views.selectData();
    timeline.selectMonths("2008/06", "2008/07");
    transactions.initContent()
      .add("29/07/2008", TransactionType.PLANNED, "Planned: epargne", "", -100.00, "epargne")
      .add("29/06/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "epargne")
      .check();

    views.selectCategorization();
    categorization.selectSavings().editSeries("epargne")
      .setIrregular()
      .checkChart(new Object[][]{
        {"2008", "June", 100.00, 100.00, true},
        {"2008", "July", 0.00, 0.00, true},
        {"2008", "August", 0.00, 0.00},
      })
      .validate();

    views.selectData();
    timeline.selectMonths("2008/06", "2008/07");
    transactions.initContent()
      .add("29/06/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "epargne")
      .check();
  }

  public void testUnkownPeriodicityAndPreverseAutomatic() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/29", -100.00, "Virement")
      .load();

    views.selectCategorization();
    categorization.selectTableRow(0);
    categorization.selectSavings()
      .selectAndCreateSavingsSeries("epargne", "Main accounts");

    categorization.selectSavings()
      .editSeries("epargne")
      .setIrregular()
      .setTwoMonths()
      .validate();

    categorization.selectSavings().editSeries("epargne")
      .setIrregular()
      .validate();

    categorization.selectSavings().editSeries("epargne")
      .setIrregular()
      .setTwoMonths()
      .validate();
  }

  public void testChangeLastMonthInIrregular() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/29", -100.00, "Virement")
      .load();

    views.selectCategorization();
    categorization.selectTableRow(0);
    categorization.selectSavings()
      .selectAndCreateSavingsSeries("epargne", "Main accounts");
    categorization.selectSavings().editSeries("epargne")
      .setIrregular()
      .setEndDate(200807)
      .validate();

    operations.openPreferences().setFutureMonthsCount(2).validate();
    
    categorization.selectSavings().editSeries("epargne")
      .setEndDate(200810)
      .validate();
    timeline.checkSpanEquals("2008/06", "2008/10");

    views.selectData();
    timeline.selectAll();
    transactions.initContent()
      .add("29/06/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "epargne")
      .check();
  }

  public void testAddMonthUpdateBudgetWithLastValidBudget() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/04", -100.00, "Virement")
      .addTransaction("2008/07/04", -10.00, "McDo")
      .load();
    views.selectCategorization();
    categorization.selectTransactions("Virement");
    categorization.selectSavings()
      .selectAndCreateSavingsSeries("epargne", "Main accounts");
    categorization.selectSavings().editSeries("epargne")
      .setTwoMonths()
      .validate();
    timeline.selectAll();
    views.selectData();
    transactions.initContent()
      .add("04/08/2008", TransactionType.PLANNED, "Planned: epargne", "", -100.00, "epargne")
      .add("04/07/2008", TransactionType.PRELEVEMENT, "McDo", "", -10.00)
      .add("04/06/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "epargne")
      .check();
    operations.openPreferences().setFutureMonthsCount(2).validate();
    timeline.selectAll();
    timeline.checkSpanEquals("2008/06", "2008/10");
    transactions.initContent()
      .add("04/10/2008", TransactionType.PLANNED, "Planned: epargne", "", -100.00, "epargne")
      .add("04/08/2008", TransactionType.PLANNED, "Planned: epargne", "", -100.00, "epargne")
      .add("04/07/2008", TransactionType.PRELEVEMENT, "McDo", "", -10.00)
      .add("04/06/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "epargne")
      .check();
  }

  public void testNoAutomaticAddMonth() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/04", -100.00, "Virement")
      .addTransaction("2008/07/04", -10.00, "McDo")
      .load();
    views.selectCategorization();
    categorization.selectTransactions("Virement");
    categorization.selectSavings()
      .selectAndCreateSavingsSeries("epargne", "Main accounts");
    operations.openPreferences().setFutureMonthsCount(1).validate();
    categorization.selectSavings().editSeries("epargne")
      .selectMonth(200809)
      .setAmount("0")
      .setTwoMonths()
      .validate();
    timeline.selectAll();
    views.selectData();
    transactions.initContent()
      .add("04/08/2008", TransactionType.PLANNED, "Planned: epargne", "", -100.00, "epargne")
      .add("04/07/2008", TransactionType.PRELEVEMENT, "McDo", "", -10.00)
      .add("04/06/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "epargne")
      .check();
    operations.openPreferences().setFutureMonthsCount(2).validate();
    timeline.selectAll();
    timeline.checkSpanEquals("2008/06", "2008/10");
    transactions.initContent()
      .add("04/10/2008", TransactionType.PLANNED, "Planned: epargne", "", -100.00, "epargne")
      .add("04/08/2008", TransactionType.PLANNED, "Planned: epargne", "", -100.00, "epargne")
      .add("04/07/2008", TransactionType.PRELEVEMENT, "McDo", "", -10.00)
      .add("04/06/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "epargne")
      .check();
  }

  public void testChangeBudgetAmountWhileInOverrun() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/08/28", 5000., "Complement")
      .load();

    views.selectCategorization();
    categorization.setExceptionalIncome("Complement", "Salaire sup", true);

    views.selectBudget();
    budgetView.income.editSeries("Salaire sup")
      .selectMonth(200808)
      .setAmount("6000")
      .validate();

    views.selectData();
    transactions.initContent()
      .add("28/08/2008", TransactionType.PLANNED, "Planned: Salaire sup", "", 1000.00, "Salaire sup")
      .add("28/08/2008", TransactionType.VIREMENT, "Complement", "", 5000.00, "Salaire sup")
      .check();
  }

  public void testExtraWithOnlyOneMonth() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2007/05/04", -100.00, "Virement")
      .addTransaction("2007/06/04", -100.00, "CENTER PARC")
      .addTransaction("2008/07/04", -10.00, "McDo")
      .load();
    views.selectCategorization();
    categorization.selectTransactions("CENTER PARC");
    categorization.selectExtras().createSeries()
      .setName("Center Parc")
      .checkSingleMonthSelected()
      .checkSingleMonthDate("June 2007")
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
    views.selectCategorization();
    categorization.selectTransactions("CENTER PARC");
    categorization.selectExtras().createSeries()
      .setName("Center Parc")
      .checkEveryMonthSelected()
      .checkStartDate("June 2007")
      .checkEndDate("Mar 2008")
      .validate();
  }

  public void testExtraIsInitializedWithASingleMonthPeriodicityWhenOnlyOneMonthIsSelected() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/04", -10.00, "McDo")
      .load();

    timeline.selectMonth("2008/06");
    views.selectBudget();
    budgetView.extras.createSeries()
      .setName("Center Parc")
      .checkSingleMonthSelected()
      .checkSingleMonthDate("June 2008")
      .validate();
  }

  public void testOneTimeAYearCanBeChangedWhenATransactionWithDifferentMonthIsSelected() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/12/30", -20., "PointP")
      .load();

    views.selectCategorization();
    categorization.selectTransactions("PointP");
    categorization.selectVariable().createSeries()
      .setName("Brico")
      .setOnceAYear()
      .toggleMonth(11)
      .checkMonthIsChecked(11)
      .cancel();
  }

  public void testCreatingAnAccountFromTheSeriesDialog() throws Exception {

    views.selectHome();
    mainAccounts.createMainAccount("Main", 0.0);

    views.selectBudget();
    SeriesEditionDialogChecker dialog = budgetView.savings.createSeries().setName("ING");

    dialog.createAccount()
      .setAccountName("Virt ING")
      .setAccountNumber("1234")
      .selectBank("ING Direct")
      .checkUpdateModeIsFileImport()
      .checkIsSavings()
      .validate();

    dialog
      .setFromAccount("Main")
      .setToAccount("Virt ING")
      .validate();

    views.selectHome();
    savingsAccounts.edit("Virt ING")
      .checkAccountName("Virt ING")
      .checkAccountNumber("1234")
      .checkSelectedBank("ING Direct")
      .checkUpdateModeIsFileImport()
      .checkIsSavings()
      .cancel();

    views.selectBudget();
    budgetView.savings.editSeries("ING")
      .checkToAccount("Virt ING")
      .validate();
    budgetView.savings.checkOrder("ING");
  }


  public void testCreateDeleteCreateBudget() throws Exception {
    operations.openPreferences().setFutureMonthsCount(12).validate();
    views.selectBudget();
    budgetView.variable.createSeries()
      .setName("Serie")
      .selectAllMonths()
      .setAmount(100)
      .validate();

    budgetView.variable.editSeries("Serie")
      .setEndDate(200808)
      .removeEndDate()
      .setStartDate(200808)
      .setEndDate(200812)
      .removeStartDate()
      .validate();
  }

  public void testAlignPlannedAndObservedAmounts() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Free Telecom")
      .load();

    timeline.selectMonth("2008/07");
    views.selectCategorization();
    categorization.checkTable(new Object[][]{
      {"29/07/2008", "", "Free Telecom", -29.00}
    });
    categorization.setNewRecurring("Free Telecom", "Internet");

    views.selectBudget();
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
      .checkAmount("")
      .validate();
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkSeries("Internet", -29.00, -29.00);
    timeline.selectMonth("2008/08");
    budgetView.recurring.checkSeries("Internet", 0.00, 0.00);
  }

  public void testAutomaticAdjustPlannedAndObservedForFirstMonthWithObserved() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder.init(this)
      .addTransaction("2008/05/15", -10.00, "Tel")
      .addTransaction("2008/06/15", -90.00, "Free")
      .addTransaction("2008/07/15", -95.00, "Free")
      .addTransaction("2008/08/15", -98.00, "Free")
      .load();

    views.selectCategorization();
    categorization.selectTransactions("Free")
      .selectRecurring()
      .createSeries("internet");

    views.selectBudget();
    timeline.selectMonth("2008/06");
    budgetView.recurring.checkSeries("internet", -90, -90);
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkSeries("internet", -95, -90);
    timeline.selectMonth("2008/08");
    budgetView.recurring.checkSeries("internet", -98, -95);
    timeline.selectMonth("2008/09");
    budgetView.recurring.checkSeries("internet", 0, -98);

    views.selectCategorization();
    categorization.showSelectedMonthsOnly();
    timeline.selectMonth("2008/07");
    categorization.selectTransaction("free")
      .setUncategorized();
    views.selectBudget();
    timeline.selectMonth("2008/06");
    budgetView.recurring.checkSeries("internet", -90, -90);
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkSeries("internet", 0, -90);
    timeline.selectMonth("2008/08");
    budgetView.recurring.checkSeries("internet", -98, 0);
    timeline.selectMonth("2008/09");
    budgetView.recurring.checkSeries("internet", 0, -98);

    views.selectCategorization();
    categorization.showSelectedMonthsOnly();
    timeline.selectMonth("2008/06");
    categorization.selectTransaction("free")
      .setUncategorized();
    views.selectBudget();
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

    views.selectCategorization();
    categorization.selectTableRow(0)
      .selectRecurring()
      .createSeries()
      .setName("Telephone")
      .setCustom()
      .setPeriodMonths(1, 3, 5)
      .validate();

    categorization.selectTableRow(1)
      .selectRecurring()
      .checkNonActiveSeries("Telephone")
      .selectSeries("Telephone");

    views.selectBudget();
    timeline.selectMonth("2008/06");
    budgetView.recurring.checkSeries("Telephone", -10, 0);
  }

  public void testChangeBugdetArea() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/01", -29.00, "Free Telecom")
      .load();

    views.selectCategorization();
    categorization.setNewRecurring("Free Telecom", "Internet");
    categorization.editSeries("Internet")
      .checkBudgetAreaContent()
      .checkBudgetArea("Recurring")
      .changeBudgetArea("Variable")
      .checkBudgetArea("Variable")
      .validate();
    categorization.selectTransaction("Free Telecom")
      .selectVariable()
      .checkNoSeriesMessageHidden();

    views.selectBudget();
    budgetView.recurring.checkTotalGauge(0, 0);
    budgetView.variable.checkTotalGauge(-29., -29.);
    budgetView.variable.checkSeries("Internet", -29., -29.);

    views.selectEvolution();
    seriesEvolution.initContent()
      .add("Main accounts", "", "", "", "", "", "", "", "", "", "", "", "")
        .add("Balance", "", "-29.00", "", "", "", "", "", "", "", "", "", "")
        .add("Savings accounts", "", "", "", "", "", "", "", "", "", "", "", "")
        .add("To categorize", "", "", "", "", "", "", "", "", "", "", "", "")
        .add("Income", "", "", "", "", "", "", "", "", "", "", "", "")
        .add("Recurring", "", "", "", "", "", "", "", "", "", "", "", "")
        .add("Variable", "", "29.00", "", "", "", "", "", "", "", "", "", "")
        .add("Internet", "", "29.00", "", "", "", "", "", "", "", "", "", "")
        .add("Extras", "", "", "", "", "", "", "", "", "", "", "", "")
        .add("Savings", "", "", "", "", "", "", "", "", "", "", "", "")
        .add("Other", "", "", "", "", "", "", "", "", "", "", "", "")
        .check();
  }

  public void testNoBudgetAreaForSavingsAndIncom() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/01", 1000, "salaire")
      .load();
    views.selectCategorization();
    categorization.setNewIncome("salaire", "salaire");
    views.selectBudget();
    budgetView.income.editSeries("salaire")
      .checkBudgetAreaIsHidden()
      .validate();
    views.selectCategorization();
    categorization.selectTransaction("salaire")
      .selectUncategorized().setUncategorized();
    views.selectCategorization();
    categorization.setNewSavings("salaire", "complement", "external", "Main accounts");
    views.selectBudget();
    budgetView.savings.editSeries("complement")
      .checkBudgetAreaIsHidden()
      .validate();
  }

  public void testChangeTabAndReopen() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/01", -29.00, "Auchan")
      .load();

    views.selectCategorization();
    categorization.selectTableRow(0);
    categorization.selectVariable().createSeries()
      .setName("Groceries")
      .gotoSubSeriesTab()
      .validate();

    categorization.editSeries("Groceries")
      .checkMainTabIsSelected()
      .cancel();
  }

  // En mode virement extern=>non importé on creé des operations automatiquement
  // et du coup on ne peut plus changer la date de debut
  // il faut autoriser le changement meme si il y a des operations
  public void testAllowChangeOfBeginOfDayOnSavingsEnvelope() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/01/01", -29.00, "Auchan") // pour creer des mois dans le passe
      .load();
    savingsAccounts.createSavingsAccount("ING", 1000.);
    views.selectSavings();
    savingsView.createSeries()
      .setName("Epargne")
      .setFromAccount("External account")
      .setToAccount("ING")
      .setDay("4")
      .selectAllMonths()
      .setAmount(100)
      .validate();

    savingsView.editSeries("ING", "Epargne")
      .setStartDate(200805)
      .validate();

    timeline.selectMonth("2008/03");

    views.selectData();
    transactions.initContent()  // should be empty
      .check();

    timeline.selectMonth("2008/08");
    views.selectSavings();
    savingsView.editSeries("ING", "Epargne")
      .setEndDate(200807)
      .validate();

    views.selectData();
    transactions.initContent()  // should be empty
      .check();
    
  }
}
