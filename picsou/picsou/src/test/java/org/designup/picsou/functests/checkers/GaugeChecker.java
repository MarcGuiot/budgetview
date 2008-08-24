package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.components.Gauge;

public class GaugeChecker extends DataChecker {
  private Gauge gauge;

  public GaugeChecker(Gauge gauge) {
    this.gauge = gauge;
  }

  public GaugeChecker set(double actualValue, double targetValue) {
    gauge.setValues(actualValue, targetValue);
    Assert.assertEquals(1.0, gauge.getFillPercent() + gauge.getOverrunPercent() + gauge.getEmptyPercent(), 0.01);
    return this;
  }

  public GaugeChecker checkFill(double percentage) {
    Assert.assertEquals(percentage, gauge.getFillPercent(), 0.01);
    return this;
  }

  public GaugeChecker checkOverrun(double percentage, boolean isError) {
    Assert.assertEquals(percentage, gauge.getOverrunPercent(), 0.01);
    Assert.assertEquals(isError, gauge.isOverrunErrorShown());
    return this;
  }

  public GaugeChecker checkEmpty(double percentage) {
    Assert.assertEquals(percentage, gauge.getEmptyPercent(), 0.01);
    return this;
  }

  public GaugeChecker checkWarningShown() {
    Assert.assertTrue(gauge.isWarningShown());
    return this;
  }

  public GaugeChecker checkWarningNotShown() {
    Assert.assertFalse(gauge.isWarningShown());
    return this;
  }
}
