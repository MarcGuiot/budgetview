package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public class PreferencesTest extends LoggedInFunctionalTestCase {

  public void testChangeFutureMonths() throws Exception {
    timeline.assertDisplays("2008/08");
    operations.getPreferences().changeFutureMonth(24).validate();
    timeline.assertSpanEquals("2008/08", "2010/08");
  }

  public void testChangeFutureMonthsAndBackAndAgainWithSeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/12", -95.00, "Auchan")
      .addTransaction("2008/08/04", -55.00, "EDF")
      .addTransaction("2008/08/01", 1200.00, "Salaire Aout")
      .load();

    views.selectCategorization();
    categorization.setEnvelope("Auchan", "Courant", MasterCategory.FOOD, true);
    categorization.setRecurring("EDF", "EDF", MasterCategory.HOUSE, true);
    categorization.setIncome("Salaire Aout", "Salaire", true);

    operations.getPreferences().changeFutureMonth(24).validate();

    timeline.selectLast();
    views.selectData();
    transactions.initContent()
      .add("12/08/2010", TransactionType.PLANNED, "Planned: Courant", "", -95.00, "Courant", MasterCategory.FOOD)
      .add("04/08/2010", TransactionType.PLANNED, "Planned: EDF", "", -55.00, "EDF", MasterCategory.HOUSE)
      .add("01/08/2010", TransactionType.PLANNED, "Planned: Salaire", "", 1200.00, "Salaire", MasterCategory.INCOME)
      .check();

    operations.getPreferences().changeFutureMonth(12).validate();
    timeline.assertSpanEquals("2008/08", "2009/08");
    timeline.selectLast();
    transactions.initContent()
      .add("12/08/2009", TransactionType.PLANNED, "Planned: Courant", "", -95.00, "Courant", MasterCategory.FOOD)
      .add("04/08/2009", TransactionType.PLANNED, "Planned: EDF", "", -55.00, "EDF", MasterCategory.HOUSE)
      .add("01/08/2009", TransactionType.PLANNED, "Planned: Salaire", "", 1200.00, "Salaire", MasterCategory.INCOME)
      .check();

    operations.getPreferences().changeFutureMonth(36).validate();

    timeline.selectLast();
    views.selectData();
    transactions.initContent()
      .add("12/08/2011", TransactionType.PLANNED, "Planned: Courant", "", -95.00, "Courant", MasterCategory.FOOD)
      .add("04/08/2011", TransactionType.PLANNED, "Planned: EDF", "", -55.00, "EDF", MasterCategory.HOUSE)
      .add("01/08/2011", TransactionType.PLANNED, "Planned: Salaire", "", 1200.00, "Salaire", MasterCategory.INCOME)
      .check();

    operations.getPreferences().changeFutureMonth(12).validate();
    timeline.assertEmpty();
  }
}
