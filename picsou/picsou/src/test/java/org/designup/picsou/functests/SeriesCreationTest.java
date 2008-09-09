package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;
import org.globsframework.utils.Dates;

public class SeriesCreationTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentDate(Dates.parseMonth("2008/06"));
    super.setUp();
  }

  public void testNewIncomeSeries() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -1129.90, "WorldCo/june")
      .load();

    views.selectCategorization();
    categorization.selectTableRows(0);
    categorization.checkLabel("WorldCo/june");
    categorization.selectIncome();

    categorization.createIncomeSeries()
      .setName("Prime")
      .setCategory(MasterCategory.INCOME)
      .validate();

    categorization.checkContainsIncomeSeries("Prime");

    views.selectData();
    transactions.getTable().selectRow(0);
    transactionDetails.checkSeries("Prime");
    transactionDetails.checkCategory(MasterCategory.INCOME);
    transactions.initContent()
      .add("30/06/2008", TransactionType.PRELEVEMENT, "WorldCo/june", "", -1129.90, "Prime")
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
      .setCategory(MasterCategory.EDUCATION)
      .validate();

    categorization.checkContainsRecurringSeries("Culture");

    views.selectData();
    transactions.getTable().selectRow(0);
    transactionDetails.checkSeries("Culture");
    transactionDetails.checkCategory(MasterCategory.EDUCATION);
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
      .setCategory(MasterCategory.FOOD)
      .validate();

    categorization.checkContainsEnvelope("Regime", MasterCategory.FOOD);

    views.selectData();
    transactions.getTable().selectRow(0);
    transactionDetails.checkSeries("Regime");
    transactionDetails.checkCategory(MasterCategory.FOOD);
  }

  // TODO CategorizationView
  public void testSeriesUnselectedAfterCategorization() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -60, "Forfait Kro")
      .addTransaction("2008/06/20", -60, "Forfait Kro")
      .addTransaction("2008/06/10", -60, "Forfait Kro")
      .addTransaction("2008/06/28", -150, "Palette Leffe")
      .load();

    views.selectCategorization();
    categorization.checkTable(new Object[][]{
      {"30/06/2008", "Forfait Kro", -60.00},
      {"20/06/2008", "Forfait Kro", -60.00},
      {"10/06/2008", "Forfait Kro", -60.00},
      {"28/06/2008", "Palette Leffe", -150.00},
    });
    categorization.checkSelectedTableRows(0, 1, 2);

    categorization.selectEnvelopes();
    categorization.createEnvelopeSeries()
      .setName("Regime")
      .setCategory(MasterCategory.FOOD)
      .validate();

    categorization.checkTable(new Object[][]{
      {"30/06/2008", "Forfait Kro", -60.00},
      {"20/06/2008", "Forfait Kro", -60.00},
      {"10/06/2008", "Forfait Kro", -60.00},
      {"28/06/2008", "Palette Leffe", -150.00},
    });
    categorization.checkSelectedTableRows(3);
    categorization.checkNoBudgetAreaSelected();
  }
}
