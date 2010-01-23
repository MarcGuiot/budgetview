package org.designup.picsou.gui.components;

import junit.framework.TestCase;
import org.designup.picsou.functests.checkers.GaugeChecker;
import org.designup.picsou.gui.components.charts.Gauge;

import java.util.Locale;

public class GaugeTest extends TestCase {
  protected void setUp() throws Exception {
    super.setUp();
    Locale.setDefault(Locale.ENGLISH);
  }

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
      .checkTooltip("Expected: 10.00");

    init()
      .set(5, 10)
      .checkFill(0.5)
      .checkOverrun(0, false)
      .checkEmpty(0.5)
      .checkTooltip("Expected: 5.00");

    init()
      .set(10, 10)
      .checkFill(1.0)
      .checkOverrun(0, false)
      .checkEmpty(0)
      .checkTooltip("Complete");
  }

  public void testOverrun() throws Exception {
    init()
      .set(-15, -10)
      .checkFill(0.66)
      .checkOverrun(0.33, true)
      .checkEmpty(0)
      .checkTooltip("Overrun: 5.00");

    init()
      .set(15, 10)
      .checkFill(0.66)
      .checkOverrun(0.33, false)
      .checkEmpty(0)
      .checkTooltip("Extra: 5.00");
  }

  public void testInvertedSign() throws Exception {
    init()
      .set(-5, 10)
      .checkFill(0.0)
      .checkBegin(0.33)
      .checkEmpty(0.66)
      .checkTooltip("Expected: 15.00");

    init()
      .set(5, -10)
      .checkFill(0.33)
      .checkBegin(0)
      .checkEmpty(0.66)
      .checkTooltip("Remainder: 15.00");
  }

  public void testNoTarget() throws Exception {
    init()
      .set(-5, 0)
      .checkFill(0.0)
      .checkOverrun(1.0, true)
      .checkEmpty(0)
      .checkTooltip("Overrun: 5.00");

    init()
      .set(5, 0)
      .checkFill(0.0)
      .checkOverrun(1.0, false)
      .checkEmpty(0)
      .checkTooltip("Extra: 5.00");
  }

  public void testAmountsAreRoundedToTwoDecimals() throws Exception {
    init()
      .set(5.552, 5.551)
      .checkFill(1.0)
      .checkOverrun(0.0, false)
      .checkEmpty(0);
  }

  public void testPartialOverrun() throws Exception {
    init()
      .set(10.0, 20.0, 3.0, 10)
      .checkFill((10.0 - 3.0) / 20.0)
      .checkOverrun(3.0 / 20.0, false)
      .checkEmpty(10.0 / 20.0);

    init()
      .set(10.0, 20.0, 3.0, 10)
      .checkFill((10.0 - 3.0) / 20.0)
      .checkOverrun(3.0 / 20.0, false)
      .checkEmpty(10.0 / 20.0);
  }

  public void testOverrunAndRemaining() throws Exception {
    init()
      .set(10, 20, 10, 0);
  }

  public void testPartialOverrunIsIgnoredIfActualGreaterThanTarget() throws Exception {
    init()
      .set(40.0, 20.0, 20.0, 0)
      .checkFill(0.5)
      .checkOverrun(0.5, false)
      .checkEmpty(0);
  }

  private GaugeChecker init() {
    Gauge gauge = new Gauge(false);
    return new GaugeChecker(gauge);
  }
}
