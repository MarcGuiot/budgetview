package org.designup.picsou.gui.transactions;

import junit.framework.TestCase;

public class TimeCostTest extends TestCase {

  public void test() throws Exception {
    check(0, "");
    check(-1, "5min");
    check(-2.5, "15min");
    check(10, "1h");
    check(11, "1h05");
    check(15, "1h30");
    check(-20, "2h");
    check(90, "9h");
    check(-100, "1j");
    check(102, "1j");
    check(-110, "1j 1h");
    check(136, "1j 3h");
    check(200, "2j");
  }

  public void testLimits() throws Exception {
    assertEquals("", TimeCost.get(0, 10, 10));
    assertEquals("", TimeCost.get(10, 0, 10));
    assertEquals("", TimeCost.get(10, 10, 0));
  }

  private void check(double amount, String result) {
    assertEquals(result, TimeCost.get(amount, 10, 10));
  }
}
