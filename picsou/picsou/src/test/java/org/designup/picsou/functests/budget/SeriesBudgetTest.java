package org.designup.picsou.functests.budget;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class SeriesBudgetTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentDate("2009/04/09");
    super.setUp();
  }

  public void testInativeSeriesBudgetAreVisibleInThePastAndPresentIfObservedAmountIsNotZero() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2009/01/01", -29.00, "Free Telecom")
      .addTransaction("2009/02/02", -29.00, "Free Telecom")
      .addTransaction("2009/03/01", -29.00, "Free Telecom")
      .addTransaction("2009/04/06", -29.00, "Free Telecom")
      .load();

    views.selectCategorization();
    categorization.setNewRecurring("Free Telecom", "Free");
    views.selectBudget();
    budgetView.recurring.editSeries("Free")
      .setRepeatEveryTwoMonths()
      .validate();

    timeline.selectMonth("2009/01");
    budgetView.recurring.checkSeries("Free", -29, -29);

    timeline.selectMonth("2009/02");
    budgetView.recurring.checkSeries("Free", -29, -29);

    timeline.selectMonth("2009/03");
    budgetView.recurring.checkSeries("Free", -29, -29);

    timeline.selectMonth("2009/04");
    budgetView.recurring.checkSeries("Free", -29, -29);
  }

  public void testAddMonthWithDifferentValueInSerireBudget() throws Exception {

    views.selectBudget();
    budgetView.variable.createSeries().setName("Courses")
      .selectAllMonths().setAmount(100).validate();
    operations.openPreferences().setFutureMonthsCount(2).validate();
    timeline.checkSpanEquals("2009/04", "2009/06");
    timeline.selectMonth("2009/06");
    budgetView.variable.
      checkSeries("Courses", 0, -100);
    budgetView.variable
      .editSeries("Courses")
      .selectMonth(200906)
      .setAmount(200)
      .validate();

    operations.openPreferences().setFutureMonthsCount(3).validate();
    timeline.checkSpanEquals("2009/04", "2009/07");
    timeline.selectMonth("2009/06");
    budgetView.variable.
      checkSeries("Courses", 0, -200);
    timeline.selectMonth("2009/07");
    budgetView.variable.
      checkSeries("Courses", 0, -200);
  }
}
