package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.checkers.CategorizationDialogChecker;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public class UncategorizedMessageTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    OfxBuilder
      .init(this)
      .addTransaction("2008/04/15", 12.00, "mac do")
      .addTransaction("2008/03/15", 100.00, "fouquets")
      .load();
  }

  public void testCategorizationFromWarningMessage() throws Exception {
    views.selectHome();

    CategorizationDialogChecker categorizer1 = informationPanel.categorize();
    categorizer1.selectOccasional();
    categorizer1.selectOccasionalSeries(MasterCategory.FOOD);
    categorizer1.validate();

    informationPanel.assertWarningIsDisplayed(1);

    CategorizationDialogChecker categorizer2 = informationPanel.categorize();
    categorizer2.selectOccasional();
    categorizer2.selectOccasionalSeries(MasterCategory.FOOD);
    categorizer2.validate();

    informationPanel.assertNoWarningIsDisplayed();
  }

  public void testCategorizationFromTransactionDetailsView() throws Exception {
    timeline.selectMonths("2008/03","2008/04");
    transactions.initContent()
      .add("15/04/2008", TransactionType.VIREMENT, "mac do", "", 12.00)
      .add("15/03/2008", TransactionType.VIREMENT, "fouquets", "", 100.00)
      .check();

    views.selectHome();
    informationPanel.assertWarningIsDisplayed(2);

    views.selectData();
    transactions.setOccasional(0, MasterCategory.FOOD);

    views.selectHome();
    informationPanel.assertWarningIsDisplayed(1);

    views.selectData();
    transactions.setOccasional(1, MasterCategory.FOOD);

    views.selectHome();
    informationPanel.assertNoWarningIsDisplayed();
  }
}
