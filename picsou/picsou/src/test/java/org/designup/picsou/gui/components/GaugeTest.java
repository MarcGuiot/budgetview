package org.designup.picsou.gui.components;

import junit.framework.TestCase;
import org.designup.picsou.functests.checkers.GaugeChecker;

public class GaugeTest extends TestCase {

  public void testInit() throws Exception {
    init()
      .checkFill(0)
      .checkOverrun(0, false)
      .checkEmpty(1.0)
      .checkWarningNotShown();
  }

  public void testStandardCases() throws Exception {
    init()
      .set(0, 10)
      .checkFill(0)
      .checkOverrun(0, false)
      .checkEmpty(1.0)
      .checkWarningNotShown();

    init()
      .set(5, 10)
      .checkFill(0.5)
      .checkOverrun(0, false)
      .checkEmpty(0.5)
      .checkWarningNotShown();

    init()
      .set(10, 10)
      .checkFill(1.0)
      .checkOverrun(0, false)
      .checkEmpty(0)
      .checkWarningNotShown();
  }

  public void testOverrun() throws Exception {
    init(true, true, true)
      .set(11, 10)
      .checkFill(0.9)
      .checkOverrun(0.1, true)
      .checkEmpty(0)
      .checkWarningShown();

    init(true, false, true)
      .set(11, 10)
      .checkFill(0.9)
      .checkOverrun(0.1, true)
      .checkEmpty(0)
      .checkWarningNotShown();

    init(false, true, true)
      .set(11, 10)
      .checkFill(0.9)
      .checkOverrun(0.1, false)
      .checkEmpty(0)
      .checkWarningNotShown();

    init(false, false, true)
      .set(11, 10)
      .checkFill(0.9)
      .checkOverrun(0.1, false)
      .checkEmpty(0)
      .checkWarningNotShown();
  }

  public void testInvertedSign() throws Exception {
    init(true, true, true)
      .set(-5, 10)
      .checkFill(0.0)
      .checkOverrun(0.33, true)
      .checkEmpty(0.66)
      .checkWarningShown();

    init(true, false, true)
      .set(-5, 10)
      .checkFill(0.0)
      .checkOverrun(0.33, true)
      .checkEmpty(0.66)
      .checkWarningNotShown();
  }

  public void testNoTarget() throws Exception {
    init(true, true, true)
      .set(5, 0)
      .checkFill(0.0)
      .checkOverrun(1.0, true)
      .checkEmpty(0)
      .checkWarningShown();

    init(false, true, true)
      .set(5, 0)
      .checkFill(0.0)
      .checkOverrun(1.0, false)
      .checkEmpty(0)
      .checkWarningNotShown();

    init(false, false, true)
      .set(5, 0)
      .checkFill(0.0)
      .checkOverrun(1.0, false)
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
