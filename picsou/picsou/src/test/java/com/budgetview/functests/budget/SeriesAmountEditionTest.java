package com.budgetview.functests.budget;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;

public class SeriesAmountEditionTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2010/08");
    super.setUp();
  }

  public void testEditingAPlannedSeriesAmount() throws Exception {

    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder.init(this)
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Free Telecom")
      .load();

    timeline.selectMonth("2008/07");
    views.selectCategorization();
    categorization.setNewRecurring("Free Telecom", "Internet");

    // First update with propagation + switching to manual mode
    views.selectBudget();
    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmountLabel("for july 2008")
      .checkNegativeAmountsSelected()
      .checkAmount(29.00)
      .checkAmountIsSelected()
      .setAmount("100")
      .checkPropagationDisabled()
      .setPropagationEnabled()
      .validate();
    budgetView.recurring.checkSeries("Internet", -29.00, -100.00);
    timeline.selectMonth("2008/08");
    budgetView.recurring.checkSeries("Internet", 0.00, -100.00);

    // Propagation disabled
    timeline.selectMonth("2008/07");
    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmount(100.00)
      .checkAmountIsSelected()
      .checkPropagationDisabled()
      .setAmount("150")
      .validate();
    budgetView.recurring.checkSeries("Internet", -29.00, -150.00);
    timeline.selectMonth("2008/08");
    budgetView.recurring.checkSeries("Internet", 0.00, -100.00);

    // Multi-selection without propagation
    timeline.selectMonths("2008/07", "2008/09");
    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmountIsEmpty()
      .checkPropagationDisabled()
      .setAmount("200")
      .validate();
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkSeries("Internet", -29.00, -200.00);
    timeline.selectMonth("2008/08");
    budgetView.recurring.checkSeries("Internet", 0.00, -100.00);
    timeline.selectMonth("2008/09");
    budgetView.recurring.checkSeries("Internet", 0.00, -200.00);
    timeline.selectMonth("2008/10");
    budgetView.recurring.checkSeries("Internet", 0.00, -100.00);

    // Multi-selection with propagation
    timeline.selectMonths("2008/07", "2008/09");
    budgetView.recurring.editPlannedAmount("Internet")
      .checkPropagationDisabled()
      .checkAmount(200.00)
      .setPropagationEnabled()
      .setAmount("300")
      .validate();
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkSeries("Internet", -29.00, -300.00);
    timeline.selectMonth("2008/08");
    budgetView.recurring.checkSeries("Internet", 0.00, -300.00);
    timeline.selectMonth("2008/09");
    budgetView.recurring.checkSeries("Internet", 0.00, -300.00);
    timeline.selectMonth("2008/10");
    budgetView.recurring.checkSeries("Internet", 0.00, -300.00);
  }

  public void testAmountEditionPeriodicityLabels() throws Exception {

    operations.openPreferences().setFutureMonthsCount(4).validate();

    OfxBuilder.init(this)
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Free Telecom")
      .load();

    timeline.selectMonth("2008/07");
    views.selectCategorization();
    categorization.setNewRecurring("Free Telecom", "Internet");

    views.selectBudget();

    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmountLabel("for july 2008")
      .checkRepeatsEveryMonth()
      .validate();

    budgetView.recurring.editSeries("Internet")
      .setRepeatEveryTwoMonths()
      .validate();
    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmountLabel("for july 2008")
      .checkRepeatsEveryTwoMonths()
      .validate();

    budgetView.recurring.editSeries("Internet")
      .setRepeatEveryTwoMonths()
      .setEndDate(200810)
      .validate();
    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmountLabel("for july 2008")
      .checkRepeatsEveryTwoMonths()
      .checkEndDate("october 2008")
      .validate();

    budgetView.recurring.editSeries("Internet")
      .setRepeatEveryTwoMonths()
      .setStartDate(200807)
      .validate();
    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmountLabel("for july 2008")
      .checkRepeatsEveryTwoMonths()
      .checkStartDate("july 2008")
      .checkEndDate("october 2008")
      .validate();

    budgetView.recurring.editSeries("Internet")
      .setRepeatEveryMonth()
      .setEndDate(200807)
      .validate();
    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmountLabel("for july 2008")
      .checkRepeatsEveryMonth()
      .checkStartDate("july 2008")
      .checkEndDate("july 2008")
      .validate();

    budgetView.recurring.editSeries("Internet")
      .setRepeatIrregular()
      .clearEndDate()
      .validate();
    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmountLabel("for july 2008")
      .checkRepeatsIrregularly()
      .checkStartDate("july 2008")
      .validate();

    budgetView.recurring.editSeries("Internet")
      .setRepeatCustom()
      .setStartDate(200801)
      .setEndDate(200812)
      .validate();
    budgetView.recurring.editPlannedAmount("Internet")
      .checkRepeatsWithCustomPattern()
      .checkStartDate("january 2008")
      .checkEndDate("december 2008")
      .validate();

    budgetView.recurring.editSeries("Internet")
      .setRepeatCustom()
      .setStartDate(200801)
      .setEndDate(200912)
      .validate();
    budgetView.recurring.editPlannedAmount("Internet")
      .checkRepeatsWithCustomPattern()
      .checkStartDate("january 2008")
      .checkEndDate("december 2009")
      .validate();
  }

  public void testEditingPlannedSeriesAmountsWithCancel() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/29", -29.00, "Free Telecom")
      .load();

    timeline.selectMonth("2008/07");
    views.selectCategorization();
    categorization.setNewRecurring("Free Telecom", "Internet");

    views.selectBudget();
    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmountLabel("for july 2008")
      .checkAmount(29.00)
      .checkAmountIsSelected()
      .setAmount("100")
      .checkPropagationDisabled()
      .cancel();
    budgetView.recurring.checkSeries("Internet", -29.00, -29.00);

    timeline.selectMonth("2008/07");
    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmount(29.00)
      .setAmount("100")
      .setPropagationEnabled()
      .validate();
    budgetView.recurring.checkSeries("Internet", -29.00, -100.00);

    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmount(100.00)
      .setAmount("200")
      .cancel();
    budgetView.recurring.checkSeries("Internet", -29.00, -100.00);
  }

  public void testAligningThePlannedSeriesAmountOnTheActualAmount() throws Exception {

    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder.init(this)
      .addTransaction("2008/07/29", +1500.00, "WorldCo")
      .addTransaction("2008/07/29", -29.00, "Free Telecom")
      .load();

    timeline.selectMonth("2008/07");
    views.selectCategorization();
    categorization.setNewRecurring("Free Telecom", "Internet");

    // First update with propagation + switching to manual mode
    views.selectBudget();
    budgetView.recurring.editPlannedAmount("Internet")
      .checkName("Internet")
      .checkNegativeAmountsSelected()
      .checkAmountLabel("for july 2008")
      .setAmount("100")
      .checkPropagationDisabled()
      .setPropagationEnabled()
      .checkAmountLabel("from july 2008")
      .validate();
    budgetView.recurring.checkSeries("Internet", -29.00, -100.00);
    timeline.selectMonth("2008/08");
    budgetView.recurring.checkSeries("Internet", 0.00, -100.00);

    timeline.selectMonth("2008/07");
    budgetView.recurring.editPlannedAmount("Internet")
      .checkNegativeAmountsSelected()
      .checkAmount(100.00)
      .checkActualAmount("29.00")
      .alignPlannedAndActual()
      .checkNegativeAmountsSelected()
      .checkAmount(29.00)
      .setPropagationEnabled()
      .validate();
    budgetView.recurring.checkSeries("Internet", -29.00, -29.00);
    timeline.selectMonth("2008/08");
    budgetView.recurring.checkSeries("Internet", 0.00, -29.00);

    timeline.selectMonths("2008/08");
    budgetView.recurring.editPlannedAmount("Internet")
      .checkName("Internet")
      .checkAmount(29.00)
      .alignPlannedAndActual()
      .setPropagationEnabled()
      .validate();
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkSeries("Internet", -29.00, -29.00);
    timeline.selectMonth("2008/08");
    budgetView.recurring.checkSeries("Internet", 0.00, 0.00);

    // Positive amounts

    timeline.selectMonth("2008/07");
    budgetView.income.createSeries("Salary");
    budgetView.income.editPlannedAmount("Salary")
      .checkPositiveAmountsSelected()
      .setAmount(1500)
      .validate();

    views.selectCategorization();
    categorization.setIncome("WorldCo", "Salary");

    views.selectBudget();
    budgetView.income.editPlannedAmount("Salary")
      .checkName("Salary")
      .checkPositiveAmountsSelected()
      .checkAmount(1500.00)
      .checkActualAmount("1500.00")
      .setAmount(1000.00)
      .alignPlannedAndActual()
      .checkPositiveAmountsSelected()
      .checkAmount(1500.00)
      .setPropagationEnabled()
      .validate();
    budgetView.income.checkSeries("Salary", 1500.00, 1500.00);
    timeline.selectMonth("2008/08");
    budgetView.income.checkSeries("Salary", 0.00, 1500.00);
  }

  public void testUsingTheChartToEditSeveralMonthsAtOnce() throws Exception {

    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder.init(this)
      .addTransaction("2010/08/03", -29.00, "Free")
      .load();

    views.selectCategorization();
    categorization.setNewRecurring("FREE", "Internet");

    timeline.selectMonth("2010/08");
    views.selectBudget();
    budgetView.recurring.editPlannedAmount("Internet")
      .checkSelectedMonths(201008)
      .checkAmountLabel("for august 2010")
      .setPropagationEnabled()
      .checkSelectedMonths(201008, 201009, 201010)
      .checkAmountLabel("from august 2010")
      .selectMonth(201009)
      .checkSelectedMonths(201009, 201010)
      .checkAmountLabel("from september 2010")
      .setAmount(50.00)
      .setPropagationDisabled()
      .checkAmountLabel("for september 2010")
      .validate();

    timeline.selectMonth("2010/08");
    budgetView.recurring.checkSeries("Internet", -29.00, -29.00);

    timeline.selectMonth("2010/09");
    budgetView.recurring.checkSeries("Internet", 0, -50.00);

    timeline.selectMonth("2010/10");
    budgetView.recurring.checkSeries("Internet", 0, -50.00);

    timeline.selectMonth("2010/08");
    budgetView.recurring.editPlannedAmount("Internet")
      .checkPropagationDisabled()
      .checkSelectedMonths(201008)
      .setAmount(40.00)
      .validate();

    timeline.selectMonth("2010/08");
    budgetView.recurring.checkSeries("Internet", -29.00, -40.00);

    timeline.selectMonth("2010/09");
    budgetView.recurring.checkSeries("Internet", 0, -50.00);

    timeline.selectMonth("2010/10");
    budgetView.recurring.checkSeries("Internet", 0, -50.00);

    timeline.selectMonth("2010/08");
    budgetView.recurring.editPlannedAmount("Internet")
      .checkPropagationDisabled()
      .checkSelectedMonths(201008)
      .checkChartColumn(0, "A", "2010", 40.00, 29.00, true)
      .checkChartColumn(1, "S", "2010", 50.00, 0.00)
      .checkChartColumn(2, "O", "2010", 50.00, 0.00)
      .setPropagationEnabled()
      .checkSelectedMonths(201008, 201009, 201010)
      .checkChartColumn(0, "A", "2010", 40.00, 29.00, true)
      .checkChartColumn(1, "S", "2010", 40.00, 0.00, true)
      .checkChartColumn(2, "O", "2010", 40.00, 0.00, true)
      .selectMonth(201009)
      .checkSelectedMonths(201009, 201010)
      .setPropagationDisabled()
      .checkSelectedMonths(201009)
      .setAmount(60.00)
      .validate();

    timeline.selectMonth("2010/08");
    budgetView.recurring.checkSeries("Internet", -29.00, -40.00);

    timeline.selectMonth("2010/09");
    budgetView.recurring.checkSeries("Internet", 0, -60.00);

    timeline.selectMonth("2010/10");
    budgetView.recurring.checkSeries("Internet", 0, -40.00);
  }

  public void testMouseWeel() throws Exception {
    operations.openPreferences().setFutureMonthsCount(24).validate();

    OfxBuilder.init(this)
      .addTransaction("2010/06/03", -29.00, "Free")
      .addTransaction("2010/07/03", -29.00, "Free")
      .addTransaction("2010/08/03", -29.00, "Free")
      .load();

    categorization.setNewRecurring("FREE", "Internet");

    budgetView.recurring.editPlannedAmount("Internet")
      .checkChartColumn(2, "A", "2010", 29.00, 29.00, true)
      .checkChartRange(201006, 201108)
      .scroll(1)
      .checkChartColumn(1, "A", "2010", 29.00, 29.00, true)
      .checkChartRange(201007, 201109)
      .scroll(20)
      .checkChartRange(201008, 201110)
      .scroll(-2)
      .checkChartRange(201006, 201108)
      .validate();
  }
}
