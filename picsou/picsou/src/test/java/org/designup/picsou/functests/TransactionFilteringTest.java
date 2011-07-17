package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

public class TransactionFilteringTest extends LoggedInFunctionalTestCase {

  public void testAccountFiltering() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount("1", 0.00, "2006/05/10")
      .addTransaction("2006/05/11", -10.0, "Transaction 1")
      .addBankAccount("2", 0.00, "2006/05/10")
      .addTransaction("2006/05/10", -10.0, "Transaction 2")
      .load();

    transactions.initContent()
      .add("11/05/2006", TransactionType.PRELEVEMENT, "TRANSACTION 1", "", -10.00)
      .add("10/05/2006", TransactionType.PRELEVEMENT, "TRANSACTION 2", "", -10.00)
      .check();

    mainAccounts.select("Account n. 1");
    mainAccounts.checkSelectedAccounts("Account n. 1");
    transactions.checkClearFilterButtonShown();
    transactions.initContent()
      .add("11/05/2006", TransactionType.PRELEVEMENT, "TRANSACTION 1", "", -10.00)
      .check();

    transactions.clearFilters();
    mainAccounts.checkNoAccountsSelected();

    mainAccounts.select("Account n. 2");
    mainAccounts.checkSelectedAccounts("Account n. 2");
    transactions.checkClearFilterButtonShown();
    transactions.initContent()
      .add("10/05/2006", TransactionType.PRELEVEMENT, "TRANSACTION 2", "", -10.00)
      .check();

    transactions.clearFilters();
    mainAccounts.checkNoAccountsSelected();
    transactions.checkClearFilterButtonHidden();
  }

  public void testSeriesFitering() throws Exception {
    OfxBuilder
      .init(this)
      .addTransactionWithNote("2006/05/01", -70.00, "essence", "frais pro")
      .addTransactionWithNote("2006/05/03", -30.00, "peage", "")
      .addTransactionWithNote("2006/05/02", -200.00, "sg", "")
      .addTransactionWithNote("2006/05/06", -100.00, "nounou", "nourrice")
      .load();

    transactions.checkSelectableSeries("All");

    categorization.setNewVariable("essence", "Voiture", -70.00);
    categorization.setNewRecurring("nounou", "Nounou");
    transactions.checkSelectableSeries("All", "Nounou", "Voiture");

    transactions.checkSelectedSeries("All");
    transactions.checkClearFilterButtonHidden();

    timeline.selectAll();
    transactions.selectSeries("Nounou");
    transactions.checkClearFilterButtonShown();

    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "NOUNOU", "nourrice", -100.00, "Nounou")
      .check();

    transactions.selectSeries("Voiture");
    transactions.checkClearFilterButtonShown();
    transactions.initContent()
      .add("01/05/2006", TransactionType.PRELEVEMENT, "ESSENCE", "frais pro", -70.00, "Voiture")
      .check();

    transactions.clearFilters();
    transactions.checkClearFilterButtonHidden();
    transactions.checkSelectedSeries("All");
    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "NOUNOU", "nourrice", -100.00, "Nounou")
      .add("03/05/2006", TransactionType.PRELEVEMENT, "PEAGE", "", -30.00)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "SG", "", -200.00)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "ESSENCE", "frais pro", -70.00, "Voiture")
      .check();

    timeline.selectAll();
    transactions.selectSeries("Nounou");
    budgetView.recurring.editSeries("Nounou").setEndDate(200605).validate();
    transactions.checkSelectableSeries("All", "Nounou", "Voiture");

    timeline.selectMonth("2006/06");
    transactions.checkSelectedSeries("All");
    transactions.checkSelectableSeries("All", "Voiture");
    transactions.initContent()
      .check();

    timeline.selectMonth("2006/05");
    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "NOUNOU", "nourrice", -100.00, "Nounou")
      .add("03/05/2006", TransactionType.PRELEVEMENT, "PEAGE", "", -30.00)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "SG", "", -200.00)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "ESSENCE", "frais pro", -70.00, "Voiture")
      .check();

    timeline.selectAll();
    budgetView.variable.editSeries("Voiture").deleteCurrentSeriesWithConfirmation();
    transactions.checkSelectableSeries("All", "Nounou");

    budgetView.recurring.editSeries("Nounou").clearEndDate().validate();
    transactions.checkSelectableSeries("All", "Nounou");
  }
}
