package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

public class SeriesCreationTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2008/06");
    super.setUp();
  }

  public void testNewIncomeSeries() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", 1129.90, "WorldCo/june")
      .load();

    views.selectCategorization();
    categorization.selectTableRows(0);
    categorization.checkLabel("WORLDCO/JUNE");

    categorization.selectIncome().createSeries()
      .setName("Prime")
      .validate();

    categorization.selectTransaction("WORLDCO/JUNE");
    categorization.selectIncome()
      .checkContainsSeries("Prime")
      .selectSeries("Prime");

    views.selectData();
    transactions.checkSeries(0, "Prime");
    transactions.initContent()
      .add("30/06/2008", TransactionType.VIREMENT, "WORLDCO/JUNE", "", 1129.90, "Prime")
      .check();
  }

  public void testNewRecurringSeries() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -60, "Telefoot+")
      .load();

    views.selectCategorization();
    categorization.selectTableRows(0);
    categorization.checkLabel("TELEFOOT+");

    categorization.selectRecurring().createSeries()
      .setName("Culture")
      .validate();

    categorization.selectRecurring()
      .checkContainsSeries("Culture")
      .selectSeries("Culture");

    views.selectData();
    transactions.checkSeries(0, "Culture");
  }

  public void testNewEnvelopeSeries() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -60, "Forfait Kro")
      .load();

    views.selectCategorization();
    categorization.selectTableRows(0);
    categorization.checkLabel("FORFAIT KRO");

    categorization.selectEnvelopes().createSeries()
      .setName("Regime")
      .validate();
    categorization.selectEnvelopes()
      .checkContainsSeries("Regime")
      .selectSeries("Regime");

    views.selectData();
    transactions.checkSeries(0, "Regime");
  }

  public void testNewExtraSeries() throws Exception {

    views.selectBudget();
    budgetView.extras.createSeries()
      .setName("Machine a laver")
      .checkSingleMonthDate("June 2008")
      .checkTable(new Object[][]{{"2008", "June", "", "0"}})
      .cancel();

    OfxBuilder
      .init(this)
      .addTransaction("2008/08/30", -10, "mac")
      .load();

    timeline.selectMonths("2008/06", "2008/07");
    budgetView.extras.createSeries()
      .setName("Machine a laver")
      .setEveryMonth()
      .checkStartDate("June 2008")
      .checkEndDate("Jul 2008")
      .checkTable(new Object[][]{
        {"2008", "July", "", "0"},
        {"2008", "June", "", "0"}
      })
      .cancel();
  }
}
