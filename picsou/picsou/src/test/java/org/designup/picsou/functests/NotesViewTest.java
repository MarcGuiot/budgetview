package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class NotesViewTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    setCurrentDate("2008/08/27");
  }

  public void testNotes() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/15", 1000, "Company")
      .addTransaction("2008/05/15", -100, "FNAC")
      .load();

    views.selectHome();
    notes.checkText("Click here to enter your own notes");

    notes.setText("One note");
    notes.checkText("One note");
  }

  public void testMonthTooltipWithNoPositionAvailable() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000123", 100, "2008/08/15")
      .addTransaction("2008/07/26", 1000, "WorldCo")
      .load();

    operations.openPreferences().setFutureMonthsCount(1).validate();

    timeline.checkMonthTooltip("2008/07", 1000, 100);
    timeline.checkMonthTooltip("2008/08", 0, 100);
  }
}
