package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public class DispensabilityTest extends LoggedInFunctionalTestCase {
  public void test() throws Exception {

    categories.createSubCategory(MasterCategory.HEALTH, "Docteur");

    OfxBuilder
      .init(this)
      .addCategory(MasterCategory.HEALTH, "Docteur")
      .addTransaction("2006/01/10", -1.0, "Station BP", MasterCategory.TRANSPORTS)
      .addTransaction("2006/01/12", -3.0, "Dr Lecter", "Docteur")
      .addTransaction("2006/01/13", -12.0, "Bricomachin", MasterCategory.HOUSE)
      .addTransaction("2006/01/14", -19.0, "Casto", MasterCategory.HOUSE)
      .load();

    categories.select(MasterCategory.ALL);
    transactions
      .initContent()
      .add("14/01/2006", TransactionType.PRELEVEMENT, "Casto", "", -19.0, MasterCategory.HOUSE)
      .add("13/01/2006", TransactionType.PRELEVEMENT, "Bricomachin", "", -12.0, MasterCategory.HOUSE)
      .add("12/01/2006", TransactionType.PRELEVEMENT, "Dr Lecter", "", -3.0, "Docteur")
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Station BP", "", -1.0, MasterCategory.TRANSPORTS)
      .check();

    categories.assertDispensabilityEquals(0.0, MasterCategory.ALL);

    categories.assertDispensabilityEquals(0, MasterCategory.HOUSE);
    transactions.toggleDispensable(0);
    categories.assertDispensabilityEquals(19.0, MasterCategory.HOUSE);

    categories.assertExpanded(MasterCategory.HEALTH, true);
    categories.assertDispensabilityEquals(0, "Docteur");
    transactions.toggleDispensable(2);
    categories.assertDispensabilityEquals(3.0, "Docteur");

    categories.assertDispensabilityEquals(22.0, MasterCategory.ALL);
  }
}
