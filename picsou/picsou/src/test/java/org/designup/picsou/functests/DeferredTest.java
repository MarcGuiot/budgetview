package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.BudgetAreaCategorizationChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.QifBuilder;

public class DeferredTest extends LoggedInFunctionalTestCase {
  protected void setUp() throws Exception {
    super.setUp();
    setCurrentDate("2009/12/09");
    operations.openPreferences().setFutureMonthsCount(2).validate();
  }

  public void testCategorisation() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder.init(this)
      .addCardAccount("1111", 100, "2008/06/30")
      .addTransaction("2008/06/27", -50, "Auchan")
      .addBankAccount("", -1, "1234", 1000, "2008/06/30")
      .addTransaction("2008/06/28", -550, "Prelevement")
      .loadDeferredCard("Card n. 1111", 28);

    views.selectCategorization();
    categorization.selectTransaction("Auchan")
      .selectOther()
      .checkContainsNoSeries()
      .checkEditSeriesButtonNotVisible();

    categorization.selectTransaction("Prelevement")
      .selectOther()
      .checkActiveSeries("Card n. 1111")
      .checkEditSeriesButtonNotVisible()
      .selectSeries("Card n. 1111");

    views.selectData();
    transactions
      .initAmountContent()
      .add("28/06/2008", "PRELEVEMENT", -550.00, "Card n. 1111", 1000.00, 1000.00, "Account n. 1234")
      .add("27/06/2008", "AUCHAN", -50.00, "To categorize", 100.00, 1000.00, "Card n. 1111")
      .check();
  }

  public void testFirstOfxImport() throws Exception {
    OfxBuilder.init(this)
      .addCardAccount("1111", -100, "2009/12/07")
      .addTransaction("2009/11/30", -60, "Auchan")
      .addTransaction("2009/11/29", -40, "Auchan")
      .addTransaction("2009/11/25", -10, "Auchan")
      .addTransaction("2009/10/29", -20, "Auchan")
      .addTransaction("2009/09/14", -35, "Auchan")
      .addBankAccount("", -1, "1234", 1000, "2009/11/30")
      .addTransaction("2009/11/28", -30, "Prelevement novembre")
      .addTransaction("2009/10/28", 0, "Prelevement octobre")
      .addTransaction("2009/09/26", -35 - 15 /* -15 : transaction precedente non importé */, "Prelevement aout")
      .loadDeferredCard("Card n. 1111", 28);

    timeline.selectAll();
    views.selectData();
    transactions.initAmountContent()
      .add("30/11/2009", "AUCHAN", -60.00, "To categorize", -100.00, 900.00, "Card n. 1111")
      .add("29/11/2009", "AUCHAN", -40.00, "To categorize", -40.00, 960.00, "Card n. 1111")
      .add("28/11/2009", "PRELEVEMENT NOVEMBRE", -30.00, "To categorize", 1000.00, 1000.00, "Account n. 1234")
      .add("25/11/2009", "AUCHAN", -10.00, "To categorize", -30.00, 1000.00, "Card n. 1111")
      .add("29/10/2009", "AUCHAN", -20.00, "To categorize", -20.00, 1000.00, "Card n. 1111")
      .add("28/10/2009", "PRELEVEMENT OCTOBRE", 0.00, "To categorize", 1030.00, 1030.00, "Account n. 1234")
      .add("26/09/2009", "PRELEVEMENT AOUT", -50.00, "To categorize", 1030.00, 1030.00, "Account n. 1234")
      .add("14/09/2009", "AUCHAN", -35.00, "To categorize", -35.00, 1030.00, "Card n. 1111")
      .check();

    views.selectCategorization();
    categorization.selectTransaction("Prelevement novembre")
      .selectOther()
      .selectSeries("Card n. 1111");
    categorization.selectTransaction("Prelevement octobre")
      .selectOther()
      .selectSeries("Card n. 1111");
    categorization.selectTransaction("Prelevement aout")
      .selectOther()
      .selectSeries("Card n. 1111");
    views.selectData();
    transactions.initAmountContent()
      .add("30/11/2009", "AUCHAN", -60.00, "To categorize", -100.00, 900.00, "Card n. 1111")
      .add("29/11/2009", "AUCHAN", -40.00, "To categorize", -40.00, 960.00, "Card n. 1111")
      .add("28/11/2009", "PRELEVEMENT NOVEMBRE", -30.00, "Card n. 1111", 1000.00, 1000.00, "Account n. 1234")
      .add("25/11/2009", "AUCHAN", -10.00, "To categorize", -30.00, 1000.00, "Card n. 1111")
      .add("29/10/2009", "AUCHAN", -20.00, "To categorize", -20.00, 1000.00, "Card n. 1111")
      .add("28/10/2009", "PRELEVEMENT OCTOBRE", 0.00, "Card n. 1111", 1030.00, 1030.00, "Account n. 1234")
      .add("26/09/2009", "PRELEVEMENT AOUT", -50.00, "Card n. 1111", 1030.00, 1030.00, "Account n. 1234")
      .add("14/09/2009", "AUCHAN", -35.00, "To categorize", -50.00, 1030.00, "Card n. 1111")
      .check();

    // check budget
    views.selectCategorization();
    BudgetAreaCategorizationChecker budgetAreaCategorizationChecker = categorization.selectTransactions("Auchan")
      .selectEnvelopes();
    budgetAreaCategorizationChecker
      .createSeries()
      .setName("Course")
      .switchToManual()
      .selectAllMonths()
      .setAmount("250")
      .validate();

    views.selectBudget();
    timeline.selectMonth("2009/12");
    budgetView.envelopes.checkSeries("Course", -100, -250);

    timeline.selectMonth("2009/11");
    budgetView.envelopes.checkSeries("Course", -30, -250);
  }

  public void testOfxOnePartialMonth() throws Exception {
    OfxBuilder.init(this)
      .addCardAccount("1111", -100, "2009/12/07")
      .addTransaction("2009/11/30", -60, "Auchan")
      .addBankAccount("", -1, "1234", 1000, "2009/11/30")
      .loadDeferredCard("Card n. 1111", 28);

    timeline.selectAll();
    views.selectData();
    transactions.initAmountContent()
      .add("30/11/2009", "AUCHAN", -60.00, "To categorize", -100.00, 940, "Card n. 1111")
      .check();
  }

  public void testFirstQifImport() throws Exception {
    String mainAccount = QifBuilder.init(this)
      //.addBankAccount("", -1, "1234", 1000, "2009/11/30")
      .addTransaction("2009/11/28", -30, "Prelevement novembre")
      .addTransaction("2009/10/28", -35 - 15 /* -15 : transaction precedente non importé */, "Prelevement octobre")
      .save();
    operations.importQifFile(mainAccount, "Autre", 1000.);

    String deferredAccount = QifBuilder.init(this)
      .addTransaction("2009/11/30", -60, "Auchan")
      .addTransaction("2009/11/29", -40, "Auchan")
      .addTransaction("2009/11/25", -10, "Auchan")
      .addTransaction("2009/10/29", -20, "Auchan")
      .addTransaction("2009/09/29", -35, "Auchan")
      .addTransaction("2009/09/14", -15, "Auchan")
      .save();
    operations.importQifFileWithDeferred(deferredAccount, "Autre", -100., 28);

    timeline.selectAll();

    views.selectData();
    transactions.initAmountContent()
      .add("30/11/2009", "AUCHAN", -60.00, "To categorize", -100.00, 900.00, "card 1111")
      .add("29/11/2009", "AUCHAN", -40.00, "To categorize", -40.00, 960.00, "card 1111")
      .add("28/11/2009", "PRELEVEMENT NOVEMBRE", -30.00, "To categorize", 1000.00, 1000.00, "Main account")
      .add("25/11/2009", "AUCHAN", -10.00, "To categorize", -30.00, 1000.00, "card 1111")
      .add("29/10/2009", "AUCHAN", -20.00, "To categorize", -20.00, 1000.00, "card 1111")
      .add("28/10/2009", "PRELEVEMENT OCTOBRE", -50.00, "To categorize", 1030.00, 1030.00, "Main account")
      .add("29/09/2009", "AUCHAN", -35.00, "To categorize", -35.00, 1030.00, "card 1111")
      .add("14/09/2009", "AUCHAN", -15.00, "To categorize", -15.00, 1080.00, "card 1111")
      .check();
    views.selectCategorization();
    categorization.selectTransaction("Prelevement novembre")
      .selectOther()
      .selectSeries("Card 1111");
    categorization.selectTransaction("Prelevement octobre")
      .selectOther()
      .selectSeries("Card 1111");
    views.selectData();
    transactions.initAmountContent()
      .add("30/11/2009", "AUCHAN", -60.00, "To categorize", -100.00, 900.00, "card 1111")
      .add("29/11/2009", "AUCHAN", -40.00, "To categorize", -40.00, 960.00, "card 1111")
      .add("28/11/2009", "PRELEVEMENT NOVEMBRE", -30.00, "card 1111", 1000.00, 1000.00, "Main account")
      .add("25/11/2009", "AUCHAN", -10.00, "To categorize", -30.00, 1000.00, "card 1111")
      .add("29/10/2009", "AUCHAN", -20.00, "To categorize", -20.00, 1000.00, "card 1111")
      .add("28/10/2009", "PRELEVEMENT OCTOBRE", -50.00, "card 1111", 1030.00, 1030.00, "Main account")
      .add("29/09/2009", "AUCHAN", -35.00, "To categorize", -50.00, 1030.00, "card 1111")
      .add("14/09/2009", "AUCHAN", -15.00, "To categorize", -15.00, 1080.00, "card 1111")
      .check();
  }

  public void testImportQifWithOperationMixInMonthWithoutVirement() throws Exception {
    String mainAccount = QifBuilder.init(this)
      .addTransaction("2009/11/28", -30, "Prelevement novembre")
      .addTransaction("2009/10/28", -35 - 15 /* -15 : transaction precedente non importé */, "Prelevement octobre")
      .save();
    operations.importQifFile(mainAccount, "Autre", 1000.);

    String deferredAccount = QifBuilder.init(this)
      .addTransaction("2009/11/30", -60, "Auchan")
      .addTransaction("2009/11/29", -40, "Auchan")
      .addTransaction("2009/11/25", -10, "Auchan")
      .addTransaction("2009/10/29", -20, "Auchan")
      .addTransaction("2009/09/14", -35, "Auchan")
      .save();
    operations.importQifFileWithDeferred(deferredAccount, "Autre", -100., 28);
    timeline.selectAll();
    transactions.initAmountContent()
      .add("30/11/2009", "AUCHAN", -60.00, "To categorize", -100.00, 900.00, "card 1111")
      .add("29/11/2009", "AUCHAN", -40.00, "To categorize", -40.00, 960.00, "card 1111")
      .add("28/11/2009", "PRELEVEMENT NOVEMBRE", -30.00, "To categorize", 1000.00, 1000.00, "Main account")
      .add("25/11/2009", "AUCHAN", -10.00, "To categorize", -30.00, 1000.00, "card 1111")
      .add("29/10/2009", "AUCHAN", -20.00, "To categorize", -20.00, 1000.00, "card 1111")
      .add("28/10/2009", "PRELEVEMENT OCTOBRE", -50.00, "To categorize", 1030.00, 1030.00, "Main account")
      .add("14/09/2009", "AUCHAN", -35.00, "To categorize", -35.00, 1080.00, "card 1111")
      .check();

    String newDeferredAccount = QifBuilder.init(this)
      .addTransaction("2009/12/02", -70, "Auchan")
      .addTransaction("2009/09/12", -15, "Auchan")
      .save();
    operations.importQifFile(newDeferredAccount, "Autre", "card 1111");
    timeline.selectAll();
    transactions.initAmountContent()
      .add("02/12/2009", "AUCHAN", -70.00, "To categorize", -170.00, 830.00, "card 1111")
      .add("30/11/2009", "AUCHAN", -60.00, "To categorize", -100.00, 900.00, "card 1111")
      .add("29/11/2009", "AUCHAN", -40.00, "To categorize", -40.00, 960.00, "card 1111")
      .add("28/11/2009", "PRELEVEMENT NOVEMBRE", -30.00, "To categorize", 1000.00, 1000.00, "Main account")
      .add("25/11/2009", "AUCHAN", -10.00, "To categorize", -30.00, 1000.00, "card 1111")
      .add("29/10/2009", "AUCHAN", -20.00, "To categorize", -20.00, 1000.00, "card 1111")
      .add("28/10/2009", "PRELEVEMENT OCTOBRE", -50.00, "To categorize", 1030.00, 1030.00, "Main account")
      .add("14/09/2009", "AUCHAN", -35.00, "To categorize", -50.00, 1080.00, "card 1111")
      .add("12/09/2009", "AUCHAN", -15.00, "To categorize", -15.00, 1080.00, "card 1111")
      .check();
  }

  public void testShiftNotAllowedOnDeferredCardOperation() throws Exception {
    OfxBuilder.init(this)
      .addCardAccount("1111", -100, "2009/12/07")
      .addTransaction("2009/11/30", -60, "Auchan")
      .addBankAccount("", -1, "1234", 1000, "2009/11/30")
      .addTransaction("2009/11/28", -30, "Prelevement novembre")
      .loadDeferredCard("Card n. 1111", 28);
    views.selectCategorization();
    categorization.selectTransaction("Auchan");
    transactionDetails.checkShiftDisabled();
  }

  public void testCheckDeferredAcountPositionInMainInDifferentMonth() throws Exception {
    fail("todo");
  }

  public void testDeferredWithPlannedAndOverburn() throws Exception {
    OfxBuilder.init(this)
      .addCardAccount("1111", -100, "2009/12/07")
      .addTransaction("2009/11/30", -60, "Auchan")
      .addTransaction("2009/11/29", -40, "Auchan")
      .addTransaction("2009/11/25", -10, "Auchan")
      .addTransaction("2009/10/29", -20, "Auchan")
      .addTransaction("2009/09/14", -35, "Auchan")
      .addBankAccount("", -1, "1234", 1000, "2009/11/30")
      .addTransaction("2009/11/28", -30, "Prelevement novembre")
      .addTransaction("2009/10/28", 0, "Prelevement octobre")
      .addTransaction("2009/09/26", -35 - 15 /* -15 : transaction precedente non importé */, "Prelevement aout")
      .loadDeferredCard("Card n. 1111", 28);

    views.selectCategorization();
    categorization
      .setNewEnvelope("Auchan", "course");
    timeline.selectAll();
    views.selectData();
    transactions.initAmountContent()
      .add("14/02/2010", "Planned: course", -30.00, "course", 840.00, "Main accounts")
      .add("14/01/2010", "Planned: course", -30.00, "course", 870.00, "Main accounts")
      .add("30/11/2009", "AUCHAN", -60.00, "course", -100.00, 900.00, "Card n. 1111")
      .add("29/11/2009", "AUCHAN", -40.00, "course", -40.00, 960.00, "Card n. 1111")
      .add("28/11/2009", "PRELEVEMENT NOVEMBRE", -30.00, "To categorize", 1000.00, 1000.00, "Account n. 1234")
      .add("25/11/2009", "AUCHAN", -10.00, "course", -30.00, 1000.00, "Card n. 1111")
      .add("29/10/2009", "AUCHAN", -20.00, "course", -20.00, 1000.00, "Card n. 1111")
      .add("28/10/2009", "PRELEVEMENT OCTOBRE", 0.00, "To categorize", 1030.00, 1030.00, "Account n. 1234")
      .add("26/09/2009", "PRELEVEMENT AOUT", -50.00, "To categorize", 1030.00, 1030.00, "Account n. 1234")
      .add("14/09/2009", "AUCHAN", -35.00, "course", -35.00, 1030.00, "Card n. 1111")
      .check();
  }

  public void testWithPlanned() throws Exception {
    OfxBuilder.init(this)
      .addCardAccount("1111", -110, "2009/12/07")
      .addTransaction("2009/12/07", -10, "Auchan")
      .addTransaction("2009/11/30", -60, "Auchan")
      .addTransaction("2009/11/29", -40, "Auchan")
      .addTransaction("2009/11/25", -80, "Auchan")
      .addTransaction("2009/10/29", -120, "Auchan")
      .addTransaction("2009/09/01", -130, "Auchan")
      .addBankAccount("", -1, "1234", 1000, "2009/11/30")
      .addTransaction("2009/11/28", -30, "Prelevement novembre")
      .loadDeferredCard("Card n. 1111", 28);

    views.selectCategorization();
    categorization
      .setNewEnvelope("Auchan", "course");
    timeline.selectAll();
    views.selectData();
    transactions.
      initAmountContent()
      .add("01/02/2010", "Planned: course", -200.00, "course", 400.00, "Main accounts")
      .add("01/01/2010", "Planned: course", -200.00, "course", 600.00, "Main accounts")
      .add("07/12/2009", "Planned: course", -90.00, "course", 910.00, "Main accounts")
      .add("07/12/2009", "AUCHAN", -10.00, "course", -110.00, 800.00, "Card n. 1111")
      .add("30/11/2009", "AUCHAN", -60.00, "course", -100.00, 810.00, "Card n. 1111")
      .add("29/11/2009", "AUCHAN", -40.00, "course", -40.00, 870.00, "Card n. 1111")
      .add("28/11/2009", "PRELEVEMENT NOVEMBRE", -30.00, "To categorize", 1000.00, 1000.00, "Account n. 1234")
      .add("25/11/2009", "AUCHAN", -80.00, "course", -200.00, 1000.00, "Card n. 1111")
      .add("29/10/2009", "AUCHAN", -120.00, "course", -120.00, 1000.00, "Card n. 1111")
      .add("01/09/2009", "AUCHAN", -130.00, "course", -130.00, 1000.00, "Card n. 1111")
      .check();


  }
}
