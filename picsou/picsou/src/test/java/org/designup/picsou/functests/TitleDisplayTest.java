package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class TitleDisplayTest extends LoggedInFunctionalTestCase {

  public void testViewNames() throws Exception {

    title.checkContent("Initialization");
    views.selectHome();
    title.checkContent("Initialization");
    views.selectBudget();
    title.checkContent("Initialization");
    views.selectHome();

    OfxBuilder
      .init(this)
      .addTransaction("2009/02/10", -10, "rent")
      .addTransaction("2009/01/10", -10, "rent")
      .addTransaction("2008/12/10", -10, "rent")
      .addTransaction("2008/11/10", -10, "rent")
      .addTransaction("2008/10/10", -10, "rent")
      .addTransaction("2008/09/10", -10, "rent")
      .addTransaction("2008/08/10", -10, "rent")
      .load();

    timeline.selectAll();
    timeline.checkSelection("2008/08", "2008/09", "2008/10", "2008/11", "2008/12", "2009/01", "2009/02");

    views.selectHome();
    title.checkContent("Dashboard august 2008 - february 2009");

    timeline.selectMonth("2008/10");

    views.selectBudget();
    title.checkContent("Budget october 2008");

    views.selectData();
    title.checkContent("Operations october 2008");

    views.selectBudget();
    title.checkContent("Budget october 2008");

  }

  public void testSeveralMonths() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2009/02/10", -10, "rent")
      .addTransaction("2008/08/10", -10, "rent")
      .load();

    views.selectData();
    timeline.selectMonths("2008/08", "2008/09", "2008/10");
    title.checkContent("Operations august - october 2008");
  }

  public void testNoMonth() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2009/02/10", -10, "rent")
      .load();

    timeline.selectNone();
    title.checkContent("Select a period");
  }
}
