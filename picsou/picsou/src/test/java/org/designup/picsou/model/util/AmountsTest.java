package org.designup.picsou.model.util;

import com.budgetview.shared.utils.Amounts;
import junit.framework.TestCase;
import org.globsframework.utils.TestUtils;

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
    checkExtract(-2223, "--2 223");
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

  public void testSplit() throws Exception {
    TestUtils.assertEquals(Amounts.split(500.00, 3), 166.00, 166.00, 168.00);
    TestUtils.assertEquals(Amounts.split(-160.00, 3), -54.00, -54.00, -52.00);
    TestUtils.assertEquals(Amounts.split(-160.00, 0));
    TestUtils.assertEquals(Amounts.split(-160.00, 2), -80.00, -80.00);
    TestUtils.assertEquals(Amounts.split(0.00, 2), 0, 0);
    TestUtils.assertEquals(Amounts.split(0.00, 0));
  }

  public void testReduceTotal() throws Exception {
    TestUtils.assertEquals(Amounts.adjustTotal(new Double[]{500.00, 250.00, 50.00}, 400.00), 250.00, 125.00, 25.00);
    TestUtils.assertEquals(Amounts.adjustTotal(new Double[]{500.00, 250.00, 50.00}, 500.00), 312.50, 156.25, 31.25);
    TestUtils.assertEquals(Amounts.adjustTotal(new Double[]{-50.00, 50.00}, 200.00), 50.00, 150.00);
    TestUtils.assertEquals(Amounts.adjustTotal(new Double[]{-20.00, 80.00}, 200.00), 50.00, 150.00);
  }
}
