package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;

public class ViewsManagementTest extends LoggedInFunctionalTestCase {
  public void testHomePage() throws Exception {
    views.selectHome();
    views.checkHomeSelected();
    transactions.assertVisible(false);
  }

  public void testNoData() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2005/03/10", -10, "rent", MasterCategory.HOUSE)
      .addTransaction("2005/01/10", +50, "income")
      .load();

    transactions.assertVisible(true);
    categories.assertVisible(true);

    categories.select(MasterCategory.BANK);
    categories.assertVisible(true);
    transactions.assertVisible(true);
    transactions.assertEmpty();

    categories.selectNone();
    categories.assertVisible(true);
    transactions.assertVisible(true);
    transactions.assertEmpty();

    categories.select(MasterCategory.HOUSE);
    categories.assertVisible(true);
    transactions.assertVisible(true);

    timeline.selectMonth("2005/02");
    categories.assertVisible(true);
    transactions.assertVisible(true);
    transactions.assertEmpty();

    timeline.selectMonth("2005/03");
    categories.assertVisible(true);
    transactions.assertVisible(true);
  }
}
