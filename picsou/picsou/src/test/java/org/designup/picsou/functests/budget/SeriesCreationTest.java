package org.designup.picsou.functests.budget;

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
      .addTransaction("2008/05/30", 1129.90, "WorldCo")
      .addTransaction("2008/06/30", 1129.90, "WorldCo")
      .load();

    views.selectCategorization();
    categorization.selectTableRows(0, 1);
    categorization.checkLabel("WORLDCO [2 operations]");

    categorization.selectIncome().createSeries()
      .setName("Prime")
      .checkEditableTargetAccount("Main accounts")
      .checkChart(new Object[][]{
        {"2008", "May", 0.00, 0.00},
        {"2008", "June", 0.00, 0.00, true}
      })
      .validate();

    categorization.selectTransaction("WORLDCO");
    categorization.selectIncome()
      .checkContainsSeries("Prime")
      .selectSeries("Prime");

    views.selectData();
    transactions.checkSeries(0, "Prime");
    transactions.initContent()
      .add("30/06/2008", TransactionType.VIREMENT, "WORLDCO", "", 1129.90, "Prime")
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
      .checkEditableTargetAccount("Main accounts")
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

    categorization.selectVariable().createSeries()
      .setName("Regime")
      .checkEditableTargetAccount("Main accounts")
      .validate();
    categorization.selectVariable()
      .checkContainsSeries("Regime")
      .selectSeries("Regime");

    views.selectData();
    transactions.checkSeries(0, "Regime");
  }

  public void testNewExtraSeries() throws Exception {

    views.selectBudget();
    budgetView.extras.createSeries()
      .setName("Machine a laver")
      .checkEditableTargetAccount("Main accounts")
      .checkSelectedProfile("Irregular")
      .checkMonthsSelected(200806)
      .checkChart(new Object[][]{
        {"2008", "June", 0.00, 0.00, true}
      })
      .cancel();

    OfxBuilder
      .init(this)
      .addTransaction("2008/08/15", -200, "Darty")
      .load();

    timeline.selectMonths("2008/06", "2008/07");
    categorization.selectTransaction("Darty");

    categorization.selectExtras().createSeries()
      .setName("Machine a laver")
      .checkSelectedProfile("Irregular")
      .checkMonthsSelected(200806, 200807)
      .checkChart(new Object[][]{
        {"2008", "July", 0.00, 0.00, true},
        {"2008", "June", 0.00, 0.00, true},
        {"2008", "Aug", 200.00, 200.00}
      })
      .cancel();
  }

  public void testCannotUseEmptySeriesNames() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/05/30", 1129.90, "WorldCo")
      .addTransaction("2008/06/30", 1129.90, "WorldCo")
      .load();

    views.selectCategorization();
    categorization.selectTableRows(0, 1);

    categorization.selectIncome().createSeries()
      .setName("")
      .validateAndCheckNameError("You must enter a name")
      .setName("Prime")
      .checkNoTipShown()
      .validate();

    categorization.selectTransaction("WORLDCO");
    categorization.selectIncome()
      .checkContainsSeries("Prime");
  }
}
