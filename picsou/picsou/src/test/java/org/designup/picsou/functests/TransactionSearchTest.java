package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
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
  }

  public void testSearchIsPreservedDuringOtherSelectionChanges() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/07/20", -5, "Vinci", MasterCategory.TRANSPORTS)
      .addTransaction("2008/07/15", -50, "Virgin", MasterCategory.LEISURES)
      .addTransaction("2008/07/15", -500, "FNAC", MasterCategory.LEISURES)
      .addTransaction("2008/06/20", -5, "Vinci", MasterCategory.TRANSPORTS)
      .addTransaction("2008/06/15", -50, "Virgin", MasterCategory.LEISURES)
      .addTransaction("2008/06/15", -500, "FNAC", MasterCategory.LEISURES)
      .load();

    TextBox searchField = transactions.getSearchField();
    searchField.setText("vi");

    transactions.initContent()
      .addOccasional("20/07/2008", TransactionType.PRELEVEMENT, "Vinci", "", -5.00, MasterCategory.TRANSPORTS)
      .addOccasional("15/07/2008", TransactionType.PRELEVEMENT, "Virgin", "", -50.00, MasterCategory.LEISURES)
      .addOccasional("20/06/2008", TransactionType.PRELEVEMENT, "Vinci", "", -5.00, MasterCategory.TRANSPORTS)
      .addOccasional("15/06/2008", TransactionType.PRELEVEMENT, "Virgin", "", -50.00, MasterCategory.LEISURES)
      .check();

    timeline.selectMonth("2008/07");
    transactions.initContent()
      .addOccasional("20/07/2008", TransactionType.PRELEVEMENT, "Vinci", "", -5.00, MasterCategory.TRANSPORTS)
      .addOccasional("15/07/2008", TransactionType.PRELEVEMENT, "Virgin", "", -50.00, MasterCategory.LEISURES)
      .check();

    categories.select(MasterCategory.LEISURES);
    transactions.initContent()
      .addOccasional("15/07/2008", TransactionType.PRELEVEMENT, "Virgin", "", -50.00, MasterCategory.LEISURES)
      .check();

    searchField.clear();
    transactions.initContent()
      .addOccasional("15/07/2008", TransactionType.PRELEVEMENT, "FNAC", "", -500.00, MasterCategory.LEISURES)
      .addOccasional("15/07/2008", TransactionType.PRELEVEMENT, "Virgin", "", -50.00, MasterCategory.LEISURES)
      .check();
  }
}
