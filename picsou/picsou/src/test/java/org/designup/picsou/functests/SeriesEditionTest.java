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
      .checkName("Internet")
      .setName("Free")
      .switchToManual()
      .checkTable(new Object[][]{
        {"2008", "August", "0.00", "29.00"},
        {"2008", "July", "29.00", "29.00"},
      })
      .checkMonthSelected(1)
      .checkAmountLabel("Planned amount for july 2008")
      .selectAllMonths()
      .checkAmountLabel("Planned amount for july - august 2008")
      .validate();

    budgetView.recurring.checkSeries("Free", -29.00, -29.00);
  }

  public void testSeriesNamesAreTrimmed() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Auchan")
      .load();

    timeline.selectMonth("2008/07");

    views.selectCategorization();
    categorization.selectTableRow(0);
    categorization.selectEnvelopes().createSeries()
      .setNameAndValidate("    Groceries   ");

    categorization.selectEnvelopes().selectSeries("Groceries");

    views.selectBudget();
    budgetView.envelopes.checkSeries("Groceries", -29.00, -29.00);
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
      .checkName("Internet")
      .setName("Free")
      .checkAccountsComboAreHidden()
      .switchToManual()
      .checkTable(new Object[][]{
        {"2008", "August", "29.00", "29.00"},
        {"2008", "July", "29.00", "29.00"},
        {"2008", "June", "29.00", "29.00"},
        {"2008", "May", "29.00", "29.00"},
      })
      .checkMonthsSelected(0, 2)
      .validate();

    timeline.checkSelection("2008/06", "2008/08");
    budgetView.recurring.checkSeries("Free", -58.00, -58.00);
  }

  public void testAllMonthsAreSelectedInBudgetTableIfCurrentMonthIsNotFound() throws Exception {

    operations.openPreferences().setFutureMonthsCount(4).validate();

    views.selectBudget();
    timeline.selectMonths("2008/10", "2008/11");
    budgetView.specials.createSeries()
      .setName("Plumber")
      .checkMonthsSelected(0, 1)
      .setAmount(200)
      .validate();

    timeline.selectMonth("2008/10");
    budgetView.specials.checkSeries("Plumber", 0, -200.00);

    timeline.selectMonth("2008/11");
    budgetView.specials.checkSeries("Plumber", 0, -200.00);
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
      .switchToManual()
      .checkTable(new Object[][]{
        {"2008", "August", "0.00", "29.00"},
        {"2008", "July", "29.00", "29.00"},
      })
      .checkMonthSelected(1)
      .checkAmount("29.00")
      .setAmount("40.00")
      .checkTable(new Object[][]{
        {"2008", "August", "0.00", "29.00"},
        {"2008", "July", "29.00", "40.00"},
      })
      .validate();

    budgetView.recurring.checkSeries("Internet", -29.00, -40.00);

    budgetView.recurring.editSeries("Internet")
      .checkTable(new Object[][]{
        {"2008", "August", "0.00", "29.00"},
        {"2008", "July", "29.00", "40.00"},
      })
      .checkAmountIsSelected()
      .checkMonthSelected(1)
      .checkAmount("40.00")
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
      .switchToManual()
      .checkTable(new Object[][]{
        {"2008", "August", "29.00", "29.00"},
        {"2008", "July", "29.00", "29.00"},
        {"2008", "June", "29.00", "0"},
        {"2008", "May", "0.00", "0"},
      });
    editionDialogChecker
      .toggleMonth("May")
      .checkTable(new Object[][]{
        {"2008", "August", "29.00", "29.00"},
        {"2008", "July", "29.00", "29.00"},
        {"2008", "June", "29.00", "0"},
      });
    editionDialogChecker
      .toggleMonth("Aug")
      .checkTable(new Object[][]{
        {"2008", "August", "29.00", "29.00"},
        {"2008", "July", "29.00", "29.00"},
        {"2008", "June", "29.00", "0"},
      });
    editionDialogChecker
      .toggleMonth("Aug")
      .checkTable(new Object[][]{
        {"2008", "August", "29.00", "29.00"},
        {"2008", "July", "29.00", "29.00"},
        {"2008", "June", "29.00", "0"},
      })
      .validate();

    budgetView.recurring.editSeries("Internet")
      .checkTable(new Object[][]{
        {"2008", "August", "29.00", "29.00"},
        {"2008", "July", "29.00", "29.00"},
        {"2008", "June", "29.00", "0"},
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
    categorization.setNewEnvelope("Auchan", "Groceries");
    categorization.setEnvelope("Monoprix", "Groceries");
    categorization.setNewRecurring("Free Telecom", "Internet");
    categorization.setNewRecurring("EDF", "Electricity");
    categorization.setExceptionalIncome("WorldCo - Bonus", "Exceptional Income", true);
    categorization.setNewIncome("WorldCo", "Salary");

    views.selectBudget();

    budgetView.recurring.editSeriesList()
      .checkSeriesListEquals("Electricity", "Internet")
      .validate();

    budgetView.envelopes.editSeriesList()
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
    budgetView.recurring.editSeriesList()
      .checkSeriesListEquals("Electricity", "Internet")
      .checkSeriesSelected("Electricity")
      .setCustom()
      .switchToManual()
      .selectAllMonths()
      .setAmount("70")
      .toggleMonth("Aug")
      .checkTable(new Object[][]{
        {"2008", "July", "55.00", "70.00"},
      })

      .selectSeries("Internet")
      .checkMonthIsChecked("Aug")
      .checkTable(new Object[][]{
        {"2008", "August", "29.00", "0"},
        {"2008", "July", "0.00", "0"},
      })
      .toggleMonth("Jul")

      .selectSeries("Electricity")
      .checkMonthIsChecked("Jul")
      .checkMonthIsNotChecked("Aug")
      .checkTable(new Object[][]{
        {"2008", "July", "55.00", "70.00"},
      })
      .validate();
  }

  public void testNoSeriesSelected() throws Exception {
    views.selectBudget();
    budgetView.recurring.createSeries()
      .setName("series")
      .validate();

    budgetView.recurring.editSeriesList()
      .unselect()
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
      .switchToManual()
      .checkTitle("Recurring")
      .checkNameIsSelected()
      .checkSeriesListEquals("New series")
      .checkSeriesSelected("New series")
      .checkName("New series")
      .setName("Free Telecom")
      .selectAllMonths()
      .setAmount("40")
      .checkSeriesListEquals("Free Telecom")
      .checkTable(new Object[][]{
        {"2008", "August", "", "40.00"},
        {"2008", "July", "", "40.00"},
      })
      .validate();

    budgetView.recurring.checkSeries("Free Telecom", -0.0, -40.0);
  }

  public void testExistingSeriesAreVisibleWhenCreatingANewSeries() throws Exception {
    views.selectBudget();

    budgetView.recurring.createSeries()
      .checkTitle("Recurring")
      .setName("My recurrence")
      .validate();

    budgetView.envelopes.createSeries()
      .checkTitle("Envelopes")
      .setName("My envelope")
      .validate();

    budgetView.envelopes.createSeries()
      .checkSeriesListEquals("My envelope", "New series")
      .checkSeriesSelected("New series")
      .setName("My new envelope")
      .validate();

    budgetView.envelopes.createSeries()
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
      .switchToManual()
      .createSeries()
      .setCustom()
      .switchToManual()
      .checkSeriesListEquals("New series", "New series");

    seriesEdition
      .selectSeries(0)
      .selectAllMonths()
      .setAmount("70")
      .toggleMonth("Aug")
      .checkTable(new Object[][]{
        {"2008", "July", "", "70.00"},
      });

    seriesEdition
      .selectSeries(1)
      .checkMonthIsChecked("Aug")
      .toggleMonth("Jul")
      .checkTable(new Object[][]{
        {"2008", "August", "", "0"},
      })

      .selectSeries(0)
      .checkMonthIsChecked("Jul")
      .checkMonthIsNotChecked("Aug")
      .checkTable(new Object[][]{
        {"2008", "July", "", "70.00"},
      })
      .cancel();
  }

  public void testCancel() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .load();
    views.selectBudget();
    budgetView.envelopes.createSeries()
      .setName("A series")
      .cancel();
    budgetView.envelopes.createSeries()
      .checkSeriesListEquals("New series")
      .cancel();
  }

  public void testEsc() throws Exception {
    views.selectBudget();
    SeriesEditionDialogChecker edition = budgetView.envelopes.createSeries();
    edition.editStartDate().pressEscapeKey();
    Thread.sleep(50);
    edition.checkNoStartDate();
    edition.cancel();
  }

  public void testEditDate() throws Exception {
    views.selectBudget();
    budgetView.envelopes.createSeries()
      .switchToManual()
      .setStartDate(200809)
      .setEndDate(200810)
      .checkStartDate("Sep 2008")
      .checkEndDate("Oct 2008")
      .checkTableIsEmpty()
      .removeStartDate()
      .removeEndDate()
      .checkNoStartDate()
      .checkNoEndDate()
      .cancel();

    budgetView.envelopes.createSeries()
      .unselect()
      .checkCalendarsAreDisabled()
      .cancel();
  }

  public void testStartEndCalendar() throws Exception {
    views.selectBudget();
    SeriesEditionDialogChecker edition = budgetView.envelopes.createSeries()
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
    categorization.setNewEnvelope("Auchan", "Courant");
    views.selectBudget();
    SeriesEditionDialogChecker dialog = budgetView.envelopes.editSeries("Courant");
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
    budgetView.envelopes.createSeries()
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
    budgetView.envelopes.createSeries()
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
    budgetView.envelopes.createSeries()
      .setName("manualThenSingleMonth")
      .switchToManual()
      .setSingleMonth()
      .checkSingleMonthDate("June 2008")
      .checkTable(new Object[][]{
        {"2008", "June", "", "0"}
      })
      .validate();

    budgetView.envelopes.createSeries()
      .setName("singleMonthThenManual")
      .setSingleMonth()
      .switchToManual()
      .checkSingleMonthDate("June 2008")
      .checkTable(new Object[][]{
        {"2008", "June", "", "0"}
      })
      .validate();

  }

  public void testDateAndBudgetSeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2007/02/10", -29.00, "Free Telecom")
      .load();
    views.selectBudget();
    SeriesEditionDialogChecker edition = budgetView.envelopes.createSeries();
    edition.setCustom()
      .switchToManual()
      .toggleMonth("Jan", "Mar", "Jul", "Sep", "Nov");

    edition.selectAllMonths()
      .setAmount("30");
    edition.setStartDate(200709)
      .checkTable(new Object[][]{
        {"2008", "August", "", "30.00"},
        {"2008", "June", "", "30.00"},
        {"2008", "May", "", "30.00"},
        {"2008", "April", "", "30.00"},
        {"2008", "February", "", "30.00"},
        {"2007", "December", "", "30.00"},
        {"2007", "October", "", "30.00"}
      });

    edition.setEndDate(200801)
      .checkTable(new Object[][]{
        {"2007", "December", "", "30.00"},
        {"2007", "October", "", "30.00"},
      });

    edition.setEndDate(200802)
      .checkTable(new Object[][]{
        {"2008", "February", "", "0"},
        {"2007", "December", "", "30.00"},
        {"2007", "October", "", "30.00"},
      });

    edition.setStartDate(200712)
      .checkTable(new Object[][]{
        {"2008", "February", "", "0"},
        {"2007", "December", "", "30.00"},
      });
    edition.selectAllMonths().setAmount("30")
      .toggleMonth("Dec", "Feb").toggleMonth("Dec", "Feb")
      .checkTable(new Object[][]{
        {"2008", "February", "", "30.00"},
        {"2007", "December", "", "30.00"},
      });
    edition.cancel();
  }

  public void testMonthIsHiddenIfLessThanOneMonthInDateRange() throws Exception {
    views.selectBudget();
    SeriesEditionDialogChecker edition = budgetView.envelopes.createSeries();
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
      .addTransaction("2008/06/30", -60, "Forfait Kro")
      .load();

    views.selectCategorization();
    categorization.selectTableRow(0);
    categorization.selectEnvelopes().createSeries()
      .setName("Drinking")
      .validate();

    views.selectData();
    timeline.selectMonths("2008/06", "2008/07");
    transactions.initContent()
      .add("30/07/2008", TransactionType.PLANNED, "Planned: Drinking", "", -60.00, "Drinking")
      .add("30/06/2008", TransactionType.PRELEVEMENT, "Forfait Kro", "", -60.00, "Drinking")
      .check();

    views.selectCategorization();
    categorization.editSeries()
      .selectSeries("Drinking")
      .setName("Leisures")
      .validate();
    categorization.getEnvelopes().checkContainsSeries("Leisures");

    views.selectData();
    transactions.initContent()
      .add("30/07/2008", TransactionType.PLANNED, "Planned: Leisures", "", -60.00, "Leisures")
      .add("30/06/2008", TransactionType.PRELEVEMENT, "Forfait Kro", "", -60.00, "Leisures")
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
    SeriesEditionDialogChecker edition = budgetView.envelopes.createSeries();
    edition.setName(null)
      .switchToManual()
      .setAmount(null)
      .getNameBox().pressKey(Key.A).pressKey(Key.A);
    TextBox amount = edition.selectAllMonths().getAmountTextBox();
    JTextField textAmount = (JTextField)amount.getAwtComponent();
    textAmount.select(0, textAmount.getText().length());
    amount.pressKey(Key.DELETE).pressKey(Key.d1).pressKey(Key.d3);
    edition
      .checkName("AA")
      .checkAmount("13")
      .checkSeriesListEquals("AA");
    edition.checkTable(new Object[][]{
      {"2008", "August", "", "13.00"},
      {"2008", "July", "", "13.00"},
      {"2008", "June", "", "13.00"}
    });
    edition.cancel();
  }

  public void testAutomaticBudget() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -20., "Auchan")
      .addTransaction("2008/07/04", -30., "Auchan")
      .load();

    views.selectCategorization();
    categorization.selectTransactions("Auchan");
    categorization.selectEnvelopes().createSeries()
      .switchToManual()
      .setName("Courant")
      .checkTable(new Object[][]{
        {"2008", "August", "", "0"},
        {"2008", "July", "", "0"},
        {"2008", "June", "", "0"}
      })
      .selectAllMonths()
      .setAmount("40")
      .validate();

    categorization.setEnvelope("Auchan", "Courant");
    categorization.selectEnvelopes().editSeries()
      .checkTable(new Object[][]{
        {"2008", "August", "0.00", "40.00"},
        {"2008", "July", "30.00", "40.00"},
        {"2008", "June", "20.00", "40.00"}
      })
      .switchToAutomatic()
      .switchToManual()
      .checkTable(new Object[][]{
        {"2008", "August", "0.00", "30.00"},
        {"2008", "July", "30.00", "20.00"},
        {"2008", "June", "20.00", "20.00"}
      })
      .cancel();
  }

  public void testMonthsAreShownOrNotDependingOnThePeriodicity() throws Exception {
    views.selectBudget();
    SeriesEditionDialogChecker edition = budgetView.envelopes.createSeries();
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
    SeriesEditionDialogChecker edition = budgetView.envelopes.createSeries();
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
      .checkMonthIsChecked(1,3,5,7,9,11)
      .toggleMonth(4)
      .checkMonthIsChecked(2, 4, 6, 8, 10);
    edition.cancel();
  }

  public void testSwitchBetweenSeries() throws Exception {
    views.selectBudget();
    SeriesEditionDialogChecker edition = budgetView.envelopes.createSeries()
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
      budgetView.envelopes.createSeries()
        .setName("S1")
        .checkProfiles("Every month", "Every two months", "Every six months",
                       "Once a year", "Single month", "Custom", "Irregular");
    edition.cancel();
  }

  public void testSeriesListVisibility() throws Exception {
    views.selectBudget();
    budgetView.envelopes.createSeries()
      .checkSeriesListIsHidden()
      .setName("")
      .validate();

    budgetView.envelopes.editSeriesList()
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
    SeriesEditionDialogChecker edition = categorization.selectEnvelopes().createSeries();

    edition.setName("Diet Food")
      .switchToManual()
      .checkTable(new Object[][]{
        {"2008", "August", "", "0"},
        {"2008", "July", "", "0"},
        {"2008", "June", "", "0"}
      });

    edition.checkNegativeAmountsSelected();

    edition.selectMonth(200806)
      .checkNegativeAmountsSelected()
      .selectPositiveAmounts()
      .setAmount("35")
      .checkTable(new Object[][]{
        {"2008", "August", "", "0"},
        {"2008", "July", "", "0"},
        {"2008", "June", "", "+35.00"}
      });

    edition.selectMonth(200807)
      .checkNegativeAmountsSelected()
      .setAmount("20")
      .checkTable(new Object[][]{
        {"2008", "August", "", "0"},
        {"2008", "July", "", "20.00"},
        {"2008", "June", "", "+35.00"}
      });

    edition.selectMonth(200808)
      .checkNegativeAmountsSelected()
      .setAmount("10")
      .checkTable(new Object[][]{
        {"2008", "August", "", "10.00"},
        {"2008", "July", "", "20.00"},
        {"2008", "June", "", "+35.00"}
      });

    edition.selectMonth(200806)
      .checkPositiveAmountsSelected()
      .checkAmount("35.00")
      .selectNegativeAmounts()
      .checkTable(new Object[][]{
        {"2008", "August", "", "10.00"},
        {"2008", "July", "", "20.00"},
        {"2008", "June", "", "35.00"}
      });

    edition.selectNoMonth()
      .checkAmountDisabled();

    edition.selectMonth(200807)
      .checkNegativeAmountsSelected()
      .checkAmount("20.00")
      .selectPositiveAmounts()
      .checkTable(new Object[][]{
        {"2008", "August", "", "10.00"},
        {"2008", "July", "", "+20.00"},
        {"2008", "June", "", "35.00"}
      });

    edition.validate();

    views.selectBudget();

    timeline.selectMonth("2008/08");
    budgetView.envelopes.checkSeries("Diet Food", 0, -10.00);

    timeline.selectMonth("2008/07");
    budgetView.envelopes.checkSeries("Diet Food", 0, 20.00);
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
      .switchToManual()
      .checkTable(new Object[][]{
        {"2008", "August", "", "0"},
        {"2008", "July", "", "0"},
        {"2008", "June", "", "0"}
      });

    edition.checkPositiveAmountsSelected();

    edition.selectMonth(200806)
      .checkPositiveAmountsSelected()
      .setAmount("35")
      .checkTable(new Object[][]{
        {"2008", "August", "", "0"},
        {"2008", "July", "", "0"},
        {"2008", "June", "", "35.00"}
      });

    edition.selectMonth(200807)
      .checkPositiveAmountsSelected()
      .selectNegativeAmounts()
      .setAmount("20")
      .checkTable(new Object[][]{
        {"2008", "August", "", "0"},
        {"2008", "July", "", "-20.00"},
        {"2008", "June", "", "35.00"}
      });

    edition.selectMonth(200808)
      .checkPositiveAmountsSelected()
      .setAmount("10")
      .selectNegativeAmounts()
      .checkTable(new Object[][]{
        {"2008", "August", "", "-10.00"},
        {"2008", "July", "", "-20.00"},
        {"2008", "June", "", "35.00"}
      });

    edition.selectMonth(200806)
      .checkPositiveAmountsSelected()
      .checkAmount("35.00")
      .setAmount("30")
      .checkTable(new Object[][]{
        {"2008", "August", "", "-10.00"},
        {"2008", "July", "", "-20.00"},
        {"2008", "June", "", "30.00"}
      });

    edition.selectNoMonth()
      .checkAmountDisabled();

    edition.selectMonth(200807)
      .checkNegativeAmountsSelected()
      .checkAmount("20.00")
      .selectPositiveAmounts()
      .checkTable(new Object[][]{
        {"2008", "August", "", "-10.00"},
        {"2008", "July", "", "20.00"},
        {"2008", "June", "", "30.00"}
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
    budgetView.envelopes.createSeries()
      .setName("S1")
      .setSixMonths()
      .switchToManual()
      .checkMonthSelectorsVisible(true)
      .toggleMonth(3)
      .checkMonthIsChecked(3, 9)
      .validate();
    timeline.selectMonth("2008/08");
    budgetView.envelopes.editSeriesList().selectSeries("S1")
      .checkAmountIsDisabled()
      .cancel();
  }

  public void testAutomaticAndManualModes() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/28", -30., "EAU")
      .addTransaction("2008/08/27", -20., "EAU")
      .load();

    views.selectCategorization();
    timeline.selectMonth("2008/06");
    categorization.setNewEnvelope("EAU", "EAU");

    views.selectCategorization();
    timeline.selectMonth("2008/08");
    categorization.setEnvelope("EAU", "EAU");
    categorization.selectEnvelopes()
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
    categorization.selectEnvelopes().createSeries().setName("Eau")
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
    categorization.selectEnvelopes().createSeries().setName("Eau")
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
      .selectAndCreateSavingsSeries("epargne", OfxBuilder.DEFAULT_ACCOUNT_NAME);
    views.selectData();
    timeline.selectMonths("2008/06", "2008/07");
    transactions.initContent()
      .add("29/07/2008", TransactionType.PLANNED, "Planned: epargne", "", -100.00, "epargne")
      .add("29/06/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "epargne")
      .check();
    views.selectCategorization();
    categorization.selectSavings().editSeries("epargne")
      .setIrregular()
      .checkTable(new Object[][]{
        {"2008", "August", "0.00", "0"},
        {"2008", "July", "0.00", "0"},
        {"2008", "June", "100.00", "100.00"}
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
      .selectAndCreateSavingsSeries("epargne", OfxBuilder.DEFAULT_ACCOUNT_NAME);

    SeriesEditionDialogChecker edition = categorization.selectSavings()
      .editSeries("epargne")
      .checkAutomaticModeSelected()
      .setTwoMonths();

    edition
      .checkAutomaticModeSelected()
      .switchToManual()
      .checkManualModeSelected()
      .setIrregular()
      .setTwoMonths()
      .checkManualModeSelected()
      .validate();

    categorization.selectSavings().editSeries("epargne")
      .checkManualModeSelected()
      .setIrregular()
      .validate();

    categorization.selectSavings().editSeries("epargne")
      .checkManualModeSelected()
      .setIrregular()
      .setTwoMonths()
      .checkManualModeSelected()
      .validate();
  }

  public void testChangeLastMonthInIrregular() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/29", -100.00, "Virement")
      .load();
    views.selectCategorization();
    categorization.selectTableRow(0);
    categorization.selectSavings()
      .selectAndCreateSavingsSeries("epargne", OfxBuilder.DEFAULT_ACCOUNT_NAME);
    categorization.selectSavings().editSeries("epargne")
      .checkAutomaticModeSelected()
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
      .selectAndCreateSavingsSeries("epargne", OfxBuilder.DEFAULT_ACCOUNT_NAME);
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
      .selectAndCreateSavingsSeries("epargne", OfxBuilder.DEFAULT_ACCOUNT_NAME);
    operations.openPreferences().setFutureMonthsCount(1).validate();
    categorization.selectSavings().editSeries("epargne")
      .switchToManual()
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

  public void testSpecialWithOnlyOneMonth() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2007/05/04", -100.00, "Virement")
      .addTransaction("2007/06/04", -100.00, "CENTER PARC")
      .addTransaction("2008/07/04", -10.00, "McDo")
      .load();
    views.selectCategorization();
    categorization.selectTransactions("CENTER PARC");
    categorization.selectSpecial().createSeries()
      .setName("Center Parc")
      .checkSingleMonthSelected()
      .checkSingleMonthDate("June 2007")
      .checkManualModeSelected()
      .validate();
  }

  public void testSpecialWithSeveralMonths() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2007/05/04", -100.00, "Virement")
      .addTransaction("2007/06/04", -100.00, "CENTER PARC")
      .addTransaction("2007/11/04", -100.00, "CENTER PARC")
      .addTransaction("2008/03/04", -100.00, "CENTER PARC")
      .addTransaction("2008/07/04", -10.00, "McDo")
      .load();
    views.selectCategorization();
    categorization.selectTransactions("CENTER PARC");
    categorization.selectSpecial().createSeries()
      .setName("Center Parc")
      .checkEveryMonthSelected()
      .checkStartDate("June 2007")
      .checkEndDate("Mar 2008")
      .checkManualModeSelected()
      .validate();
  }

  public void testSpecialIsInitializedWithASingleMonthPeriodicityWhenOnlyOneMonthIsSelected() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/04", -10.00, "McDo")
      .load();

    timeline.selectMonth("2008/06");
    views.selectBudget();
    budgetView.specials.createSeries()
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
    categorization.selectEnvelopes().createSeries()
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
  }
}
