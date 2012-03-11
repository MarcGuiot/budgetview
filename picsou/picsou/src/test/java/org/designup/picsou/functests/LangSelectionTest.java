package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.OfxBuilder;

public class LangSelectionTest extends LangTestCase {
  public void setUp() throws Exception {
    resetWindow();
    setCurrentDate("2008/08/30");
    setInMemory(false);
    setDeleteLocalPrevayler(true);
    super.setUp();
    setDeleteLocalPrevayler(false);
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
}