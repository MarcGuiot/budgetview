package org.designup.picsou.functests.general;

import org.designup.picsou.functests.checkers.ImportDialogChecker;
import org.designup.picsou.functests.utils.LangTestCase;

import java.util.Locale;

public class LangEnTest extends LangTestCase {

  protected Locale getDefaultLocale() {
    return Locale.ENGLISH;
  }

  public void test() throws Exception {
    loadSingleTransaction("2008/08/15", "Auchan", "2008/08/20");
    checkDates("08/15/2008", "To categorize", "08/15/2008", "August 11, 2008", "Account n. 007");
    checkBankListContains("Other");
  }

  protected String getAccountName() {
    return "Account n. 007";
  }
}
