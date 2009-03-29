package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.QifBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public class LearningTest extends LoggedInFunctionalTestCase {

  public void testLearning() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K 1")
      .addTransaction("2006/01/11", -1.1, "Fouquet's")
      .load();

    views.selectCategorization();
    categorization.setEnvelope("Menu K 1", "dej", MasterCategory.FOOD, true);

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/12", -1.3, "Menu K 2")
      .load();
    views.selectData();
    transactions.checkSeries(0, "dej");
    transactions.checkSeries(2, "dej");
  }

  public void testLearningWithCardTransactions() throws Exception {
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
    categorization.setEnvelope("Menu K", "dej", MasterCategory.FOOD, true);
    OfxBuilder
      .init(this)
      .addCardAccount("000111", -1.3, "2006/01/12")
      .addTransaction("2006/01/12", -1.3, "Menu K")
      .load();
    views.selectData();
    transactions.initContent()
      .add("12/01/2006", TransactionType.CREDIT_CARD, "Menu K", "", -1.30, "dej", MasterCategory.FOOD)
      .add("11/01/2006", TransactionType.CREDIT_CARD, "Fouquet's", "", -1.10)
      .add("10/01/2006", TransactionType.CREDIT_CARD, "Menu K", "", -1.10, "dej", MasterCategory.FOOD)
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
    categorization.setEnvelope("Menu K", "dej", MasterCategory.FOOD, true);
    categorization.selectTableRows(0)
      .selectEnvelopeSeries("resto", MasterCategory.FOOD, true);
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/12", -1.3, "Menu K")
      .load();
    views.selectData();
    transactions.checkSeries(0, "dej");

    views.selectCategorization();
    categorization.selectTableRow(2).selectEnvelopeSeries("resto", MasterCategory.FOOD, false);
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
    categorization.setEnvelope("Cheque N°1", "dej", MasterCategory.FOOD, true);
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/11", -1.1, "Cheque 2")
      .load();
    views.selectData();
    transactions.initContent()
      .add("11/01/2006", TransactionType.CHECK, "CHEQUE N°2", "", -1.10)
      .add("10/01/2006", TransactionType.CHECK, "CHEQUE N°1", "", -1.10, "dej", MasterCategory.FOOD)
      .check();
  }

  public void testLearningWithDot() throws Exception {
    QifBuilder
      .init(this)
      .addTransaction("2006/01/15", -1.1, "PRELEVEMENT 3766941826  M.N.P.A.F. M.N.P.A.F. 8811941800")
      .load(0.);

    views.selectCategorization();
    categorization.setEnvelope(0, "Mutuel", MasterCategory.FOOD, true);

    QifBuilder
      .init(this)
      .addTransaction("2006/01/15", -1.3, "PRELEVEMENT 3766941834  M.N.P.A.F. M.N.P.A.F. 8811941800")
      .load();

    views.selectData();
    transactions.checkSeries(0, "Mutuel");
    transactions.checkSeries(1, "Mutuel");

  }
}
