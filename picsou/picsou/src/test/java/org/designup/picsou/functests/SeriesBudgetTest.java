package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;

public class SeriesBudgetTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentDate("2009/04/03");
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
      .setTwoMonths()
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

//  public void testSerieBudgetWithShift() throws Exception {
//    OfxBuilder.init(this)
//      .addTransaction("2009/01/01", 1000.00, "Salaire")
//      .addTransaction("2009/01/01", -1000.00, "Loyer")
//      .addTransaction("2009/02/02", 1000.00, "Salaire")
//      .addTransaction("2009/02/02", -1000.00, "Loyer")
//      .addTransaction("2009/03/01", 1000.00, "Salaire")
//      .addTransaction("2009/03/01", -1000.00, "Loyer")
//      .addTransaction("2009/04/06", 1000.00, "Salaire")
//      .addTransaction("2009/04/06", -1000.00, "Loyer")
//      .load();
//    views.selectCategorization();
//    categorization.setNewIncome("Salaire", "Salaire");
//    categorization.setNewEnvelope("Loyer", "Loyer");
//    String path = operations.backup(this);
//    System.out.println("SeriesBudgetTest.testSerieBudgetWithShift " + path);
//    openApplication();
//  }
}
