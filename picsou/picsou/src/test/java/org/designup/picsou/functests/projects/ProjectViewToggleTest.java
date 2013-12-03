package org.designup.picsou.functests.projects;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class ProjectViewToggleTest extends LoggedInFunctionalTestCase {
  protected void setUp() throws Exception {
    setCurrentMonth("2013/12");
    super.setUp();
    operations.hideSignposts();
    operations.openPreferences().setFutureMonthsCount(12).validate();
  }

  public void testToggleProjectDetails() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2013/12/01", 1000.00, "Income")
      .load();

    projects.checkDetailsHidden();
    projectChart.checkShowsCreation();
    projectChart.checkShowDetailsButtonHidden();

    projectChart.create();
    projects.checkDetailsShown();
    currentProject.setName("MyProject")
      .addExpenseItem(0, "Item1", 201312, -100.00);
    projectChart.checkShowDetailsButtonHidden();

    projects.hideDetails();
    projects.checkDetailsHidden();
    projectChart.checkShowDetailsButtonShown();

    projectChart.select("MyProject");
    projects.checkDetailsShown();
    projectChart.checkShowDetailsButtonHidden();
  }

  public void testProjectDetailsAreHiddenWhenFirstProjectCreationIsCancelled() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2013/12/01", 1000.00, "Income")
      .load();

    projects.checkDetailsHidden();
    projectChart.checkShowDetailsButtonHidden();
    projectChart.checkShowsCreation();

    projectChart.create();
    projects.checkDetailsShown();
    projectChart.checkShowDetailsButtonHidden();

    currentProject.cancelNameEdition();
    projects.checkDetailsHidden();
    projectChart.checkShowDetailsButtonHidden();
    projectChart.checkShowsCreation();
  }

  public void testAdditionnalMonthsAreShownInSummaryGraphsWhenProjectDetailsAreHidden() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2013/01/01", 1000.00, "Income")
      .addTransaction("2013/12/01", 1000.00, "Income")
      .load();

    projects.checkDetailsHidden();
    summary.getMainChart().checkRange(201306, 201412);
    summary.getSavingsChart().checkRange(201306, 201412);

    timeline.selectMonth(201312);
    projectChart.create();
    projects.checkDetailsShown();
    currentProject.setName("MyProject");
    checkChartsRange(201310, 201407);

    projects.hideDetails();
    projects.checkDetailsHidden();
    checkChartsRange(201306, 201412);
  }

  private void checkChartsRange(int firstMonth, int lastMonth) {
    projectChart.getChart().checkRange(firstMonth, lastMonth);
    summary.getMainChart().checkRange(firstMonth, lastMonth);
    summary.getSavingsChart().checkRange(firstMonth, lastMonth);
  }
}
