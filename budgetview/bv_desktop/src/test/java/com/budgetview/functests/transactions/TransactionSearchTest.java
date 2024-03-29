package com.budgetview.functests.transactions;

import com.budgetview.functests.checkers.SeriesEditionDialogChecker;
import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;
import com.budgetview.model.TransactionType;
import org.junit.Test;
import org.uispec4j.Key;
import org.uispec4j.TextBox;

public class TransactionSearchTest extends LoggedInFunctionalTestCase {

  @Test
  public void testSearchIsUpdatedOnEveryTextChange() throws Exception {
    OfxBuilder
      .init(this)
      .addTransactionWithNote("2008/07/20", -500, "Fouquet's", "un café")
      .addTransactionWithNote("2008/07/19", -50, "Léon", "mmhhh")
      .addTransactionWithNote("2008/07/18", -10, "Pizza Hut", "miam")
      .addTransactionWithNote("2008/07/17", -20, "Pizza Lapino", "beurk")
      .addTransactionWithNote("2008/07/16", -15, "Pizza Pino", "miam miam")
      .load();

    TextBox searchField = transactions.getSearchField();
    searchField.setText("pizza");
    transactions.checkFilterMessage("Text: pizza");
    transactions.initContent()
      .add("18/07/2008", TransactionType.PRELEVEMENT, "Pizza Hut", "miam", -10.00)
      .add("17/07/2008", TransactionType.PRELEVEMENT, "Pizza Lapino", "beurk", -20.00)
      .add("16/07/2008", TransactionType.PRELEVEMENT, "Pizza Pino", "miam miam", -15.00)
      .check();

    searchField.clear();
    transactions.checkNoFilterMessageShown();
    transactions.initContent()
      .add("20/07/2008", TransactionType.PRELEVEMENT, "Fouquet's", "un café", -500.00)
      .add("19/07/2008", TransactionType.PRELEVEMENT, "Léon", "mmhhh", -50.00)
      .add("18/07/2008", TransactionType.PRELEVEMENT, "Pizza Hut", "miam", -10.00)
      .add("17/07/2008", TransactionType.PRELEVEMENT, "Pizza Lapino", "beurk", -20.00)
      .add("16/07/2008", TransactionType.PRELEVEMENT, "Pizza Pino", "miam miam", -15.00)
      .check();

    searchField.appendText("a");
    transactions.checkFilterMessage("Text: a");
    transactions.initContent()
      .add("20/07/2008", TransactionType.PRELEVEMENT, "Fouquet's", "un café", -500.00)
      .add("18/07/2008", TransactionType.PRELEVEMENT, "Pizza Hut", "miam", -10.00)
      .add("17/07/2008", TransactionType.PRELEVEMENT, "Pizza Lapino", "beurk", -20.00)
      .add("16/07/2008", TransactionType.PRELEVEMENT, "Pizza Pino", "miam miam", -15.00)
      .check();

    searchField.insertText("c", 0);
    transactions.checkFilterMessage("Text: ca");
    transactions.initContent()
      .add("20/07/2008", TransactionType.PRELEVEMENT, "Fouquet's", "un café", -500.00)
      .check();

    searchField.pressKey(Key.DELETE);
    assertEquals("a", searchField.getText());
    transactions.checkFilterMessage("Text: a");
    transactions.initContent()
      .add("20/07/2008", TransactionType.PRELEVEMENT, "Fouquet's", "un café", -500.00)
      .add("18/07/2008", TransactionType.PRELEVEMENT, "Pizza Hut", "miam", -10.00)
      .add("17/07/2008", TransactionType.PRELEVEMENT, "Pizza Lapino", "beurk", -20.00)
      .add("16/07/2008", TransactionType.PRELEVEMENT, "Pizza Pino", "miam miam", -15.00)
      .check();

    searchField.setText("50.");
    transactions.checkFilterMessage("Text: 50.");
    transactions.initContent()
      .add("19/07/2008", TransactionType.PRELEVEMENT, "Léon", "mmhhh", -50.00)
      .check();

    searchField.setText("unknown");
    transactions.checkFilterMessage("Text: unknown");
    transactions.checkEmpty();

    searchField.setText("50.");
    transactions.checkFilterMessage("Text: 50.");
    transactions.initContent()
      .add("19/07/2008", TransactionType.PRELEVEMENT, "Léon", "mmhhh", -50.00)
      .check();

    searchField.setText("é");
    transactions.checkFilterMessage("Text: é");
    transactions.initContent()
      .add("20/07/2008", TransactionType.PRELEVEMENT, "Fouquet's", "un café", -500.00)
      .add("19/07/2008", TransactionType.PRELEVEMENT, "Léon", "mmhhh", -50.00)
      .add("17/07/2008", TransactionType.PRELEVEMENT, "Pizza Lapino", "beurk", -20.00)
      .check();

    searchField.setText("É");
    transactions.checkFilterMessage("Text: É");
    transactions.initContent()
      .add("20/07/2008", TransactionType.PRELEVEMENT, "Fouquet's", "un café", -500.00)
      .add("19/07/2008", TransactionType.PRELEVEMENT, "Léon", "mmhhh", -50.00)
      .add("17/07/2008", TransactionType.PRELEVEMENT, "Pizza Lapino", "beurk", -20.00)
      .check();

    searchField.setText("e");
    transactions.checkFilterMessage("Text: e");
    transactions.initContent()
      .add("20/07/2008", TransactionType.PRELEVEMENT, "Fouquet's", "un café", -500.00)
      .add("19/07/2008", TransactionType.PRELEVEMENT, "Léon", "mmhhh", -50.00)
      .add("17/07/2008", TransactionType.PRELEVEMENT, "Pizza Lapino", "beurk", -20.00)
      .check();
  }

  @Test
  public void testSearchIsPreservedDuringOtherSelectionChanges() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/07/20", -5, "Vinci")
      .addTransaction("2008/07/15", -50, "Virgin")
      .addTransaction("2008/07/15", -500, "FNAC")
      .addTransaction("2008/06/20", -5, "Vinci")
      .addTransaction("2008/06/15", -50, "Virgin")
      .addTransaction("2008/06/15", -500, "FNAC")
      .load();

    categorization.setNewVariable("Vinci", "Transports");
    categorization.setNewVariable("Virgin", "Leisures");
    categorization.setVariable("FNAC", "Leisures");

    timeline.selectAll();
    transactions.checkNoFilterMessageShown();

    transactions.setSearchText("vi");
    transactions.checkFilterMessage("Text: vi");
    transactions.initContent()
      .add("20/07/2008", TransactionType.PRELEVEMENT, "Vinci", "", -5.00, "Transports")
      .add("15/07/2008", TransactionType.PRELEVEMENT, "Virgin", "", -50.00, "Leisures")
      .add("20/06/2008", TransactionType.PRELEVEMENT, "Vinci", "", -5.00, "Transports")
      .add("15/06/2008", TransactionType.PRELEVEMENT, "Virgin", "", -50.00, "Leisures")
      .check();

    timeline.selectMonth("2008/07");
    transactions.checkFilterMessage("Text: vi");
    transactions.initContent()
      .add("20/07/2008", TransactionType.PRELEVEMENT, "Vinci", "", -5.00, "Transports")
      .add("15/07/2008", TransactionType.PRELEVEMENT, "Virgin", "", -50.00, "Leisures")
      .check();

    transactions.clearSearch();
    transactions.checkNoFilterMessageShown();
    transactions.initContent()
      .add("20/07/2008", TransactionType.PRELEVEMENT, "Vinci", "", -5.00, "Transports")
      .add("15/07/2008", TransactionType.PRELEVEMENT, "FNAC", "", -500.00, "Leisures")
      .add("15/07/2008", TransactionType.PRELEVEMENT, "Virgin", "", -50.00, "Leisures")
      .check();

    transactions.setSearchText("FN");
    transactions.checkFilterMessage("Text: FN");
    transactions.initContent()
      .add("15/07/2008", TransactionType.PRELEVEMENT, "FNAC", "", -500.00, "Leisures")
      .check();

    transactions.clearCurrentFilter();
    transactions.initContent()
      .add("20/07/2008", TransactionType.PRELEVEMENT, "VINCI", "", -5.00, "Transports")
      .add("15/07/2008", TransactionType.PRELEVEMENT, "FNAC", "", -500.00, "Leisures")
      .add("15/07/2008", TransactionType.PRELEVEMENT, "VIRGIN", "", -50.00, "Leisures")
      .check();
    transactions.checkSearchTextIsEmpty();
    mainAccounts.checkNoAccountsSelected();
    transactions.checkNoFilterMessageShown();
  }

  @Test
  public void testSearchTakesIntoAccountWhetherPlannedTransactionsAreShownOrNot() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/20", -5, "Vinci")
      .addTransaction("2008/06/15", -50, "Virgin")
      .addTransaction("2008/06/15", -50, "Other")
      .addTransaction("2008/06/15", -500, "FNAC")
      .load();

    categorization.setNewVariable("Vinci", "Transports", -5.);
    categorization.setNewVariable("Virgin", "Leisures", -550.);
    categorization.setVariable("FNAC", "Leisures");

    timeline.selectAll();

    transactions
      .showPlannedTransactions()
      .initContent()
      .add("11/08/2008", TransactionType.PLANNED, "Planned: Leisures", "", -550.00, "Leisures")
      .add("04/08/2008", TransactionType.PLANNED, "Planned: Transports", "", -5.00, "Transports")
      .add("11/07/2008", TransactionType.PLANNED, "Planned: Leisures", "", -550.00, "Leisures")
      .add("04/07/2008", TransactionType.PLANNED, "Planned: Transports", "", -5.00, "Transports")
      .add("20/06/2008", TransactionType.PRELEVEMENT, "VINCI", "", -5.00, "Transports")
      .add("15/06/2008", TransactionType.PRELEVEMENT, "FNAC", "", -500.00, "Leisures")
      .add("15/06/2008", TransactionType.PRELEVEMENT, "Other", "", -50.00)
      .add("15/06/2008", TransactionType.PRELEVEMENT, "VIRGIN", "", -50.00, "Leisures")
      .check();

    transactions.setSearchText("i");
    transactions.initContent()
      .add("11/08/2008", TransactionType.PLANNED, "Planned: Leisures", "", -550.00, "Leisures")
      .add("11/07/2008", TransactionType.PLANNED, "Planned: Leisures", "", -550.00, "Leisures")
      .add("20/06/2008", TransactionType.PRELEVEMENT, "VINCI", "", -5.00, "Transports")
      .add("15/06/2008", TransactionType.PRELEVEMENT, "FNAC", "", -500.00, "Leisures")
      .add("15/06/2008", TransactionType.PRELEVEMENT, "VIRGIN", "", -50.00, "Leisures")
      .check();

    transactions.hidePlannedTransactions();
    transactions.initContent()
      .add("20/06/2008", TransactionType.PRELEVEMENT, "VINCI", "", -5.00, "Transports")
      .add("15/06/2008", TransactionType.PRELEVEMENT, "FNAC", "", -500.00, "Leisures")
      .add("15/06/2008", TransactionType.PRELEVEMENT, "VIRGIN", "", -50.00, "Leisures")
      .check();

    transactions.clearSearch();
    transactions.initContent()
      .add("20/06/2008", TransactionType.PRELEVEMENT, "VINCI", "", -5.00, "Transports")
      .add("15/06/2008", TransactionType.PRELEVEMENT, "FNAC", "", -500.00, "Leisures")
      .add("15/06/2008", TransactionType.PRELEVEMENT, "Other", "", -50.00)
      .add("15/06/2008", TransactionType.PRELEVEMENT, "VIRGIN", "", -50.00, "Leisures")
      .check();

    transactions.showPlannedTransactions();
    transactions.initContent()
      .add("11/08/2008", TransactionType.PLANNED, "Planned: Leisures", "", -550.00, "Leisures")
      .add("04/08/2008", TransactionType.PLANNED, "Planned: Transports", "", -5.00, "Transports")
      .add("11/07/2008", TransactionType.PLANNED, "Planned: Leisures", "", -550.00, "Leisures")
      .add("04/07/2008", TransactionType.PLANNED, "Planned: Transports", "", -5.00, "Transports")
      .add("20/06/2008", TransactionType.PRELEVEMENT, "VINCI", "", -5.00, "Transports")
      .add("15/06/2008", TransactionType.PRELEVEMENT, "FNAC", "", -500.00, "Leisures")
      .add("15/06/2008", TransactionType.PRELEVEMENT, "Other", "", -50.00)
      .add("15/06/2008", TransactionType.PRELEVEMENT, "VIRGIN", "", -50.00, "Leisures")
      .check();
  }

  @Test
  public void testSearchingBySubseries() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/07/20", -500, "Fouquet's")
      .addTransaction("2008/07/19", -50, "Mac Do")
      .addTransactionWithNote("2008/07/18", -10, "Pizza Hut", "miam")
      .addTransactionWithNote("2008/07/17", -20, "Pizza Lapino", "beurk")
      .addTransactionWithNote("2008/07/16", -15, "Pizza Pino", "miam miam")
      .load();

    budgetView.variable.createSeries()
      .setName("Alimentation")
      .editSubSeries()
      .addSubSeries("Francais")
      .addSubSeries("Americain")
      .addSubSeries("Italien")
      .validate();

    budgetView.variable.createSeries()
      .setName("Gastronomie")
      .editSubSeries()
      .addSubSeries("Francaise")
      .addSubSeries("Americaine")
      .addSubSeries("Italienne")
      .validate();

    categorization.setVariable("Fouquet's", "Alimentation", "Francais");
    categorization.setVariable("Mac Do", "Gastronomie", "Americaine");
    categorization.setVariable("Pizza Hut", "Gastronomie", "Italienne");
    categorization.setVariable("Pizza Lapino", "Alimentation", "Italien");
    categorization.setVariable("Pizza Pino", "Alimentation", "Italien");

    categorization.search("Italien");
    categorization.checkFilterMessage("Text: Italien");
    categorization.initContent()
      .add("18/07/2008", "Gastronomie / Italienne", "Pizza Hut", -10.00)
      .add("17/07/2008", "Alimentation / Italien", "Pizza Lapino", -20.00)
      .add("16/07/2008", "Alimentation / Italien", "Pizza Pino", -15.00)
      .check();

    TextBox searchField = transactions.getSearchField();
    searchField.setText("Italien");
    transactions.checkFilterMessage("Text: Italien");
    transactions.initContent()
      .add("18/07/2008", TransactionType.PRELEVEMENT, "PIZZA HUT", "miam", -10.00, "Gastronomie / Italienne")
      .add("17/07/2008", TransactionType.PRELEVEMENT, "PIZZA LAPINO", "beurk", -20.00, "Alimentation / Italien")
      .add("16/07/2008", TransactionType.PRELEVEMENT, "PIZZA PINO", "miam miam", -15.00, "Alimentation / Italien")
      .check();

    budgetView.variable.editSeries("Alimentation")
      .editSubSeries()
      .renameSubSeries("Italien", "Napolitain")
      .validate();
    transactions.initContent()
      .add("18/07/2008", TransactionType.PRELEVEMENT, "PIZZA HUT", "miam", -10.00, "Gastronomie / Italienne")
      .check();

    searchField.setText("Francais");
    SeriesEditionDialogChecker seriesDialog = budgetView.variable.editSeries("Alimentation")
      .editSubSeries();
    seriesDialog.deleteSubSeriesWithConfirmation("Francais")
      .selectDeletionOption("Move them to envelope 'Alimentation'")
      .validate();
    seriesDialog.validate();
    transactions.checkEmpty();

    transactions.clearSearch();
    transactions.initContent()
      .add("20/07/2008", TransactionType.PRELEVEMENT, "FOUQUET'S", "", -500.00, "Alimentation")
      .add("19/07/2008", TransactionType.PRELEVEMENT, "MAC DO", "", -50.00, "Gastronomie / Americaine")
      .add("18/07/2008", TransactionType.PRELEVEMENT, "PIZZA HUT", "miam", -10.00, "Gastronomie / Italienne")
      .add("17/07/2008", TransactionType.PRELEVEMENT, "PIZZA LAPINO", "beurk", -20.00, "Alimentation / Napolitain")
      .add("16/07/2008", TransactionType.PRELEVEMENT, "PIZZA PINO", "miam miam", -15.00, "Alimentation / Napolitain")
      .check();

    searchField.setText("Alimentation");
    transactions.initContent()
      .add("20/07/2008", TransactionType.PRELEVEMENT, "FOUQUET'S", "", -500.00, "Alimentation")
      .add("17/07/2008", TransactionType.PRELEVEMENT, "PIZZA LAPINO", "beurk", -20.00, "Alimentation / Napolitain")
      .add("16/07/2008", TransactionType.PRELEVEMENT, "PIZZA PINO", "miam miam", -15.00, "Alimentation / Napolitain")
      .check();

    budgetView.variable.editSeries("Alimentation").setName("Restaurant").validate();
    transactions.checkEmpty();
  }
}
