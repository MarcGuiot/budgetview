package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class TitleDisplayTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    super.setUp();
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
  }

  public void testViewNames() throws Exception {
    timeline.selectMonth("2008/10");

    views.selectHome();
    title.checkContent("Dashboard - october 2008");

    views.selectBudget();
    title.checkContent("Budget - october 2008");

    views.selectData();
    title.checkContent("Operations - october 2008");

    views.selectEvolution();
    title.checkContent("Evolution - october 2008");

    views.selectRepartition();
    title.checkContent("Repartition - october 2008");
  }

  public void testNoMonth() throws Exception {
    timeline.selectNone();
    title.checkContent("Select a period");
  }
}
