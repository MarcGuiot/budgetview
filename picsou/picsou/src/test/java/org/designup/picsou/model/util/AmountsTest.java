package org.designup.picsou.model.util;

import com.budgetview.shared.utils.Amounts;
import junit.framework.TestCase;

public class AmountsTest extends TestCase {

  public void testExtractAmount() throws Exception {
    checkExtract(0.2, "0.2");
    checkExtract(0.2, "0,20");
    checkExtract(0.20, "0.2");
    checkExtract(0.02, "0.02");
    checkExtract(6, "6");
    checkExtract(33, "33");
    checkExtract(100, "100");
    checkExtract(100.2, "100,2");
    checkExtract(100.23, "100,23");
    checkExtract(2223.9, "2,223.9");
    checkExtract(2223.9, "2,223.90");
    checkExtract(2223.9, "2223,9");
    checkExtract(2223.9, "2223,90");
    checkExtract(2223.9, "2.223,90");
    checkExtract(2223.9, "2.223,9");
    checkExtract(2223.9, "2 223,9");
    checkExtract(2223.9, "2 223.9");
    checkExtract(2223.9, "2 223.90");
    checkExtract(2223.9, "2 223,90");
    checkExtract(2223, "2223");
    checkExtract(2.223, "2,223");
    checkExtract(2.223, "2.223");
    checkExtract(2223, "2 223");
    checkExtract(-2223, "-2 223");
    checkExtract(2223, "2" + '\u00A0' + "223,00 ");
    checkExtract(2223, "2" + '\u00A0' + "223,00" + '\u00A0');

    checkExtract(1222.223, "1,222.223");
    checkExtract(1222.2, "1,222.2");
    checkExtract(1222., "1,222.");

    checkExtract(13745.40, "13'745.40"); // en suisse
  }

  private void checkExtract(double expected, String input) {
    assertEquals(expected, Amounts.extractAmount(input));
  }

  public void testUpperOrder() throws Exception {
    assertEquals(0.0, Amounts.upperOrder(0));
    assertEquals(10.0, Amounts.upperOrder(9));
    assertEquals(100.0, Amounts.upperOrder(80));
    assertEquals(1000.0, Amounts.upperOrder(999));
  }
}
