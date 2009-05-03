package org.designup.picsou.model.util;

import junit.framework.TestCase;

public class AmountsTest extends TestCase {

  public void test() throws Exception {
    check(2223.9, "2,223.9");
    check(2223.9, "2,223.90");
    check(2223.9, "2223,9");
    check(2223.9, "2223,90");
    check(2223.9, "2.223,90");
    check(2223.9, "2.223,9");
    check(2223.9, "2 223,9");
    check(2223.9, "2 223.9");
    check(2223.9, "2 223.90");
    check(2223.9, "2 223,90");
    check(2223, "2223");
    check(2223, "2,223");
    check(2223, "2.223");
    check(2223, "2 223");
  }

  private void check(double expected, String input) {
    assertEquals(expected, Amounts.extractAmount(input));
  }
}
