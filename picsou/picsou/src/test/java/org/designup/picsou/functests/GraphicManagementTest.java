package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;

public class GraphicManagementTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    mainWindow.getToggleButton("evolutionCardToggle").click();
  }

  public void testDisplaysImportedData() throws Exception {
    OfxBuilder ofxBuilder = OfxBuilder.init(this);
    ofxBuilder
      .addTransaction("2006/03/20", -15, "sport")
      .addTransaction("2006/02/20", +12, "income")
      .addTransaction("2006/01/20", +5, "income")
      .addTransaction("2006/01/10", -10, "health")
      .load();

    graphics.initCheck()
      .setMonths(200602, 200602, 200603)
      .setIncome(5.0, 12.0, 0.0)
      .setExpenses(10.0, 0.0, 15.0)
      .check();

    ofxBuilder
      .addTransaction("2006/04/20", -20, "health")
      .addTransaction("2006/03/20", -1, "health")
      .load();

    graphics.initCheck()
      .setMonths(200602, 200602, 200603, 200604)
      .setIncome(5.0, 12.0, 0.0, 0.0)
      .setExpenses(10.0, 0.0, 16.0, 20.0)
      .check();
  }

  public void testExpensesLineNotShownIfAlwaysZero() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/02/20", +12, "income")
      .addTransaction("2006/01/20", +5, "income")
      .load();

    graphics.initCheck()
      .setMonths(200602, 200602)
      .setIncome(5.0, 12.0)
      .setNoExpenses()
      .check();
  }

  public void testIncomeLineNotShownIfAlwaysZero() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/02/20", -15, "sport")
      .addTransaction("2006/01/10", -10, "health")
      .load();

    graphics.initCheck()
      .setMonths(200602, 200602)
      .setNoIncome()
      .setExpenses(10.0, 15.0)
      .check();
  }

  public void testSelectingCategories() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/03/20", -15, "agf", MasterCategory.HEALTH)
      .addTransaction("2006/03/20", -50, "transports")
      .addTransaction("2006/02/20", +12, "income")
      .addTransaction("2006/01/20", +5, "income")
      .addTransaction("2006/01/10", -10, "agf", MasterCategory.HEALTH)
      .load();

    categories.select(MasterCategory.HEALTH);
    graphics.initCheck()
      .setMonths(200602, 200602, 200603)
      .setNoIncome()
      .setExpenses(10.0, 0.0, 15.0)
      .check();
  }

  public void testIgnoresInternalOperations() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/03/20", -15, "sport")
      .addTransaction("2006/01/20", +5, "internal", MasterCategory.INTERNAL)
      .addTransaction("2006/02/20", +12, "income")
      .addTransaction("2006/01/10", -10, "internal", MasterCategory.INTERNAL)
      .addTransaction("2006/01/5", -3, "misc")
      .load();

    graphics.initCheck()
      .setMonths(200602, 200602, 200603)
      .setIncome(0.0, 12.0, 0.0)
      .setExpenses(3.0, 0.0, 15.0)
      .check();
  }

  public void testMonthMarkers() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/10/15", -15, "tr1")
      .addTransaction("2006/03/20", -3, "tr2")
      .load();

    periods.selectCells(0);
    graphics.initMarker()
      .add(0.5, 1.5)
      .check();

    periods.selectCells(1);
    graphics.initMarker()
      .add(1.5, 2.5)
      .check();

    periods.selectCells(1, 3);
    graphics.initMarker()
      .add(1.5, 2.5)
      .add(3.5, 4.5)
      .check();

    periods.selectCells(1, 2, 3, 5);
    graphics.initMarker()
      .add(1.5, 4.5)
      .add(5.5, 6.5)
      .check();
  }
}
