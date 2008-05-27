package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public class InformationViewTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    OfxBuilder
      .init(this)
      .addTransactionWithNote("2006/04/01", 12.00, "mac do", "", MasterCategory.FOOD)
      .addTransactionWithNote("2006/04/03", 10.50, "fouquets", "", MasterCategory.FOOD)
      .addTransactionWithNote("2006/05/01", -70.00, "essence", "frais pro", MasterCategory.TRANSPORTS)
      .addTransactionWithNote("2006/05/03", -30.00, "peage", "", MasterCategory.TRANSPORTS)
      .addTransactionWithNote("2006/05/02", -200.00, "cic", "", MasterCategory.BANK)
      .addTransactionWithNote("2006/05/06", -100.00, "nounou", "nourrice", MasterCategory.HOUSE, MasterCategory.EDUCATION)
      .load();
  }

  public void testNoWarningDisplayedIfNoMultiCategories() throws Exception {
    periods.selectCell(0);
    transactions.initContent()
      .add("03/04/2006", TransactionType.VIREMENT, "fouquets", "", 10.50, MasterCategory.FOOD)
      .add("01/04/2006", TransactionType.VIREMENT, "mac do", "", 12.00, MasterCategory.FOOD)
      .check();
    informationPanel.assertNoWarningIsDisplayed();
  }

  public void testWarningIsDisplayedIfMultiCategories() throws Exception {
    periods.selectCell(1);
    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION, MasterCategory.HOUSE)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "peage", "", -30.00, MasterCategory.TRANSPORTS)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "cic", "", -200.00, MasterCategory.BANK)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.TRANSPORTS)
      .check();
    informationPanel.assertWarningIsDisplayed();
  }

  public void testWarningDisappearsWhenASingleCategoryIsChosen() throws Exception {
    periods.selectCell(1);
    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION, MasterCategory.HOUSE)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "peage", "", -30.00, MasterCategory.TRANSPORTS)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "cic", "", -200.00, MasterCategory.BANK)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.TRANSPORTS)
      .check();
    informationPanel.assertWarningIsDisplayed();

    transactions.assignCategory(MasterCategory.EDUCATION, 0);
    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "peage", "", -30.00, MasterCategory.TRANSPORTS)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "cic", "", -200.00, MasterCategory.BANK)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.TRANSPORTS)
      .check();
    informationPanel.assertNoWarningIsDisplayed();
  }
}
