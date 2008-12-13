package org.designup.picsou.gui.components;

import junit.framework.TestCase;
import org.designup.picsou.functests.checkers.GaugeChecker;

public class GaugeTest extends TestCase {

  public void testInit() throws Exception {
    init()
      .checkFill(0)
      .checkOverrun(0, false)
      .checkEmpty(1.0)
      .checkTooltip("No value defined");
  }

  public void testStandardCases() throws Exception {
    init()
      .set(0, 10)
      .checkFill(0)
      .checkOverrun(0, false)
      .checkEmpty(1.0)
      .checkTooltip("Remainder: 10.00");

    init()
      .set(5, 10)
      .checkFill(0.5)
      .checkOverrun(0, false)
      .checkEmpty(0.5)
      .checkTooltip("Remainder: 5.00");

    init()
      .set(10, 10)
      .checkFill(1.0)
      .checkOverrun(0, false)
      .checkEmpty(0)
      .checkTooltip("Complete");
  }

  public void testOverrun() throws Exception {
    init(true, true)
      .set(15, 10)
      .checkFill(0.66)
      .checkOverrun(0.33, true)
      .checkEmpty(0)
      .checkTooltip("Overrun: 5.00");

    init(false, true)
      .set(15, 10)
      .checkFill(0.66)
      .checkOverrun(0.33, false)
      .checkEmpty(0)
      .checkTooltip("Extra: 5.00");
  }

  public void testInvertedSign() throws Exception {
    init(true, true)
      .set(-5, 10)
      .checkFill(0.0)
      .checkOverrun(0.33, true)
      .checkEmpty(0.66)
      .checkTooltip("Inverted: 5.00");

    init(true, false)
      .set(-5, 10)
      .checkFill(0.0)
      .checkOverrun(0.33, true)
      .checkEmpty(0.66)
      .checkTooltip("Bonus: 5.00");
  }

  public void testNoTarget() throws Exception {
    init(true, true)
      .set(5, 0)
      .checkFill(0.0)
      .checkOverrun(1.0, true)
      .checkEmpty(0)
      .checkTooltip("Overrun: 5.00");

    init(false, true)
      .set(5, 0)
      .checkFill(0.0)
      .checkOverrun(1.0, false)
      .checkEmpty(0)
      .checkTooltip("Extra: 5.00");
  }

  public void testAmountsAreRoundedToTwoDecimals() throws Exception {
    init(true, true)
      .set(5.552, 5.551)
      .checkFill(1.0)
      .checkOverrun(0.0, false)
      .checkEmpty(0);
  }

  public void testPartialOverrun() throws Exception {
    init(true, false)
      .set(10.0, 20.0, 3.0)
      .checkFill((10.0 - 3.0) / 20.0)
      .checkOverrun(3.0 / 20.0, true)
      .checkEmpty(10.0 / 20.0)
      .checkTooltip("<html>" +
                    "<p>Remainder: 10.00</p>" +
                    "<p>Overrun: 3.00</p>" +
                    "</html>");

    init(false, false)
      .set(10.0, 20.0, 3.0)
      .checkFill((10.0 - 3.0) / 20.0)
      .checkOverrun(3.0 / 20.0, false)
      .checkEmpty(10.0 / 20.0)
      .checkTooltip("<html>" +
                    "<p>Remainder: 10.00</p>" +
                    "<p>Extra: 3.00</p>" +
                    "</html>");
  }

  public void testPartialOverrunIsIgnoredIfActualGreatedThanTarget() throws Exception {
    init(true, false)
      .set(40.0, 20.0, 5.0)
      .checkFill(0.5)
      .checkOverrun(0.5, true)
      .checkEmpty(0);
  }

  private GaugeChecker init() {
    return init(true, true);
  }

  private GaugeChecker init(boolean overrunError, boolean invertedSignError) {
    Gauge gauge = new Gauge(overrunError, invertedSignError);
    return new GaugeChecker(gauge);
  }
}
