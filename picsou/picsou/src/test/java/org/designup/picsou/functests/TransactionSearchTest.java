package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;
import org.uispec4j.Key;
import org.uispec4j.TextBox;

public class TransactionSearchTest extends LoggedInFunctionalTestCase {

  public void testSearchIsUpdatedOnEveryTextChange() throws Exception {
    OfxBuilder
      .init(this)
      .addTransactionWithNote("2008/07/20", -500, "Fouquet's", "un cafe")
      .addTransactionWithNote("2008/07/19", -50, "Mac Do", "mmhhh")
      .addTransactionWithNote("2008/07/18", -10, "Pizza Hut", "miam")
      .addTransactionWithNote("2008/07/17", -20, "Pizza Lapino", "beurk")
      .addTransactionWithNote("2008/07/16", -15, "Pizza Pino", "miam miam")
      .load();

    views.selectData();
    TextBox searchField = transactions.getSearchField();
    searchField.setText("pizza");
    transactions.initContent()
      .add("18/07/2008", TransactionType.PRELEVEMENT, "Pizza Hut", "miam", -10.00)
      .add("17/07/2008", TransactionType.PRELEVEMENT, "Pizza Lapino", "beurk", -20.00)
      .add("16/07/2008", TransactionType.PRELEVEMENT, "Pizza Pino", "miam miam", -15.00)
      .check();

    searchField.clear();
    transactions.initContent()
      .add("20/07/2008", TransactionType.PRELEVEMENT, "Fouquet's", "un cafe", -500.00)
      .add("19/07/2008", TransactionType.PRELEVEMENT, "Mac Do", "mmhhh", -50.00)
      .add("18/07/2008", TransactionType.PRELEVEMENT, "Pizza Hut", "miam", -10.00)
      .add("17/07/2008", TransactionType.PRELEVEMENT, "Pizza Lapino", "beurk", -20.00)
      .add("16/07/2008", TransactionType.PRELEVEMENT, "Pizza Pino", "miam miam", -15.00)
      .check();

    searchField.appendText("a");
    transactions.initContent()
      .add("20/07/2008", TransactionType.PRELEVEMENT, "Fouquet's", "un cafe", -500.00)
      .add("19/07/2008", TransactionType.PRELEVEMENT, "Mac Do", "mmhhh", -50.00)
      .add("18/07/2008", TransactionType.PRELEVEMENT, "Pizza Hut", "miam", -10.00)
      .add("17/07/2008", TransactionType.PRELEVEMENT, "Pizza Lapino", "beurk", -20.00)
      .add("16/07/2008", TransactionType.PRELEVEMENT, "Pizza Pino", "miam miam", -15.00)
      .check();

    searchField.insertText("c", 0);
    transactions.initContent()
      .add("20/07/2008", TransactionType.PRELEVEMENT, "Fouquet's", "un cafe", -500.00)
      .check();

    searchField.pressKey(Key.DELETE);
    assertEquals("a", searchField.getText());
    transactions.initContent()
      .add("20/07/2008", TransactionType.PRELEVEMENT, "Fouquet's", "un cafe", -500.00)
      .add("19/07/2008", TransactionType.PRELEVEMENT, "Mac Do", "mmhhh", -50.00)
      .add("18/07/2008", TransactionType.PRELEVEMENT, "Pizza Hut", "miam", -10.00)
      .add("17/07/2008", TransactionType.PRELEVEMENT, "Pizza Lapino", "beurk", -20.00)
      .add("16/07/2008", TransactionType.PRELEVEMENT, "Pizza Pino", "miam miam", -15.00)
      .check();

    searchField.setText("50.");
    transactions.initContent()
      .add("19/07/2008", TransactionType.PRELEVEMENT, "Mac Do", "mmhhh", -50.00)
      .check();
  }

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

    views.selectCategorization();
    categorization.setNewVariable("Vinci", "Transports");
    categorization.setNewVariable("Virgin", "Leisures");
    categorization.setVariable("FNAC", "Leisures");

    views.selectData();
    timeline.selectAll();
    transactions.checkClearFilterButtonHidden();

    transactions.setSearchText("vi");
    transactions.checkClearFilterButtonShown();
    transactions.initContent()
      .add("20/07/2008", TransactionType.PRELEVEMENT, "Vinci", "", -5.00, "Transports")
      .add("15/07/2008", TransactionType.PRELEVEMENT, "Virgin", "", -50.00, "Leisures")
      .add("20/06/2008", TransactionType.PRELEVEMENT, "Vinci", "", -5.00, "Transports")
      .add("15/06/2008", TransactionType.PRELEVEMENT, "Virgin", "", -50.00, "Leisures")
      .check();

    timeline.selectMonth("2008/07");
    transactions.checkClearFilterButtonShown();
    transactions.initContent()
      .add("20/07/2008", TransactionType.PRELEVEMENT, "Vinci", "", -5.00, "Transports")
      .add("15/07/2008", TransactionType.PRELEVEMENT, "Virgin", "", -50.00, "Leisures")
      .check();

    transactions.clearSearch();
    transactions.initContent()
      .add("20/07/2008", TransactionType.PRELEVEMENT, "Vinci", "", -5.00, "Transports")
      .add("15/07/2008", TransactionType.PRELEVEMENT, "FNAC", "", -500.00, "Leisures")
      .add("15/07/2008", TransactionType.PRELEVEMENT, "Virgin", "", -50.00, "Leisures")
      .check();

    transactions.setSearchText("FN");
    transactions.checkClearFilterButtonShown();
    transactions.initContent()
      .add("15/07/2008", TransactionType.PRELEVEMENT, "FNAC", "", -500.00, "Leisures")
      .check();

    transactions.clearFilters();
    transactions.initContent()
      .add("20/07/2008", TransactionType.PRELEVEMENT, "VINCI", "", -5.00, "Transports")
      .add("15/07/2008", TransactionType.PRELEVEMENT, "FNAC", "", -500.00, "Leisures")
      .add("15/07/2008", TransactionType.PRELEVEMENT, "VIRGIN", "", -50.00, "Leisures")
      .check();
    transactions.checkSearchTextIsEmpty();
    transactions.checkSelectedAccount("All accounts");
    transactions.checkClearFilterButtonHidden();
  }

  public void testSearchTakesIntoAccountWhetherPlannedTransactionsAreShownOrNot() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/20", -5, "Vinci")
      .addTransaction("2008/06/15", -50, "Virgin")
      .addTransaction("2008/06/15", -500, "FNAC")
      .load();

    views.selectCategorization();
    categorization.setNewVariable("Vinci", "Transports", -5.);
    categorization.setNewVariable("Virgin", "Leisures", -550.);
    categorization.setVariable("FNAC", "Leisures");

    views.selectData();
    timeline.selectAll();

    transactions.initContent()
      .add("20/08/2008", TransactionType.PLANNED, "Planned: Transports", "", -5.00, "Transports")
      .add("15/08/2008", TransactionType.PLANNED, "Planned: Leisures", "", -550.00, "Leisures")
      .add("20/07/2008", TransactionType.PLANNED, "Planned: Transports", "", -5.00, "Transports")
      .add("15/07/2008", TransactionType.PLANNED, "Planned: Leisures", "", -550.00, "Leisures")
      .add("20/06/2008", TransactionType.PRELEVEMENT, "VINCI", "", -5.00, "Transports")
      .add("15/06/2008", TransactionType.PRELEVEMENT, "FNAC", "", -500.00, "Leisures")
      .add("15/06/2008", TransactionType.PRELEVEMENT, "VIRGIN", "", -50.00, "Leisures")
      .check();

    transactions.setSearchText("i");
    transactions.initContent()
      .add("15/08/2008", TransactionType.PLANNED, "Planned: Leisures", "", -550.00, "Leisures")
      .add("15/07/2008", TransactionType.PLANNED, "Planned: Leisures", "", -550.00, "Leisures")
      .add("20/06/2008", TransactionType.PRELEVEMENT, "VINCI", "", -5.00, "Transports")
      .add("15/06/2008", TransactionType.PRELEVEMENT, "VIRGIN", "", -50.00, "Leisures")
      .check();

    transactions.hidePlannedTransactions();
    transactions.initContent()
      .add("20/06/2008", TransactionType.PRELEVEMENT, "VINCI", "", -5.00, "Transports")
      .add("15/06/2008", TransactionType.PRELEVEMENT, "VIRGIN", "", -50.00, "Leisures")
      .check();

    transactions.clearSearch();
    transactions.initContent()
      .add("20/06/2008", TransactionType.PRELEVEMENT, "VINCI", "", -5.00, "Transports")
      .add("15/06/2008", TransactionType.PRELEVEMENT, "FNAC", "", -500.00, "Leisures")
      .add("15/06/2008", TransactionType.PRELEVEMENT, "VIRGIN", "", -50.00, "Leisures")
      .check();

    transactions.showPlannedTransactions();
    transactions.initContent()
      .add("20/08/2008", TransactionType.PLANNED, "Planned: Transports", "", -5.00, "Transports")
      .add("15/08/2008", TransactionType.PLANNED, "Planned: Leisures", "", -550.00, "Leisures")
      .add("20/07/2008", TransactionType.PLANNED, "Planned: Transports", "", -5.00, "Transports")
      .add("15/07/2008", TransactionType.PLANNED, "Planned: Leisures", "", -550.00, "Leisures")
      .add("20/06/2008", TransactionType.PRELEVEMENT, "VINCI", "", -5.00, "Transports")
      .add("15/06/2008", TransactionType.PRELEVEMENT, "FNAC", "", -500.00, "Leisures")
      .add("15/06/2008", TransactionType.PRELEVEMENT, "VIRGIN", "", -50.00, "Leisures")
      .check();
  }
}
