package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;

public class SeriesEvolutionViewTest extends LoggedInFunctionalTestCase {

  public void testStandardDisplay() throws Exception {
    fail();
  }

  public void testColumnNamesAreUpdatedOnMonthSelections() throws Exception {
    fail();
  }

  public void testSeriesValue() throws Exception {
    fail();
  }

  public void testExpandCollapse() throws Exception {
    views.selectEvolution();
    seriesEvolution.checkRowLabels("All", "To categorize",
                                   "Income", "Recurring", "Envelopes", "Occasional", "Special", "Savings");
    fail();
  }

  public void testEditingASeries() throws Exception {
     fail();
  }
}
