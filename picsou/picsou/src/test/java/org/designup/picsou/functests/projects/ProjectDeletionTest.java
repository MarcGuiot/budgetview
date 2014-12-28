package org.designup.picsou.functests.projects;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

public class ProjectDeletionTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2010/12");
    setInitialGuidesShown(true);
    super.setUp();
    addOns.activateProjects();
  }

  public void testDeletingAProjectWithNoAssignedTransactions() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/01")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .load();

    operations.openPreferences().setFutureMonthsCount(6).validate();
    operations.hideSignposts();

    projects.createFirst();
    currentProject.setNameAndValidate("My project");
    currentProject
      .addExpenseItem(0, "Reservation", 201101, -200.00)
      .addExpenseItem(1, "Hotel", 201101, -300.00)
      .backToList();
    projectList.checkCurrentProjects("| My project | Jan | 500.00 | on |");

    timeline.selectMonth(201101);
    budgetView.extras.checkSeries("My project", 0.00, -500.00);
    mainAccounts.checkEndOfMonthPosition("Account n. 001111", 500.00);

    views.selectHome();
    projects.select("My project");
    currentProject.delete();
    projectList.checkListPageShown();
    projectList.checkNoProjectShown();
    projects.checkNoProjectShown();
    budgetView.extras.checkNoSeriesShown();
    mainAccounts.checkEndOfMonthPosition("Account n. 001111", 1000.00);

    categorization.selectTransaction("Income");
    categorization.selectExtras()
      .checkDoesNotContainSeries("My project");
  }

  public void testDeletingAProjectWithAssignedTransactions() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/01")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .load();

    operations.openPreferences().setFutureMonthsCount(6).validate();
    operations.hideSignposts();

    projects.createFirst();
    currentProject
      .setNameAndValidate("My project")
      .addExpenseItem(0, "Reservation", 201101, -200.00)
      .addExpenseItem(1, "Hotel", 201101, -500.00);

    OfxBuilder.init(this)
      .addBankAccount("001111", 800.00, "2011/01/10")
      .addTransaction("2011/01/01", -200.00, "Resa")
      .addTransaction("2011/01/01", -500.00, "MegaHotel")
      .addTransaction("2011/01/01", -10.00, "Something else")
      .load();

    categorization.setExtra("Resa", "Reservation");
    categorization.setExtra("MegaHotel", "Hotel");

    views.selectHome();
    projects.select("My project");
    currentProject.openDeleteAndNavigate();
    views.checkCategorizationSelected();
    categorization.checkTable(new Object[][]{
      {"01/01/2011", "Hotel", "MEGAHOTEL", -500.0},
      {"01/01/2011", "Reservation", "RESA", -200.0},
    });
    categorization.checkSelectedTableRows(0, 1);
    categorization.unselectAllTransactions();
    categorization.showAllTransactions();

    views.selectHome();
    projects.select("My project");
    currentProject.deleteWithConfirmation("Existing operations",
                                          "Some operations have been assigned to this project");

    views.checkCategorizationSelected();
    categorization.checkSelectedTableRows("RESA", "MEGAHOTEL");
    categorization.checkTable(new Object[][]{
      {"01/01/2011", "", "MEGAHOTEL", -500.0},
      {"01/01/2011", "", "RESA", -200.0},
    });
    categorization.checkSelectedTableRows(0, 1);

    views.selectHome();
    projectList.checkNoProjectShown();
    projects.checkNoProjectShown();
    mainAccounts.checkEndOfMonthPosition("Account n. 001111", 290.00);
  }

  public void testTransactionsAreUnassignedWhenItemsAreDeleted() throws Exception {
    operations.hideSignposts();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2012/12/15")
      .addTransaction("2012/12/01", -100.00, "SNCF")
      .addTransaction("2012/12/10", -150.00, "Europcar")
      .addTransaction("2012/12/15", -550.00, "Sheraton")
      .load();

    projects.createFirst();
    currentProject
      .setNameAndValidate("My project")
      .addExpenseItem(0, "Travel", 201212, -300.00)
      .addExpenseItem(1, "Accomodation", 201212, -500.00);

    categorization.selectTransaction("SNCF");
    categorization.selectExtras().checkGroupContainsSeries("My project", "Travel", "Accomodation");
    categorization.setExtra("SNCF", "Travel");
    categorization.setExtra("EUROPCAR", "Travel");
    categorization.setExtra("SHERATON", "Accomodation");

    views.selectHome();
    projects.select("My project");
    currentProject
      .checkItems("| Travel       | Dec | 250.00 | 300.00 |\n" +
                  "| Accomodation | Dec | 550.00 | 500.00 |")
      .deleteItem(0);

    categorization.selectTransaction("EUROPCAR").getExtras().checkNoSeriesSelected();
    categorization.selectTransaction("SHERATON").getExtras().checkSelectedSeries("Accomodation");
    categorization.selectTransaction("SNCF").getExtras().checkNoSeriesSelected();

    transactions.initContent()
      .add("15/12/2012", TransactionType.PRELEVEMENT, "SHERATON", "", -550.00, "Accomodation")
      .add("10/12/2012", TransactionType.PRELEVEMENT, "EUROPCAR", "", -150.00)
      .add("01/12/2012", TransactionType.PRELEVEMENT, "SNCF", "", -100.00)
      .check();
  }

}
