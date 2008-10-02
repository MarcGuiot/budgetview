package org.designup.picsou.gui.components;

import junit.framework.TestCase;
import org.designup.picsou.functests.checkers.GaugeChecker;

public class GaugeTest extends TestCase {

  public void testInit() throws Exception {
    init()
      .checkFill(0)
      .checkOverrun(0, false)
      .checkEmpty(1.0)
      .checkWarningNotShown()
      .checkTooltip("No value defined");
  }

  public void testStandardCases() throws Exception {
    init()
      .set(0, 10)
      .checkFill(0)
      .checkOverrun(0, false)
      .checkEmpty(1.0)
      .checkWarningNotShown()
      .checkTooltip("Remainder: 10.00");

    init()
      .set(5, 10)
      .checkFill(0.5)
      .checkOverrun(0, false)
      .checkEmpty(0.5)
      .checkWarningNotShown()
      .checkTooltip("Remainder: 5.00");

    init()
      .set(10, 10)
      .checkFill(1.0)
      .checkOverrun(0, false)
      .checkEmpty(0)
      .checkWarningNotShown()
      .checkTooltip("Complete");
  }

  public void testOverrun() throws Exception {
    init(true, true, true)
      .set(15, 10)
      .checkFill(0.66)
      .checkOverrun(0.33, true)
      .checkEmpty(0)
      .checkWarningShown()
      .checkTooltip("Overrun: 5.00");

    init(true, false, true)
      .set(15, 10)
      .checkFill(0.66)
      .checkOverrun(0.33, true)
      .checkEmpty(0)
      .checkWarningNotShown()
      .checkTooltip("Overrun: 5.00");

    init(false, true, true)
      .set(15, 10)
      .checkFill(0.66)
      .checkOverrun(0.33, false)
      .checkEmpty(0)
      .checkWarningNotShown()
      .checkTooltip("Extra: 5.00");

    init(false, false, true)
      .set(15, 10)
      .checkFill(0.66)
      .checkOverrun(0.33, false)
      .checkEmpty(0)
      .checkWarningNotShown()
      .checkTooltip("Extra: 5.00");
  }

  public void testInvertedSign() throws Exception {
    init(true, true, true)
      .set(-5, 10)
      .checkFill(0.0)
      .checkOverrun(0.33, true)
      .checkEmpty(0.66)
      .checkWarningShown()
      .checkTooltip("Inverted: 5.00");

    init(true, false, true)
      .set(-5, 10)
      .checkFill(0.0)
      .checkOverrun(0.33, true)
      .checkEmpty(0.66)
      .checkWarningNotShown()
      .checkTooltip("Inverted: 5.00");

    init(true, false, false)
      .set(-5, 10)
      .checkFill(0.0)
      .checkOverrun(0.33, true)
      .checkEmpty(0.66)
      .checkWarningNotShown()
      .checkTooltip("Bonus: 5.00");
  }

  public void testNoTarget() throws Exception {
    init(true, true, true)
      .set(5, 0)
      .checkFill(0.0)
      .checkOverrun(1.0, true)
      .checkEmpty(0)
      .checkWarningShown()
      .checkTooltip("Overrun: 5.00");

    init(false, true, true)
      .set(5, 0)
      .checkFill(0.0)
      .checkOverrun(1.0, false)
      .checkEmpty(0)
      .checkWarningNotShown()
      .checkTooltip("Extra: 5.00");

    init(false, false, true)
      .set(5, 0)
      .checkFill(0.0)
      .checkOverrun(1.0, false)
      .checkEmpty(0)
      .checkWarningNotShown()
      .checkTooltip("Extra: 5.00");
  }

  public void testAmountsAreRoundedToTwoDecimals() throws Exception {
    init(true, true, true)
      .set(5.552, 5.551)
      .checkFill(1.0)
      .checkOverrun(0.0, false)
      .checkEmpty(0)
      .checkWarningNotShown();
  }

  private GaugeChecker init() {
    return init(true, true, true);
  }

  private GaugeChecker init(boolean overrunError, boolean showWarning, boolean invertedSignError) {
    Gauge gauge = new Gauge(overrunError, showWarning, invertedSignError);
    return new GaugeChecker(gauge);
  }
}
