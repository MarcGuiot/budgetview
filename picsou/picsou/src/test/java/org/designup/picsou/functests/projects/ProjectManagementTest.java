package org.designup.picsou.functests.projects;

import org.designup.picsou.functests.checkers.AccountEditionChecker;
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

    projectChart.create();
    currentProject
      .setDefaultAccount("Account n. 001111")
      .checkProjectGaugeHidden()
      .checkProjectButtonsHidden()
      .setNameAndValidate("My project")
      .checkProjectButtonsShown()
      .checkItemCount(0);
    currentProject.addExpenseItem()
      .editExpense(0)
      .checkTargetAccountCombo("Account n. 001111")
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
      "| My project | Jan | 200.00 | on |"
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
      .checkItems("| Reservation | Jan | 0.00 | 200.00 |\n" +
                  "| Travel      | Feb | 0.00 | 100.00 |\n" +
                  "| Hotel       | Feb | 0.00 | 500.00 |")
      .checkProjectGauge(0.00, -800.00);

    projectChart.checkProjectList("My project");
    projectChart.checkProject("My project", 201101, 201102, 800.00);
    currentProject.backToList();
    projects.checkCurrentProjects(
      "| My project | Jan | 800.00 | on |"
    );
    budgetView.extras.checkSeries("My project", 0, -200.00);
    budgetView.getSummary().checkEndPosition(1700.00);
    budgetView.extras.checkContent("| My project | 0.00 | 200.00 |\n");
    budgetView.extras.expandGroup("My project");
    budgetView.extras.checkContent("| My project  | 0.00 | 200.00 |\n" +
                                   "| Reservation | 0.00 | 200.00 |\n");

    timeline.selectMonth("2011/02");
    budgetView.extras.checkSeries("My project", 0, -600.00);
    budgetView.getSummary().checkEndPosition(1100.00);

    views.selectCategorization();
    categorization.selectTransaction("Resa Travel Plus");
    categorization.selectExtras().checkContainsSeries("Reservation", "Travel", "Hotel");

    views.selectHome();
    projectChart.select("My project");
    currentProject
      .checkItems("| Reservation | Jan | 0.00 | 200.00 |\n" +
                  "| Travel      | Feb | 0.00 | 100.00 |\n" +
                  "| Hotel       | Feb | 0.00 | 500.00 |")
      .deleteItem(1)
      .checkProjectGauge(0.00, -700.00);

    categorization.selectTransaction("Resa Travel Plus");
    categorization.selectExtras().checkGroupContainsSeries("My project", "Reservation", "Hotel");
    categorization.selectExtras().checkGroupDoesNotContainSeries("My project", "Travel");

    categorization.setExtra("Resa Travel Plus", "Reservation");

    timeline.selectMonth("2011/02");
    projectChart.checkProject("My project", 201101, 201102, 700.00);
    budgetView.extras.checkSeries("My project", 0, -500.00);
    budgetView.getSummary().checkEndPosition(1300.00);

    views.selectHome();
    projectChart.select("My project");
    currentProject
      .checkItems("| Reservation | Jan | 100.00 | 200.00 |\n" +
                  "| Hotel       | Feb | 0.00   | 500.00 |")
      .toggleAndEditExpense(1)
      .setAmount(-50.00)
      .validate();
    currentProject
      .checkItems("| Reservation | Jan | 100.00 | 200.00 |\n" +
                  "| Hotel       | Feb | 0.00   | 50.00  |")
      .deleteItem(1)
      .checkItems("| Reservation | Jan | 100.00 | 200.00 |")
      .checkProjectGauge(-100.00, -200.00);

    timeline.selectMonth("2011/01");
    projectChart.checkProject("My project", 201101, 201101, 200.00);

    timeline.selectMonth("2011/02");
    budgetView.extras.checkSeriesNotPresent("My project");

    timeline.selectMonth("2011/01");
    budgetView.extras.checkSeries("My project", -100.00, -200.00);
    budgetView.getSummary().checkEndPosition(1800.00);

    transactions.initAmountContent()
      .add("01/01/2011", "RESA TRAVEL PLUS", -100.00, "Reservation", 1900.00, 1900.00, "Account n. 001111")
      .add("01/01/2011", "INCOME", 1000.00, "To categorize", 2000.00, 2000.00, "Account n. 001111")
      .check();
  }

  public void testCannotHaveEmptyProjectOrProjectItemNames() {

    operations.hideSignposts();

    mainAccounts.createMainAccount("Main account", 1000.00);

    projectChart.create();
    currentProject
      .checkProjectNameMessage("You must provide a name for this project")
      .setNameAndValidate("My project")
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

  public void testCancellingANewlyCreatedProjectDeletesIt() throws Exception {
    operations.hideSignposts();

    mainAccounts.createMainAccount("Main account", 1000.00);

    projectChart.create();
    currentProject
      .cancelEdition();
    projects.checkProjectsHidden();

    operations.undo();
    projects.checkProjectsShown();
    projects.checkEditionShown();
    currentProject
      .checkNameEditionInProgress("")
      .backToList();

    projects
      .checkCreationPageShown();

    operations.undo();
    projects.checkEditionShown();
    currentProject
      .checkNameEditionInProgress("")
      .setNameAndValidate("New Project")
      .backToList();

    projects
      .checkListPageShown()
      .checkCurrentProjects("| New Project |  | 0.00 | on |");
  }

  public void testProjectWithItemsNotDeletedWhenCancellingEmptyName() throws Exception {
    operations.hideSignposts();

    mainAccounts.createMainAccount("Main account", 1000.00);

    projectChart.create();
    currentProject
      .setNameAndValidate("My project")
      .addExpenseItem(0, "Item 1", 201212, -300.00)
      .backToList();

    projects.checkCurrentProjects("| My project | Dec | 300.00 | on |");

    projects.select("My project");
    currentProject.edit()
      .clearName()
      .cancelEdition();

    currentProject
      .checkName("My project")
      .backToList();
    projects.checkCurrentProjects("| My project | Dec | 300.00 | on |");

    projects.select("My project");
    currentProject.edit()
      .clearName()
      .backToList();
    projects.checkCurrentProjects("| My project | Dec | 300.00 | on |");

    projects.select("My project");
    currentProject.checkNoEditionInProgress();
  }

  public void testCancellingANewlyCreatedItemDeletesIt() throws Exception {
    operations.hideSignposts();

    mainAccounts.createMainAccount("Main account", 1000.00);

    projectChart.create();
    currentProject
      .setNameAndValidate("My project")
      .addExpenseItem()
      .editExpense(0)
      .cancel();
    currentProject.checkItemCount(0);
  }

  public void testRenamingAnItemRenamesTheCorrespondingSeries() throws Exception {
    operations.hideSignposts();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2011/01/01")
      .addTransaction("2011/01/01", 1000.00, "Income")
      .load();

    projectChart.create();
    currentProject
      .setNameAndValidate("My project")
      .addExpenseItem(0, "Item 1", 201101, 100.00)
      .addExpenseItem(1, "Item 2", 201101, 100.00);

    categorization.selectTransaction("Income");
    categorization.selectExtras().checkGroupContainsSeries("My project", "Item 1", "Item 2");

    projectChart.select("My project");
    currentProject.toggleAndEditExpense(0)
      .setLabel("NewItem1")
      .validate();

    categorization.selectExtras().checkGroupContainsSeries("My project", "NewItem1", "Item 2");
    categorization.selectExtras().checkGroupDoesNotContainSeries("My project", "Item 1");
  }

  public void testGaugesShownForProjectItems() throws Exception {
    operations.hideSignposts();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2012/12/15")
      .addTransaction("2012/12/01", -100.00, "SNCF")
      .addTransaction("2012/12/10", -150.00, "Europcar")
      .addTransaction("2012/12/15", -550.00, "Sheraton")
      .load();

    projectChart.create();
    currentProject
      .setNameAndValidate("My project")
      .addExpenseItem(0, "Travel", 201212, -300.00)
      .addExpenseItem(1, "Accomodation", 201212, -500.00)
      .checkItems("| Travel       | Dec | 0.00 | 300.00 |\n" +
                  "| Accomodation | Dec | 0.00 | 500.00 |")
      .checkItemGauge(0, 0.00, -300.00)
      .checkItemGauge(1, 0.00, -500.00)
      .checkProjectGauge(0.00, -800.00);

    categorization.selectTransaction("SNCF");
    categorization.selectExtras()
      .checkGroupContainsSeries("My project", "Travel", "Accomodation");
    categorization.setExtra("SNCF", "Travel");
    categorization.setExtra("EUROPCAR", "Travel");
    categorization.setExtra("SHERATON", "Accomodation");

    views.selectHome();
    projectChart.select("My project");
    currentProject
      .checkItems("| Travel       | Dec | 250.00 | 300.00 |\n" +
                  "| Accomodation | Dec | 550.00 | 500.00 |")
      .addExpenseItem(2, "Other", 201301, -100.00)
      .checkItems("| Travel       | Dec | 250.00 | 300.00 |\n" +
                  "| Accomodation | Dec | 550.00 | 500.00 |\n" +
                  "| Other        | Jan | 0.00   | 100.00 |")
      .checkItemGauge(0, -250.00, -300.00)
      .checkItemGauge(1, -550.00, -500.00)
      .checkItemGauge(2, 0.00, -100.00)
      .checkProjectGauge(-800.00, -900.00);
  }

  public void testProjectViewIsAlwaysShownWhenEditingAssociatedSeriesOrGroup() throws Exception {

    operations.hideSignposts();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/01/10")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2011/01/05", -100.00, "Resa")
      .load();

    budgetView.extras.createProject();
    views.checkHomeSelected();
    currentProject
      .setNameAndValidate("My Project")
      .addExpenseItem(0, "Reservation", 201101, -100.00)
      .addExpenseItem(1, "Hotel", 201102, -500.00)
      .backToList();

    views.selectBudget();
    budgetView.extras.editProjectForGroup("My Project");
    views.checkHomeSelected();
    projects.checkEditionShown();
    currentProject
      .checkName("My Project")
      .checkItems("| Reservation | Jan | 0.00 | 100.00 |\n" +
                  "| Hotel       | Feb | 0.00 | 500.00 |")
      .backToList();

    views.selectBudget();
    budgetView.extras
      .expandGroup("My project")
      .editPlannedAmountForProject("Reservation");
    views.checkHomeSelected();
    projects.checkEditionShown();
    currentProject
      .checkName("My Project")
      .editExpense(0)
      .cancel();
    currentProject
      .checkItems("| Reservation | Jan | 0.00 | 100.00 |\n" +
                  "| Hotel       | Feb | 0.00 | 500.00 |")
      .backToList();

    timeline.selectAll();
    categorization.showAllTransactions();
    categorization.selectTransaction("Resa");

    categorization.selectExtras().selectSeries("Reservation");
    categorization.selectExtras().editProjectSeries("Reservation");
    views.checkHomeSelected();
    projects.checkEditionShown();
    currentProject
      .checkName("My Project")
      .cancelExpenseEdition(0)
      .checkItems("| Reservation | Jan | 100.00 | 100.00 |\n" +
                  "| Hotel       | Feb | 0.00   | 500.00 |");
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
      .setNameAndValidate("My project")
      .addExpenseItem(0, "Trip", 201101, -150.00);

    views.checkHomeSelected();
    projectChart.checkProjectList("My project");
    projectChart.select("My project");
    currentProject
      .checkItems("| Trip | Jan | 0.00 | 150.00 |");
  }

  public void testManagesProjectsWithPositiveAmounts() throws Exception {
    operations.hideSignposts();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/01/10")
      .addTransaction("2010/11/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2011/01/05", 100.00, "Resa")
      .load();

    projectChart.create();
    currentProject
      .setNameAndValidate("My project")
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

    projectChart.create();
    currentProject
      .setNameAndValidate("My project")
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

    projectChart.create();
    currentProject
      .setNameAndValidate("Trip")
      .addExpenseItem(0, "Booking", 201011, -200.00);

    currentProject
      .create()
      .setNameAndValidate("Another project")
      .addExpenseItem(0, "Another item", 201011, -200.00);

    timeline.selectMonth("2010/11");
    categorization.setExtra("RESA", "Booking");

    views.selectData();
    transactions.initContent()
      .add("15/11/2010", TransactionType.PRELEVEMENT, "RESA", "", -100.00, "Booking")
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

    projectChart.create();
    currentProject
      .setNameAndValidate("Trip")
      .addExpenseItem(0, "Booking", 201011, -250.00)
      .addExpenseItem(1, "Hotel", 201012, -200.00);

    categorization.setExtra("RESA 1", "Booking");
    categorization.setExtra("RESA 2", "Booking");
    categorization.setExtra("Sheraton", "Hotel");

    views.selectHome();
    currentProject
      .checkItems("| Booking | Nov | 200.00 | 250.00 |\n" +
                  "| Hotel   | Dec | 250.00 | 200.00 |");

    timeline.selectMonth("2011/01");

    currentProject.view(0).showTransactionsThroughActual();
    views.checkDataSelected();
    timeline.checkSelection("2010/11", "2010/12");
    transactions.initContent()
      .add("10/12/2010", TransactionType.PRELEVEMENT, "RESA 2", "", -100.00, "Booking")
      .add("15/11/2010", TransactionType.PRELEVEMENT, "RESA 1", "", -100.00, "Booking")
      .check();

    views.selectHome();
    currentProject.view(1).showTransactionsThroughActual();
    views.checkDataSelected();
    transactions.initContent()
      .add("15/12/2010", TransactionType.PRELEVEMENT, "SHERATON", "", -250.00, "Hotel")
      .check();

    timeline.selectMonth("2010/12");
    views.selectHome();
    currentProject.view(0).showTransactionsThroughMenu();
    views.checkDataSelected();
    timeline.checkSelection("2010/11", "2010/12");
    transactions.initContent()
      .add("10/12/2010", TransactionType.PRELEVEMENT, "RESA 2", "", -100.00, "Booking")
      .add("15/11/2010", TransactionType.PRELEVEMENT, "RESA 1", "", -100.00, "Booking")
      .check();
  }

  public void testItemsAreAddedWithTheLastMonthAsDefault() throws Exception {
    operations.hideSignposts();
    operations.openPreferences().setFutureMonthsCount(6).validate();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/30")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/12/10", -100.00, "Resa")
      .load();

    projectChart.create();
    currentProject
      .setNameAndValidate("My project")
      .addExpenseItem()
      .editExpense(0)
      .setLabel("Item 1")
      .checkMonth("Dec 2010")
      .setMonth(201101)
      .setAmount(-250.00)
      .validate();
    currentProject
      .addExpenseItem()
      .editExpense(1)
      .setLabel("Item 2")
      .checkMonth("Jan 2011")
      .setMonth(201102)
      .switchToSeveralMonths()
      .setMonthCount(3)
      .setAmount(-60.00)
      .validate();
    currentProject
      .addExpenseItem()
      .editExpense(2)
      .setLabel("Item 3")
      .checkMonth("Feb 2011")
      .setAmount(-150.00)
      .validate();

    currentProject.checkItems("| Item 1 | Jan | 0.00 | 250.00 |\n" +
                              "| Item 2 | Feb | 0.00 | 180.00 |\n" +
                              "| Item 3 | Feb | 0.00 | 150.00 |");
  }

  public void testURLsAreTruncatedAfter50Chars() throws Exception {
    operations.hideSignposts();
    operations.openPreferences().setFutureMonthsCount(6).validate();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/30")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/12/10", -100.00, "Resa")
      .load();

    projectChart.create();
    currentProject
      .setNameAndValidate("My project")
      .addExpenseItem()
      .editExpense(0)
      .setLabel("Item 1")
      .checkMonth("Dec 2010")
      .setAmount(-250.00)
      .setURL("123456789-123456789-123456789-123456789-123456789-123456789")
      .validate();

    currentProject.view(0)
      .checkURL("123456789-123456789-123456789-123456789-1234567...",
                "123456789-123456789-123456789-123456789-123456789-123456789");
  }

  public void testSortingProjetItems() throws Exception {
    operations.hideSignposts();
    operations.openPreferences().setFutureMonthsCount(6).validate();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/30")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/12/10", -100.00, "Resa")
      .load();

    projectChart.create();
    currentProject
      .setNameAndValidate("My project")
      .addExpenseItem(0, "First", 201102, -100.00)
      .addExpenseItem(1, "Second", 201101, -100.00)
      .addExpenseItem(2, "Third", 201102, -100.00)
      .addExpenseItem(3, "Fourth", 201012, -100.00)
      .addExpenseItem(4, "Fifth", 201012, -200.00);
    currentProject.checkItems("| First  | Feb | 0.00 | 100.00 |\n" +
                              "| Second | Jan | 0.00 | 100.00 |\n" +
                              "| Third  | Feb | 0.00 | 100.00 |\n" +
                              "| Fourth | Dec | 0.00 | 100.00 |\n" +
                              "| Fifth  | Dec | 0.00 | 200.00 |");

    currentProject.sortItems();
    currentProject.checkItems("| Fourth | Dec | 0.00 | 100.00 |\n" +
                              "| Fifth  | Dec | 0.00 | 200.00 |\n" +
                              "| Second | Jan | 0.00 | 100.00 |\n" +
                              "| First  | Feb | 0.00 | 100.00 |\n" +
                              "| Third  | Feb | 0.00 | 100.00 |");
  }

  public void testWithMultipleMainAccounts() throws Exception {
    operations.hideSignposts();
    operations.openPreferences().setFutureMonthsCount(6).validate();
    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/30")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/12/10", -100.00, "first 1111")
      .load();

    OfxBuilder.init(this)
      .addBankAccount("002222", 00.00, "2010/12/30")
      .addTransaction("2010/12/01", 500.00, "Income")
      .addTransaction("2010/12/10", -400.00, "second 2222")
      .load();
    projectChart.create();

    currentProject
      .setNameAndDefaultAccount("My project", "Account n. 002222")
      .addExpenseItem(0, "First 1111", 201012, -1000.00, "Account n. 001111")
      .addExpenseItem(1, "Second 2222", 201012, -100.00);
    currentProject
      .toggleAndEditExpense(0)
      .checkTargetAccountCombo("Account n. 001111");
    currentProject
      .toggleAndEditExpense(1)
      .checkTargetAccountCombo("Account n. 002222");

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("11/12/2010", "Planned: Second 2222", -100.00, "Second 2222", -100.00, -100.00, "Account n. 002222")
      .add("11/12/2010", "Planned: First 1111", -1000.00, "First 1111", 0.00, 0.00, "Account n. 001111")
      .add("10/12/2010", "SECOND 2222", -400.00, "To categorize", 0.00, 1000.00, "Account n. 002222")
      .add("10/12/2010", "FIRST 1111", -100.00, "To categorize", 1000.00, 1400.00, "Account n. 001111")
      .add("01/12/2010", "INCOME", 500.00, "To categorize", 400.00, 1500.00, "Account n. 002222")
      .add("01/12/2010", "INCOME", 1000.00, "To categorize", 1100.00, 1000.00, "Account n. 001111")
      .check();

    categorization.selectTransaction("FIRST 1111")
      .selectExtras()
      .checkGroupDoesNotContainSeries("My project", "Second 2222")
      .selectSeries("First 1111");

    currentProject
      .editExpense(0)
      .checkTargetAccountLabel("Account n. 001111");
    currentProject
      .editExpense(1)
      .checkTargetAccountCombo("Account n. 002222");

    categorization.selectTransaction("SECOND 2222")
      .selectExtras()
      .checkGroupDoesNotContainSeries("My project", "First 1111")
      .selectSeries("Second 2222");

    currentProject
      .editExpense(1)
      .checkTargetAccountLabel("Account n. 002222");

    currentProject.editExpense(0).validate();
    currentProject.editExpense(1).validate();

    transactions.initAmountContent()
      .add("11/12/2010", "Planned: First 1111", -900.00, "First 1111", 100.00, 100.00, "Account n. 001111")
      .add("10/12/2010", "SECOND 2222", -400.00, "Second 2222", 0.00, 1000.00, "Account n. 002222")
      .add("10/12/2010", "FIRST 1111", -100.00, "First 1111", 1000.00, 1400.00, "Account n. 001111")
      .add("01/12/2010", "INCOME", 500.00, "To categorize", 400.00, 1500.00, "Account n. 002222")
      .add("01/12/2010", "INCOME", 1000.00, "To categorize", 1100.00, 1000.00, "Account n. 001111")
      .check();

    currentProject
      .toggleAndEditExpense(0)
      .checkTargetAccountLabel("Account n. 001111")
      .cancel();
  }

  public void testCreatingAProjectWithNoAccounts() throws Exception {
    operations.hideSignposts();
    operations.openPreferences().setFutureMonthsCount(6).validate();
    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/30")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/12/10", -100.00, "first 1111")
      .load();

    OfxBuilder.init(this)
      .addBankAccount("002222", 00.00, "2010/12/30")
      .addTransaction("2010/12/01", 500.00, "Income")
      .addTransaction("2010/12/10", -400.00, "second 2222")
      .load();

    projectChart.create();
    currentProject.setNameAndValidate("My project");
    currentProject.checkDefaultAccountLabel("Account n. 001111");
    currentProject.addExpenseItem(0, "Item 1", 201012, -200.00);
    budgetView.extras.checkContent("| My project | 0.00 | 200.00 |\n");

    currentProject.edit()
      .checkDefaultAccountCombo("Account n. 001111")
      .setDefaultAccount("Account n. 002222")
      .validateProjectEdition();
    currentProject.toggleAndEditExpense(0).checkTargetAccountCombo("Account n. 001111").validate();

    budgetView.extras.checkContent("| My project | 0.00 | 200.00 |\n");
  }

  public void testAtLeastOneMainAccountMustExistToCreateProjectsOrItems() throws Exception {
    operations.hideSignposts();

    AccountEditionChecker accountEdition =
      AccountEditionChecker.open(projectChart.createAndOpenConfirmation()
                                   .checkMessageContains("In order to prepare projects, you must first create a main bank account")
                                   .getOkTrigger("Create an account"));

    accountEdition
      .checkIsMain()
      .setName("Main account")
      .selectBank("CIC")
      .setAccountNumber("000123")
      .setPosition(1000.00)
      .validate();

    projectChart.create();
    currentProject.setNameAndValidate("Trip");

    mainAccounts.openDelete("Main account").validate();

    currentProject.checkAddExpenseWithNoAccount()
      .checkIsMain()
      .setName("Main account")
      .selectBank("CIC")
      .setAccountNumber("000123")
      .setPosition(1000.00)
      .validate();

    mainAccounts.openDelete("Main account").validate();

    currentProject.checkAddTransferItemWithNoAccount()
      .checkIsMain()
      .setName("Main account")
      .selectBank("CIC")
      .setAccountNumber("000123")
      .setPosition(1000.00)
      .validate();

    mainAccounts.openDelete("Main account").validate();
  }

  public void testCannotAddOrRemoveSeriesFromProjectGroups() throws Exception {
    operations.hideSignposts();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2011/01/10")
      .addTransaction("2011/01/01", 1000.00, "Income")
      .addTransaction("2011/01/10", -100.00, "Resa")
      .load();

    budgetView.extras.createProject();
    views.checkHomeSelected();
    currentProject
      .setNameAndValidate("My Project")
      .addExpenseItem(0, "Reservation", 201101, -100.00)
      .addExpenseItem(1, "Hotel", 201101, -500.00)
      .backToList();

    views.selectBudget();
    budgetView.extras.createSeries()
      .setName("Eric's birthday")
      .setAmount(150.00)
      .validate();
    budgetView.extras.createSeries()
      .setName("Maries's birthday")
      .setAmount(150.00)
      .validate();
    budgetView.extras.expandGroup("My Project");
    budgetView.extras.checkContent(
      "| My Project        | 0.00 | 600.00 |\n" +
      "| Hotel             | 0.00 | 500.00 |\n" +
      "| Reservation       | 0.00 | 100.00 |\n" +
      "| Eric's birthday   | 0.00 | 150.00 |\n" +
      "| Maries's birthday | 0.00 | 150.00 |");

    // -- Project groups cannot be modified --

    budgetView.extras.checkSeriesActions("My Project",
                                         "Collapse group",
                                         "Edit project",
                                         "Show operations",
                                         "See in Analysis view");

    // -- Cannot remove series from a project group --

    budgetView.extras.checkSeriesActions("Hotel",
                                         "Edit project",
                                         "Show operations",
                                         "See in Analysis view");

    // -- Cannot add/remove series to/from a project group --

    budgetView.extras.checkAddToGroupOptions("Eric's birthday", "New group...");
    budgetView.extras.addToNewGroup("Eric's birthday", "Birthdays");
  }
}
