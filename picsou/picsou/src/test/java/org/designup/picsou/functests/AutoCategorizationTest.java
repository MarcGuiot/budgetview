package org.designup.picsou.functests;

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
      .load();

    views.selectCategorization();
    categorization.setNewEnvelope("Menu K 1", "dej");

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/12", -1.3, "Menu K 2")
      .load();
    views.selectData();
    transactions.checkSeries(0, "dej");
    transactions.checkSeries(2, "dej");
  }

  public void testAutoCategorizationWithSubSeries() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -15.00, "Menu K 1")
      .addTransaction("2006/01/11", -100.00, "Fouquet's")
      .load();

    views.selectCategorization();
    categorization.selectTransaction("MENU K 1");
    categorization.selectEnvelopes()
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
      .load();

    views.selectCategorization();
    categorization.checkTable(new Object[][]{
      {"11/01/2006", "", "FOUQUET'S", -100.00},
      {"12/01/2006", "", "FOUQUET'S II", -100.00},
      {"10/01/2006", "Restau / Jap", "MENU K 1", -15.00},
      {"12/01/2006", "Restau / Jap", "MENU K 2", -15.00},
    });
  }

  public void testNoAutoCategorizationIfAmbiguityOnSubSeries() throws Exception {

    views.selectBudget();
    budgetView.envelopes
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
    categorization.setEnvelope("MENU K 1", "Restau", "Jap");
    categorization.setEnvelope("MENU K 2", "Restau", "Grec");

    categorization.checkTable(new Object[][]{
      {"10/01/2006", "Restau / Jap", "MENU K 1", -15.00},
      {"10/01/2006", "Restau / Grec", "MENU K 2", -15.00},
    });

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/12", -15.00, "Menu K 3")
      .addTransaction("2006/01/12", -100.00, "FOUQUET's")
      .load();

    views.selectCategorization();
    categorization.checkTable(new Object[][]{
      {"12/01/2006", "", "FOUQUET'S", -100.00},
      {"10/01/2006", "Restau / Jap", "MENU K 1", -15.00},
      {"10/01/2006", "Restau / Grec", "MENU K 2", -15.00},
      {"12/01/2006", "", "MENU K 3", -15.00},
    });
  }

  public void testAutoCategorizationWithCardTransactions() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount(30066, 10674, "000123", 0, "2006/01/11")
      .addCardAccount("000111", 0, "2006/01/11")
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .addTransaction("2006/01/11", -1.1, "Fouquet's")
      .load();

    views.selectData();
    transactions.initContent()
      .add("11/01/2006", TransactionType.CREDIT_CARD, "Fouquet's", "", -1.10)
      .add("10/01/2006", TransactionType.CREDIT_CARD, "Menu K", "", -1.10)
      .check();

    views.selectCategorization();
    categorization.setNewEnvelope("Menu K", "dej");
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
      .check();
  }

  public void testTakesThreeLastTransactions() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .addTransaction("2006/01/09", -1.1, "Menu K")
      .addTransaction("2006/01/08", -1.1, "Menu K")
      .addTransaction("2006/01/07", -1.1, "Menu K")
      .load();
    views.selectCategorization();
    categorization.setNewEnvelope("Menu K", "dej");
    categorization.setNewEnvelope(0, "resto");
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/12", -1.3, "Menu K")
      .load();
    views.selectData();
    transactions.checkSeries(0, "dej");

    views.selectCategorization();
    categorization.setEnvelope(2, "resto");
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
    categorization.setNewEnvelope("Cheque N°1", "dej");
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/11", -1.1, "Cheque 2")
      .load();
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
      .load(0.);

    views.selectCategorization();
    categorization.setNewEnvelope(0, "Mutuelle");

    QifBuilder
      .init(this)
      .addTransaction("2006/01/15", -1.3, "PRELEVEMENT 3766941834  M.N.P.A.F. M.N.P.A.F. 8811941800")
      .load();

    views.selectData();
    transactions.checkSeries(0, "Mutuelle");
    transactions.checkSeries(1, "Mutuelle");
  }

  public void testNoAutomaticCategorisationIfSeriesIsDisable() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/05/25", -50.0, "Auchan")
      .addTransaction("2008/05/25", -50.0, "Auchan")
      .load();

    views.selectBudget();
    budgetView.envelopes.createSeries()
      .setName("Courses")
      .setCustom()
      .setStartDate(200804)
      .setEndDate(200805)
      .validate();

    budgetView.envelopes.createSeries()
      .setName("Courses_2")
      .setCustom()
      .validate();

    views.selectCategorization();

    categorization
      .selectTransactions("Auchan")
      .selectEnvelopes()
      .selectSeries("Courses");

    OfxBuilder
      .init(this)
      .addTransaction("2008/06/11", -50.0, "2_Auchan")
      .load();
    categorization
      .selectTransaction("2_Auchan")
      .checkToCategorize()
      .selectEnvelopes()
      .selectSeries("Courses_2");

    OfxBuilder
      .init(this)
      .addTransaction("2008/06/12", -50.0, "3_Auchan")
      .load();

    categorization
      .selectTransaction("3_Auchan")
      .getEnvelopes().checkSeriesIsSelected("Courses_2");
  }

}