package org.designup.picsou.functests.categorization;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.QifBuilder;
import org.designup.picsou.model.TransactionType;

public class AutoCategorizationTest extends LoggedInFunctionalTestCase {

  public void testAutoCategorization() throws Exception {

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K 1")
      .addTransaction("2006/01/11", -1.1, "Fouquet's")
      .load(2, 0, 0);

    views.selectCategorization();
    categorization.setNewVariable("Menu K 1", "dej");

    views.selectHome();
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/12", -1.3, "Menu K 2")
      .loadAndGotoCategorize(1, 0, 1);

    transactions.checkSeries("Menu K 1", "dej");
    transactions.checkSeries("Menu K 2", "dej");
  }

  public void testAutoCategorizationWithSubSeries() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -15.00, "Menu K 1")
      .addTransaction("2006/01/11", -100.00, "Fouquet's")
      .load();

    views.selectCategorization();
    categorization.selectTransaction("MENU K 1");
    categorization.selectVariable()
      .createSeries()
      .setName("Restau")
      .gotoSubSeriesTab()
      .addSubSeries("Jap")
      .addSubSeries("Grec")
      .selectSubSeries("Jap")
      .validate();

    categorization.checkTable(new Object[][]{
      {"11/01/2006", "", "FOUQUET'S", -100.00},
      {"10/01/2006", "Restau / Jap", "MENU K 1", -15.00},
    });

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/12", -15.00, "Menu K 2")
      .addTransaction("2006/01/12", -100.00, "FOUQUET's II")
      .load(2, 0, 1);

    categorization.showAllTransactions();
    categorization.checkTable(new Object[][]{
      {"11/01/2006", "", "FOUQUET'S", -100.00},
      {"12/01/2006", "", "FOUQUET'S II", -100.00},
      {"10/01/2006", "Restau / Jap", "MENU K 1", -15.00},
      {"12/01/2006", "Restau / Jap", "MENU K 2", -15.00},
    });
  }

  public void testNoAutoCategorizationIfAmbiguityOnSubSeries() throws Exception {
    views.selectBudget();
    budgetView.variable
      .createSeries()
      .setName("Restau")
      .gotoSubSeriesTab()
      .addSubSeries("Jap")
      .addSubSeries("Grec")
      .selectSubSeries("Jap")
      .validate();

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -15.00, "Menu K 1")
      .addTransaction("2006/01/10", -15.00, "Menu K 2")
      .load();

    views.selectCategorization();
    categorization.setVariable("MENU K 1", "Restau", "Jap");
    categorization.setVariable("MENU K 2", "Restau", "Grec");

    categorization.checkTable(new Object[][]{
      {"10/01/2006", "Restau / Jap", "MENU K 1", -15.00},
      {"10/01/2006", "Restau / Grec", "MENU K 2", -15.00},
    });

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/12", -15.00, "Menu K 3")
      .addTransaction("2006/01/12", -100.00, "FOUQUET's")
      .load();

    categorization.showAllTransactions();
    categorization.checkTable(new Object[][]{
      {"12/01/2006", "", "FOUQUET'S", -100.00},
      {"10/01/2006", "Restau / Jap", "MENU K 1", -15.00},
      {"10/01/2006", "Restau / Grec", "MENU K 2", -15.00},
      {"12/01/2006", "", "MENU K 3", -15.00},
    });
  }

  public void testAutoCategorizationWithCardTransactions() throws Exception {
    String path = OfxBuilder
      .init(this)
      .addBankAccount(30066, 10674, "000123", 0, "2006/01/11")
      .addTransaction("2006/01/10", -2.2, "virement")
      .addCardAccount("000111", 0, "2006/01/11")
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .addTransaction("2006/01/11", -1.1, "Fouquet's")
      .save();
//    System.out.println("AutoCategorizationTest.testAutoCategorizationWithCardTransactions " + path);
//    openApplication();
    operations
      .openImportDialog()
      .setFilePath(path)
      .acceptFile()
      .setMainAccount()
      .doImport()
      .setDeferredAccount(25, 28, 0, "Account n. 000123")
      .completeImport();

    views.selectData();
    transactions.initContent()
      .add("11/01/2006", TransactionType.CREDIT_CARD, "Fouquet's", "", -1.10)
      .add("10/01/2006", TransactionType.CREDIT_CARD, "Menu K", "", -1.10)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "virement", "", -2.2)
      .check();

    views.selectCategorization();
    categorization.setNewVariable("Menu K", "dej");
    OfxBuilder
      .init(this)
      .addCardAccount("000111", -1.3, "2006/01/12")
      .addTransaction("2006/01/12", -1.3, "Menu K")
      .load();
    views.selectData();
    transactions.initContent()
      .add("12/01/2006", TransactionType.CREDIT_CARD, "Menu K", "", -1.30, "dej")
      .add("11/01/2006", TransactionType.CREDIT_CARD, "Fouquet's", "", -1.10)
      .add("10/01/2006", TransactionType.CREDIT_CARD, "Menu K", "", -1.10, "dej")
      .add("10/01/2006", TransactionType.PRELEVEMENT, "virement", "", -2.2)
      .check();
  }

  public void testTakesThreeLastTransactions() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .addTransaction("2006/01/09", -1.1, "Menu K")
      .addTransaction("2005/12/08", -1.1, "Menu K")
      .addTransaction("2005/11/07", -1.1, "Menu K")
      .load();
    views.selectCategorization();
    categorization.setNewVariable("Menu K", "dej");
    categorization.setNewVariable(0, "resto");
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/12", -1.3, "Menu K")
      .load();
    views.selectData();
    transactions.checkSeries(0, "dej");

    views.selectCategorization();
    categorization.showAllTransactions();
    categorization.setVariable(2, "resto");
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/13", -1.3, "Menu K")
      .load();
    views.selectData();
    transactions.checkSeries(0, "To categorize");
  }

  public void testIgnoresChecks() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Cheque 1")
      .load();
    views.selectCategorization();
    categorization.setNewVariable("Cheque N°1", "dej");
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/11", -1.1, "Cheque 2")
      .load(1, 0, 0);
    views.selectData();
    transactions.initContent()
      .add("11/01/2006", TransactionType.CHECK, "CHEQUE N°2", "", -1.10)
      .add("10/01/2006", TransactionType.CHECK, "CHEQUE N°1", "", -1.10, "dej")
      .check();
  }

  public void testAutoCategorizationWithDots() throws Exception {
    QifBuilder
      .init(this)
      .addTransaction("2006/01/15", -1.1, "PRELEVEMENT 3766941826  M.N.P.A.F. M.N.P.A.F. 8811941800")
      .load(0.00);

    views.selectCategorization();
    categorization.setNewVariable(0, "Mutuelle");

    QifBuilder
      .init(this)
      .addTransaction("2006/01/15", -1.3, "PRELEVEMENT 3766941834  M.N.P.A.F. M.N.P.A.F. 8811941800")
      .load(1, 0, 1);

    views.selectData();
    transactions.checkSeries(0, "Mutuelle");
    transactions.checkSeries(1, "Mutuelle");
  }

  public void testNoAutomaticCategorisationIfSeriesIsDisabled() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/05/25", -50.0, "Auchan")
      .addTransaction("2008/05/25", -50.0, "Auchan")
      .load(2, 0, 0);

    views.selectBudget();
    budgetView.variable.createSeries()
      .setName("Courses")
      .setRepeatCustom()
      .setStartDate(200804)
      .setEndDate(200805)
      .validate();

    budgetView.variable.createSeries()
      .setName("Courses_2")
      .setRepeatCustom()
      .validate();

    views.selectCategorization();
    categorization
      .selectTransactions("Auchan")
      .selectVariable()
      .selectSeries("Courses");

    OfxBuilder
      .init(this)
      .addTransaction("2008/06/11", -50.0, "2_Auchan")
      .load(1, 0, 0);

    categorization
      .unselectAllTransactions()
      .selectTransaction("2_Auchan")
      .checkToCategorize()
      .selectVariable()
      .selectSeries("Courses_2");

    OfxBuilder
      .init(this)
      .addTransaction("2008/06/12", -50.0, "3_Auchan")
      .load(1, 0, 1);

    categorization
      .selectTransaction("3_Auchan")
      .getVariable().checkSeriesIsSelected("Courses_2");
  }

  public void testAutoCategorisationOnSameLabelWithNumber() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K 1")
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .load();

    views.selectCategorization();
    categorization.setNewVariable("Menu K 1", "dej");
    categorization.setNewVariable("Menu K", "petit dej");

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/12", -1.3, "Menu K 1")
      .load(1, 0, 1);
    views.selectData();
    transactions.checkSeries(0, "dej");
    transactions.checkSeries(1, "petit dej");
    transactions.checkSeries(2, "dej");
  }
}
