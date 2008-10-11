package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.CategoryChooserChecker;
import org.designup.picsou.functests.checkers.SeriesEditionDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
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
    categorization.setRecurring("Free Telecom", "Internet", MasterCategory.TELECOMS, true);

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
    categorization.selectEnvelopes();
    categorization.createEnvelopeSeries()
      .setName("    Groceries   ")
      .setCategory(MasterCategory.FOOD)
      .validate();

    categorization.selectEnvelopeSeries("Groceries", MasterCategory.FOOD, true);

    views.selectBudget();
    budgetView.envelopes.checkSeries("Groceries", -(double)29, -(double)29);
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
    categorization.setRecurring(0, "Internet", MasterCategory.TELECOMS, true);
    categorization.setRecurring(1, "Internet", MasterCategory.TELECOMS, false);
    categorization.setRecurring(2, "Internet", MasterCategory.TELECOMS, false);
    categorization.setRecurring(3, "Internet", MasterCategory.TELECOMS, false);
    views.selectBudget();
    timeline.selectMonths("2008/06", "2008/08");
    budgetView.recurring.checkSeries("Internet", -58.00, -58.00);

    budgetView.recurring.editSeries("Internet")
      .checkName("Internet")
      .setName("Free")
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

  public void testChangingTheAmountForAMonth() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Free Telecom")
      .load();

    timeline.selectMonth("2008/07");
    views.selectCategorization();
    categorization.setRecurring("Free Telecom", "Internet", MasterCategory.TELECOMS, true);

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
    categorization.setRecurring("Free Telecom", "Internet", MasterCategory.TELECOMS, true);

    views.selectBudget();
    timeline.selectMonths("2008/08", "2008/06");
    budgetView.recurring.editSeries("Internet")
      .setCustom()
      .switchToManual()
      .checkTable(new Object[][]{
        {"2008", "August", "29.00", "29.00"},
        {"2008", "July", "29.00", "29.00"},
        {"2008", "June", "29.00", "0"},
        {"2008", "May", "0.00", "0"},
      })
      .toggleMonth("May")
      .checkTable(new Object[][]{
        {"2008", "August", "29.00", "29.00"},
        {"2008", "July", "29.00", "29.00"},
        {"2008", "June", "29.00", "0"},
      })
      .toggleMonth("Aug")
      .checkTable(new Object[][]{
        {"2008", "July", "29.00", "29.00"},
        {"2008", "June", "29.00", "0"},
      })
      .toggleMonth("Aug")
      .checkTable(new Object[][]{
        {"2008", "August", "29.00", "0"},
        {"2008", "July", "29.00", "29.00"},
        {"2008", "June", "29.00", "0"},
      })
      .validate();

    budgetView.recurring.editSeries("Internet")
      .checkTable(new Object[][]{
        {"2008", "August", "29.00", "0"},
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
    categorization.setEnvelope("Auchan", "Groceries", MasterCategory.FOOD, true);
    categorization.setEnvelope("Monoprix", "Groceries", MasterCategory.FOOD, false);
    categorization.setRecurring("Free Telecom", "Internet", MasterCategory.TELECOMS, true);
    categorization.setRecurring("EDF", "Electricity", MasterCategory.HOUSE, true);
    categorization.setExceptionalIncome("WorldCo - Bonus", "Exceptional Income", true);
    categorization.setIncome("WorldCo", "Salary", true);

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
    categorization.setRecurring("Free Telecom", "Internet", MasterCategory.TELECOMS, true);
    categorization.setRecurring("EDF", "Electricity", MasterCategory.HOUSE, true);

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
      .checkCategory(MasterCategory.TELECOMS)
      .checkTable(new Object[][]{
        {"2008", "August", "29.00", "0"},
        {"2008", "July", "0.00", "0"},
      })
      .toggleMonth("Jul")

      .selectSeries("Electricity")
      .checkMonthIsChecked("Jul")
      .checkCategory(MasterCategory.HOUSE)
      .checkMonthIsNotChecked("Aug")
      .checkTable(new Object[][]{
        {"2008", "July", "55.00", "70.00"},
      })
      .validate();
  }

  public void testEmptySeriesList() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Free Telecom")
      .load();

    timeline.selectMonth("2008/07");

    views.selectBudget();

    budgetView.recurring.createSeries()
      .unselect()
      .checkAllMonthsDisabled()
      .checkAllFieldsDisabled()
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
      .setCategory(MasterCategory.TELECOMS)
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

    budgetView.recurring.checkSeries("Free Telecom", -(double)0, -(double)40);
  }

  public void testExistingSeriesAreVisibleWhenCreatingANewSeries() throws Exception {
    views.selectBudget();

    budgetView.recurring.createSeries()
      .checkTitle("Recurring")
      .setName("My recurrence")
      .setCategory(MasterCategory.FOOD)
      .validate();

    budgetView.envelopes.createSeries()
      .checkTitle("Envelopes")
      .setName("My envelope")
      .setCategory(MasterCategory.HOUSE)
      .validate();

    budgetView.envelopes.createSeries()
      .checkSeriesListEquals("My envelope", "New series")
      .checkSeriesSelected("New series")
      .setName("My new envelope")
      .setCategory(MasterCategory.HOUSE)
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
    budgetView.recurring
      .createSeries()
      .setCustom()
      .switchToManual()
      .createSeries()
      .setCustom()
      .switchToManual()
      .checkSeriesListEquals("New series", "New series")

      .selectSeries(0)
      .selectAllMonths()
      .setAmount("70")
      .toggleMonth("Aug")
      .checkTable(new Object[][]{
        {"2008", "July", "", "70.00"},
      })

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

  public void testCreatingEnvelopeSeriesWithMultipleCategories() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .load();

    views.selectBudget();
    SeriesEditionDialogChecker edition = budgetView.envelopes.createSeries();

    edition
      .checkSingleCategorizeIsVisible(false)
      .checkMultiCategorizeIsVisible(true);

    edition
      .setName("courant")
      .switchToManual()
      .setCategory(MasterCategory.CLOTHING, MasterCategory.FOOD);

    edition.openCategory()
      .checkSelected(MasterCategory.CLOTHING, MasterCategory.FOOD)
      .cancel();

    edition.selectAllMonths()
      .setAmount("1000");

    edition.createSeries()
      .checkNoCategory()
      .setName("bank")
      .setCategory(MasterCategory.BANK)
      .validate();

    budgetView.envelopes.editSeries("courant")
      .checkCategory(MasterCategory.CLOTHING, MasterCategory.FOOD)
      .cancel();

    budgetView.envelopes.checkSeries("courant", -(double)0, -(double)1000);

    views.selectCategorization();
    categorization
      .selectTableRow(0)
      .selectEnvelopes()
      .selectEnvelopeSeries("courant", MasterCategory.FOOD, false)
      .selectEnvelopeSeries("courant", MasterCategory.CLOTHING, false);

    views.selectData();
    transactions.checkCategory(1, MasterCategory.CLOTHING);
    transactions.checkSeries(1, "courant");
  }

  public void testUnselectAllCategories() throws Exception {
    views.selectBudget();
    SeriesEditionDialogChecker edition = budgetView.envelopes.createSeries();
    edition
      .setName("courant")
      .setCategory(MasterCategory.CLOTHING, MasterCategory.FOOD)
      .unselectCategory(MasterCategory.FOOD, MasterCategory.CLOTHING)
      .checkNoCategory()
      .openCategory()
      .checkUnselected(MasterCategory.FOOD, MasterCategory.CLOTHING)
      .validate();
    edition
      .checkNoCategory();
    edition
      .setCategory(MasterCategory.CLOTHING, MasterCategory.FOOD)
      .createSeries()
      .checkNoCategory()
      .cancel();
  }

  public void testIncomeCategorization() throws Exception {
    views.selectBudget();
    budgetView.income
      .createSeries()
      .checkCategorizeEnable(true)
      .checkMultiCategorizeIsVisible(false)
      .checkOkEnabled(false)
      .setCategory(MasterCategory.INCOME)
      .checkOkEnabled(true)
      .switchToManual()
      .checkAmountLabel("Planned amount for august 2008")
      .validate();
  }

  public void testEmptySeriesListDisablesCategorization() throws Exception {
    views.selectBudget();
    budgetView.envelopes
      .createSeries().unselect()
      .checkCategorizeEnable(false)
      .checkCategoryListEnable(true)
      .cancel();

    budgetView.income
      .createSeries().unselect()
      .checkCategorizeEnable(false)
      .checkCategorizeLabelIsEmpty()
      .checkMultiCategorizeIsVisible(false)
      .cancel();
  }

  public void testCancel() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .load();
    views.selectBudget();
    budgetView.envelopes.createSeries()
      .cancel();
    budgetView.envelopes.createSeries()
      .checkSeriesListEquals("New series")
      .cancel();
  }

  public void testEsc() throws Exception {
    views.selectBudget();
    SeriesEditionDialogChecker edition = budgetView.envelopes.createSeries();
    edition.getStartCalendar().pressEscapeKey();
    Thread.sleep(50);
    edition.checkNoStartDate();
  }

  public void testEditDate() throws Exception {
    views.selectBudget();
    budgetView.envelopes.createSeries()
      .switchToManual()
      .setStartDate(200809)
      .setEndDate(200810)
      .checkStartDate("Sep 2008")
      .checkEndDate("Oct 2008")
      .checkTable(new Object[][]{
      })
      .removeBeginDate()
      .removeEndDate()
      .checkNoStartDate()
      .checkNoEndDate()
      .cancel();
    budgetView.envelopes.createSeries()
      .unselect()
      .checkCalendarsAreDisable()
      .cancel();
  }

  public void testStartEndCalendar() throws Exception {
    views.selectBudget();
    SeriesEditionDialogChecker edition = budgetView.envelopes.createSeries()
      .setStartDate(200809);
    edition
      .getStartCalendar()
      .checkIsEnabled(200806, 200810)
      .cancel();
    edition.getEndCalendar()
      .checkIsEnabled(200809, 200810)
      .checkIsDisabled(200808)
      .cancel();
    edition.setEndDate(200811);
    edition.getStartCalendar()
      .checkIsDisabled(200812)
      .checkIsEnabled(200811, 200805)
      .cancel();

    edition.getEndCalendar()
      .checkIsDisabled(200808)
      .checkIsEnabled(200809, 200901)
      .cancel();

    edition.removeBeginDate();
    edition.getEndCalendar()
      .checkIsEnabled(200701, 200901)
      .cancel();
  }

  public void testStartEndDateWithTransaction() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .load();

    views.selectCategorization();
    categorization.setEnvelope("Auchan", "Courant", MasterCategory.FOOD, true);
    views.selectBudget();
    SeriesEditionDialogChecker editSeries = budgetView.envelopes.editSeries("Courant");
    editSeries.getStartCalendar()
      .checkIsEnabled(200805, 200806, 200807)
      .checkIsDisabled(200808, 200809)
      .selectMonth(200806);

    editSeries.getEndCalendar()
      .checkIsDisabled(200805, 200806)
      .checkIsEnabled(200807, 200808)
      .selectMonth(200808);

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
        {"2008", "February", "", "0"},
        {"2007", "December", "", "0"},
      });
  }

  public void testMonthIsHiddenIfLessThanOneMonthInDateRange() throws Exception {
    views.selectBudget();
    SeriesEditionDialogChecker edition = budgetView.envelopes.createSeries();
    edition.setStartDate(200705)
      .setEndDate(200709)
      .checkMonthIsEnabled("May", "Jun", "Jul", "Aug", "Sep")
      .checkMonthIsDisabled("Feb", "Mar", "Apr", "Oct")
      .setEndDate(200802)
      .checkMonthIsEnabled("Feb", "May", "Jun", "Jul", "Aug", "Sep")
      .checkMonthIsDisabled("Mar", "Apr")
      .cancel();
  }

  public void testOkButtonIsReenabledWhenTheDialogIsReopened() throws Exception {
    views.selectBudget();

    budgetView.envelopes.createSeries()
      .setName("My Series")
      .checkOkEnabled(false)
      .setCategory(MasterCategory.HOUSE)
      .checkOkEnabled(true)
      .validate();

    budgetView.envelopes.createSeries()
      .checkOkEnabled(false)
      .cancel();

    budgetView.envelopes.editSeriesList()
      .checkOkEnabled(true)
      .cancel();
  }

  public void testCreateNewCategoryFromCategoryChooser() throws Exception {
    views.selectBudget();
    SeriesEditionDialogChecker edition = budgetView.envelopes.createSeries();
    CategoryChooserChecker chooser = edition.openCategory();
    chooser.openCategoryEdition()
      .createMasterCategory("Assurance")
      .validate();
    chooser.checkContains("Assurance");
  }

  public void testCannotSelectCategoriesAllAndNone() throws Exception {
    views.selectBudget();

    CategoryChooserChecker chooser =
      budgetView.envelopes
        .createSeries()
        .openCategory();

    chooser.checkNotFound(MasterCategory.ALL, MasterCategory.NONE);
  }

  public void testRenameSeriesAndCategory() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -60, "Forfait Kro")
      .load();

    views.selectCategorization();
    categorization.selectTableRow(0);
    categorization.selectEnvelopes();
    categorization.createEnvelopeSeries()
      .setName("AA")
      .setCategory(MasterCategory.FOOD)
      .validate();
    views.selectData();
    timeline.selectMonths("2008/06", "2008/07");
    transactions.initContent()
      .add("30/07/2008", TransactionType.PLANNED, "Planned: AA", "", -60.00, "AA", MasterCategory.FOOD)
      .add("30/06/2008", TransactionType.PRELEVEMENT, "Forfait Kro", "", -60.00, "AA", MasterCategory.FOOD)
      .check();

    views.selectCategorization();
    SeriesEditionDialogChecker seriesEdition = categorization.editSeries(false)
      .selectSeries("AA")
      .setName("AA1");
    CategoryChooserChecker categoryChooser = seriesEdition
      .openCategory();
    categoryChooser.openCategoryEdition()
      .selectMaster(MasterCategory.FOOD)
      .renameMaster(getCategoryName(MasterCategory.FOOD), "Boire")
      .validate();
    categoryChooser.checkContains("Boire");
    categoryChooser.validate();
    seriesEdition.checkCategory("Boire");
    seriesEdition.validate();
    categorization.checkContainsLabelInEnvelope("AA1");
    categorization.checkContainsLabelInEnvelope("Boire");

    views.selectData();
    transactions.initContent()
      .add("30/07/2008", TransactionType.PLANNED, "Planned: AA1", "", -60.00, "AA1", "Boire")
      .add("30/06/2008", TransactionType.PRELEVEMENT, "Forfait Kro", "", -60.00, "AA1", "Boire")
      .check();

  }

  public void testRenameRecurrent() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -60, "Forfait Kro")
      .load();

    views.selectCategorization();
    categorization.selectTableRow(0);
    categorization.selectRecurring();
    categorization.createRecurringSeries()
      .setName("AA")
      .setCategory(MasterCategory.FOOD)
      .validate();
    views.selectCategorization();
    categorization.selectRecurring();
    categorization.checkContainsButtonInReccuring("AA");
    views.selectBudget();
    budgetView.recurring.editSeriesList().selectSeries("AA").setName("AA2").validate();
    views.selectCategorization();
    categorization.checkContainsButtonInReccuring("AA2");
  }

  public void testDeleteNewlyCreatedSeries() throws Exception {
    views.selectBudget();
    budgetView.income.createSeries()
      .setName("AA")
      .deleteSeries()
      .checkSeriesListEquals();
  }

  public void testDeleteUsedSeries() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -60, "Forfait Kro")
      .load();

    views.selectCategorization();
    categorization.selectTableRow(0);
    categorization.selectEnvelopes();
    categorization.createEnvelopeSeries()
      .setName("AA")
      .setCategory(MasterCategory.FOOD)
      .validate();
    categorization.setEnvelope("Forfait Kro", "AA", MasterCategory.FOOD, false);
    views.selectBudget();
    SeriesEditionDialogChecker edition = budgetView.envelopes
      .editSeriesList();
    edition
      .selectSeries("AA")
      .deleteSeriesWithConfirmation()
      .checkMessage()
      .validate();
    edition.checkSeriesListEquals();
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
    TextBox amount = edition.selectAllMonths()
      .getAmount();
    JTextField textAmount = (JTextField)amount.getAwtComponent();
    textAmount.select(0, textAmount.getText().length());
    amount.pressKey(Key.DELETE).pressKey(Key.d1).pressKey(Key.d3);
    edition.setCategory(MasterCategory.FOOD)
      .checkName("AA")
      .checkAmount("13")
      .checkSeriesListEquals("AA");
    edition.checkTable(new Object[][]{
      {"2008", "August", "", "13.00"},
      {"2008", "July", "", "13.00"},
      {"2008", "June", "", "13.00"}
    });
  }

  public void testAutomatique() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -20., "Auchan")
      .addTransaction("2008/07/04", -30., "Auchan")
      .load();

    views.selectCategorization();
    categorization.selectTableRows("Auchan");
    SeriesEditionDialogChecker edition =
      categorization.selectEnvelopes().createEnvelopeSeries();
    edition.switchToManual()
      .setName("Courant")
      .setCategory(MasterCategory.FOOD)
      .checkTable(new Object[][]{
        {"2008", "August", "", "0"},
        {"2008", "July", "", "0"},
        {"2008", "June", "", "0"}
      })
      .selectAllMonths()
      .setAmount("40")
      .validate();
    categorization.setEnvelope("Auchan", "Courant", MasterCategory.FOOD, false);
    categorization.selectEnvelopes().editSeries(false)
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
      });
  }

  public void testMonthsAreShowOrNot() throws Exception {
    views.selectBudget();
    SeriesEditionDialogChecker edition = budgetView.envelopes.createSeries();
    edition.setUnknown()
      .monthsAreHidden()
      .setSixMonths()
      .monthsAreVisible()
      .setCustom()
      .monthsAreVisible()
      .setUnknown()
      .monthsAreHidden();
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

    edition.setFourMonths()
      .checkMonthIsChecked(2, 6, 10)
      .toggleMonth(10)
      .checkMonthIsChecked(10, 2, 6)
      .toggleMonth(3)
      .checkMonthIsChecked(3, 7, 11);

  }

  public void testSwitchBetweenSeries() throws Exception {
    views.selectBudget();
    SeriesEditionDialogChecker edition = budgetView.envelopes.createSeries()
      .setName("S1");
    edition.setSixMonths()
      .toggleMonth(3)
      .checkMonthIsChecked(3, 9);

    edition.createSeries().setName("S2")
      .setFourMonths()
      .checkMonthIsChecked(1, 5, 9)
      .toggleMonth(2)
      .checkMonthIsChecked(2, 6, 10);

    edition.selectSeries("S1")
      .monthsAreVisible()
      .checkMonthIsChecked(3, 9);
    edition
      .selectSeries("S2")
      .monthsAreVisible()
      .checkMonthIsChecked(2, 6, 10);
  }

  public void testPeriodOrder() throws Exception {
    views.selectBudget();
    SeriesEditionDialogChecker edition =
      budgetView.envelopes.createSeries()
        .setName("S1")
        .checkPeriods("Every month", "Every two months", "Every three months", "Every four months", "Every six months",
                      "Once a year", "Custom", "Irregular");
  }

  public void testSeriesListVisibility() throws Exception {
    views.selectBudget();
    budgetView.envelopes.createSeries()
      .checkSeriesListIsHidden()
      .setName("")
      .setCategory(MasterCategory.FOOD)
      .validate();

    budgetView.envelopes.editSeriesList()
      .checkSeriesListIsVisible()
      .cancel();
  }

  public void testChangeCategoriesOnEnvelopeChangePlannedTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -20., "PointP")
      .load();

    views.selectCategorization();
    categorization.selectTableRows("PointP");

    categorization.selectEnvelopes().createEnvelopeSeries()
      .setName("Maison")
      .setCategory("Entretien")
      .validate();
    views.selectData();
    timeline.selectMonth("2008/07");
    transactions
      .initContent()
      .add("30/07/2008", TransactionType.PLANNED, "Planned: Maison", "", -20.00, "Maison", "Entretien")
      .check();

    views.selectBudget();
    budgetView.envelopes.editSeries("Maison")
      .addCategory("Furniture")
      .validate();

    views.selectData();
    transactions
      .initContent()
      .add("30/07/2008", TransactionType.PLANNED, "Planned: Maison", "", -20.00, "Maison", MasterCategory.HOUSE)
      .check();

    views.selectBudget();
    budgetView.envelopes.editSeries("Maison")
      .addCategory(getCategoryName(MasterCategory.LEISURES))
      .validate();

    views.selectData();
    transactions
      .initContent()
      .add("30/07/2008", TransactionType.PLANNED, "Planned: Maison", "", -20.00, "Maison", MasterCategory.NONE)
      .check();
  }

  public void testEnteringPositiveOrNegativeValuesInAnExpensesBudgetArea() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -20.0, "McDo")
      .load();

    views.selectCategorization();
    categorization.selectTableRows("McDo");
    SeriesEditionDialogChecker edition = categorization.selectEnvelopes().createEnvelopeSeries();

    edition.setName("Diet Food")
      .setCategory(MasterCategory.FOOD)
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
    categorization.selectTableRows("WorldCo");
    SeriesEditionDialogChecker edition = categorization.selectIncome().createIncomeSeries();

    edition.setName("Salary")
      .setCategory(MasterCategory.INCOME)
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
      .setCategory(MasterCategory.FOOD)
      .toggleMonth(3)
      .checkMonthIsChecked(3, 9)
      .validate();
    timeline.selectMonth("2008/08");
    budgetView.envelopes.editSeriesList().selectSeries("S1")
      .checkAmountIsDisable();
  }

  public void testAutomatiqueAndManual() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/28", -30., "EAU")
      .addTransaction("2008/08/27", -20., "EAU")
      .load();

    views.selectCategorization();
    timeline.selectMonth("2008/06");
    categorization.setEnvelope("EAU", "EAU", MasterCategory.HOUSE, true);

    views.selectCategorization();
    timeline.selectMonth("2008/08");
    categorization.setEnvelope("EAU", "EAU", MasterCategory.HOUSE, false);
    categorization.selectEnvelopes()
      .editSeries(false)
      .selectSeries("EAU")
      .setTwoMonths()
      .toggleMonth(6)
      .validate();
    views.selectData();
    timeline.selectMonths("2008/06", "2008/07", "2008/08");
    transactions.initContent()
      .add("28/08/2008", TransactionType.PLANNED, "Planned: EAU", "", -10.00, "EAU", MasterCategory.HOUSE)
      .add("27/08/2008", TransactionType.PRELEVEMENT, "EAU", "", -20.00, "EAU", MasterCategory.HOUSE)
      .add("28/06/2008", TransactionType.PRELEVEMENT, "EAU", "", -30.00, "EAU", MasterCategory.HOUSE)
      .check();
  }

}
