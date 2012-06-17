package org.designup.picsou.functests.general;

import org.designup.picsou.functests.utils.LangTestCase;

import java.util.Locale;

public class LangFrTest extends LangTestCase {

  protected Locale getDefaultLocale() {
    return Locale.FRENCH;
  }

  public void test() throws Exception {
    loadSingleTransaction("2008/08/15", "Auchan", "2008/08/20");
    checkDates("15/08/2008", "À classer", "15/08/2008", "11 Août 2008", "Compte 007");
    operations.openImportDialog()
      .getBankDownload()
      .checkContainsBanks("Autre");
  }
}
