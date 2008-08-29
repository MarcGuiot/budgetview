package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.model.MasterCategory;

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
      .checkMonthsSelected(1,3)
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
}
