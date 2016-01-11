package org.designup.picsou.functests.budget;

import org.designup.picsou.functests.checkers.SeriesEditionDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

public class SubSeriesEditionTest extends LoggedInFunctionalTestCase {

  public void testStandardUsage() throws Exception {

    budgetView.variable.createSeries()
      .setName("Series")
      .editSubSeries()
      .addSubSeries("SubSeries 1")
      .addSubSeries("SubSeries 2")
      .validate();

    OfxBuilder.init(this)
      .addTransaction("2008/07/01", -29.00, "Tx 1")
      .addTransaction("2008/07/15", -40.00, "Tx 2")
      .load();

    categorization
      .selectTransaction("Tx 1")
      .selectVariable()
      .checkSeriesContainsSubSeries("Series", "SubSeries 1", "SubSeries 2")
      .selectSeries("SubSeries 1");

    categorization
      .selectTransaction("Tx 2")
      .selectVariable()
      .checkSeriesContainsSubSeries("Series", "SubSeries 1", "SubSeries 2")
      .selectSeries("SubSeries 2");

    categorization
      .selectTransaction("Tx 1")
      .selectVariable()
      .checkSelectedSeries("SubSeries 1");

    categorization
      .selectTransaction("Tx 2")
      .selectVariable()
      .checkSeriesNotSelected("SubSeries 1");

    transactions.initContent()
      .add("15/07/2008", TransactionType.PRELEVEMENT, "TX 2", "", -40.00, "Series / SubSeries 2")
      .add("01/07/2008", TransactionType.PRELEVEMENT, "TX 1", "", -29.00, "Series / SubSeries 1")
      .check();

    categorization
      .selectTransaction("Tx 1")
      .selectVariable()
      .selectSeries("Series")
      .checkSelectedSeries("Series");

    transactions.initContent()
      .add("15/07/2008", TransactionType.PRELEVEMENT, "TX 2", "", -40.00, "Series / SubSeries 2")
      .add("01/07/2008", TransactionType.PRELEVEMENT, "TX 1", "", -29.00, "Series")
      .check();
  }

  public void testCreationChecks() throws Exception {

    SeriesEditionDialogChecker dialog = budgetView.variable.createSeries();
    dialog
      .setName("series1")
      .editSubSeries()
      .checkNoSubSeriesMessage()
      .checkAddSubSeriesEnabled(false);
    dialog
      .enterSubSeriesName("subSeries1")
      .checkAddSubSeriesEnabled(true)
      .addSubSeries()
      .checkNoSubSeriesMessage()
      .checkSubSeriesList("subSeries1")
      .checkAddSubSeriesTextIsEmpty()
      .checkAddSubSeriesEnabled(false);
    dialog
      .enterSubSeriesName("subSeries1")
      .addSubSeries()
      .checkSubSeriesMessage("A sub-series with this name already exists")
      .checkSubSeriesList("subSeries1")
      .enterSubSeriesName("subSeries2")
      .checkNoSubSeriesMessage()
      .addSubSeries()
      .checkSubSeriesList("subSeries1", "subSeries2")
      .validate();
  }

  public void testSelectedSubSeriesIsAssignedToCurrentTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -129.90, "PharmaPlus")
      .load();

    categorization.selectTableRows(0);
    categorization.checkLabel("PHARMAPLUS");

    categorization.selectVariable().createSeries()
      .setName("Health")
      .editSubSeries()
      .addSubSeries("Pharmacy")
      .validate();

    categorization.selectTransaction("PHARMAPLUS");
    categorization.getVariable().checkSeriesIsSelectedWithSubSeries("Health", "Pharmacy");

    categorization.checkTable(new Object[][]{
      {"30/06/2008", "Health / Pharmacy", "PHARMAPLUS", -129.90},
    });

    transactions.checkSeries(0, "Health / Pharmacy");
    transactions.initContent()
      .add("30/06/2008", TransactionType.PRELEVEMENT, "PHARMAPLUS", "", -129.90, "Health / Pharmacy")
      .check();
  }

  public void testRename() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -129.90, "Auchan")
      .load();

    categorization.selectTableRows(0);
    categorization.checkLabel("AUCHAN");

    categorization.selectVariable().createSeries()
      .setName("Groceries")
      .editSubSeries()
      .addSubSeries("Food")
      .validate();

    categorization.selectTransaction("AUCHAN");
    categorization.getVariable().checkSeriesIsSelectedWithSubSeries("Groceries", "Food");

    categorization.checkTable(new Object[][]{
      {"30/06/2008", "Groceries / Food", "AUCHAN", -129.90},
    });

    categorization.getVariable().editSeries("Groceries")
      .editSubSeries()
      .renameSubSeries("Food", "Misc")
      .validate();

    categorization.getVariable()
      .checkNotPresent("Food")
      .checkSeriesIsSelectedWithSubSeries("Groceries", "Misc");

    transactions.initContent()
      .add("30/06/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -129.90, "Groceries / Misc")
      .check();
  }

  public void testCannotUseExistingNameDuringRename() throws Exception {
    SeriesEditionDialogChecker dialog = budgetView.variable.createSeries();
    dialog
      .setName("series1")
      .editSubSeries()
      .addSubSeries("subSeries1")
      .addSubSeries("subSeries2")
      .checkRenameSubSeriesMessage("subSeries2", "subSeries1", "A sub-series with this name already exists")
      .checkSubSeriesList("subSeries1", "subSeries2")
      .validate();
  }

  public void testSubSeriesAreOrderedInAlphabeticalOrder() throws Exception {
    budgetView.variable.createSeries()
      .setName("series1")
      .editSubSeries()
      .addSubSeries("subSeries2") // Create subSeries2 first to make sure sorting is not based on IDs
      .addSubSeries("subSeries1")
      .checkSubSeriesList("subSeries1", "subSeries2")
      .validate();

    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -129.90, "Auchan")
      .load();

    categorization.selectTransaction("AUCHAN")
      .selectVariable()
      .checkSeriesContainsSubSeriesList("series1", "subSeries1", "subSeries2");

    budgetView.variable.editSeries("series1")
      .editSubSeries()
      .renameSubSeries("subSeries1", "First SubSeries")
      .checkSubSeriesList("First SubSeries", "subSeries2")
      .renameSubSeries("subSeries2", "Second SubSeries")
      .checkSubSeriesList("First SubSeries", "Second SubSeries")
      .validate();
  }

  public void testSubSeriesAreNotUsedForPlannedTransactions() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -20., "PointP")
      .load();

    categorization.selectTransactions("PointP");
    categorization.selectVariable().createSeries()
      .setName("Maison")
      .selectAllMonths()
      .setAmount(20)
      .editSubSeries()
      .addSubSeries("Entretien")
      .validate();

    timeline.selectMonth("2008/07");
    transactions
      .showPlannedTransactions()
      .initContent()
      .add("27/07/2008", TransactionType.PLANNED, "Planned: Maison", "", -20.00, "Maison")
      .check();

    budgetView.variable.editSeries("Maison")
      .editSubSeries()
      .renameSubSeries("Entretien", "Travaux")
      .validate();

    transactions
      .initContent()
      .add("27/07/2008", TransactionType.PLANNED, "Planned: Maison", "", -20.00, "Maison")
      .check();

    SeriesEditionDialogChecker editionDialogChecker = budgetView.variable.editSeries("Maison");
    editionDialogChecker
      .editSubSeries()
      .deleteSubSeriesWithConfirmation("Travaux")
      .validate();
    editionDialogChecker.validate();

    transactions
      .initContent()
      .add("27/07/2008", TransactionType.PLANNED, "Planned: Maison", "", -20.00, "Maison")
      .check();
  }

  public void testDeleteSubSeries() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -10.00, "Tx1")
      .addTransaction("2008/06/30", -10.00, "Tx1")
      .addTransaction("2008/06/30", -10.00, "Tx2")
      .addTransaction("2008/06/30", -10.00, "Tx3")
      .load();

    categorization.selectTransaction("Tx1");
    categorization.selectVariable().createSeries()
      .setName("series1")
      .editSubSeries()
      .addSubSeries("subSeries1")
      .addSubSeries("subSeries2")
      .addSubSeries("subSeries3")
      .addSubSeries("subSeries4")
      .addSubSeries("subSeries5")
      .validate();

    categorization.setVariable("Tx1", "series1", "subSeries1");
    categorization.setVariable("Tx2", "series1", "subSeries2");
    categorization.setVariable("Tx3", "series1", "subSeries3");

    categorization.checkTable(new Object[][]{
      {"30/06/2008", "series1 / subSeries1", "Tx1", -10.00},
      {"30/06/2008", "series1 / subSeries1", "Tx1", -10.00},
      {"30/06/2008", "series1 / subSeries2", "Tx2", -10.00},
      {"30/06/2008", "series1 / subSeries3", "Tx3", -10.00},
    });

    SeriesEditionDialogChecker seriesDialog = categorization.getVariable().editSeries("series1");
    seriesDialog
      .editSubSeries()
      .deleteSubSeriesWithConfirmation("subSeries1", "subSeries3")
      .checkDeletionOptions("Move them to envelope 'series1'",
                            "Move them to sub-envelope 'subSeries2'",
                            "Move them to sub-envelope 'subSeries4'",
                            "Move them to sub-envelope 'subSeries5'",
                            "Uncategorize them")
      .selectDeletionOption("Move them to envelope 'series1'")
      .validate();
    seriesDialog
      .checkSubSeriesList("subSeries2", "subSeries4", "subSeries5")
      .validate();

    categorization.checkTable(new Object[][]{
      {"30/06/2008", "series1", "Tx1", -10.00},
      {"30/06/2008", "series1", "Tx1", -10.00},
      {"30/06/2008", "series1 / subSeries2", "Tx2", -10.00},
      {"30/06/2008", "series1", "Tx3", -10.00}
    });

    categorization.selectTransaction("Tx2");
    SeriesEditionDialogChecker dialog2 = categorization.selectVariable().editSeries("series1");
    dialog2
      .editSubSeries()
      .deleteSubSeriesWithConfirmation("subSeries2")
      .checkDeletionOptions("Move them to envelope 'series1'",
                            "Move them to sub-envelope 'subSeries4'",
                            "Move them to sub-envelope 'subSeries5'",
                            "Uncategorize them")
      .selectDeletionOption("Move them to sub-envelope 'subSeries4'")
      .validate();
    dialog2
      .checkSubSeriesList("subSeries4", "subSeries5")
      .validate();

    categorization.checkTable(new Object[][]{
      {"30/06/2008", "series1", "Tx1", -10.00},
      {"30/06/2008", "series1", "Tx1", -10.00},
      {"30/06/2008", "series1 / subSeries4", "Tx2", -10.00},
      {"30/06/2008", "series1", "Tx3", -10.00}
    });

    categorization.selectTransaction("Tx2");
    SeriesEditionDialogChecker dialog3 = categorization.selectVariable().editSeries("series1");
    dialog3
      .editSubSeries()
      .deleteSubSeriesWithConfirmation("subSeries4")
      .checkDeletionOptions("Move them to envelope 'series1'",
                            "Move them to sub-envelope 'subSeries5'",
                            "Uncategorize them")
      .selectDeletionOption("Uncategorize them")
      .validate();
    dialog3
      .checkSubSeriesList("subSeries5")
      .validate();

    categorization.checkTable(new Object[][]{
      {"30/06/2008", "series1", "Tx1", -10.00},
      {"30/06/2008", "series1", "Tx1", -10.00},
      {"30/06/2008", "", "Tx2", -10.00},
      {"30/06/2008", "series1", "Tx3", -10.00}
    });

    categorization.selectTransaction("Tx2");
    categorization.selectVariable().editSeries("series1")
      .editSubSeries()
      .deleteSubSeries("subSeries5")
      .checkSubSeriesListIsEmpty()
      .validate();
  }
}
