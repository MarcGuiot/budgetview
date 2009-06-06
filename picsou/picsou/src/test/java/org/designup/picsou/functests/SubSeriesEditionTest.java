package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.checkers.SeriesEditionDialogChecker;
import org.designup.picsou.model.TransactionType;

public class SubSeriesEditionTest extends LoggedInFunctionalTestCase {

  public void testStandardUsage() throws Exception {

    views.selectBudget();

    budgetView.envelopes.createSeries()
      .setName("Series")
      .gotoSubSeriesTab()
      .addSubSeries("SubSeries 1")
      .addSubSeries("SubSeries 2")
      .validate();

    OfxBuilder.init(this)
      .addTransaction("2008/07/01", -29.00, "Tx 1")
      .addTransaction("2008/07/15", -40.00, "Tx 2")
      .load();

    views.selectCategorization();
    categorization
      .selectTransaction("Tx 1")
      .selectEnvelopes()
      .checkSeriesContainsSubSeries("Series", "SubSeries 1", "SubSeries 2")
      .selectSeries("SubSeries 1");

    categorization
      .selectTransaction("Tx 2")
      .selectEnvelopes()
      .checkSeriesContainsSubSeries("Series", "SubSeries 1", "SubSeries 2")
      .selectSeries("SubSeries 2");

    categorization
      .selectTransaction("Tx 1")
      .selectEnvelopes()
      .checkSeriesIsSelected("SubSeries 1")
      .checkSeriesNotSelected("SubSeries 2");

    categorization
      .selectTransaction("Tx 2")
      .selectEnvelopes()
      .checkSeriesNotSelected("SubSeries 1")
      .checkSeriesIsSelected("SubSeries 2");

    views.selectCategorization();
    categorization
      .selectTransaction("Tx 1")
      .selectEnvelopes()
      .selectSeries("Series")
      .checkSeriesIsSelected("Series");
  }

  public void testCreationChecks() throws Exception {

    views.selectBudget();
    SeriesEditionDialogChecker dialog = budgetView.envelopes.createSeries();
    dialog
      .setName("series1")
      .gotoSubSeriesTab()
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

    views.selectCategorization();
    categorization.selectTableRows(0);
    categorization.checkLabel("PHARMAPLUS");

    categorization.selectEnvelopes().createSeries()
      .setName("Health")
      .gotoSubSeriesTab()
      .addSubSeries("Pharmacy")
      .validate();

    categorization.selectTransaction("PHARMAPLUS");
    categorization.getEnvelopes().checkSeriesIsSelectedWithSubSeries("Health", "Pharmacy");

    views.selectCategorization();
    categorization.checkTable(new Object[][]{
      {"30/06/2008", "Health / Pharmacy", "PHARMAPLUS", -129.90},
    });

    views.selectData();
    transactions.checkSeries(0, "Health", "Pharmacy");
    transactions.initContent()
      .add("30/06/2008", TransactionType.PRELEVEMENT, "PHARMAPLUS", "", -129.90, "Health", "Pharmacy")
      .check();
  }

  public void testRename() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -129.90, "Auchan")
      .load();

    views.selectCategorization();
    categorization.selectTableRows(0);
    categorization.checkLabel("AUCHAN");

    categorization.selectEnvelopes().createSeries()
      .setName("Groceries")
      .gotoSubSeriesTab()
      .addSubSeries("Food")
      .validate();

    categorization.selectTransaction("AUCHAN");
    categorization.getEnvelopes().checkSeriesIsSelectedWithSubSeries("Groceries", "Food");

    views.selectCategorization();
    categorization.checkTable(new Object[][]{
      {"30/06/2008", "Groceries / Food", "AUCHAN", -129.90},
    });

    categorization.getEnvelopes().editSeries("Groceries", false)
      .renameSubSeries("Food", "Misc")
      .validate();

    categorization.getEnvelopes()
      .checkNotPresent("Food")
      .checkSeriesIsSelectedWithSubSeries("Groceries", "Misc");
    
    views.selectData();
    transactions.initContent()
      .add("30/06/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -129.90, "Groceries", "Misc")
      .check();
  }

  public void testCannotUseExistingNameDuringRename() throws Exception {
    views.selectBudget();
    SeriesEditionDialogChecker dialog = budgetView.envelopes.createSeries();
    dialog
      .setName("series1")
      .gotoSubSeriesTab()
      .addSubSeries("subSeries1")
      .addSubSeries("subSeries2")
      .checkRenameSubSeriesMessage("subSeries2", "subSeries1", "A sub-series with this name already exists")
      .checkSubSeriesList("subSeries1", "subSeries2")
      .validate();
  }

  public void testSubSeriesAreNotUsedForPlannedTransactions() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -20., "PointP")
      .load();

    views.selectCategorization();
    categorization.selectTransactions("PointP");
    categorization.selectEnvelopes().createSeries()
      .setName("Maison")
      .gotoSubSeriesTab()
      .addSubSeries("Entretien")
      .validate();

    views.selectData();
    timeline.selectMonth("2008/07");
    transactions
      .initContent()
      .add("30/07/2008", TransactionType.PLANNED, "Planned: Maison", "", -20.00, "Maison", "")
      .check();

    views.selectBudget();
    budgetView.envelopes.editSeries("Maison")
      .gotoSubSeriesTab()
      .renameSubSeries("Entretien", "Travaux")
      .validate();

    views.selectData();
    transactions
      .initContent()
      .add("30/07/2008", TransactionType.PLANNED, "Planned: Maison", "", -20.00, "Maison", "")
      .check();

    views.selectBudget();
    budgetView.envelopes.editSeries("Maison")
      .deleteSubSeriesWithConfirmation("Travaux")
      .validate();

    views.selectData();
    transactions
      .initContent()
      .add("30/07/2008", TransactionType.PLANNED, "Planned: Maison", "", -20.00, "Maison", "")
      .check();
  }
}
