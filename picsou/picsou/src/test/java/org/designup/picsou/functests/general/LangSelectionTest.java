package org.designup.picsou.functests.general;

import org.designup.picsou.functests.utils.LangTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.utils.Lang;

public class LangSelectionTest extends LangTestCase {
  public void setUp() throws Exception {
    resetWindow();
    setCurrentDate("2008/08/30");
    setInMemory(false);
    setDeleteLocalPrevayler(true);
    super.setUp();
    setDeleteLocalPrevayler(false);
  }

  protected void tearDown() throws Exception {
    resetWindow();
    super.tearDown();
  }

  protected String getAccountName() {
    return "Account n. 007";
  }

  public void testDateFormats() throws Exception {

    OfxBuilder.init(this)
      .addBankAccount("007", 1000.00, "2008/08/20")
      .addTransaction("2008/08/15", -100.00, "Auchan")
      .load();

    checkDates("2008/08/15", "2008/08/15", "August 11, 2008");

    operations.openPreferences()
      .checkTextDateSelected("month day, year")
      .selectTextDate("day month year")
      .checkNumericDateSelected("yyyy/mm/dd")
      .selectNumericDate("dd/mm/yyyy")
      .validate();

    checkDates("15/08/2008", "15/08/2008", "11 August 2008");

    operations.openPreferences()
      .selectTextDate("month day, year")
      .selectNumericDate("mm/dd/yyyy")
      .validate();

    checkDates("08/15/2008", "08/15/2008", "August 11, 2008");

    restartApplication();

    operations.openPreferences()
      .checkTextDateSelected("month day, year")
      .selectTextDate("day month year")
      .checkNumericDateSelected("mm/dd/yyyy")
      .selectNumericDate("dd/mm/yyyy")
      .validate();

    checkDates("15/08/2008", "15/08/2008", "11 August 2008");
  }

  public void testChangeLang() throws Exception {
    operations.openPreferences()
      .setLang("fr")
      .validate();
    restartApplication();

    assertEquals("fr", Lang.getLang());

    operations.openPreferences()
      .setLang("en")
      .validate();

    restartApplication();
    assertEquals("en", Lang.getLang());
  }
}
