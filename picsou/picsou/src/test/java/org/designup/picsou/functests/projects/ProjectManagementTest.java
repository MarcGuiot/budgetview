package org.designup.picsou.functests.projects;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

public class ProjectManagementTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2010/12");
    setInitialGuidesShown(true);
    super.setUp();
  }

  public void testCreatingAProject() throws Exception {
    setCurrentMonth("2011/01");
    operations.changeDate();
    OfxBuilder.init(this)
      .addBankAccount("001111", 1900, "2011/01/01")
      .addTransaction("2011/01/01", 1000.00, "Income")
      .addTransaction("2011/01/01", -100.00, "Resa Travel Plus")
      .load();

    operations.openPreferences().setFutureMonthsCount(6).validate();
    operations.hideSignposts();

    projects.create();
    currentProject
      .checkProjectGaugeHidden()
      .setName("My project")
      .checkItemCount(0);
    currentProject.addExpenseItem()
      .editExpense(0)
      .setLabel("Reservation")
      .checkMonth("Jan 2011")
      .setAmount(-200.00)
      .validate();
    currentProject
      .checkProjectGauge(0.00, -200.00)
      .checkItemCount(1)
      .checkItem(0, "Reservation", "Jan", 0.00, -200.00);

    projectChart.checkProjectList("My project");
    projectChart.checkProject("My project", 201101, 201101, 200.00);
    currentProject.backToList();
    projects.checkCurrentProjects(
      "| My project | Jan | -200.00 | on |"
    );
    projects.checkNoPastProjects();

    timeline.selectMonth("2011/01");
    budgetView.extras.checkSeries("My project", 0, -200.00);
    budgetView.getSummary().checkEndPosition(1700.00);

    views.selectHome();
    projects.select("My project");
    currentProject
      .addExpenseItem(1, "Travel", 201102, -100.00)
      .checkItemCount(2)
      .checkProjectGauge(0.00, -300.00)
      .checkItem(1, "Travel", "Feb", 0.00, -100.00);

    currentProject
      .addExpenseItem(2, "Hotel", 201102, -500.00)
      .checkItemCount(3)
      .checkItems("Reservation | Jan | 0.00 | -200.00\n" +
                  "Travel | Feb | 0.00 | -100.00\n" +
                  "Hotel | Feb | 0.00 | -500.00")
      .checkProjectGauge(0.00, -800.00);

    projectChart.checkProjectList("My project");
    projectChart.checkProject("My project", 201101, 201102, 800.00);
    currentProject.backToList();
    projects.checkCurrentProjects(
      "| My project | Jan | -800.00 | on |"
    );
    budgetView.extras.checkSeries("My project", 0, -200.00);
    budgetView.getSummary().checkEndPosition(1700.00);

    timeline.selectMonth("2011/02");
    budgetView.extras.checkSeries("My project", 0, -600.00);
    budgetView.getSummary().checkEndPosition(1100.00);

    views.selectCategorization();
    categorization.selectTransaction("Resa Travel Plus");
    categorization.selectExtras().checkSeriesContainsSubSeries("My project",
                                                               "Reservation", "Travel", "Hotel");

    views.selectHome();
    projectChart.select("My project");
    currentProject
      .checkItems("Reservation | Jan | 0.00 | -200.00\n" +
                  "Travel | Feb | 0.00 | -100.00\n" +
                  "Hotel | Feb | 0.00 | -500.00")
      .deleteItem(1)
      .checkProjectGauge(0.00, -700.00);

    categorization.selectTransaction("Resa Travel Plus");
    categorization.selectExtras().checkSeriesContainsSubSeries("My project", "Reservation", "Hotel");
    categorization.selectExtras().checkSeriesDoesNotContainSubSeries("My project", "Travel");

    categorization.setExtra("Resa Travel Plus", "My project", "Reservation");

    timeline.selectMonth("2011/02");
    projectChart.checkProject("My project", 201101, 201102, 700.00);
    budgetView.extras.checkSeries("My project", 0, -500.00);
    budgetView.getSummary().checkEndPosition(1300.00);

    views.selectHome();
    projectChart.select("My project");
    currentProject
      .checkItems("Reservation | Jan | -100.00 | -200.00\n" +
                  "Hotel | Feb | 0.00 | -500.00")
      .toggleAndEditExpense(1)
      .setAmount(-50.00)
      .validate();
    currentProject
      .checkItems("Reservation | Jan | -100.00 | -200.00\n" +
                  "Hotel | Feb | 0.00 | -50.00")
      .deleteItem(1)
      .checkItems("Reservation | Jan | -100.00 | -200.00")
      .checkProjectGauge(-100.00, -200.00);

    timeline.selectMonth("2011/01");
    projectChart.checkProject("My project", 201101, 201101, 200.00);

    timeline.selectMonth("2011/02");
    budgetView.extras.checkSeriesNotPresent("My project");

    timeline.selectMonth("2011/01");
    budgetView.extras.checkSeries("My project", -100.00, -200.00);
    budgetView.getSummary().checkEndPosition(1800.00);

    transactions.initAmountContent()
      .add("01/01/2011", "RESA TRAVEL PLUS", -100.00, "My project / Reservation", 1900.00, 1900.00, "Account n. 001111")
      .add("01/01/2011", "INCOME", 1000.00, "To categorize", 2000.00, 2000.00, "Account n. 001111")
      .check();
  }

  public void testDeletingAProjectWithNoAssignedTransactions() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/01")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .load();

    operations.openPreferences().setFutureMonthsCount(6).validate();
    operations.hideSignposts();

    projects.create();
    currentProject
      .setName("My project")
      .addExpenseItem(0, "Reservation", 201101, -200.00)
      .addExpenseItem(1, "Hotel", 201101, -300.00)
      .backToList();
    projects.checkCurrentProjects("| My project | Jan | -500.00 | on |");

    timeline.selectMonth(201101);
    budgetView.extras.checkSeries("My project", 0.00, -500.00);
    budgetView.getSummary().checkEndPosition(500.00);

    views.selectHome();
    projectChart.select("My project");
    currentProject.delete();
    projects.checkListShown();
    projects.checkNoProjectShown();
    projectChart.checkNoProjectShown();
    budgetView.extras.checkNoSeriesShown();
    budgetView.getSummary().checkEndPosition(1000.00);

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

    projects.create();
    currentProject
      .setName("My project")
      .addExpenseItem(0, "Reservation", 201101, -200.00)
      .addExpenseItem(1, "Hotel", 201101, -500.00);

    OfxBuilder.init(this)
      .addBankAccount("001111", 800.00, "2011/01/10")
      .addTransaction("2011/01/01", -200.00, "Resa")
      .addTransaction("2011/01/01", -500.00, "Hotel")
      .addTransaction("2011/01/01", -10.00, "Something else")
      .load();

    categorization.setExtra("Resa", "My project");
    categorization.setExtra("Hotel", "My project");

    views.selectHome();
    projectChart.select("My project");
    currentProject.openDeleteAndNavigate();
    views.checkCategorizationSelected();
    categorization.checkTable(new Object[][]{
      {"01/01/2011", "My project", "HOTEL", -500.0},
      {"01/01/2011", "My project", "RESA", -200.0},
    });
    categorization.checkSelectedTableRows(0, 1);
    categorization.unselectAllTransactions();
    categorization.showAllTransactions();

    views.selectHome();
    projectChart.select("My project");
    currentProject
      .deleteWithConfirmation("Existing operations",
                              "Some operations have been assigned to this project");

    views.checkCategorizationSelected();
    categorization.checkSelectedTableRows("RESA", "HOTEL");
    categorization.checkTable(new Object[][]{
      {"01/01/2011", "", "HOTEL", -500.0},
      {"01/01/2011", "", "RESA", -200.0},
    });
    categorization.checkSelectedTableRows(0, 1);

    views.selectHome();
    projects.checkNoProjectShown();
    projectChart.checkNoProjectShown();
    budgetView.getSummary().checkEndPosition(290.00);
  }

  public void testCannotHaveEmptyProjectOrProjectItemNames() {

    operations.hideSignposts();

    projects.create();
    currentProject
      .checkProjectNameMessage("You must provide a name for this project")
      .setName("My project")
      .checkNoErrorTipDisplayed();

    currentProject
      .addExpenseItem()
      .editExpense(0)
      .setMonth(201101)
      .setAmount(-200.00)
      .validateAndCheckNameTip("You must provide a name for this item")
      .setLabel("Item 1")
      .checkNoTipShown()
      .validate();

    currentProject
      .checkNoErrorTipDisplayed();

    projectChart.checkProject("My project", 201101, 201101, 200.00);
  }

  public void testCancellingANewlyCreatedItemDeletesIt() throws Exception {
    operations.hideSignposts();

    projects.create();
    currentProject
      .setName("My project")
      .addExpenseItem()
      .editExpense(0)
      .cancel();
    currentProject.checkItemCount(0);
  }

  public void testRenamingAnItemRenamesTheCorrespondingSubSeries() throws Exception {
    operations.hideSignposts();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2011/01/01")
      .addTransaction("2011/01/01", 1000.00, "Income")
      .load();

    projects.create();
    currentProject
      .setName("My project")
      .addExpenseItem(0, "Item 1", 201101, 100.00)
      .addExpenseItem(1, "Item 2", 201101, 100.00);

    categorization.selectTransaction("Income");
    categorization.selectExtras().checkSeriesContainsSubSeries("My project", "Item 1", "Item 2");

    projectChart.select("My project");
    currentProject.toggleAndEditExpense(0)
      .setLabel("NewItem1")
      .validate();

    categorization.selectExtras().checkSeriesContainsSubSeries("My project", "NewItem1", "Item 2");
    categorization.selectExtras().checkSeriesDoesNotContainSubSeries("My project", "Item 1");
  }

  public void testGaugesShownForProjectItems() throws Exception {
    operations.hideSignposts();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2012/12/15")
      .addTransaction("2012/12/01", -100.00, "SNCF")
      .addTransaction("2012/12/10", -150.00, "Europcar")
      .addTransaction("2012/12/15", -550.00, "Sheraton")
      .load();

    projects.create();
    currentProject
      .setName("My project")
      .addExpenseItem(0, "Travel", 201212, -300.00)
      .addExpenseItem(1, "Accomodation", 201212, -500.00)
      .checkItems("Travel | Dec | 0.00 | -300.00\n" +
                  "Accomodation | Dec | 0.00 | -500.00")
      .checkItemGauge(0, 0.00, -300.00)
      .checkItemGauge(1, 0.00, -500.00)
      .checkProjectGauge(0.00, -800.00);

    categorization.selectTransaction("SNCF");
    categorization.selectExtras()
      .checkSeriesContainsSubSeries("My project", "Travel", "Accomodation");
    categorization.setExtra("SNCF", "My project", "Travel");
    categorization.setExtra("EUROPCAR", "My project", "Travel");
    categorization.setExtra("SHERATON", "My project", "Accomodation");

    views.selectHome();
    projectChart.select("My project");
    currentProject
      .checkItems("Travel | Dec | -250.00 | -300.00\n" +
                  "Accomodation | Dec | -550.00 | -500.00")
      .addExpenseItem(2, "Other", 201301, -100.00)
      .checkItems("Travel | Dec | -250.00 | -300.00\n" +
                  "Accomodation | Dec | -550.00 | -500.00\n" +
                  "Other | Jan | 0.00 | -100.00")
      .checkItemGauge(0, -250.00, -300.00)
      .checkItemGauge(1, -550.00, -500.00)
      .checkItemGauge(2, 0.00, -100.00)
      .checkProjectGauge(-800.00, -900.00);
  }

  public void testTransactionsAreAssignedToTheProjectWhenItemsAreDeleted() throws Exception {
    operations.hideSignposts();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2012/12/15")
      .addTransaction("2012/12/01", -100.00, "SNCF")
      .addTransaction("2012/12/10", -150.00, "Europcar")
      .addTransaction("2012/12/15", -550.00, "Sheraton")
      .load();

    projects.create();
    currentProject
      .setName("My project")
      .addExpenseItem(0, "Travel", 201212, -300.00)
      .addExpenseItem(1, "Accomodation", 201212, -500.00);

    categorization.selectTransaction("SNCF");
    categorization.selectExtras().checkSeriesContainsSubSeries("My project", "Travel", "Accomodation");
    categorization.setExtra("SNCF", "My project", "Travel");
    categorization.setExtra("EUROPCAR", "My project", "Travel");
    categorization.setExtra("SHERATON", "My project", "Accomodation");

    views.selectHome();
    projectChart.select("My project");
    currentProject
      .checkItems("Travel | Dec | -250.00 | -300.00\n" +
                  "Accomodation | Dec | -550.00 | -500.00")
      .deleteItem(0);

    categorization.selectTransaction("EUROPCAR").getExtras().checkSeriesIsSelected("My project");
    categorization.selectTransaction("SNCF").getExtras().checkSeriesIsSelected("My project");
  }

  public void testProjectViewIsAlwaysShownWhenEditingAssociatedSeries() throws Exception {

    operations.hideSignposts();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/01/10")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2011/01/05", 100.00, "Resa")
      .load();

    budgetView.extras.createProject();
    views.checkHomeSelected();
    currentProject
      .setName("Past Project")
      .addExpenseItem(0, "Reservation", 201101, -100.00)
      .addExpenseItem(1, "Hotel", 201102, -500.00)
      .backToList();

    views.selectBudget();
    budgetView.extras.editProjectSeries("Past Project");
    views.checkHomeSelected();
    projects.checkEditionShown();
    currentProject
      .checkName("Past Project")
      .checkItems("Reservation | Jan | 0.00 | -100.00\n" +
                  "Hotel | Feb | 0.00 | -500.00")
      .backToList();

    views.selectBudget();
    budgetView.extras.editPlannedAmountForProject("Past Project");
    views.checkHomeSelected();
    projects.checkEditionShown();
    currentProject
      .checkName("Past Project")
      .checkItems("Reservation | Jan | 0.00 | -100.00\n" +
                  "Hotel | Feb | 0.00 | -500.00")
      .backToList();

    timeline.selectAll();
    categorization.showAllTransactions();
    categorization.selectTransaction("Resa");

    categorization.selectExtras().selectSeries("Past Project");
    categorization.selectExtras().editProjectSeries("Past Project");
    views.checkHomeSelected();
    projects.checkEditionShown();
    currentProject
      .checkName("Past Project")
      .checkItems("Reservation | Jan | 0.00 | -100.00\n" +
                  "Hotel | Feb | 0.00 | -500.00");
  }

  public void testCanCreateProjectsFromTheCategorizationView() throws Exception {

    operations.hideSignposts();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/01/10")
      .addTransaction("2010/11/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2011/01/05", 100.00, "Resa")
      .load();

    categorization.selectTransaction("Resa");
    categorization.selectExtras().createProject();
    currentProject
      .setName("My project")
      .addExpenseItem(0, "Trip", 201101, -150.00);

    views.checkHomeSelected();
    projectChart.checkProjectList("My project");
    projectChart.select("My project");
    currentProject
      .checkItems("Trip | Jan | 0.00 | -150.00");
  }

  public void testManagesProjectsWithPositiveAmounts() throws Exception {
    operations.hideSignposts();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/01/10")
      .addTransaction("2010/11/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2011/01/05", 100.00, "Resa")
      .load();

    projects.create();
    currentProject
      .setName("My project")
      .addExpenseItem(0, "Bonus", 201101, 1000.00);

    projectChart.checkProjectList("My project");
    projectChart.select("My project");

    budgetView.extras.checkSeries("My project", 0, 1000.00);
  }

  public void testClickingOnTheBackgroundSelectsTheGivenMonth() throws Exception {
    operations.hideSignposts();

    operations.openPreferences().setFutureMonthsCount(4).validate();
    operations.hideSignposts();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/01")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .load();

    projects.create();
    currentProject
      .setName("My project")
      .addExpenseItem(0, "Reservation", 201012, -200.00);

    projectChart.selectMonth(201101);
    timeline.checkSelection("2011/01");
  }

  public void testEditingProjectTransactionsNavigatesToTheProject() throws Exception {
    operations.hideSignposts();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/01/10")
      .addTransaction("2010/11/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/11/15", -100.00, "Resa")
      .load();

    projects.create();
    currentProject
      .setName("Trip")
      .addExpenseItem(0, "Booking", 201011, -200.00);

    currentProject
      .create()
      .setName("Another project")
      .addExpenseItem(0, "Another item", 201011, -200.00);

    timeline.selectMonth("2010/11");
    categorization.setExtra("RESA", "Trip");

    views.selectData();
    transactions.initContent()
      .add("15/11/2010", TransactionType.PRELEVEMENT, "RESA", "", -100.00, "Trip")
      .add("01/11/2010", TransactionType.VIREMENT, "INCOME", "", 1000.00)
      .check();
    transactions.editProject(0);

    views.checkHomeSelected();
    currentProject
      .checkName("Trip");
  }

  public void testNavigatingToItemTransactions() throws Exception {
    operations.hideSignposts();
    operations.openPreferences().setFutureMonthsCount(3).validate();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/30")
      .addTransaction("2010/11/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/11/15", -100.00, "Resa 1")
      .addTransaction("2010/12/10", -100.00, "Resa 2")
      .addTransaction("2010/12/15", -250.00, "Sheraton")
      .load();

    projects.create();
    currentProject
      .setName("Trip")
      .addExpenseItem(0, "Booking", 201011, -250.00)
      .addExpenseItem(1, "Hotel", 201012, -200.00);

    categorization.setExtra("RESA 1", "Trip", "Booking");
    categorization.setExtra("RESA 2", "Trip", "Booking");
    categorization.setExtra("Sheraton", "Trip", "Hotel");

    views.selectHome();
    currentProject
      .checkItems("Booking | Nov | -200.00 | -250.00\n" +
                  "Hotel | Dec | -250.00 | -200.00");

    timeline.selectMonth("2011/01");

    currentProject.view(0).showTransactionsThroughActual();
    views.checkDataSelected();
    timeline.checkSelection("2010/11", "2010/12");
    transactions.initContent()
      .add("10/12/2010", TransactionType.PRELEVEMENT, "RESA 2", "", -100.00, "Trip / Booking")
      .add("15/11/2010", TransactionType.PRELEVEMENT, "RESA 1", "", -100.00, "Trip / Booking")
      .check();

    views.selectHome();
    currentProject.view(1).showTransactionsThroughActual();
    views.checkDataSelected();
    transactions.initContent()
      .add("15/12/2010", TransactionType.PRELEVEMENT, "SHERATON", "", -250.00, "Trip / Hotel")
      .check();

    timeline.selectMonth("2010/12");
    views.selectHome();
    currentProject.view(0).showTransactionsThroughMenu();
    views.checkDataSelected();
    timeline.checkSelection("2010/11", "2010/12");
    transactions.initContent()
      .add("10/12/2010", TransactionType.PRELEVEMENT, "RESA 2", "", -100.00, "Trip / Booking")
      .add("15/11/2010", TransactionType.PRELEVEMENT, "RESA 1", "", -100.00, "Trip / Booking")
      .check();
  }
}
