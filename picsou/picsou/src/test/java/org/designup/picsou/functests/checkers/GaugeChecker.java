package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.components.Gauge;
import org.uispec4j.Panel;

public class GaugeChecker extends DataChecker {
  private Gauge gauge;

  public GaugeChecker(Panel panel, String componentName) {
    this(panel.findSwingComponent(Gauge.class, componentName));
  }

  public GaugeChecker(Gauge gauge) {
    this.gauge = gauge;
    Assert.assertNotNull(gauge);
  }

  public GaugeChecker set(double actualValue, double targetValue) {
    gauge.setValues(actualValue, targetValue);
    Assert.assertEquals(1.0, gauge.getFillPercent() + gauge.getOverrunPercent() + gauge.getEmptyPercent(), 0.01);
    return this;
  }

  public GaugeChecker set(double actualValue, double targetValue, double overrunPart) {
    gauge.setValues(actualValue, targetValue, overrunPart);
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

  public void checkTooltip(String text) {
    Assert.assertEquals(text, gauge.getToolTipText());
  }

  public GaugeChecker checkActualValue(double amount) {
    Assert.assertEquals(amount, gauge.getActualValue(), 0.01);
    return this;
  }

  public GaugeChecker checkTargetValue(double amount) {
    Assert.assertEquals(amount, gauge.getTargetValue(), 0.01);
    return this;
  }

  public GaugeChecker checkOverrunPart(double amount) {
    Assert.assertEquals(amount, gauge.getOverrunPart(), 0.01);
    return this;
  }
}
