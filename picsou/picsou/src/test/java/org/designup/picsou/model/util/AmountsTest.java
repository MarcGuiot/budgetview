package org.designup.picsou.model.util;

import junit.framework.TestCase;

public class AmountsTest extends TestCase {

  public void testExtractAmount() throws Exception {
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
    checkExtract(2223, "2,223");
    checkExtract(2223, "2.223");
    checkExtract(2223, "2 223");
    checkExtract(-2223, "-2 223");
    checkExtract(2223, "2" + '\u00A0' + "223,00 ");
    checkExtract(2223, "2" + '\u00A0' + "223,00" + '\u00A0');
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
