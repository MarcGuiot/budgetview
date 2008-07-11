package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;

public class TitleDisplayTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    OfxBuilder
      .init(this)
      .addTransaction("2005/10/01", -10, "rent")
      .addTransaction("2006/04/20", +50, "income")
      .load();
  }

  public void testCategoryAll() throws Exception {
    categories.select(MasterCategory.ALL);
    periods.selectCells("2005/10");
    title.checkContent("All categories - october 2005");
  }

  public void testSimpleSelection() throws Exception {
    categories.select(MasterCategory.HOUSE);
    periods.selectCells("2006/01");
    title.checkContent("Category 'Housing' - january 2006");
  }

  public void testCategoryNone() throws Exception {
    categories.select(MasterCategory.NONE);
    periods.selectCells("2005/10");
    title.checkContent("Operations to categorize - october 2005");
  }

  public void testNoCategory() throws Exception {
    categories.selectNone();
    title.checkContent("Select a category");
  }

  public void testNoMonth() throws Exception {
    periods.selectNone();
    title.checkContent("Select a period");
  }
}
