package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public class UncategorizedMessageTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    OfxBuilder
      .init(this)
      .addTransactionWithNote("2006/04/01", 12.00, "mac do", "", MasterCategory.FOOD)
      .addTransactionWithNote("2006/04/03", 10.50, "fouquets", "", MasterCategory.FOOD)
      .addTransactionWithNote("2006/05/01", -70.00, "essence", "frais pro", MasterCategory.TRANSPORTS)
      .addTransactionWithNote("2006/05/03", -30.00, "peage", "", MasterCategory.TRANSPORTS)
      .addTransactionWithNote("2006/05/02", -200.00, "cic", "")
      .addTransactionWithNote("2006/05/06", -100.00, "nounou", "nourrice", MasterCategory.EDUCATION, MasterCategory.HOUSE)
      .load();
  }

  public void testNoWarningDisplayedIfNoMultiCategories() throws Exception {
    periods.selectCell(0);
    transactions.initContent()
      .add("03/04/2006", TransactionType.VIREMENT, "fouquets", "", 10.50, MasterCategory.FOOD)
      .add("01/04/2006", TransactionType.VIREMENT, "mac do", "", 12.00, MasterCategory.FOOD)
      .check();

    views.selectHome();
    informationPanel.assertNoWarningIsDisplayed();
  }

  public void testWarningIsDisplayedIfUnassignedTransactionsAreShown() throws Exception {
    periods.selectCell(1);
    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION, MasterCategory.HOUSE)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "peage", "", -30.00, MasterCategory.TRANSPORTS)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "cic", "", -200.00)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.TRANSPORTS)
      .check();

    views.selectHome();
    informationPanel.assertWarningIsDisplayed();
  }

  public void testWarningDisappearsWhenACategoryIsChosen() throws Exception {
    periods.selectCell(1);
    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION, MasterCategory.HOUSE)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "peage", "", -30.00, MasterCategory.TRANSPORTS)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "cic", "", -200.00)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.TRANSPORTS)
      .check();

    views.selectHome();
    informationPanel.assertWarningIsDisplayed();

    views.selectData();
    transactions.assignCategory(MasterCategory.EDUCATION, 0);

    views.selectHome();
    informationPanel.assertWarningIsDisplayed();

    views.selectData();
    transactions.assignCategory(MasterCategory.BANK, 2);

    views.selectHome();
    informationPanel.assertNoWarningIsDisplayed();
  }
}
