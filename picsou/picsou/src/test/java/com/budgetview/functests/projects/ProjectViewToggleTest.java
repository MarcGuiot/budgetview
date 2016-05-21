package com.budgetview.functests.projects;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;

public class ProjectViewToggleTest extends LoggedInFunctionalTestCase {
  protected void setUp() throws Exception {
    setCurrentMonth("2013/12");
    super.setUp();
    operations.hideSignposts();
    operations.openPreferences().setFutureMonthsCount(12).validate();
    addOns.activateProjects();
  }

  public void testProjectDetailsAreHiddenWhenFirstProjectCreationIsCancelled() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2013/12/01", 1000.00, "Income")
      .load();

    projectList.checkShown();
    projects.checkShowsCreation();

    projects.createFirst();
    projects.checkShowsChart();
    projectList.checkShown();

    currentProject.cancelEdition();
    projectList.checkShown();
    projects.checkShowsCreation();
  }
}
