package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.CategorizationDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;
import org.globsframework.utils.Dates;

public class CategorizationTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentDate(Dates.parseMonth("2008/06"));
    super.setUp();
  }

  public void testStandardIncomeTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -1129.90, "WorldCo/june")
      .load();

    CategorizationDialogChecker dialog = transactions.categorize(0);
    dialog.checkLabel("WorldCo/june");
    dialog.selectIncome();
    dialog.selectIncomeSeries("Salary", true);
    dialog.validate();

    transactionDetails.checkSeries("Salary");
    transactionDetails.checkCategory(MasterCategory.INCOME);

    CategorizationDialogChecker reopenedDialog = transactions.categorize(0);
    reopenedDialog.checkIncomeSeriesIsSelected("Salary");
    reopenedDialog.createIncomeSeries()
      .setName("Exceptional Income")
      .setCategory(MasterCategory.INCOME)
      .setAmount("0.0")
      .validate();
    reopenedDialog.selectIncomeSeries("Exceptional Income", false);
    reopenedDialog.checkIncomeSeriesIsNotSelected("Salary");
  }

  public void testStandardRecurringTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -29.90, "Free Telecom")
      .load();

    CategorizationDialogChecker dialog = transactions.categorize(0);
    dialog.checkLabel("Free Telecom");
    dialog.selectRecurring();
    dialog.checkContainsRecurringSeries();
    dialog.selectRecurringSeries("Internet", MasterCategory.TELECOMS, true);
    dialog.validate();

    transactionDetails.checkSeries("Internet");
    transactionDetails.checkCategory(MasterCategory.TELECOMS);

    CategorizationDialogChecker reopenedDialog = transactions.categorize(0);
    reopenedDialog.checkRecurringSeriesIsSelected("Internet");
    reopenedDialog.selectRecurringSeries("Rental", MasterCategory.HOUSE, true);
    reopenedDialog.checkRecurringSeriesIsNotSelected("Internet");
    reopenedDialog.cancel();
  }

  public void testStandardEnvelopeTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -29.90, "AUCHAN C'EST BON")
      .load();

    CategorizationDialogChecker dialog = transactions.categorize(0);
    dialog.checkLabel("AUCHAN C'EST BON");
    dialog.selectEnvelopes();
    dialog.selectEnvelopeSeries("Groceries", MasterCategory.FOOD, true);
    dialog.validate();

    transactionDetails.checkSeries("Groceries");
    transactionDetails.checkCategory(MasterCategory.FOOD);

    //TODO avec l'ajout de plusieur category a une enveloppe
//    CategorizationDialogChecker reopenedDialog = transactions.categorize(0);
//    reopenedDialog.checkEnvelopeSeriesIsSelected("Groceries", MasterCategory.FOOD);
//    reopenedDialog.selectEnvelopeSeries("Groceries", MasterCategory.HOUSE, true);
//    reopenedDialog.checkEnvelopeSeriesIsSelected("Groceries", MasterCategory.HOUSE);
//    reopenedDialog.checkEnveloppeSeriesIsNotSelected("Groceries", MasterCategory.FOOD);
  }

  public void testStandardOccasionalTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -199.90, "Fouquet's")
      .load();

    categories.createSubCategory(MasterCategory.FOOD, "Saucisson");
    categories.select(MasterCategory.ALL);

    CategorizationDialogChecker dialog = transactions.categorize(0);
    dialog.checkLabel("Fouquet's");
    dialog.selectOccasional();
    dialog.checkContainsOccasional(MasterCategory.MULTIMEDIA,
                                   MasterCategory.CLOTHING,
                                   MasterCategory.BEAUTY,
                                   MasterCategory.EDUCATION);
    dialog.checkContainsOccasional(MasterCategory.FOOD, "Saucisson");
    dialog.selectOccasionalSeries(MasterCategory.FOOD);
    dialog.validate();

    transactionDetails.checkSeries("Occasional");
    transactionDetails.checkCategory(MasterCategory.FOOD);

    CategorizationDialogChecker reopenedDialog = transactions.categorize(0);
    reopenedDialog.checkOccasionalSeries(MasterCategory.FOOD);
    reopenedDialog.selectOccasionalSeries(MasterCategory.FOOD, "Saucisson");
    reopenedDialog.validate();

    transactionDetails.checkSeries("Occasional");
    transactionDetails.checkCategory("Saucisson");
  }

  public void testSeriesUnselectedAfterCategorization() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -60, "Forfait Kro")
      .addTransaction("2008/06/28", -50, "France Telecom")
      .load();

    timeline.selectAll();
    CategorizationDialogChecker dialog = transactions.categorize(0, 1);
    dialog.checkTable(new Object[][]{
      {"30/06/2008", "Forfait Kro", -60.00},
      {"28/06/2008", "France Telecom", -50.00},
    });
    dialog.selectTableRows(1);
    dialog.enableAutoHide();

    dialog.selectRecurring();
    dialog.selectRecurringSeries("Internet", MasterCategory.TELECOMS, true);
    dialog.checkTable(new Object[][]{
      {"30/06/2008", "Forfait Kro", -60.00},
    });
    dialog.checkNoTransactionSelected();
    dialog.checkNoBudgetAreaSelected();
    dialog.validate();
  }

  public void testSwitchingFromTransactions() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -29.90, "Free Telecom")
      .addTransaction("2008/06/15", -40, "Auchan")
      .load();

    CategorizationDialogChecker dialog = transactions.categorize(0, 1);
    dialog.checkTable(new Object[][]{
      {"15/06/2008", "Auchan", -40.00},
      {"30/06/2008", "Free Telecom", -29.90},
    });
    dialog.selectTableRows(0);
    dialog.checkLabel("Auchan");
    dialog.checkNoBudgetAreaSelected();
    dialog.selectEnvelopes();
    dialog.selectEnvelopeSeries("Groceries", MasterCategory.FOOD, true);

    dialog.selectTableRows(1);
    dialog.checkLabel("Free Telecom");
    dialog.checkNoBudgetAreaSelected();
    dialog.selectRecurring();
    dialog.selectRecurringSeries("Internet", MasterCategory.TELECOMS, true);

    dialog.selectTableRows(0);
    dialog.cancel();
  }

  public void testUnassignedTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -199.90, "LDLC")
      .load();

    CategorizationDialogChecker dialog = transactions.categorize(0);
    dialog.checkLabel("LDLC");

    dialog.checkNoBudgetAreaSelected();
    dialog.checkTextVisible("You must select the type first");
    dialog.cancel();
  }

  public void testCancel() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -29.90, "Free Telecom")
      .load();

    CategorizationDialogChecker dialog = transactions.categorize(0);
    dialog.checkLabel("Free Telecom");

    dialog.selectRecurring();
    dialog.checkContainsRecurringSeries();
    dialog.selectRecurringSeries("Internet", MasterCategory.TELECOMS, true);
    dialog.cancel();

    transactionDetails.checkNoSeries();
  }

  public void testMultiCategorizationFromTransactionTable() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -29.90, "Carouf")
      .addTransaction("2008/06/15", -40.00, "Auchan")
      .load();

    CategorizationDialogChecker dialog = transactions.categorize(0, 1);
    dialog.checkTable(new Object[][]{
      {"15/06/2008", "Auchan", -40.00},
      {"30/06/2008", "Carouf", -29.90},
    });
    dialog.checkSelectedTableRows(0, 1);
    dialog.selectEnvelopes();
    dialog.selectEnvelopeSeries("Groceries", MasterCategory.FOOD, true);
    dialog.validate();

    transactions.initContent()
      .add("30/06/2008", TransactionType.PLANNED, "Groceries", "", -0.0, MasterCategory.FOOD)
      .add("30/06/2008", TransactionType.PRELEVEMENT, "Carouf", "", -29.90, MasterCategory.FOOD)
      .add("15/06/2008", TransactionType.PRELEVEMENT, "Auchan", "", -40.00, MasterCategory.FOOD)
      .check();

    transactions.getTable().selectRow(0);
    transactionDetails.checkSeries("Groceries");
    transactions.getTable().selectRow(1);
    transactionDetails.checkSeries("Groceries");

    CategorizationDialogChecker reopenedDialog = transactions.categorize(1, 2);
    reopenedDialog.checkSelectedTableRows(0, 1);
    reopenedDialog.checkEnvelopeSeriesIsSelected("Groceries", MasterCategory.FOOD);
    reopenedDialog.cancel();
  }

  public void testMultiCategorizationFromErrorMessageBlock() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -29.90, "Carouf")
      .addTransaction("2008/06/15", -40.00, "Auchan")
      .load();

    views.selectHome();
    informationPanel.assertWarningIsDisplayed(2);

    CategorizationDialogChecker dialog = informationPanel.categorize();
    dialog.checkTable(new Object[][]{
      {"15/06/2008", "Auchan", -40.00},
      {"30/06/2008", "Carouf", -29.90},
    });
    dialog.checkSelectedTableRows(0);
    dialog.selectTableRows(0, 1);
    dialog.selectEnvelopes();
    dialog.selectEnvelopeSeries("Groceries", MasterCategory.FOOD, true);
    dialog.validate();

    views.selectData();
    transactions.initContent()
      .add("30/06/2008", TransactionType.PLANNED, "Groceries", "", -0.0, MasterCategory.FOOD)
      .add("30/06/2008", TransactionType.PRELEVEMENT, "Carouf", "", -29.90, MasterCategory.FOOD)
      .add("15/06/2008", TransactionType.PRELEVEMENT, "Auchan", "", -40.00, MasterCategory.FOOD)
      .check();

    transactions.getTable().selectRow(0);
    transactionDetails.checkSeries("Groceries");
    transactions.getTable().selectRow(1);
    transactionDetails.checkSeries("Groceries");

    views.selectHome();
    informationPanel.assertNoWarningIsDisplayed();
  }

  public void testSelectingRecurringSelectsBudgetArea() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/28", -29.90, "Free Telecom")
      .load();

    CategorizationDialogChecker dialog = transactions.categorize(0);
    dialog.selectRecurring();
    dialog.selectRecurringSeries("Internet", MasterCategory.TELECOMS, true);
    dialog.checkBudgetAreaIsSelected(BudgetArea.RECURRING_EXPENSES);
  }

  public void testEscClosesTheDialogAndCancelsChanges() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/28", -29.90, "Free Telecom")
      .load();

    CategorizationDialogChecker dialog = transactions.categorize(0);
    dialog.assertVisible(true);
    dialog.selectRecurring();
    dialog.selectRecurringSeries("Internet", MasterCategory.TELECOMS, true);

    dialog.pressEscapeKey();
    dialog.assertVisible(false);
    transactionDetails.checkNoSeries();
  }

  public void testNoSelection() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -29.90, "Carouf")
      .addTransaction("2008/06/15", -40.00, "Auchan")
      .load();

    views.selectHome();
    CategorizationDialogChecker dialog = informationPanel.categorize();
    dialog.checkTable(new Object[][]{
      {"15/06/2008", "Auchan", -40.00},
      {"30/06/2008", "Carouf", -29.90},
    });

    dialog.unselectAllTransactions();
    dialog.checkBudgetAreasAreDisabled();

    dialog.selectTableRows(0);
    dialog.checkBudgetAreasAreEnabled();
    dialog.cancel();
  }

  public void testAutomaticSelectionOfSimilarTransactions() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/26", -29.90, "Free Telecom 26/06")
      .addTransaction("2008/05/25", -29.90, "Free Telecom 25/05")
      .addTransaction("2008/04/24", -29.90, "Free Telecom 21/04")
      .addTransaction("2008/05/15", -90.0, "Auchan 1111")
      .addTransaction("2008/05/14", -80.0, "Auchan 2222")
      .load();

    CategorizationDialogChecker dialog = informationPanel.categorize();
    dialog.checkTable(new Object[][]{
      {"15/05/2008", "Auchan 1111", -90.00},
      {"14/05/2008", "Auchan 2222", -80.00},
      {"24/04/2008", "Free Telecom 21/04", -29.90},
      {"25/05/2008", "Free Telecom 25/05", -29.90},
      {"26/06/2008", "Free Telecom 26/06", -29.90}
    });

    dialog.checkAutoSelectionEnabled(true);
    dialog.checkLabel(2);

    dialog.checkSelectedTableRows(0, 1);
    dialog.selectTableRows(3);
    dialog.checkSelectedTableRows(2, 3, 4);

    dialog.disableAutoSelection();
    dialog.checkSelectedTableRows(2, 3, 4);
    dialog.selectTableRows(1);
    dialog.checkSelectedTableRows(1);
    dialog.selectTableRows(3);
    dialog.checkSelectedTableRows(3);

    dialog.enableAutoSelection();
    dialog.checkSelectedTableRows(2, 3, 4);
    dialog.selectRecurring();
    dialog.selectRecurringSeries("Internet", MasterCategory.TELECOMS, true);

    dialog.selectTableRows("Auchan 1111");
    dialog.checkSelectedTableRows(0, 1);
    dialog.selectEnvelopes();
    dialog.selectEnvelopeSeries("Groceries", MasterCategory.FOOD, true);

    dialog.validate();

    views.selectData();
    transactions.initContent()
      .add("26/06/2008", TransactionType.PRELEVEMENT, "Free Telecom 26/06", "", -29.90, "Internet")
      .add("15/06/2008", TransactionType.PLANNED, "Groceries", "", -170.00, "Groceries")
      .add("25/05/2008", TransactionType.PRELEVEMENT, "Free Telecom 25/05", "", -29.90, "Internet")
      .add("15/05/2008", TransactionType.PRELEVEMENT, "Auchan 1111", "", -90.00, "Groceries")
      .add("14/05/2008", TransactionType.PRELEVEMENT, "Auchan 2222", "", -80.00, "Groceries")
      .add("24/04/2008", TransactionType.PRELEVEMENT, "Free Telecom 21/04", "", -29.90, "Internet")
      .check();
  }

  public void testAutomaticSelectionExcludesChecks() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/26", -12.90, "Cheque 12345")
      .addTransaction("2008/05/25", -34.90, "Cheque 23456")
      .addTransaction("2008/04/24", -56.90, "Cheque 34556")
      .load();

    CategorizationDialogChecker dialog = informationPanel.categorize();
    dialog.checkAutoSelectionEnabled(true);
    dialog.checkSelectedTableRows(0);
    dialog.cancel();
  }

  public void testManualMultiSelectionOverridesTheAutomaticSelectionMechanism() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/26", -29.90, "Free Telecom 26/06")
      .addTransaction("2008/05/25", -29.90, "Free Telecom 25/05")
      .addTransaction("2008/04/24", -29.90, "Free Telecom 21/04")
      .load();

    CategorizationDialogChecker dialog = informationPanel.categorize();
    dialog.checkAutoSelectionEnabled(true);
    dialog.selectTableRows(0, 2);
    dialog.checkSelectedTableRows(0, 2);
    dialog.cancel();
  }

  public void testAutoHideCategorizedTransactions() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/26", -29.90, "Free Telecom 26/06")
      .addTransaction("2008/05/25", -29.90, "Free Telecom 25/05")
      .addTransaction("2008/05/15", -90.00, "Auchan")
      .addTransaction("2008/05/14", -80.00, "Carouf")
      .load();

    CategorizationDialogChecker dialog = informationPanel.categorize();
    dialog.checkTable(new Object[][]{
      {"15/05/2008", "Auchan", -90.00},
      {"14/05/2008", "Carouf", -80.00},
      {"25/05/2008", "Free Telecom 25/05", -29.90},
      {"26/06/2008", "Free Telecom 26/06", -29.90}
    });
    dialog.checkAutoHideEnabled(true);
    dialog.checkSelectedTableRows(0);
    dialog.selectEnvelopes();
    dialog.selectEnvelopeSeries("Groceries", MasterCategory.FOOD, true);

    dialog.checkTable(new Object[][]{
      {"14/05/2008", "Carouf", -80.00},
      {"25/05/2008", "Free Telecom 25/05", -29.90},
      {"26/06/2008", "Free Telecom 26/06", -29.90}
    });
    dialog.checkNoTransactionSelected();
    dialog.checkBudgetAreasAreDisabled();

    dialog.selectTableRows(1);
    dialog.checkBudgetAreasAreEnabled();
    dialog.checkSelectedTableRows(1, 2);
    dialog.selectRecurring();
    dialog.selectRecurringSeries("Internet", MasterCategory.TELECOMS, true);

    dialog.checkTable(new Object[][]{
      {"14/05/2008", "Carouf", -80.00},
    });
    dialog.checkSelectedTableRows();

    dialog.selectTableRows(0);
    dialog.selectEnvelopes();
    dialog.selectEnvelopeSeries("Groceries", MasterCategory.FOOD, false);

    dialog.checkTableIsEmpty();

    dialog.validate();

    views.selectData();
    transactions
      .initContent()
      .add("26/06/2008", TransactionType.PRELEVEMENT, "Free Telecom 26/06", "", -29.90, "Internet")
      .add("15/06/2008", TransactionType.PLANNED, "Groceries", "", -90.00, "Groceries")
      .add("25/05/2008", TransactionType.PRELEVEMENT, "Free Telecom 25/05", "", -29.90, "Internet")
      .add("15/05/2008", TransactionType.PRELEVEMENT, "Auchan", "", -90.00, "Groceries")
      .add("14/05/2008", TransactionType.PRELEVEMENT, "Carouf", "", -80.00, "Groceries")
      .check();
  }

  public void testAutoHideActivation() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/26", -29.90, "Free Telecom 26/06")
      .addTransaction("2008/05/25", -29.90, "Free Telecom 25/05")
      .addTransaction("2008/05/15", -90.00, "Auchan")
      .addTransaction("2008/05/14", -80.00, "Carouf")
      .load();

    CategorizationDialogChecker dialog = informationPanel.categorize();
    dialog.checkTable(new Object[][]{
      {"15/05/2008", "Auchan", -90.00},
      {"14/05/2008", "Carouf", -80.00},
      {"25/05/2008", "Free Telecom 25/05", -29.90},
      {"26/06/2008", "Free Telecom 26/06", -29.90}
    });

    dialog.disableAutoHide();
    dialog.selectTableRows(0, 1);
    dialog.selectEnvelopes();
    dialog.selectEnvelopeSeries("Groceries", MasterCategory.FOOD, true);

    dialog.checkTable(new Object[][]{
      {"15/05/2008", "Auchan", -90.00},
      {"14/05/2008", "Carouf", -80.00},
      {"25/05/2008", "Free Telecom 25/05", -29.90},
      {"26/06/2008", "Free Telecom 26/06", -29.90}
    });
    dialog.checkSelectedTableRows(0, 1);
    dialog.checkBudgetAreasAreEnabled();

    dialog.enableAutoHide();
    dialog.checkTable(new Object[][]{
      {"25/05/2008", "Free Telecom 25/05", -29.90},
      {"26/06/2008", "Free Telecom 26/06", -29.90}
    });
    dialog.checkNoTransactionSelected();

    dialog.disableAutoHide();
    dialog.checkTable(new Object[][]{
      {"15/05/2008", "Auchan", -90.00},
      {"14/05/2008", "Carouf", -80.00},
      {"25/05/2008", "Free Telecom 25/05", -29.90},
      {"26/06/2008", "Free Telecom 26/06", -29.90}
    });
    dialog.checkNoTransactionSelected();

    dialog.cancel();
  }

  public void testSeriesList() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/26", -29.90, "Free Telecom 26/06")
      .load();

    CategorizationDialogChecker dialog = informationPanel.categorize();
    dialog.disableAutoHide();

    dialog.selectIncome()
      .checkNoIncomeSeriesDisplayed()
      .checkEditIncomeSeriesDisabled();

    dialog
      .createIncomeSeries()
      .setName("Salary")
      .setCategory(MasterCategory.INCOME)
      .validate();

    dialog.checkContainsIncomeSeries("Salary");

    dialog.cancel();
  }

  public void testEditingOccasionalCategories() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/26", -29.90, "Free Telecom 26/06")
      .load();

    CategorizationDialogChecker dialog = informationPanel.categorize();
    dialog.editOccasionalCategories()
      .selectMaster(MasterCategory.FOOD)
      .createSubCategory("Apero")
      .validate();

    dialog.checkContainsOccasional(MasterCategory.FOOD, "Apero");

    dialog.editOccasionalCategories()
      .selectMaster(MasterCategory.FOOD)
      .selectSub("Apero")
      .deleteSubCategory()
      .validate();

    dialog.checkDoesNotContainOccasional(MasterCategory.FOOD, "Apero");

    dialog.cancel();
  }
}
