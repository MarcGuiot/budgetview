package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.LicenseChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;
import org.globsframework.utils.Dates;

public class PlanificationTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentDate(Dates.parse("2008/07/01"));
    super.setUp();
  }

  public void testFirstSeriesInitialization() throws Exception {
    LicenseChecker license = new LicenseChecker(mainWindow);
    license.enterLicense("admin", "", 24);
    OfxBuilder.init(this)
      .addTransaction("2008/07/08", -29.9, "free telecom")
      .load();
    timeline.assertSpanEquals("2008/07", "2010/07");
    transactions.setRecurring(0, "Internet", MasterCategory.TELECOMS, true);
    timeline.selectMonth("2008/07");
    transactions.initContent()
      .add("08/07/2008", TransactionType.PRELEVEMENT, "free telecom", "", -29.90, "Internet")
      .check();
    views.selectHome();
    timeline.selectMonth("2008/07");
    monthSummary.init()
      .checkRecurring(29.9)
      .checkPlannedRecurring(29.9);
    timeline.selectMonth("2008/08");
    transactions.initContent()
      .add("08/08/2008", TransactionType.PLANNED, "Internet", "", -29.90, "Internet")
      .check();
    monthSummary.init()
      .checkRecurring(0)
      .checkPlannedRecurring(29.9);
  }
}
