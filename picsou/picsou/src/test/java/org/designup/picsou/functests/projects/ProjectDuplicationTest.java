package org.designup.picsou.functests.projects;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class ProjectDuplicationTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2010/12");
    setInitialGuidesShown(true);
    super.setUp();
    operations.hideSignposts();
  }

  public void testDuplicateSimpleProject() throws Exception {
    operations.openPreferences().setFutureMonthsCount(12).validate();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/15")
      .addTransaction("2010/10/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/12/15", -200.00, "FNAC")
      .load();

    projectChart.create();
    currentProject
      .setName("Camera")
      .addExpenseItem(0, "Camera Body", 201012, -80.00, 10)
      .addExpenseItem(1, "Lens", 201012, -200.00)
      .addExpenseItem(2, "Bag", 201101, -100.00)
      .checkItems("| Camera Body | Dec | 0.00 | 800.00 |\n" +
                  "| Lens        | Dec | 0.00 | 200.00 |\n" +
                  "| Bag         | Jan | 0.00 | 100.00 |");

    timeline.selectMonth(201012);
    categorization.setExtra("FNAC", "Camera", "Camera Body");
    budgetView.extras.checkSeries("Camera", -200.00, -280.00);

    views.selectHome();
    currentProject.openDuplicate()
      .setName("Other camera")
      .checkFirstMonth("December 2010")
      .setFirstMonth(201106)
      .validate();

    projectChart.checkProjectList("Camera", "Other camera");
    currentProject.checkName("Other camera")
      .checkItems("| Camera Body | June | 0.00 | 800.00 |\n" +
                  "| Lens        | June | 0.00 | 200.00 |\n" +
                  "| Bag         | Jul  | 0.00 | 100.00 |")
      .checkPeriod("June 2011 - March 2012")
      .checkProjectGauge(0.00, -1100.00);

    timeline.selectMonth(201101);
    budgetView.extras.checkSeries("Camera", 0.00, -180.00);
    timeline.selectMonth(201102);
    budgetView.extras.checkSeries("Camera", 0.00, -80.00);
    timeline.selectMonth(201103);
    budgetView.extras.checkSeries("Camera", 0.00, -80.00);
    timeline.selectMonth(201104);
    budgetView.extras.checkSeries("Camera", 0.00, -80.00);
    budgetView.extras.checkSeriesList("Camera");

    timeline.selectMonth(201106);
    budgetView.extras.checkSeriesList("Camera", "Other camera");
    budgetView.extras.checkSeries("Other camera", 0.00, -280.00);
    timeline.selectMonth(201107);
    budgetView.extras.checkSeries("Other camera", 0.00, -180.00);
    timeline.selectMonth(201108);
    budgetView.extras.checkSeries("Camera", 0.00, -80.00);
    timeline.selectMonth(201109);
    budgetView.extras.checkSeries("Camera", 0.00, -80.00);
  }

  public void testDuplicationErrorsAndCancel() throws Exception {

    // Cannot duplicate empty projects
    projectChart.create();
    currentProject.setName("Empty");
    currentProject.checkDuplicateDisabled();

    // Name is mandatory
    currentProject.addExpenseItem(0, "Item1", 201012, -100.00);
    currentProject.openDuplicate()
      .validateAndCheckNameError("You must provide a name for this project")
      .setName("Copy")
      .checkNoTipsShown()
      .setFirstMonth(201302)
      .validate();
    currentProject.backToList();
    projects.checkCurrentProjects("| Empty | Dec | 100.00 | on |\n" +
                                  "| Copy  | Feb | 100.00 | on |");

    // Cancel
    projectChart.duplicate("Empty")
      .setName("Other")
      .cancel();
    currentProject.backToList();
    projects.checkCurrentProjects("| Empty | Dec | 100.00 | on |\n" +
                                  "| Copy  | Feb | 100.00 | on |");
  }
}