package org.designup.picsou.functests.general;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class TitleDisplayTest extends LoggedInFunctionalTestCase {

  public void testViewNames() throws Exception {

    screen.checkContent("Operations", "Initialization");
    views.selectHome();
    screen.checkContent("Dashboard", "Initialization");
    views.selectBudget();
    screen.checkContent("Budget", "Initialization");
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
    screen.checkContent("Dashboard", "<html><b>August</b> 2008 - <b>February</b> 2009</html>");

    timeline.selectMonth("2008/10");
    views.selectBudget();
    screen.checkContent("Budget", "<html><b>October</b> 2008</html>");

    views.selectData();
    screen.checkContent("Operations", "<html><b>October</b> 2008</html>");

    views.selectBudget();
    screen.checkContent("Budget", "<html><b>October</b> 2008</html>");
  }

  public void testSeveralMonths() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2009/02/10", -10, "rent")
      .addTransaction("2008/08/10", -10, "rent")
      .load();

    views.selectData();
    timeline.selectMonths("2008/08", "2008/09", "2008/10");
    screen.checkContent("Operations", "<html><b>August</b> - <b>October</b> 2008</html>");
  }

  public void testNoMonth() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2009/02/10", -10, "rent")
      .load();

    timeline.selectNone();
    screen.checkContent("Operations", "Select a period");
  }
}
