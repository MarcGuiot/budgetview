package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
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
    categorization.checkLabel("WorldCo/june");
    categorization.selectIncome();

    categorization.createIncomeSeries()
      .setName("Prime")
      .checkCategory(MasterCategory.INCOME)
      .validate();

    categorization.checkContainsIncomeSeries("Prime");
    categorization.setIncome("WorldCo/june", "Prime", false);
    views.selectData();
    transactions.checkSeries(0, "Prime");
    transactions.checkCategory(0, MasterCategory.INCOME);
    transactions.initContent()
      .add("30/06/2008", TransactionType.VIREMENT, "WorldCo/june", "", 1129.90, "Prime", MasterCategory.INCOME)
      .check();
  }

  public void testNewRecurringSeries() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -60, "Telefoot+")
      .load();

    views.selectCategorization();
    categorization.selectTableRows(0);
    categorization.checkLabel("Telefoot+");

    categorization.selectRecurring();
    categorization.createRecurringSeries()
      .setName("Culture")
      .checkNoCategory()
      .setCategory(MasterCategory.EDUCATION)
      .validate();

    categorization.selectRecurringSeries("Culture", MasterCategory.EDUCATION, false);
    categorization.checkContainsRecurringSeries("Culture");

    views.selectData();
    transactions.checkSeries(0, "Culture");
    transactions.checkCategory(0, MasterCategory.EDUCATION);
  }

  public void testNewEnvelopeSeries() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -60, "Forfait Kro")
      .load();

    views.selectCategorization();
    categorization.selectTableRows(0);
    categorization.checkLabel("Forfait Kro");

    categorization.selectEnvelopes();

    categorization.createEnvelopeSeries()
      .setName("Regime")
      .checkNoCategory()
      .setCategory(MasterCategory.FOOD)
      .validate();
    categorization.selectEnvelopeSeries("Regime", MasterCategory.FOOD, false);
    categorization.checkContainsEnvelope("Regime", MasterCategory.FOOD);

    views.selectData();
    transactions.checkSeries(0, "Regime");
    transactions.checkCategory(0, MasterCategory.FOOD);
  }

  public void testNewSpecialSeries() throws Exception {

    views.selectBudget();
    budgetView.specials.createSeries()
      .setName("Machine a laver")
      .checkNoCategory()
      .setCategory(MasterCategory.HOUSE)
      .checkSingleMonthDate("June 2008")
      .checkTable(new Object[][]{{"2008", "June", "", "0"}})
      .cancel();

    OfxBuilder
      .init(this)
      .addTransaction("2008/08/30", -10, "mac")
      .load();

    timeline.selectMonths("2008/06", "2008/07");
    budgetView.specials.createSeries()
      .setName("Machine a laver")
      .setCategory(MasterCategory.HOUSE)
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
