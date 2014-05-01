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

    projects.checkProjectsHidden();
    projectChart.checkShowsCreation();
    projectChart.checkShowDetailsButtonHidden();

    projectChart.create();
    projects.checkProjectsShown();
    currentProject.setNameAndValidate("MyProject")
      .addExpenseItem(0, "Item1", 201312, -100.00);
    projectChart.checkShowDetailsButtonHidden();

    projects.hideProjects();
    projects.checkProjectsHidden();
    projectChart.checkShowDetailsButtonShown();

    projectChart.select("MyProject");
    projects.checkProjectsShown();
    projectChart.checkShowDetailsButtonHidden();
  }

  public void testProjectDetailsAreHiddenWhenFirstProjectCreationIsCancelled() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2013/12/01", 1000.00, "Income")
      .load();

    projects.checkProjectsHidden();
    projectChart.checkShowDetailsButtonHidden();
    projectChart.checkShowsCreation();

    projectChart.create();
    projects.checkProjectsShown();
    projectChart.checkShowDetailsButtonHidden();

    currentProject.cancelEdition();
    projects.checkProjectsHidden();
    projectChart.checkShowDetailsButtonHidden();
    projectChart.checkShowsCreation();
  }

  public void testAdditionnalMonthsAreShownInSummaryGraphsWhenProjectDetailsAreHidden() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2013/01/01", 1000.00, "Income")
      .addTransaction("2013/12/01", 1000.00, "Income")
      .load();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00002234", 2800.00, "2013/12/10")
      .addTransaction("2013/12/10", 3000.00, "WorldCo")
      .load();

    mainAccounts.edit("Account n. 00002234").setAsSavings().validate();

    projects.checkProjectsHidden();
    summary.getAccountChart("Account n. 00001123").checkRange(201306, 201412);
    summary.getAccountChart("Account n. 00002234").checkRange(201306, 201412);

    timeline.selectMonth(201312);
    projectChart.create();
    projects.checkProjectsShown();
    currentProject.setNameAndValidate("MyProject");
    checkChartsRange(201310, 201407);

    projects.hideProjects();
    projects.checkProjectsHidden();
    checkChartsRange(201306, 201412);
  }

  private void checkChartsRange(int firstMonth, int lastMonth) {
    projectChart.getChart().checkRange(firstMonth, lastMonth);
    summary.getAccountChart("Account n. 00001123").checkRange(firstMonth, lastMonth);
    summary.getAccountChart("Account n. 00002234").checkRange(firstMonth, lastMonth);
  }
}
