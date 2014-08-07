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

  public void testProjectDetailsAreHiddenWhenFirstProjectCreationIsCancelled() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2013/12/01", 1000.00, "Income")
      .load();

    projects.checkHidden();
    projectChart.checkShowsCreation();

    projectChart.create();
    projects.checkShown();

    currentProject.cancelEdition();
    projects.checkHidden();
    projectChart.checkShowsCreation();
  }
}
