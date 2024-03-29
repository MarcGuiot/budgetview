package com.budgetview.functests.general;

import com.budgetview.functests.utils.LangTestCase;
import org.junit.Test;

import java.util.Locale;

public class LangFrTest extends LangTestCase {

  protected Locale getDefaultLocale() {
    return Locale.FRENCH;
  }

  @Test
  public void test() throws Exception {
    loadSingleTransaction("2008/08/15", "Auchan", "2008/08/20");
    checkDates("15/08/2008", "À classer", "15/08/2008", "11 août 2008", "Compte 007");
  }

  protected String getAccountName() {
    return "Compte 007";
  }
}
