package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public class LearningTest extends LoggedInFunctionalTestCase {

  public void testLearning() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .addTransaction("2006/01/11", -1.1, "MiamMiam")
      .load();
    views.selectCategorization();
    categorization.setEnvelope("Menu K", "dej", MasterCategory.FOOD, true);
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/12", -1.3, "Menu K")
      .load();
    views.selectData();
    transactions.initContent()
      .add("12/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.30, "dej", MasterCategory.FOOD)
      .add("11/01/2006", TransactionType.PRELEVEMENT, "MiamMiam", "", -1.10, "To categorize")
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.10, "dej", MasterCategory.FOOD)
      .check();
  }

  public void testTakeThreeLastTransaction() throws Exception {
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

  public void testIgnoreCheck() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Cheque 1")
      .load();
    views.selectCategorization();
    categorization.checkTable(new Object[][]{});
    categorization.setEnvelope("Cheque 1", "dej", MasterCategory.FOOD, true);
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/11", -1.1, "Cheque 2")
      .load();
    views.selectData();
    transactions.checkSeries(1, "To categorize");
  }
}
