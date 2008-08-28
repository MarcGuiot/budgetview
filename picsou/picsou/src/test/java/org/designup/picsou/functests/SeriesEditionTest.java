package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

public class SeriesEditionTest extends LoggedInFunctionalTestCase {
  public void test() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Free Telecom")
      .load();

    timeline.selectMonth("2008/07");
    views.selectData();
    transactions.initContent()
      .add("29/07/2008", TransactionType.PRELEVEMENT, "Free Telecom", "", -29.00)
      .check();
    transactions.setRecurring("Free Telecom", "Internet", true);

    views.selectBudget();

    budgetView.recurring.editSeries("Internet")
      .checkName("Internet")
      .setName("Free")
      .checkTable(new Object[][]{
        {"2008", "July", "-29.00"},
        {"2008", "August", "-29.00"},
      })
//      .checkMonthSelected("Mar 2008")
      .validate();

    budgetView.recurring.checkSeries("Free", 29.00, 29.00);
  }
}
