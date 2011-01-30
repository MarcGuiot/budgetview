package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

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
      .checkAmountLabel("Planned amount for july 2008")
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
      .setAmountAndValidate("150");
    budgetView.recurring.checkSeries("Internet", -29.00, -150.00);
    timeline.selectMonth("2008/08");
    budgetView.recurring.checkSeries("Internet", 0.00, -100.00);

    // Multi-selection without propagation
    timeline.selectMonths("2008/07", "2008/09");
    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmountIsEmpty()
      .checkPropagationDisabled()
      .setAmountAndValidate("200");
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
      .setAmountAndValidate("300");
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkSeries("Internet", -29.00, -300.00);
    timeline.selectMonth("2008/08");
    budgetView.recurring.checkSeries("Internet", 0.00, -300.00);
    timeline.selectMonth("2008/09");
    budgetView.recurring.checkSeries("Internet", 0.00, -300.00);
    timeline.selectMonth("2008/10");
    budgetView.recurring.checkSeries("Internet", 0.00, -300.00);
  }

  public void testAmountEditionDialogPeriodicityLabels() throws Exception {

    operations.openPreferences().setFutureMonthsCount(4).validate();

    OfxBuilder.init(this)
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Free Telecom")
      .load();

    timeline.selectMonth("2008/07");
    views.selectCategorization();
    categorization.setNewRecurring("Free Telecom", "Internet");

    views.selectBudget();

    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmountLabel("Planned amount for july 2008")
      .checkPeriodicity("Every month")
      .validate();

    budgetView.recurring.editSeries("Internet")
      .setTwoMonths()
      .validate();
    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmountLabel("Planned amount for july 2008")
      .checkPeriodicity("Every two months")
      .validate();

    budgetView.recurring.editSeries("Internet")
      .setTwoMonths()
      .setEndDate(200810)
      .validate();
    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmountLabel("Planned amount for july 2008")
      .checkPeriodicity("Every two months until october 2008")
      .validate();

    budgetView.recurring.editSeries("Internet")
      .setTwoMonths()
      .setStartDate(200807)
      .validate();
    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmountLabel("Planned amount for july 2008")
      .checkPeriodicity("Every two months from july to october 2008")
      .validate();

    budgetView.recurring.editSeries("Internet")
      .setEveryMonth()
      .setEndDate(200807)
      .validate();
    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmountLabel("Planned amount for july 2008")
      .checkPeriodicity("July 2008 only")
      .validate();

    budgetView.recurring.editSeries("Internet")
      .setIrregular()
      .clearEndDate()
      .validate();
    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmountLabel("Planned amount for july 2008")
      .checkPeriodicity("Irregular from july 2008")
      .validate();

    budgetView.recurring.editSeries("Internet")
      .setCustom()
      .setStartDate(200801)
      .setEndDate(200812)
      .validate();
    budgetView.recurring.editPlannedAmount("Internet")
      .checkPeriodicity("Custom from january to december 2008")
      .validate();

    budgetView.recurring.editSeries("Internet")
      .setCustom()
      .setStartDate(200801)
      .setEndDate(200912)
      .validate();
    budgetView.recurring.editPlannedAmount("Internet")
      .checkPeriodicity("Custom from 2008 to 2009")
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
      .checkAmountLabel("Planned amount for july 2008")
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
      .checkSeriesName("Internet")
      .checkNegativeAmountsSelected()
      .checkAmountLabel("Planned amount for july 2008")
      .setAmount("100")
      .checkPropagationDisabled()
      .setPropagationEnabled()
      .checkAmountLabel("Planned amount from july 2008")
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
      .checkSeriesName("Internet")      
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
      .checkSeriesName("Salary")
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

  public void testUsingTheSliderToSetThePlannedAmount() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder.init(this)
      .addTransaction("2008/07/29", +1500.00, "WorldCo")
      .addTransaction("2008/07/29", -29.00, "Free Telecom")
      .load();

    timeline.selectMonth("2008/07");
    views.selectCategorization();
    categorization.setNewRecurring("Free Telecom", "Internet");
    categorization.setNewIncome("WorldCo", "Salary");

    views.selectBudget();
    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmount(29.00)
      .checkSliderLabels("0", "25", "50", "75", "100", "125", "150", "175", "200")
      .checkSliderPosition(100 * 29 / 200)
      .setSliderPosition(100 * 40 / 200)
      .checkAmount(40.00)
      .checkNegativeAmountsSelected()
      .validate();
    budgetView.recurring.checkSeries("Internet", -29.00, -40.00);

    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmount(40.00)
      .checkNegativeAmountsSelected()
      .setAmount(200.00)
      .checkSliderLabels("0", "100", "200", "300", "400", "500")
      .checkSliderPosition(100 * 200 / 500)
      .setSliderPosition(100 * 30 / 500)
      .selectPositiveAmounts()
      .checkAmount(30.00)
      .validate();
    budgetView.recurring.checkSeries("Internet", -29.00, +30.00);

    budgetView.recurring.editPlannedAmount("Internet")
      .checkAmount(30.00)
      .selectNegativeAmounts()
      .checkSliderLabels("0", "25", "50", "75", "100", "125", "150", "175", "200")
      .checkSliderPosition(100 * 30 / 200)
      .checkAmount(30.00)
      .validate();
    budgetView.recurring.checkSeries("Internet", -29.00, -30.00);

    budgetView.income.editPlannedAmount("Salary")
      .checkAmount(1500)
      .checkPositiveAmountsSelected()
      .checkSliderLabels("0", "1000", "2000", "3000", "4000", "5000")
      .checkSliderPosition(100 * 1500 / 5000)
      .setSliderPosition(100 * 1700 / 5000)
      .checkAmount(1700.00)
      .checkPositiveAmountsSelected()
      .validate();
    budgetView.income.checkSeries("Salary", +1500, +1700);
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
      .checkAmountLabel("Planned amount for august 2010")
      .setPropagationEnabled()
      .checkSelectedMonths(201008, 201009, 201010)
      .checkAmountLabel("Planned amount from august 2010")
      .clickMonth(201009)
      .checkSelectedMonths(201009, 201010)
      .checkAmountLabel("Planned amount from september 2010")
      .setAmount(50.00)
      .setPropagationDisabled()
      .checkAmountLabel("Planned amount for september 2010")
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
      .checkChartColumn(0, "Aug", "2010", 40.00, 29.00, true)
      .checkChartColumn(1, "Sep", "2010", 50.00, 0.00)
      .checkChartColumn(2, "Oct", "2010", 50.00, 0.00)
      .setPropagationEnabled()
      .checkSelectedMonths(201008, 201009, 201010)
      .checkChartColumn(0, "Aug", "2010", 40.00, 29.00, true)
      .checkChartColumn(1, "Sep", "2010", 40.00, 0.00, true)
      .checkChartColumn(2, "Oct", "2010", 40.00, 0.00, true)
      .clickMonth(201009)
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
      .addTransaction("2010/08/03", -29.00, "Free")
      .load();

    views.selectCategorization();
    categorization.setNewRecurring("FREE", "Internet");
   
    budgetView.recurring.editPlannedAmount("Internet")
      .checkChartColumn(0, "Aug", "2010", 29.00, 29.00, true)
      .scroll(6)
      .checkChartColumn(0, "Oct", "2010", 29.00, 0.00, false)
      .validate();

  }
}
