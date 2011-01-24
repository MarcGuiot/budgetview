package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.DeferredCardCategorizationChecker;
import org.designup.picsou.functests.checkers.ImportDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.QifBuilder;
import org.designup.picsou.model.TransactionType;

public class DeferredTest extends LoggedInFunctionalTestCase {
  protected void setUp() throws Exception {
    super.setUp();
    setCurrentDate("2009/12/09");
    operations.openPreferences().setFutureMonthsCount(2).validate();
  }

  public void testCategorization() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder.init(this)
      .addCardAccount("1111", 100, "2008/06/30")
      .addTransaction("2008/06/27", -50, "Auchan")
      .addBankAccount("1234", 1000, "2008/06/30")
      .addTransaction("2008/06/28", -550, "Prelevement")
      .loadDeferredCard("Card n. 1111");

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
    transactions
      .initAmountContent()
      .add("28/06/2008", "PRELEVEMENT", -550.00, "Card n. 1111", 1000.00, 1000.00, "Account n. 1234")
      .add("27/06/2008", "AUCHAN", -50.00, "To categorize", 100.00, 1000.00, "Card n. 1111")
      .check();
  }

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

    ImportDialogChecker importer = cardCategorization.importAccount()
      .setFilePath(cardFile)
      .acceptFile();
    importer.addNewAccount()
      .setAccountName("Card account")
      .selectBank("CIC")
      .setAsDeferredCard()
      .setPosition(-1000)
      .validate();
    importer.completeImport();

    views.selectCategorization();
    cardCategorization
      .checkMessage("Assign the monthly transfer operation to the corresponding card account:")
      .selectSeries("Card account");

    categorization.selectTransaction("Prelevement carte")
      .checkOtherSeriesIsSelected("Card account");
  }

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
      .loadDeferredCard("Card n. 1111");

    timeline.selectAll();

    views.selectCategorization();
    categorization.setDeferred("Prelevement novembre", "Card n. 1111");
    categorization.setDeferred("Prelevement octobre", "Card n. 1111");
    categorization.setDeferred("Prelevement aout", "Card n. 1111");

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
    categorization.selectTransactions("Auchan")
      .selectVariable()
      .createSeries()
      .setName("Course")
      .selectAllMonths()
      .setAmount("250")
      .validate();

    views.selectBudget();
    timeline.selectMonth("2009/12");
    budgetView.variable.checkSeries("Course", -100, -250);

    timeline.selectMonth("2009/11");
    budgetView.variable.checkSeries("Course", -30, -250);
  }

  public void testFirstQifImport() throws Exception {
    String mainAccount = QifBuilder.init(this)
      //.addBankAccount("", -1, "1234", 1000, "2009/11/30")
      .addTransaction("2009/11/28", -30, "Prelevement novembre")
      .addTransaction("2009/10/28", -35 - 15 /* -15 : transaction precedente non importée */, "Prelevement octobre")
      .save();
    operations.importQifFile(mainAccount, "Autre", 1000.00);

    String deferredAccount = QifBuilder.init(this)
      .addTransaction("2009/11/30", -60, "Auchan")
      .addTransaction("2009/11/29", -40, "Auchan")
      .addTransaction("2009/11/25", -10, "Auchan")
      .addTransaction("2009/10/29", -20, "Auchan")
      .addTransaction("2009/09/29", -35, "Auchan")
      .addTransaction("2009/09/14", -15, "Auchan")
      .save();
    operations.importQifFileWithDeferred(deferredAccount, "Autre", -100.00);

    timeline.selectAll();

    views.selectCategorization();
    categorization.selectTransaction("Prelevement novembre")
      .selectOther()
      .selectDeferred()
      .selectSeries("Card 1111");
    categorization.selectTransaction("Prelevement octobre")
      .selectOther()
      .selectDeferred()
      .selectSeries("Card 1111");
    views.selectData();
    transactions.initAmountContent()
      .add("30/11/2009", "AUCHAN", -60.00, "To categorize", -100.00, 900.00, "card 1111")
      .add("29/11/2009", "AUCHAN", -40.00, "To categorize", -40.00, 960.00, "card 1111")
      .add("28/11/2009", "PRELEVEMENT NOVEMBRE", -30.00, "card 1111", 1000.00, 1000.00, "Main account")
      .add("25/11/2009", "AUCHAN", -10.00, "To categorize", -30.00, 1000.00, "card 1111")
      .add("29/10/2009", "AUCHAN", -20.00, "To categorize", -20.00, 1000.00, "card 1111")
      .add("28/10/2009", "PRELEVEMENT OCTOBRE", -50.00, "card 1111", 1030.00, 1030.00, "Main account")
      .add("29/09/2009", "AUCHAN", -35.00, "To categorize", -50.00, 1080.00, "card 1111")
      .add("14/09/2009", "AUCHAN", -15.00, "To categorize", -15.00, 1080.00, "card 1111")
      .check();
  }

  public void testImportQifWithOperationMixInMonthWithoutTransfer() throws Exception {
    String mainAccount = QifBuilder.init(this)
      .addTransaction("2009/11/28", -30, "Prelevement novembre")
      .addTransaction("2009/10/28", -35 - 15 /* -15 : transaction precedente non importé */, "Prelevement octobre")
      .save();
    operations.importQifFile(mainAccount, "Autre", 1000.00);

    String deferredAccount = QifBuilder.init(this)
      .addTransaction("2009/11/30", -60, "Auchan")
      .addTransaction("2009/11/29", -40, "Auchan")
      .addTransaction("2009/11/25", -10, "Auchan")
      .addTransaction("2009/10/29", -20, "Auchan")
      .addTransaction("2009/09/14", -35, "Auchan")
      .save();
    operations.importQifFileWithDeferred(deferredAccount, "Autre", -100.00);

    views.selectCategorization();
    categorization.setDeferred("Prelevement novembre", "card 1111");
    categorization.setDeferred("Prelevement octobre", "card 1111");

    timeline.selectAll();
    views.selectData();
    transactions.initAmountContent()
      .add("30/11/2009", "AUCHAN", -60.00, "To categorize", -100.00, 900.00, "card 1111")
      .add("29/11/2009", "AUCHAN", -40.00, "To categorize", -40.00, 960.00, "card 1111")
      .add("28/11/2009", "PRELEVEMENT NOVEMBRE", -30.00, "card 1111", 1000.00, 1000.00, "Main account")
      .add("25/11/2009", "AUCHAN", -10.00, "To categorize", -30.00, 1000.00, "card 1111")
      .add("29/10/2009", "AUCHAN", -20.00, "To categorize", -20.00, 1000.00, "card 1111")
      .add("28/10/2009", "PRELEVEMENT OCTOBRE", -50.00, "card 1111", 1030.00, 1030.00, "Main account")
      .add("14/09/2009", "AUCHAN", -35.00, "To categorize", -35.00, 1080.00, "card 1111")
      .check();

    String newDeferredAccount = QifBuilder.init(this)
      .addTransaction("2009/12/02", -70, "Auchan")
      .addTransaction("2009/09/28", -15, "Auchan")
      .save();
    operations.importQifFile(newDeferredAccount, "Autre", "card 1111");

    timeline.selectAll();
    views.selectData();
    transactions.initAmountContent()
      .add("02/12/2009", "AUCHAN", -70.00, "To categorize", -170.00, 830.00, "card 1111")
      .add("30/11/2009", "AUCHAN", -60.00, "To categorize", -100.00, 900.00, "card 1111")
      .add("29/11/2009", "AUCHAN", -40.00, "To categorize", -40.00, 960.00, "card 1111")
      .add("28/11/2009", "PRELEVEMENT NOVEMBRE", -30.00, "card 1111", 1000.00, 1000.00, "Main account")
      .add("25/11/2009", "AUCHAN", -10.00, "To categorize", -30.00, 1000.00, "card 1111")
      .add("29/10/2009", "AUCHAN", -20.00, "To categorize", -20.00, 1000.00, "card 1111")
      .add("28/10/2009", "PRELEVEMENT OCTOBRE", -50.00, "card 1111", 1030.00, 1030.00, "Main account")
      .add("28/09/2009", "AUCHAN", -15.00, "To categorize", -50.00, 1080.00, "card 1111")
      .add("14/09/2009", "AUCHAN", -35.00, "To categorize", -35.00, 1080.00, "card 1111")
      .check();

    String filePath = QifBuilder.init(this)
      .addTransaction("2009/12/04", -20, "cheque 1")
      .save();
    operations.importQifFile(filePath, "Société Générale", "Main account");

    views.selectData();
    transactions.initAmountContent()
      .add("04/12/2009", "CHEQUE N°1", -20.00, "To categorize", 980.00, 980.00, "Main account")
      .add("02/12/2009", "AUCHAN", -70.00, "To categorize", -170.00, 810.00, "card 1111")
      .add("30/11/2009", "AUCHAN", -60.00, "To categorize", -100.00, 880.00, "card 1111")
      .add("29/11/2009", "AUCHAN", -40.00, "To categorize", -40.00, 940.00, "card 1111")
      .check();
  }

  public void testShiftNotAllowedOnDeferredCardOperation() throws Exception {
    OfxBuilder.init(this)
      .addCardAccount("1111", -100, "2009/12/07")
      .addTransaction("2009/11/30", -60, "Auchan")
      .addBankAccount("1234", 1000, "2009/11/30")
      .addTransaction("2009/11/28", -30, "Prelevement novembre")
      .loadDeferredCard("Card n. 1111");
    views.selectCategorization();
    categorization.selectTransaction("Auchan");
    transactionDetails.checkShiftDisabled();
  }

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
      .loadDeferredCard("Card n. 1111");

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
      .loadDeferredCard("Card n. 1111");

    views.selectCategorization();
    categorization
      .setNewVariable("Auchan", "course", -30.00);
    categorization.setDeferred("Prelevement novembre", "1111")
      .setDeferred("Prelevement octobre", "1111")
      .setDeferred("Prelevement aout", "1111");
    timeline.selectAll();
    views.selectData();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("14/02/2010", "Planned: course", -30.00, "course", 840.00, "Main accounts")
      .add("14/01/2010", "Planned: course", -30.00, "course", 870.00, "Main accounts")
      .add("30/11/2009", "AUCHAN", -60.00, "course", -100.00, 900.00, "Card n. 1111")
      .add("29/11/2009", "AUCHAN", -40.00, "course", -40.00, 960.00, "Card n. 1111")
      .add("28/11/2009", "PRELEVEMENT NOVEMBRE", -30.00, "Card n. 1111", 1000.00, 1000.00, "Account n. 1234")
      .add("25/11/2009", "AUCHAN", -10.00, "course", -30.00, 1000.00, "Card n. 1111")
      .add("29/10/2009", "AUCHAN", -20.00, "course", -20.00, 1000.00, "Card n. 1111")
      .add("28/10/2009", "PRELEVEMENT OCTOBRE", 0.00, "Card n. 1111", 1030.00, 1030.00, "Account n. 1234")
      .add("26/09/2009", "PRELEVEMENT AOUT", -50.00, "Card n. 1111", 1030.00, 1030.00, "Account n. 1234")
      .add("14/09/2009", "AUCHAN", -35.00, "course", -50.00, 1030.00, "Card n. 1111")
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
      .addBankAccount("1234", 1000, "2009/11/30")
      .addTransaction("2009/11/28", -200, "Prelevement novembre")
      .addTransaction("2009/10/28", -130, "Prelevement octobre")
      .loadDeferredCard("Card n. 1111");

    categorization.setDeferred("Prelevement novembre", "1111");
    categorization.setDeferred("Prelevement octobre", "1111");

    views.selectCategorization();
    categorization
      .setNewVariable("Auchan", "course", -200.0);
    timeline.selectAll();
    views.selectData();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("01/02/2010", "Planned: course", -200.00, "course", 400.00, "Main accounts")
      .add("01/01/2010", "Planned: course", -200.00, "course", 600.00, "Main accounts")
      .add("07/12/2009", "Planned: course", -90.00, "course", 910.00, "Main accounts")
      .add("07/12/2009", "AUCHAN", -10.00, "course", -110.00, 800.00, "Card n. 1111")
      .add("30/11/2009", "AUCHAN", -60.00, "course", -100.00, 810.00, "Card n. 1111")
      .add("29/11/2009", "AUCHAN", -40.00, "course", -40.00, 870.00, "Card n. 1111")
      .add("28/11/2009", "PRELEVEMENT NOVEMBRE", -200.00, "Card n. 1111", 1000.00, 1000.00, "Account n. 1234")
      .add("25/11/2009", "AUCHAN", -80.00, "course", -200.00, 1000.00, "Card n. 1111")
      .add("29/10/2009", "AUCHAN", -120.00, "course", -120.00, 1000.00, "Card n. 1111")
      .add("28/10/2009", "PRELEVEMENT OCTOBRE", -130.00, "Card n. 1111", 1200.00, 1200.00, "Account n. 1234")
      .add("01/09/2009", "AUCHAN", -130.00, "course", -130.00, 1330.00, "Card n. 1111")
      .check();
  }

  public void testChangeAccountType() throws Exception {
    OfxBuilder.init(this)
      .addCardAccount("1111", -110, "2009/12/07")
      .addTransaction("2009/12/07", -10, "Auchan")
      .addBankAccount("1234", 1000, "2009/11/30")
      .addTransaction("2009/11/28", -30, "Prelevement novembre")
      .loadDeferredCard("Card n. 1111");

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
      .loadDeferredCard("Card n. 1111");

    views.selectBudget();
    budgetView.variable.createSeries()
      .setName("Mc Do")
      .selectAllMonths()
      .setAmount("100")
      .validate();

    views.selectCategorization();
    categorization.setDeferred("Prelevement octobre", "Card n. 1111");

    views.selectData();
    timeline.selectMonth("2009/12");
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("07/12/2009", "Planned: Mc Do", -100.00, "Mc Do", 890.00, "Main accounts")
      .add("07/12/2009", "AUCHAN", -10.00, "To categorize", -110.00, 880.00, "Card n. 1111")
      .add("04/12/2009", "CHEQUE N°5", -10.00, "To categorize", 990.00, 990.00, "Account n. 1234")
      .check();

    timeline.selectMonth("2009/11");
    transactions.initAmountContent()
      .add("30/11/2009", "AUCHAN", -60.00, "To categorize", -300.00, 1000.00, "Card n. 1111")
      .add("29/11/2009", "AUCHAN", -40.00, "To categorize", -240.00, 1000.00, "Card n. 1111")
      .add("28/11/2009", "PRELEVEMENT NOVEMBRE", -200.00, "To categorize", 1000.00, 1000.00, "Account n. 1234")
      .add("25/11/2009", "AUCHAN", -80.00, "To categorize", -200.00, 1000.00, "Card n. 1111")
      .add("31/10/2009", "AUCHAN", -120.00, "To categorize", -120.00, 1000.00, "Card n. 1111")
      .check();

    timeline.selectMonth("2009/10");
    transactions.initAmountContent()
      .add("30/10/2009", "CHEQUE N°4", -10.00, "To categorize", 1200.00, 1200.00, "Account n. 1234")
      .add("28/10/2009", "PRELEVEMENT OCTOBRE", -130.00, "Card n. 1111", 1210.00, 1210.00, "Account n. 1234")
      .add("27/10/2009", "CHEQUE N°3", -10.00, "To categorize", 1340.00, 1340.00, "Account n. 1234")
      .check();

    views.selectCategorization();
    categorization.selectTransaction("Prelevement novembre")
      .selectOther()
      .selectDeferred()
      .selectSeries("Card n. 1111");
    categorization.selectTransaction("Prelevement septembre")
      .selectOther()
      .selectDeferred()
      .selectSeries("Card n. 1111");

    timeline.selectMonth("2009/09");
    views.selectData();
    transactions.initAmountContent()
      .add("30/09/2009", "CHEQUE N°2", -10.00, "To categorize", 1350.00, 1350.00, "Account n. 1234")
      .add("28/09/2009", "CHEQUE N°1", -10.00, "To categorize", 1360.00, 1360.00, "Account n. 1234")
      .add("28/09/2009", "PRELEVEMENT SEPTEMBRE", -130.00, "Card n. 1111", 1370.00, 1370.00, "Account n. 1234")
      .check();

    timeline.selectMonth("2009/11");
    transactions.initAmountContent()
      .add("28/11/2009", "PRELEVEMENT NOVEMBRE", -200.00, "Card n. 1111", 1000.00, 1000.00, "Account n. 1234")
      .add("25/11/2009", "AUCHAN", -80.00, "To categorize", -200.00, 1000.00, "Card n. 1111")
      .add("31/10/2009", "AUCHAN", -120.00, "To categorize", -120.00, 1000.00, "Card n. 1111")
      .check();

    timeline.selectMonth("2009/12");
    transactions.initAmountContent()
      .add("07/12/2009", "Planned: Mc Do", -100.00, "Mc Do", 890.00, "Main accounts")
      .add("07/12/2009", "AUCHAN", -10.00, "To categorize", -110.00, 780.00, "Card n. 1111")
      .add("04/12/2009", "CHEQUE N°5", -10.00, "To categorize", 990.00, 990.00, "Account n. 1234")
      .add("30/11/2009", "AUCHAN", -60.00, "To categorize", -100.00, 790.00, "Card n. 1111")
      .add("29/11/2009", "AUCHAN", -40.00, "To categorize", -40.00, 850.00, "Card n. 1111")
      .check();
  }
}
