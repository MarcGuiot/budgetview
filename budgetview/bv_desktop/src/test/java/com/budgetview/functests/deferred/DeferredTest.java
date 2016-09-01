package com.budgetview.functests.deferred;

import com.budgetview.functests.checkers.DeferredCardCategorizationChecker;
import com.budgetview.functests.checkers.ImportDialogPreviewChecker;
import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;
import com.budgetview.functests.utils.QifBuilder;
import com.budgetview.model.TransactionType;
import org.junit.Test;

public class DeferredTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentDate("2009/12/09");
    super.setUp();
    operations.openPreferences().setFutureMonthsCount(2).validate();
  }

  @Test
  public void testCategorization() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder.init(this)
      .addCardAccount("1111", 100, "2008/06/30")
      .addTransaction("2008/06/27", -550, "Auchan")
      .addBankAccount("1234", 1000, "2008/06/30")
      .addTransaction("2008/06/28", -550, "Prelevement")
      .loadDeferredCard("Card n. 1111", "Account n. 1234");

    mainAccounts.edit("Card n. 1111").setDeferred(27, 28, 0).validate();


    // si on selection les trois mois ca plante en NPE.
    views.selectCategorization();
    categorization.selectTransaction("Auchan")
      .selectOther()
      .selectDeferred()
      .checkHidden()
      .toggle()
      .checkMessage("Select a transfer operation from your main account to a card account.")
      .checkContainsNoSeries()
      .checkEditSeriesButtonNotVisible()
      .toggle()
      .checkHidden();

    categorization.selectTransaction("Prelevement")
      .selectOther()
      .selectDeferred()
      .checkShown()
      .checkMessage("Assign the monthly transfer operation to the corresponding card account:")
      .checkActiveSeries("Card n. 1111")
      .selectSeries("Card n. 1111");

    views.selectData();
    timeline.selectAll();
    transactions
      .initAmountContent()
      .add("28/06/2008", "PRELEVEMENT", -550.00, "Card n. 1111", -550., "Card n. 1111")
      .add("27/06/2008", "AUCHAN", -50.00, "To categorize", 1000.00, 1000., "Account n. 1234") //=> on avait 1550
      .check();

    transactions.deleteAndUpdatePosition("PRELEVEMENT");

    transactions
      .initAmountContent()
      .add("27/06/2008", "AUCHAN", -50.00, "To categorize", -50.00, 1500.00, "Account n. 1234")
      .check();
  }

  @Test
  public void testCategorizationMessageAllowsToImportTheCardAccount() throws Exception {
    QifBuilder.init(this)
      .addTransaction("2008/06/28", -550, "Prelevement carte")
      .load(0.00);

    views.selectCategorization();
    DeferredCardCategorizationChecker cardCategorization = categorization.selectTransaction("Prelevement carte")
      .selectOther()
      .selectDeferred()
      .checkContainsNoSeries()
      .checkEditSeriesButtonNotVisible()
      .checkMessage("You must first import the corresponding card account operations.");

    String cardFile = QifBuilder.init(this)
      .addTransaction("2009/11/30", -60, "Auchan")
      .addTransaction("2009/11/29", -40, "Auchan")
      .addTransaction("2009/11/25", -10, "Auchan")
      .addTransaction("2009/10/29", -20, "Auchan")
      .addTransaction("2009/09/14", -35, "Auchan")
      .save();

    ImportDialogPreviewChecker preview = cardCategorization.importAccount()
      .setFilePath(cardFile)
      .importFileAndPreview();
    preview.addNewAccount()
      .setAccountName("Card account")
      .selectBank("CIC")
      .setDeferredAccount(25, 28, 0)
      .setPosition(-1000)
      .importAccountAndComplete();

    views.selectCategorization();
    cardCategorization
      .checkMessage("Assign the monthly transfer operation to the corresponding card account:")
      .selectSeries("Card account");

    categorization.selectTransaction("Prelevement carte")
      .checkOtherSeriesIsSelected("Card account");
  }

  @Test
  public void testFirstOfxImport() throws Exception {
    OfxBuilder.init(this)
      .addCardAccount("1111", -100, "2009/12/07")
      .addTransaction("2009/11/30", -60, "Auchan")
      .addTransaction("2009/11/29", -40, "Auchan")
      .addTransaction("2009/11/25", -10, "Auchan")
      .addTransaction("2009/10/29", -20, "Auchan")
      .addTransaction("2009/09/14", -35, "Auchan")
      .addBankAccount("1234", 1000, "2009/11/30")
      .addTransaction("2009/11/28", -30, "Prelevement novembre")
      .addTransaction("2009/10/28", 0, "Prelevement octobre")
      .addTransaction("2009/09/26", -35 - 15 /* -15 : transaction precedente non importé */, "Prelevement aout")
      .loadDeferredCard("Card n. 1111", "Account n. 1234");

    timeline.selectAll();

    categorization.setDeferred("Prelevement novembre", "Card n. 1111");
    categorization.setDeferred("Prelevement octobre", "Card n. 1111");
    categorization.setDeferred("Prelevement aout", "Card n. 1111");

    transactions.initAmountContent()
      .add("30/11/2009", "AUCHAN", -60.00, "To categorize", 900.00, 900.00, "Card n. 1111")
      .add("29/11/2009", "AUCHAN", -40.00, "To categorize", 960.00, 960.00, "Card n. 1111")
      .add("28/11/2009", "PRELEVEMENT NOVEMBRE", -30.00, "Card n. 1111", 1000.00, 1000.00, "Account n. 1234")
      .add("25/11/2009", "AUCHAN", -10.00, "To categorize", 1030.00, 1030.00, "Card n. 1111")
      .add("29/10/2009", "AUCHAN", -20.00, "To categorize", 1030.00, 1030.00, "Card n. 1111")
      .add("28/10/2009", "PRELEVEMENT OCTOBRE", 0.00, "Card n. 1111", 1030.00, 1030.00, "Account n. 1234")
      .add("26/09/2009", "PRELEVEMENT AOUT", -50.00, "Card n. 1111", 1030.00, 1030.00, "Account n. 1234")
      .add("14/09/2009", "AUCHAN", -35.00, "To categorize", 1080.00, 1080.00, "Card n. 1111")
      .check();

    categorization.selectTransactions("Auchan")
      .selectVariable()
      .createSeries()
      .setName("Course")
      .selectAllMonths()
      .setAmount("250")
      .validate();

    transactions.initAmountContent()
      .add("30/11/2009", "AUCHAN", -60.00, "Course", 530.00, 530.00, "Card n. 1111")
      .add("29/11/2009", "AUCHAN", -40.00, "Course", 590.00, 590.00, "Card n. 1111")
      .add("28/11/2009", "PRELEVEMENT NOVEMBRE", -30.00, "Card n. 1111", 1000.00, 1000.00, "Account n. 1234")
      .add("25/11/2009", "AUCHAN", -10.00, "Course", 1030.00, 1030.00, "Card n. 1111")
      .add("29/10/2009", "AUCHAN", -20.00, "Course", 1030.00, 1030.00, "Card n. 1111")
      .add("28/10/2009", "PRELEVEMENT OCTOBRE", 0.00, "Card n. 1111", 1030.00, 1030.00, "Account n. 1234")
      .add("26/09/2009", "PRELEVEMENT AOUT", -50.00, "Card n. 1111", 1030.00, 1030.00, "Account n. 1234")
      .add("14/09/2009", "AUCHAN", -35.00, "Course", 1080.00, 1080.00, "Card n. 1111")
      .check();

    timeline.selectMonth("2009/12");
    budgetView.variable.checkSeries("Course", -100, -250);

    timeline.selectMonth("2009/11");
    budgetView.variable.checkSeries("Course", -30, -250);
  }

  @Test
  public void testFirstQifImport() throws Exception {
    String mainAccount = QifBuilder.init(this)
      .addTransaction("2009/11/28", -30, "Prelevement novembre")
      .addTransaction("2009/10/28", -35 - 15 /* -15 : transaction precedente non importée */, "Prelevement octobre")
      .addTransaction("2009/09/28", -35, "Prelevement septembre")
      .save();
    operations.importQifFile(mainAccount, "Other", 1000.00);

    String deferredAccount = QifBuilder.init(this)
      .addTransaction("2009/11/30", -60, "Auchan")
      .addTransaction("2009/11/29", -40, "Auchan")
      .addTransaction("2009/11/25", -10, "Auchan")
      .addTransaction("2009/10/29", -20, "Auchan")
      .addTransaction("2009/09/29", -35, "Auchan")
      .addTransaction("2009/09/14", -15, "Auchan")
      .save();
    operations.importQifFileWithDeferred(deferredAccount, "Other", -100.00, "Main account");

    categorization.setNewVariable("Auchan", "Courses", -200.00);
    categorization.setDeferred("Prelevement novembre", "Card 1111");
    categorization.setDeferred("Prelevement octobre", "Card 1111");
    categorization.setDeferred("Prelevement septembre", "card 1111");

    timeline.selectAll();
    transactions.showPlannedTransactions()
      .initAmountContent()
      .add("11/02/2010", "Planned: Courses", -200.00, "Courses", 230.00, 230.00, "Main account")
      .add("11/01/2010", "Planned: Courses", -200.00, "Courses", 430.00, 430.00, "Main account")
      .add("11/12/2009", "Planned: Courses", -100.00, "Courses", 730.00, 730.00, "Main account")
      .add("30/11/2009", "Planned: Courses", -170.00, "Courses", 830.00, 830.00, "Main account")
      .add("30/11/2009", "AUCHAN", -60.00, "Courses", 630.00, 630.00, "card 1111")
      .add("29/11/2009", "AUCHAN", -40.00, "Courses", 690.00, 690.00, "card 1111")
      .add("28/11/2009", "PRELEVEMENT NOVEMBRE", -30.00, "card 1111", 1000.00, 1000.00, "Main account")
      .add("25/11/2009", "AUCHAN", -10.00, "Courses", 1030.00, 1030.00, "card 1111")
      .add("29/10/2009", "AUCHAN", -20.00, "Courses", 1030.00, 1030.00, "card 1111")
      .add("28/10/2009", "PRELEVEMENT OCTOBRE", -50.00, "card 1111", 1030.00, 1030.00, "Main account")
      .add("29/09/2009", "AUCHAN", -35.00, "Courses", 1080.00, 1080.00, "card 1111")
      .add("28/09/2009", "PRELEVEMENT SEPTEMBRE", -35.00, "card 1111", 1080.00, 1080.00, "Main account")
      .add("14/09/2009", "AUCHAN", -15.00, "Courses", 1115.00, 1115.00, "card 1111")
      .check();

  }

  @Test
  public void testImportQifWithOperationMixInMonthWithoutTransfer() throws Exception {
    String mainAccount = QifBuilder.init(this)
      .addTransaction("2009/11/28", -30, "Prelevement novembre")
      .addTransaction("2009/10/28", -35 - 15 /* -15 : transaction precedente non importé */, "Prelevement octobre")
      .addTransaction("2009/09/28", -35, "Prelevement septembre")
      .save();
    operations.importQifFile(mainAccount, "Other", 1000.00);

    String deferredAccount = QifBuilder.init(this)
      .addTransaction("2009/11/30", -60, "Auchan")
      .addTransaction("2009/11/29", -40, "Auchan")
      .addTransaction("2009/11/25", -10, "Auchan")
      .addTransaction("2009/10/29", -20, "Auchan")
      .addTransaction("2009/09/14", -35, "Auchan")
      .save();
    operations.importQifFileWithDeferred(deferredAccount, "Other", -100.00, "Main account");

    views.selectCategorization();
    categorization.setDeferred("Prelevement novembre", "card 1111");
    categorization.setDeferred("Prelevement octobre", "card 1111");
    categorization.setDeferred("Prelevement septembre", "card 1111");

    timeline.selectAll();
    views.selectData();
    transactions.initAmountContent()
      .add("30/11/2009", "AUCHAN", -60.00, "To categorize", 900.00, 900.00, "card 1111")
      .add("29/11/2009", "AUCHAN", -40.00, "To categorize", 960.00, 960.00, "card 1111")
      .add("28/11/2009", "PRELEVEMENT NOVEMBRE", -30.00, "card 1111", 1000.00, 1000.00, "Main account")
      .add("25/11/2009", "AUCHAN", -10.00, "To categorize", 1030.00, 1030.00, "card 1111")
      .add("29/10/2009", "AUCHAN", -20.00, "To categorize", 1030.00, 1030.00, "card 1111")
      .add("28/10/2009", "PRELEVEMENT OCTOBRE", -50.00, "card 1111", 1030.00, 1030.00, "Main account")
      .add("28/09/2009", "PRELEVEMENT SEPTEMBRE", -35.00, "card 1111", 1080.00, 1080.00, "Main account")
      .add("14/09/2009", "AUCHAN", -35.00, "To categorize", 1115.00, 1115.00, "card 1111")
      .check();

    String newDeferredAccount = QifBuilder.init(this)
      .addTransaction("2009/12/02", -70, "Auchan")
      .addTransaction("2009/09/28", -15, "Auchan")
      .save();
    operations.importFile(newDeferredAccount, "card 1111");

    timeline.selectAll();
    views.selectData();
    transactions.initAmountContent()
      .add("02/12/2009", "AUCHAN", -70.00, "To categorize", 830.00, 830.00, "card 1111")
      .add("30/11/2009", "AUCHAN", -60.00, "To categorize", 900.00, 900.00, "card 1111")
      .add("29/11/2009", "AUCHAN", -40.00, "To categorize", 960.00, 960.00, "card 1111")
      .add("28/11/2009", "PRELEVEMENT NOVEMBRE", -30.00, "card 1111", 1000.00, 1000.00, "Main account")
      .add("25/11/2009", "AUCHAN", -10.00, "To categorize", 1030.00, 1030.00, "card 1111")
      .add("29/10/2009", "AUCHAN", -20.00, "To categorize", 1030.00, 1030.00, "card 1111")
      .add("28/10/2009", "PRELEVEMENT OCTOBRE", -50.00, "card 1111", 1030.00, 1030.00, "Main account")
      .add("28/09/2009", "AUCHAN", -15.00, "To categorize", 1080.00, 1080.00, "card 1111")
      .add("28/09/2009", "PRELEVEMENT SEPTEMBRE", -35.00, "card 1111", 1080.00, 1080.00, "Main account")
      .add("14/09/2009", "AUCHAN", -35.00, "To categorize", 1115.00, 1115.00, "card 1111")
      .check();

    String filePath = QifBuilder.init(this)
      .addTransaction("2009/12/04", -20, "cheque 1")
      .save();
    operations.importFile(filePath, "Main account");

    views.selectData();
    transactions.initAmountContent()
      .add("04/12/2009", "CHEQUE N°1", -20.00, "To categorize", 980.00, 980.00, "Main account")
      .add("02/12/2009", "AUCHAN", -70.00, "To categorize", 810.00, 810.00, "card 1111")
      .add("30/11/2009", "AUCHAN", -60.00, "To categorize", 880.00, 880.00, "card 1111")
      .add("29/11/2009", "AUCHAN", -40.00, "To categorize", 940.00, 940.00, "card 1111")
      .check();
  }

  @Test
  public void testShiftNotAllowedOnDeferredCardOperation() throws Exception {
    OfxBuilder.init(this)
      .addCardAccount("1111", -100, "2009/12/07")
      .addTransaction("2009/11/30", -60, "Auchan")
      .addBankAccount("1234", 1000, "2009/11/30")
      .addTransaction("2009/11/28", -30, "Prelevement novembre")
      .loadDeferredCard("Card n. 1111", "Account n. 1234");
    views.selectCategorization();
    categorization.selectTransaction("Auchan");
    transactionDetails.checkShiftDisabled();
  }

  @Test
  public void testCheckShowTransactionAtBudgetMonth() throws Exception {
    OfxBuilder.init(this)
      .addCardAccount("1111", -100, "2009/12/07")
      .addTransaction("2009/11/30", -60, "Auchan")
      .addTransaction("2009/11/29", -40, "Auchan")
      .addTransaction("2009/11/25", -10, "Auchan")
      .addTransaction("2009/10/29", -20, "Auchan")
      .addTransaction("2009/09/14", -35, "Auchan")
      .addBankAccount("1234", 1000, "2009/11/30")
      .addTransaction("2009/11/28", -30, "Prelevement novembre")
      .addTransaction("2009/10/28", 0, "Prelevement octobre")
      .addTransaction("2009/09/26", -35 - 15, "Prelevement septembre")
      .loadDeferredCard("Card n. 1111", "Account n. 1234");

    views.selectCategorization();
    categorization
      .setNewVariable("Auchan", "course");
    categorization.setDeferred("Prelevement novembre", "1111")
      .setDeferred("Prelevement octobre", "1111")
      .setDeferred("Prelevement septembre", "1111");
    views.selectData();
    timeline.selectMonth("2009/12");
    transactions.initContent()
      .add("30/11/2009", TransactionType.CREDIT_CARD, "AUCHAN", "", -60.00, "course")
      .add("29/11/2009", TransactionType.CREDIT_CARD, "AUCHAN", "", -40.00, "course")
      .check();

    timeline.selectMonth("2009/11");
    transactions.initContent()
      .add("28/11/2009", TransactionType.PRELEVEMENT, "PRELEVEMENT NOVEMBRE", "", -30.00, "Card n. 1111")
      .add("25/11/2009", TransactionType.CREDIT_CARD, "AUCHAN", "", -10.00, "course")
      .add("29/10/2009", TransactionType.CREDIT_CARD, "AUCHAN", "", -20.00, "course")
      .check();

    timeline.selectMonth("2009/10");
    transactions.initContent()
      .add("28/10/2009", TransactionType.VIREMENT, "PRELEVEMENT OCTOBRE", "", 0.00, "Card n. 1111")
      .check();
  }

  @Test
  public void testDeferredWithPlannedAndOverrun() throws Exception {
    OfxBuilder.init(this)
      .addCardAccount("1111", -100, "2009/12/07")
      .addTransaction("2009/11/30", -60, "Auchan")
      .addTransaction("2009/11/29", -40, "Auchan")
      .addTransaction("2009/11/25", -10, "Auchan")
      .addTransaction("2009/10/29", -20, "Auchan")
      .addTransaction("2009/09/14", -35, "Auchan")
      .addBankAccount("1234", 1000, "2009/11/30")
      .addTransaction("2009/11/28", -30, "Prelevement novembre")
      .addTransaction("2009/10/28", 0, "Prelevement octobre")
      .addTransaction("2009/09/26", -35 - 15 /* -15 : transaction precedente non importé */, "Prelevement aout")
      .loadDeferredCard("Card n. 1111", "Account n. 1234");

    categorization.setNewVariable("Auchan", "course", -30.00);
    categorization
      .setDeferred("Prelevement novembre", "1111")
      .setDeferred("Prelevement octobre", "1111")
      .setDeferred("Prelevement aout", "1111");

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("27/02/2010", "Planned: course", -30.00, "course", 840.00, 840.00, "Account n. 1234")
      .add("27/01/2010", "Planned: course", -30.00, "course", 870.00, 870.00, "Account n. 1234")
      .add("30/11/2009", "AUCHAN", -60.00, "course", 900.00, 900.00, "Card n. 1111")
      .add("29/11/2009", "AUCHAN", -40.00, "course", 960.00, 960.00, "Card n. 1111")
      .add("28/11/2009", "PRELEVEMENT NOVEMBRE", -30.00, "Card n. 1111", 1000.00, 1000.00, "Account n. 1234")
      .add("25/11/2009", "AUCHAN", -10.00, "course", 1030.00, 1030.00, "Card n. 1111")
      .add("29/10/2009", "AUCHAN", -20.00, "course", 1030.00, 1030.00, "Card n. 1111")
      .add("28/10/2009", "PRELEVEMENT OCTOBRE", 0.00, "Card n. 1111", 1030.00, 1030.00, "Account n. 1234")
      .add("26/09/2009", "PRELEVEMENT AOUT", -50.00, "Card n. 1111", 1030.00, 1030.00, "Account n. 1234")
      .add("14/09/2009", "AUCHAN", -35.00, "course", 1080.00, 1080.00, "Card n. 1111")
      .check();
  }

  @Test
  public void testWithPlanned() throws Exception {
    OfxBuilder.init(this)
      .addCardAccount("1111", -110, "2009/12/07")
      .addTransaction("2009/12/07", -10, "Auchan")
      .addTransaction("2009/11/30", -60, "Auchan")
      .addTransaction("2009/11/29", -40, "Auchan")
      .addTransaction("2009/11/25", -80, "Auchan")
      .addTransaction("2009/10/29", -110, "Auchan")
      .addTransaction("2009/09/01", -130, "Auchan")
      .addBankAccount("1234", 1000, "2009/11/30")
      .addTransaction("2009/11/28", -190, "Prelevement novembre")
      .addTransaction("2009/09/28", -130, "Prelevement septembre")
      .loadDeferredCard("Card n. 1111", "Account n. 1234");

    categorization.setDeferred("Prelevement novembre", "1111");
    categorization.setDeferred("Prelevement septembre", "1111");

    categorization.setNewVariable("Auchan", "course", -200.0);

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("27/02/2010", "Planned: course", -200.00, "course", 400.00, 400.00, "Account n. 1234")
      .add("27/01/2010", "Planned: course", -200.00, "course", 600.00, 600.00, "Account n. 1234")
      .add("27/12/2009", "Planned: course", -90.00, "course", 910.00, 910.00, "Account n. 1234")
      .add("07/12/2009", "AUCHAN", -10.00, "course", 800.00, 800.00, "Card n. 1111")
      .add("30/11/2009", "AUCHAN", -60.00, "course", 810.00, 810.00, "Card n. 1111")
      .add("29/11/2009", "AUCHAN", -40.00, "course", 870.00, 870.00, "Card n. 1111")
      .add("28/11/2009", "PRELEVEMENT NOVEMBRE", -190.00, "Card n. 1111", 1000.00, 1000.00, "Account n. 1234")
      .add("25/11/2009", "AUCHAN", -80.00, "course", 1190.00, 1190.00, "Card n. 1111")
      .add("29/10/2009", "AUCHAN", -110.00, "course", 1190.00, 1190.00, "Card n. 1111")
      .add("28/09/2009", "PRELEVEMENT SEPTEMBRE", -130.00, "Card n. 1111", 1190.00, 1190.00, "Account n. 1234")
      .add("01/09/2009", "AUCHAN", -130.00, "course", 1320.00, 1320.00, "Card n. 1111")
      .check();
  }

  @Test
  public void testWithPlannedInCurrentMonth() throws Exception {
    OfxBuilder.init(this)
      .addCardAccount("1111", -110, "2009/12/07")
      .addTransaction("2009/11/29", -40, "Auchan")
      .addTransaction("2009/11/25", -80, "Auchan")
      .addTransaction("2009/10/29", -110, "Auchan")
      .addTransaction("2009/09/01", -130, "Auchan")
      .addBankAccount("1234", 1000, "2009/11/30")
      .addTransaction("2009/11/27", -190, "Prelevement novembre")
      .addTransaction("2009/09/27", -130, "Prelevement septembre")
      .loadDeferredCard("Card n. 1111", "Account n. 1234");

    categorization.setDeferred("Prelevement novembre", "1111");
    categorization.setDeferred("Prelevement septembre", "1111");

    views.selectCategorization();
    categorization
      .setNewVariable("Auchan", "course", -200.0);
    timeline.selectAll();
    views.selectData();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("27/02/2010", "Planned: course", -200.00, "course", 390.00, 390.00, "Account n. 1234")
      .add("27/01/2010", "Planned: course", -200.00, "course", 590.00, 590.00, "Account n. 1234")
      .add("27/12/2009", "Planned: course", -160.00, "course", 830.00, 830.00, "Account n. 1234")
      .add("29/11/2009", "Planned: course", -10.00, "course", 990.00, 990.00, "Account n. 1234")
      .add("29/11/2009", "AUCHAN", -40.00, "course", 790.00, 790.00, "Card n. 1111")
      .add("27/11/2009", "PRELEVEMENT NOVEMBRE", -190.00, "Card n. 1111", 1000.00, 1000.00, "Account n. 1234")
      .add("25/11/2009", "AUCHAN", -80.00, "course", 1190.00, 1190.00, "Card n. 1111")
      .add("29/10/2009", "AUCHAN", -110.00, "course", 1190.00, 1190.00, "Card n. 1111")
      .add("27/09/2009", "PRELEVEMENT SEPTEMBRE", -130.00, "Card n. 1111", 1190.00, 1190.00, "Account n. 1234")
      .add("01/09/2009", "AUCHAN", -130.00, "course", 1320.00, 1320.00, "Card n. 1111")
      .check();
  }

  @Test
  public void testChangeAccountType() throws Exception {
    OfxBuilder.init(this)
      .addCardAccount("1111", -110, "2009/12/07")
      .addTransaction("2009/12/07", -10, "Auchan")
      .addBankAccount("1234", 1000, "2009/11/30")
      .addTransaction("2009/11/28", -30, "Prelevement novembre")
      .loadDeferredCard("Card n. 1111", "Account n. 1234");

    views.selectCategorization();
    categorization
      .selectTransaction("Prelevement novembre")
      .selectOther()
      .selectDeferred()
      .selectSeries("Card n. 1111");

    views.selectHome();
    mainAccounts.edit("Card n. 1111")
      .setAsMain()
      .validate();
    views.selectData();
    transactions.initContent()
      .add("07/12/2009", TransactionType.CREDIT_CARD, "AUCHAN", "", -10.00)
      .check();
  }

  @Test
  public void testChangeDayFrom31TO28() throws Exception {

    OfxBuilder.init(this)
      .addCardAccount("1111", -110, "2009/12/07")
      .addTransaction("2009/12/07", -10, "Auchan")
      .addTransaction("2009/11/30", -60, "Auchan")
      .addTransaction("2009/11/29", -40, "Auchan")
      .addTransaction("2009/11/25", -80, "Auchan")
      .addTransaction("2009/10/31", -120, "Auchan")
      .addTransaction("2009/09/30", -130, "Auchan")
      .addBankAccount("1234", 990, "2009/12/07")
      .addTransaction("2009/12/04", -10, "cheque 5")
      .addTransaction("2009/11/28", -200, "Prelevement novembre")
      .addTransaction("2009/10/30", -10, "cheque 4")
      .addTransaction("2009/10/28", -130, "Prelevement octobre")
      .addTransaction("2009/10/27", -10, "cheque 3")
      .addTransaction("2009/09/30", -10, "cheque 2")
      .addTransaction("2009/09/28", -130, "Prelevement septembre")
      .addTransaction("2009/09/28", -10, "cheque 1")
      .loadDeferredCard("Card n. 1111", "Account n. 1234");

    views.selectBudget();
    budgetView.variable.createSeries()
      .setName("Mc Do")
      .setTargetAccount("Account n. 1234")
      .selectAllMonths()
      .setAmount("100")
      .validate();

    views.selectCategorization();
    categorization.setDeferred("Prelevement octobre", "Card n. 1111");
    categorization.setDeferred("Prelevement novembre", "Card n. 1111");
    categorization.setDeferred("Prelevement septembre", "Card n. 1111");

    timeline.selectMonth("2009/09");
    views.selectData();
    transactions.showPlannedTransactions();

    transactions.initAmountContent()
      .add("30/09/2009", "CHEQUE N°2", -10.00, "To categorize", 1350.00, 1350.00, "Account n. 1234")
      .add("28/09/2009", "CHEQUE N°1", -10.00, "To categorize", 1360.00, 1360.00, "Account n. 1234")
      .add("28/09/2009", "PRELEVEMENT SEPTEMBRE", -130.00, "Card n. 1111", 1370.00, 1370.00, "Account n. 1234")
      .check();

    timeline.selectMonth("2009/11");
    transactions.initAmountContent()
      .add("28/11/2009", "PRELEVEMENT NOVEMBRE", -200.00, "Card n. 1111", 1000.00, 1000.00, "Account n. 1234")
      .add("25/11/2009", "AUCHAN", -80.00, "To categorize", 1200.00, 1200.00, "Card n. 1111")
      .add("31/10/2009", "AUCHAN", -120.00, "To categorize", 1200.00, 1200.00, "Card n. 1111")
      .check();

    timeline.selectMonth("2009/12");
    transactions.initAmountContent()
      .add("11/12/2009", "Planned: Mc Do", -100.00, "Mc Do", 890.00, 890.00, "Account n. 1234")
      .add("07/12/2009", "AUCHAN", -10.00, "To categorize", 780.00, 780.00, "Card n. 1111")
      .add("04/12/2009", "CHEQUE N°5", -10.00, "To categorize", 990.00, 990.00, "Account n. 1234")
      .add("30/11/2009", "AUCHAN", -60.00, "To categorize", 790.00, 790.00, "Card n. 1111")
      .add("29/11/2009", "AUCHAN", -40.00, "To categorize", 850.00, 850.00, "Card n. 1111")
      .check();
  }

  @Test
  public void testMonthChange() throws Exception {
    setCurrentDate("2011/08/30");
    setInMemory(false);
    restartApplication(true);
    setDeleteLocalPrevayler(false);
    operations.openPreferences().setFutureMonthsCount(2).validate();

    String m1 = QifBuilder.init(this)
      .addTransaction("2011/08/24", -12., "5")
      .save();
    operations.importQifFile(m1, SOCIETE_GENERALE, 100.);

    String d1 = QifBuilder.init(this)
      .addTransaction("2011/08/23", -12., "diffe 1")
      .addTransaction("2011/08/25", -12., "diffe 2")
      .addTransaction("2011/08/26", -12., "diffe 3")
      .addTransaction("2011/08/30", -12., "diffe 4")
      .save();
    operations.importQifFileWithDeferred(d1, SOCIETE_GENERALE, -48., "Main account");

    budgetView.income.createSeries()
      .setName("salary").setPropagationEnabled().setTargetAccount("Main account").setAmount("200").validate();

    budgetView.recurring.createSeries()
      .setName("EDF").setPropagationEnabled().setTargetAccount("Main account").setAmount("40").validate();

    budgetView.recurring.createSeries()
      .setName("courses").setPropagationEnabled().setTargetAccount("Main account").setAmount("200").validate();

    categorization.selectTransactions("DIFFE 1", "DIFFE 2", "DIFFE 3", "DIFFE 4")
      .selectRecurring().selectSeries("courses");

    timeline.selectAll();
    transactions.showPlannedTransactions();
    transactions.sortByBankDate()
      .initAmountContent()
      .add("15/10/2011", "Planned: courses", -200.00, "courses", 4.00, 4.00, "Main account")
      .add("15/10/2011", "Planned: EDF", -40.00, "EDF", 204.00, 204.00, "Main account")
      .add("15/10/2011", "Planned: salary", 200.00, "salary", 244.00, 244.00, "Main account")
      .add("30/08/2011", "DIFFE 4", -12.00, "courses", 44.00, 44.00, "card 1111")
      .add("26/08/2011", "DIFFE 3", -12.00, "courses", 56.00, 56.00, "card 1111")
      .add("15/09/2011", "Planned: courses", -176.00, "courses", 68.00, 68.00, "Main account")
      .add("15/09/2011", "Planned: EDF", -40.00, "EDF", 244.00, 244.00, "Main account")
      .add("15/09/2011", "Planned: salary", 200.00, "salary", 284.00, 284.00, "Main account")
      .add("30/08/2011", "Planned: courses", -176.00, "courses", 84.00, 84.00, "Main account")
      .add("30/08/2011", "Planned: EDF", -40.00, "EDF", 260.00, 260.00, "Main account")
      .add("30/08/2011", "Planned: salary", 200.00, "salary", 300.00, 300.00, "Main account")
      .add("25/08/2011", "DIFFE 2", -12.00, "courses", 100.00, 100.00, "card 1111")
      .add("23/08/2011", "DIFFE 1", -12.00, "courses", 100.00, 100.00, "card 1111")
      .add("24/08/2011", "5", -12.00, "To categorize", 100.00, 100.00, "Main account")
      .check();

    setCurrentDate("2011/08/31");
    restartApplication();

    timeline.selectAll();
    transactions.showPlannedTransactions();
    transactions.sortByBankDate()
      .initAmountContent()
      .add("15/10/2011", "Planned: courses", -200.00, "courses", 4.00, 4.00, "Main account")
      .add("15/10/2011", "Planned: EDF", -40.00, "EDF", 204.00, 204.00, "Main account")
      .add("15/10/2011", "Planned: salary", 200.00, "salary", 244.00, 244.00, "Main account")
      .add("30/08/2011", "DIFFE 4", -12.00, "courses", 44.00, 44.00, "card 1111")
      .add("26/08/2011", "DIFFE 3", -12.00, "courses", 56.00, 56.00, "card 1111")
      .add("15/09/2011", "Planned: courses", -176.00, "courses", 68.00, 68.00, "Main account")
      .add("15/09/2011", "Planned: EDF", -40.00, "EDF", 244.00, 244.00, "Main account")
      .add("15/09/2011", "Planned: salary", 200.00, "salary", 284.00, 284.00, "Main account")
      .add("30/08/2011", "Planned: courses", -176.00, "courses", 84.00, 84.00, "Main account")
      .add("30/08/2011", "Planned: EDF", -40.00, "EDF", 260.00, 260.00, "Main account")
      .add("30/08/2011", "Planned: salary", 200.00, "salary", 300.00, 300.00, "Main account")
      .add("25/08/2011", "DIFFE 2", -12.00, "courses", 100.00, 100.00, "card 1111")
      .add("23/08/2011", "DIFFE 1", -12.00, "courses", 100.00, 100.00, "card 1111")
      .add("24/08/2011", "5", -12.00, "To categorize", 100.00, 100.00, "Main account")
      .check();

    String d2 = QifBuilder.init(this)
      .addTransaction("2011/08/31", -100., "diffe 6")
      .save();

    operations.importFile(d2, "card 1111");

    timeline.selectAll();

    transactions
      .initAmountContent()
      .add("15/10/2011", "Planned: courses", -200.00, "courses", 4.00, 4.00, "Main account")
      .add("15/10/2011", "Planned: EDF", -40.00, "EDF", 204.00, 204.00, "Main account")
      .add("15/10/2011", "Planned: salary", 200.00, "salary", 244.00, 244.00, "Main account")
      .add("31/08/2011", "DIFFE 6", -100.00, "courses", 44.00, 44.00, "card 1111")
      .add("30/08/2011", "DIFFE 4", -12.00, "courses", 144.00, 144.00, "card 1111")
      .add("26/08/2011", "DIFFE 3", -12.00, "courses", 156.00, 156.00, "card 1111")
      .add("15/09/2011", "Planned: courses", -76.00, "courses", 168.00, 168.00, "Main account")
      .add("15/09/2011", "Planned: EDF", -40.00, "EDF", 244.00, 244.00, "Main account")
      .add("15/09/2011", "Planned: salary", 200.00, "salary", 284.00, 284.00, "Main account")
      .add("31/08/2011", "Planned: courses", -176.00, "courses", 84.00, 84.00, "Main account")
      .add("31/08/2011", "Planned: EDF", -40.00, "EDF", 260.00, 260.00, "Main account")
      .add("31/08/2011", "Planned: salary", 200.00, "salary", 300.00, 300.00, "Main account")
      .add("25/08/2011", "DIFFE 2", -12.00, "courses", 100.00, 100.00, "card 1111")
      .add("23/08/2011", "DIFFE 1", -12.00, "courses", 100.00, 100.00, "card 1111")
      .add("24/08/2011", "5", -12.00, "To categorize", 100.00, 100.00, "Main account")
      .check();

    String m2 = QifBuilder.init(this)
      .addTransaction("2011/08/31", -24, "virement cdd")
      .save();

    operations.importFile(m2, "Main account");
    categorization.selectTransaction("virement cdd")
      .selectOther().selectDeferred().selectSeries("card 1111");

    timeline.selectAll();

    transactions.initAmountContent()
      .add("15/10/2011", "Planned: courses", -200.00, "courses", -20.00, -20.00, "Main account")
      .add("15/10/2011", "Planned: EDF", -40.00, "EDF", 180.00, 180.00, "Main account")
      .add("15/10/2011", "Planned: salary", 200.00, "salary", 220.00, 220.00, "Main account")
      .add("31/08/2011", "DIFFE 6", -100.00, "courses", 20.00, 20.00, "card 1111")
      .add("30/08/2011", "DIFFE 4", -12.00, "courses", 120.00, 120.00, "card 1111")
      .add("26/08/2011", "DIFFE 3", -12.00, "courses", 132.00, 132.00, "card 1111")
      .add("15/09/2011", "Planned: courses", -76.00, "courses", 144.00, 144.00, "Main account")
      .add("15/09/2011", "Planned: EDF", -40.00, "EDF", 220.00, 220.00, "Main account")
      .add("15/09/2011", "Planned: salary", 200.00, "salary", 260.00, 260.00, "Main account")
      .add("31/08/2011", "Planned: courses", -176.00, "courses", 60.00, 60.00, "Main account")
      .add("31/08/2011", "Planned: EDF", -40.00, "EDF", 236.00, 236.00, "Main account")
      .add("31/08/2011", "Planned: salary", 200.00, "salary", 276.00, 276.00, "Main account")
      .add("31/08/2011", "VIREMENT CDD", -24.00, "card 1111", 76.00, 76.00, "Main account")
      .add("25/08/2011", "DIFFE 2", -12.00, "courses", 100.00, 100.00, "card 1111")
      .add("23/08/2011", "DIFFE 1", -12.00, "courses", 100.00, 100.00, "card 1111")
      .add("24/08/2011", "5", -12.00, "To categorize", 100.00, 100.00, "Main account")
      .check();

    setCurrentDate("2011/09/03");
    restartApplication();

    String d3 = QifBuilder.init(this)
      .addTransaction("2011/09/03", -12., "diffe 7")
      .save();
    operations.importFile(d3, "card 1111");

    String m3 = QifBuilder.init(this)
      .addTransaction("2011/09/02", -53, "8")
      .save();
    operations.importFile(m3, "Main account");

    timeline.selectAll();
    transactions.showPlannedTransactions();
    transactions.initAmountContent()
      .add("15/11/2011", "Planned: courses", -200.00, "courses", -97.00, -97.00, "Main account")
      .add("15/11/2011", "Planned: EDF", -40.00, "EDF", 103.00, 103.00, "Main account")
      .add("15/11/2011", "Planned: salary", 200.00, "salary", 143.00, 143.00, "Main account")
      .add("15/10/2011", "Planned: courses", -200.00, "courses", -57.00, -57.00, "Main account")
      .add("15/10/2011", "Planned: EDF", -40.00, "EDF", 143.00, 143.00, "Main account")
      .add("15/10/2011", "Planned: salary", 200.00, "salary", 183.00, 183.00, "Main account")
      .add("15/09/2011", "Planned: courses", -64.00, "courses", 119.00, 119.00, "Main account")
      .add("15/09/2011", "Planned: EDF", -40.00, "EDF", 183.00, 183.00, "Main account")
      .add("15/09/2011", "Planned: salary", 200.00, "salary", 223.00, 223.00, "Main account")
      .add("03/09/2011", "DIFFE 7", -12.00, "courses", -17.00, -17.00, "card 1111")
      .add("02/09/2011", "8", -53.00, "To categorize", 23.00, 23.00, "Main account")
      .add("31/08/2011", "VIREMENT CDD", -24.00, "card 1111", 76.00, 76.00, "Main account")
      .add("31/08/2011", "DIFFE 6", -100.00, "courses", -5.00, -5.00, "card 1111")
      .add("30/08/2011", "DIFFE 4", -12.00, "courses", 95.00, 95.00, "card 1111")
      .add("26/08/2011", "DIFFE 3", -12.00, "courses", 107.00, 107.00, "card 1111")
      .add("25/08/2011", "DIFFE 2", -12.00, "courses", 100.00, 100.00, "card 1111")
      .add("24/08/2011", "5", -12.00, "To categorize", 100.00, 100.00, "Main account")
      .add("23/08/2011", "DIFFE 1", -12.00, "courses", 100.00, 100.00, "card 1111")
      .check();

    setCurrentDate("2011/09/07");
    restartApplication();

    String d4 = QifBuilder.init(this)
      .addTransaction("2011/09/07", -23., "diffe 9")
      .save();

    operations.importFile(d4, "card 1111");
    timeline.selectAll();
    transactions.showPlannedTransactions();
    transactions.initAmountContent()
      .add("15/11/2011", "Planned: courses", -200.00, "courses", -97.00, -97.00, "Main account")
      .add("15/11/2011", "Planned: EDF", -40.00, "EDF", 103.00, 103.00, "Main account")
      .add("15/11/2011", "Planned: salary", 200.00, "salary", 143.00, 143.00, "Main account")
      .add("15/10/2011", "Planned: courses", -200.00, "courses", -57.00, -57.00, "Main account")
      .add("15/10/2011", "Planned: EDF", -40.00, "EDF", 143.00, 143.00, "Main account")
      .add("15/10/2011", "Planned: salary", 200.00, "salary", 183.00, 183.00, "Main account")
      .add("15/09/2011", "Planned: courses", -41.00, "courses", 142.00, 142.00, "Main account")
      .add("15/09/2011", "Planned: EDF", -40.00, "EDF", 183.00, 183.00, "Main account")
      .add("15/09/2011", "Planned: salary", 200.00, "salary", 223.00, 223.00, "Main account")
      .add("07/09/2011", "DIFFE 9", -23.00, "courses", -17.00, -17.00, "card 1111")
      .add("03/09/2011", "DIFFE 7", -12.00, "courses", 6.00, 6.00, "card 1111")
      .add("02/09/2011", "8", -53.00, "To categorize", 23.00, 23.00, "Main account")
      .add("31/08/2011", "VIREMENT CDD", -24.00, "card 1111", 76.00, 76.00, "Main account")
      .add("31/08/2011", "DIFFE 6", -100.00, "courses", 18.00, 18.00, "card 1111")
      .add("30/08/2011", "DIFFE 4", -12.00, "courses", 118.00, 118.00, "card 1111")
      .add("26/08/2011", "DIFFE 3", -12.00, "courses", 130.00, 130.00, "card 1111")
      .add("25/08/2011", "DIFFE 2", -12.00, "courses", 100.00, 100.00, "card 1111")
      .add("24/08/2011", "5", -12.00, "To categorize", 100.00, 100.00, "Main account")
      .add("23/08/2011", "DIFFE 1", -12.00, "courses", 100.00, 100.00, "card 1111")
      .check();

    mainAccounts.checkReferencePosition(23., "2011/09/02");
    resetWindow();
  }

  @Test
  public void testDeferredWithBankDateInTheFuture() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2009/11/03", 1000, "salaire")
      .addTransaction("2009/11/05", -15, "edf")
      .addTransaction("2009/11/07", -20, "ecole")
      .load();

    categorization.setNewRecurring("salaire", "salaire")
      .setNewRecurring("edf", "edf")
      .setNewRecurring("ecole", "ecole");

    OfxBuilder.init(this)
      .addCardAccount("carte 1", -100, "2009/12/09")
      .addTransaction("2009/11/03", "2010/01/29", -10, "op d. 1")
      .addTransaction("2009/11/14", "2010/01/29", -10, "op d. 2")
      .addTransaction("2009/11/30", "2010/01/29", -10, "op d. 3")
      .addTransaction("2009/12/02", "2010/01/29", -10, "op d. 4")
      .addTransaction("2009/12/06", "2010/01/29", -10, "op d. 5")
      .addTransaction("2009/12/09", "2010/01/29", -10, "op d. 6")
      .loadOneDeferredCard("Boursorama", "Account n. 00001123");

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .sortByBankDate()
      .initAmountContent()
      .add("09/12/2009", "OP D. 6", -10.00, "To categorize", 2835.00, 2835.00, "Card n. carte 1")
      .add("06/12/2009", "OP D. 5", -10.00, "To categorize", 2845.00, 2845.00, "Card n. carte 1")
      .add("02/12/2009", "OP D. 4", -10.00, "To categorize", 2855.00, 2855.00, "Card n. carte 1")
      .add("30/11/2009", "OP D. 3", -10.00, "To categorize", 2865.00, 2865.00, "Card n. carte 1")
      .add("14/11/2009", "OP D. 2", -10.00, "To categorize", 2875.00, 2875.00, "Card n. carte 1")
      .add("03/11/2009", "OP D. 1", -10.00, "To categorize", 2885.00, 2885.00, "Card n. carte 1")
      .add("11/02/2010", "Planned: salaire", 1000.00, "salaire", 2895.00, 2895.00, "Account n. 00001123")
      .add("04/02/2010", "Planned: ecole", -20.00, "ecole", 1895.00, 1895.00, "Account n. 00001123")
      .add("04/02/2010", "Planned: edf", -15.00, "edf", 1915.00, 1915.00, "Account n. 00001123")
      .add("11/01/2010", "Planned: salaire", 1000.00, "salaire", 1930.00, 1930.00, "Account n. 00001123")
      .add("04/01/2010", "Planned: ecole", -20.00, "ecole", 930.00, 930.00, "Account n. 00001123")
      .add("04/01/2010", "Planned: edf", -15.00, "edf", 950.00, 950.00, "Account n. 00001123")
      .add("11/12/2009", "Planned: salaire", 1000.00, "salaire", 965.00, 965.00, "Account n. 00001123")
      .add("04/12/2009", "Planned: ecole", -20.00, "ecole", -35.00, -35.00, "Account n. 00001123")
      .add("04/12/2009", "Planned: edf", -15.00, "edf", -15.00, -15.00, "Account n. 00001123")
      .add("07/11/2009", "ECOLE", -20.00, "ecole", 0.00, 0.00, "Account n. 00001123")
      .add("05/11/2009", "EDF", -15.00, "edf", 20.00, 20.00, "Account n. 00001123")
      .add("03/11/2009", "SALAIRE", 1000.00, "salaire", 35.00, 35.00, "Account n. 00001123")
      .check();
  }

  @Test
  public void testDeferredSpecificSeriesNotShownInAnalysisView() throws Exception {
    addOns.activateAnalysis();
    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder.init(this)
      .addCardAccount("1111", 100, "2008/06/30")
      .addTransaction("2008/06/27", -50, "Auchan")
      .addTransaction("2008/06/28", 1000, "WorldCo")
      .addBankAccount("1234", 1000, "2008/06/30")
      .addTransaction("2008/06/28", -550, "Prelevement")
      .loadDeferredCard("Card n. 1111", "Account n. 1234");

    categorization.selectTransaction("Prelevement")
      .selectOther()
      .selectDeferred()
      .selectSeries("Card n. 1111");

    categorization.setNewIncome("WorldCo", "Income", 1000.00);
    categorization.setNewVariable("Auchan", "Groceries", -100.00);

    timeline.selectAll();
    transactions.initContent()
      .add("28/06/2008", TransactionType.CREDIT_CARD, "WORLDCO", "", 1000.00, "Income")
      .add("28/06/2008", TransactionType.PRELEVEMENT, "PRELEVEMENT", "", -550.00, "Card n. 1111")
      .add("27/06/2008", TransactionType.CREDIT_CARD, "AUCHAN", "", -50.00, "Groceries")
      .check();

    timeline.selectMonth(200806);
    analysis.budget().balanceChart
      .getRightDataset()
      .checkSize(1)
      .checkValue("Variable", 100.00);

    analysis.budget().seriesChart
      .getSingleDataset()
      .checkSize(1)
      .checkValue("Groceries", 100.00);

    analysis.budget();
    timeline.selectMonth(200806);
    analysis.table().checkNoTableRowWithLabel("Card n. 1111");
    analysis.table().checkNoTableRowWithLabel("Other");
  }

  @Test
  public void testDeferredAccountInManual() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2009/12/08", -30., "cheque 1")
      .addTransaction("2009/12/04", -30., "cheque 2")
      .addTransaction("2009/11/29", -10., "cheque 3")
      .addTransaction("2009/11/19", -100, "Virement octobre/novembre")
      .addTransaction("2009/11/15", -30., "cheque 4")
      .addTransaction("2009/11/10", -15, "cheque 5")
      .load();

    categorization
      .setNewRecurring("CHEQUE N°1", "course")
      .setRecurring("CHEQUE N°2", "course")
      .setRecurring("CHEQUE N°3", "course")
      .setRecurring("CHEQUE N°4", "course")
      .setRecurring("CHEQUE N°5", "course");
    categorization.editSeries("course")
      .setAmount(300)
      .setPropagationEnabled()
      .setTargetAccount("Account n. 00001123")
      .validate();

    accounts.createNewAccount()
      .setName("Compte differé")
      .selectBank("CIC")
      .setAsDeferredCard()
      .setTargetAccount("Account n. 00001123")
      .setDeferred(15, 19, 0)
      .validate();

    timeline.selectMonth(200911);
    transactionCreation.show()
      .selectAccount("Compte differé")
      .create(1, "Auchan 1", -50.00)
      .create(10, "Auchan 2", -50.00)
      .create(16, "Auchan 3", -10.00)
      .create(19, "Auchan 4", -10.00)
      .selectMonth(200912)
      .create(1, "Auchan 5", -5.00)
      .create(6, "Auchan 6", -10.00);

    categorization.selectTransactions("Auchan 1", "Auchan 2", "Auchan 3", "Auchan 4", "Auchan 5", "Auchan 6")
      .selectRecurring().selectSeries("course");

    categorization.selectTransaction("VIREMENT OCTOBRE/NOVEMBRE").selectOther().selectDeferred().selectSeries("Compte differé");

    timeline.selectAll();
    transactions.showPlannedTransactions().initAmountContent()
      .add("27/02/2010", "Planned: course", -21.00, "course", -840.00, -840.00, "Account n. 00001123")
      .add("19/02/2010", "Planned: course", -192.00, "course", -819.00, -819.00, "Account n. 00001123")
      .add("11/02/2010", "Planned: course", -87.00, "course", -627.00, -627.00, "Account n. 00001123")
      .add("27/01/2010", "Planned: course", -21.00, "course", -540.00, -540.00, "Account n. 00001123")
      .add("19/01/2010", "Planned: course", -192.00, "course", -519.00, -519.00, "Account n. 00001123")
      .add("11/01/2010", "Planned: course", -87.00, "course", -327.00, -327.00, "Account n. 00001123")
      .add("27/12/2009", "Planned: course", -21.00, "course", -240.00, -240.00, "Account n. 00001123")
      .add("19/12/2009", "Planned: course", -184.00, "course", -219.00, -219.00, "Account n. 00001123")
      .add("08/12/2009", "CHEQUE N°1", -30.00, "course", 0.00, 0.00, "Account n. 00001123")
      .add("06/12/2009", "AUCHAN 6", -10.00, "course", -35.00, -35.00, "Compte differé")
      .add("04/12/2009", "CHEQUE N°2", -30.00, "course", 30.00, 30.00, "Account n. 00001123")
      .add("01/12/2009", "AUCHAN 5", -5.00, "course", -25.00, -25.00, "Compte differé")
      .add("29/11/2009", "CHEQUE N°3", -10.00, "course", 60.00, 60.00, "Account n. 00001123")
      .add("19/11/2009", "AUCHAN 4", -10.00, "course", -20.00, -20.00, "Compte differé")
      .add("19/11/2009", "VIREMENT OCTOBRE/NOVEMBRE", -100.00, "Compte differé", 70.00, 70.00, "Account n. 00001123")
      .add("16/11/2009", "AUCHAN 3", -10.00, "course", -10.00, -10.00, "Compte differé")
      .add("15/11/2009", "CHEQUE N°4", -30.00, "course", 170.00, 170.00, "Account n. 00001123")
      .add("10/11/2009", "AUCHAN 2", -50.00, "course", 170.00, 170.00, "Compte differé")
      .add("10/11/2009", "CHEQUE N°5", -15.00, "course", 200.00, 200.00, "Account n. 00001123")
      .add("01/11/2009", "AUCHAN 1", -50.00, "course", 170.00, 170.00, "Compte differé")
      .check();

    mainAccounts.checkPosition("Compte differé", -35);
    mainAccounts.checkPosition("Account n. 00001123", 0);
    mainAccounts.checkReferencePosition(0, "2009/12/08");

  }

  @Test
  public void testShiftOneMonth() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2009/10/09", -100, "any")
      .addTransaction("2009/11/09", -100, "Virement septembre/octobre")
      .load();

    budgetView.recurring.createSeries().setName("course").setAmount(300).setPropagationEnabled()
      .setTargetAccount("Account n. 00001123").validate();

    accounts.createNewAccount()
      .setName("Compte differé")
      .selectBank("CIC")
      .setAsDeferredCard()
      .setDeferred(15, 19, 1)
      .setTargetAccount("Account n. 00001123")
      .validate();

    timeline.selectMonth(200910);
    transactionCreation.show()
      .selectAccount("Compte differé")
      .create(1, "Auchan -1", -50.00)
      .create(10, "Auchan 0", -50.00)
      .selectMonth(200911)
      .create(1, "Auchan 1", -50.00)
      .create(10, "Auchan 2", -50.00)
      .create(16, "Auchan 3", -10.00)
      .create(19, "Auchan 4", -10.00)
      .selectMonth(200912)
      .create(1, "Auchan 5", -5.00)
      .create(6, "Auchan 6", -10.00);

    categorization.selectTransactions("Auchan -1", "Auchan 0", "Auchan 1", "Auchan 2", "Auchan 3", "Auchan 4", "Auchan 5", "Auchan 6")
      .selectRecurring().selectSeries("course");

    categorization.selectTransaction("VIREMENT SEPTEMBRE/OCTOBRE").selectOther().selectDeferred().selectSeries("Compte differé");

    views.selectData();

    mainAccounts.checkPosition("Compte differé", -100.00);
    mainAccounts.checkPosition("Account n. 00001123", 0.00);
    mainAccounts.checkReferencePosition(0.00, "2009/11/09");

    timeline.selectAll();
    transactions.showPlannedTransactions().initAmountContent()
      .add("19/02/2010", "Planned: course", -300.00, "course", -900.00, -900.00, "Account n. 00001123")
      .add("19/01/2010", "Planned: course", -265.00, "course", -600.00, -600.00, "Account n. 00001123")
      .add("19/12/2009", "Planned: course", -200.00, "course", -300.00, -300.00, "Account n. 00001123")
      .add("06/12/2009", "AUCHAN 6", -10.00, "course", -335.00, -335.00, "Compte differé")
      .add("01/12/2009", "AUCHAN 5", -5.00, "course", -325.00, -325.00, "Compte differé")
      .add("19/11/2009", "AUCHAN 4", -10.00, "course", -320.00, -320.00, "Compte differé")
      .add("16/11/2009", "AUCHAN 3", -10.00, "course", -310.00, -310.00, "Compte differé")
      .add("10/11/2009", "AUCHAN 2", -50.00, "course", -100.00, -100.00, "Compte differé")
      .add("09/11/2009", "VIREMENT SEPTEMBRE/OCTOBRE", -100.00, "Compte differé", 0.00, 0.00, "Account n. 00001123")
      .add("01/11/2009", "AUCHAN 1", -50.00, "course", -50.00, -50.00, "Compte differé")
      .add("10/10/2009", "AUCHAN 0", -50.00, "course", 100.00, 100.00, "Compte differé")
      .add("09/10/2009", "ANY", -100.00, "To categorize", 100.00, 100.00, "Account n. 00001123")
      .add("01/10/2009", "AUCHAN -1", -50.00, "course", 100.00, 100.00, "Compte differé")
      .check();
  }
}
