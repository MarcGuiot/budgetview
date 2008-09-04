package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.SeriesEditionDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public class SeriesEditionTest extends LoggedInFunctionalTestCase {
  public void testStandardEdition() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Free Telecom")
      .load();

    timeline.selectMonth("2008/07");
    views.selectData();
    transactions.initContent()
      .add("29/07/2008", TransactionType.PRELEVEMENT, "Free Telecom", "", -29.00)
      .check();
    transactions.setRecurring("Free Telecom", "Internet", MasterCategory.TELECOMS, true);

    views.selectBudget();

    budgetView.recurring.editSeries("Internet")
      .checkName("Internet")
      .setName("Free")
      .checkTable(new Object[][]{
        {"2008", "July", "-29.00"},
        {"2008", "August", "-29.00"},
      })
      .checkMonthSelected(0)
      .validate();

    budgetView.recurring.checkSeries("Free", 29.00, 29.00);
  }

  public void testCurrentMonthsInitiallySelectedInBudgetTable() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/29", "2008/08/01", -29.00, "Free Telecom")
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Free Telecom")
      .addTransaction("2008/06/29", "2008/08/01", -29.00, "Free Telecom")
      .addTransaction("2008/05/29", "2008/08/01", -29.00, "Free Telecom")
      .load();

    timeline.selectAll();
    views.selectData();
    transactions.getTable().selectRowSpan(0, 3);
    transactions.setRecurring("Free Telecom", "Internet", MasterCategory.TELECOMS, true);

    views.selectBudget();
    timeline.selectMonths("2008/08", "2008/06");
    budgetView.recurring.editSeries("Internet")
      .checkName("Internet")
      .setName("Free")
      .checkTable(new Object[][]{
        {"2008", "May", "-29.00"},
        {"2008", "June", "-29.00"},
        {"2008", "July", "-29.00"},
        {"2008", "August", "-29.00"},
      })
      .checkMonthsSelected(1, 3)
      .validate();

    budgetView.recurring.checkSeries("Free", 58.00, 58.00);
  }

  public void testChangingTheAmountForAMonth() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Free Telecom")
      .load();

    timeline.selectMonth("2008/07");
    views.selectData();
    transactions.setRecurring("Free Telecom", "Internet", MasterCategory.TELECOMS, true);

    views.selectBudget();
    budgetView.recurring.editSeries("Internet")
      .checkTable(new Object[][]{
        {"2008", "July", "-29.00"},
        {"2008", "August", "-29.00"},
      })
      .checkMonthSelected(0)
      .checkAmount("-29.00")
      .setAmount("-40.00")
      .checkTable(new Object[][]{
        {"2008", "July", "-40.00"},
        {"2008", "August", "-29.00"},
      })
      .validate();

    budgetView.recurring.checkSeries("Internet", 29.00, 40.00);

    budgetView.recurring.editSeries("Internet")
      .checkTable(new Object[][]{
        {"2008", "July", "-40.00"},
        {"2008", "August", "-29.00"},
      })
      .checkMonthSelected(0)
      .checkAmount("-40.00")
      .setAmount("-30.00")
      .validate();

    budgetView.recurring.checkSeries("Internet", 29.00, 30.00);
  }

  public void testActivatingMonths() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/29", "2008/08/01", -29.00, "Free Telecom")
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Free Telecom")
      .addTransaction("2008/06/29", "2008/08/01", -29.00, "Free Telecom")
      .addTransaction("2008/05/29", "2008/08/01", -29.00, "Free Telecom")
      .load();

    timeline.selectAll();
    views.selectData();
    transactions.getTable().selectRowSpan(0, 3);
    transactions.setRecurring("Free Telecom", "Internet", MasterCategory.TELECOMS, true);

    views.selectBudget();
    timeline.selectMonths("2008/08", "2008/06");
    budgetView.recurring.editSeries("Internet")
      .checkTable(new Object[][]{
        {"2008", "May", "-29.00"},
        {"2008", "June", "-29.00"},
        {"2008", "July", "-29.00"},
        {"2008", "August", "-29.00"},
      })
      .toggleMonth("May")
      .checkTable(new Object[][]{
        {"2008", "June", "-29.00"},
        {"2008", "July", "-29.00"},
        {"2008", "August", "-29.00"},
      })
      .toggleMonth("Aug")
      .checkTable(new Object[][]{
        {"2008", "June", "-29.00"},
        {"2008", "July", "-29.00"},
      })
      .toggleMonth("Aug")
      .checkTable(new Object[][]{
        {"2008", "June", "-29.00"},
        {"2008", "July", "-29.00"},
        {"2008", "August", "-29.00"},
      })
      .validate();

    budgetView.recurring.editSeries("Internet")
      .checkTable(new Object[][]{
        {"2008", "June", "-29.00"},
        {"2008", "July", "-29.00"},
        {"2008", "August", "-29.00"},
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

    transactions.setEnvelope("Auchan", "Groceries", MasterCategory.FOOD, true);
    transactions.setEnvelope("Monoprix", "Groceries", MasterCategory.FOOD, false);
    transactions.setRecurring("Free Telecom", "Internet", MasterCategory.TELECOMS, true);
    transactions.setRecurring("EDF", "Electricity", MasterCategory.HOUSE, true);
    transactions.setExceptionalIncome("WorldCo - Bonus", "Exceptional Income", true);
    transactions.setIncome("WorldCo", "Salary", true);

    views.selectBudget();

    budgetView.recurring.editSeriesList()
      .checkSeriesList("Electricity", "Internet")
      .validate();

    budgetView.envelopes.editSeriesList()
      .checkSeriesList("Groceries")
      .validate();

    budgetView.income.editSeriesList()
      .checkSeriesList("Exceptional Income", "Salary")
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

    transactions.setRecurring("Free Telecom", "Internet", MasterCategory.TELECOMS, true);
    transactions.setRecurring("EDF", "Electricity", MasterCategory.HOUSE, true);

    views.selectBudget();
    budgetView.recurring.editSeriesList()
      .checkSeriesList("Electricity", "Internet")
      .checkSeriesSelected("Electricity")
      .selectAllMonths()
      .setAmount("-70")
      .toggleMonth("Aug")
      .checkTable(new Object[][]{
        {"2008", "July", "-70.00"},
      })

      .selectSeries("Internet")
      .checkMonthIsActive("Aug")
      .checkTable(new Object[][]{
        {"2008", "July", "-29.00"},
        {"2008", "August", "-29.00"},
      })
      .toggleMonth("Jul")

      .selectSeries("Electricity")
      .checkMonthIsActive("Jul")
      .checkMonthIsInactive("Aug")
      .checkTable(new Object[][]{
        {"2008", "July", "-70.00"},
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
      .checkSeriesList("New series")
      .checkSeriesSelected("New series")
      .setCategory(MasterCategory.TELECOMS)
      .checkName("New series")
      .setName("Free Telecom")
      .checkSeriesList("Free Telecom")
      .checkTable(new Object[][]{
        {"2008", "July", "0.00"},
        {"2008", "August", "0.00"}
      })
      .validate();

    budgetView.recurring.checkSeries("Free Telecom", 0, 0);
  }

  public void testSwitchingBetweenTwoNewSeriesWithTheSameName() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/15", -29.00, "Free Telecom")
      .addTransaction("2008/07/15", -55.00, "EDF")
      .load();

    views.selectBudget();
    budgetView.recurring
      .createSeries()
      .createSeries()
      .checkSeriesList("New series", "New series")

      .selectSeries(0)
      .selectAllMonths()
      .setAmount("-70")
      .toggleMonth("Aug")
      .checkTable(new Object[][]{
        {"2008", "July", "-70.00"},
      })

      .selectSeries(1)
      .checkMonthIsActive("Aug")
      .toggleMonth("Jul")
      .checkTable(new Object[][]{
        {"2008", "August", "0.00"},
      })

      .selectSeries(0)
      .checkMonthIsActive("Jul")
      .checkMonthIsInactive("Aug")
      .checkTable(new Object[][]{
        {"2008", "July", "-70.00"},
      })
      .cancel();
  }

  public void testCreateEnvelopeSeriesWithManyCategory() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .load();
    views.selectBudget();
    SeriesEditionDialogChecker edition = budgetView.envelopes
      .createSeries()
      .setName("courant")
      .setCategory(MasterCategory.CLOTHING, MasterCategory.FOOD);
    edition.openCategory()
      .checkSelected(MasterCategory.CLOTHING, MasterCategory.FOOD)
      .cancel();

    edition.selectAllMonths()
      .setAmount("-1000")
      .checkSingleCategorizeIsVisible(false)
      .checkMultiCategorizeIsVisible(true);

    edition.createSeries()
      .checkCategory()
      .setName("bank")
      .setCategory(MasterCategory.BANK)
      .validate();

    budgetView.envelopes.checkSeries("courant", 0, 1000);
    views.selectData();
    transactions.categorize(0)
      .disableAutoHide()
      .selectEnvelopes()
      .selectEnvelopeSeries("courant", MasterCategory.FOOD, false)
      .selectEnvelopeSeries("courant", MasterCategory.CLOTHING, false)
      .validate();
    transactionDetails.checkCategory(MasterCategory.CLOTHING);
    transactionDetails.checkSeries("courant");
  }

  public void testUnselectAllCategory() throws Exception {
    views.selectBudget();
    SeriesEditionDialogChecker edition = budgetView.envelopes.createSeries();
    edition
      .setName("courant")
      .setCategory(MasterCategory.CLOTHING, MasterCategory.FOOD)
      .unselectCategory(MasterCategory.FOOD, MasterCategory.CLOTHING)
      .checkCategory()
      .openCategory()
      .checkUnSelected(MasterCategory.FOOD, MasterCategory.CLOTHING)
      .validate();
    edition.checkCategory();
    edition
      .setCategory(MasterCategory.CLOTHING, MasterCategory.FOOD)
      .createSeries()
      .checkCategory()
      .cancel();
  }

  public void testIncomCategorization() throws Exception {
    views.selectBudget();
    budgetView.income
      .createSeries()
      .checkCategorizeEnable(true)
      .checkMultiCategorizeIsVisible(false)
      .checkOk(false)
      .setCategory(MasterCategory.INCOME)
      .checkOk(true)
      .validate();
  }

  public void testEmptySeriesListDisableCategorization() throws Exception {
    views.selectBudget();
    budgetView.envelopes
      .createSeries().unselect()
      .checkCategorizeEnable(false)
      .checkCategoryListEnable(true)
      .cancel();

    budgetView.income
      .createSeries().unselect()
      .checkCategorizeEnable(false)
      .checkCategorizeLabel()
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
      .checkSeriesList("New series")
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

  public void testDateAndBudgetSerie() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2007/02/10", -29.00, "Free Telecom")
      .load();
    views.selectBudget();
    SeriesEditionDialogChecker edition = budgetView.envelopes.createSeries();
    edition.toggleMonth("Jan", "Mar", "Jul", "Sep", "Nov");

    edition.setStartDate(200709)
      .checkTable(new Object[][]{
        {"2007", "October", "0.00"},
        {"2007", "December", "0.00"},
        {"2008", "February", "0.00"},
        {"2008", "April", "0.00"},
        {"2008", "May", "0.00"},
        {"2008", "June", "0.00"},
        {"2008", "August", "0.00"}
      });

    edition.setEndDate(200801)
      .checkTable(new Object[][]{
        {"2007", "October", "0.00"},
        {"2007", "December", "0.00"},
      });

    edition.setEndDate(200802)
      .checkTable(new Object[][]{
        {"2007", "October", "0.00"},
        {"2007", "December", "0.00"},
        {"2008", "February", "0.00"}
      });

    edition.setStartDate(200712)
      .checkTable(new Object[][]{
        {"2007", "December", "0.00"},
        {"2008", "February", "0.00"}
      });
  }
}
