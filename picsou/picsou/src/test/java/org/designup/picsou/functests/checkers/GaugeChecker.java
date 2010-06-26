package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.components.charts.Gauge;
import org.designup.picsou.model.util.Amounts;
import org.uispec4j.Mouse;
import org.uispec4j.Panel;

public class GaugeChecker extends GuiChecker {
  private Gauge gauge;

  public GaugeChecker(Panel panel, String componentName) {
    this(panel.findSwingComponent(Gauge.class, componentName));
  }

  public GaugeChecker(Gauge gauge) {
    this.gauge = gauge;
    Assert.assertNotNull(gauge);
  }

  Panel getPanel() {
    return new Panel(gauge);
  }

  public GaugeChecker set(double actualValue, double targetValue) {
    gauge.setValues(actualValue, targetValue);
    Assert.assertEquals(1.0, gauge.getFillPercent() + gauge.getOverrunPercent() +
                             gauge.getEmptyPercent() + gauge.getBeginPercent(), 0.01);
    return this;
  }

  public GaugeChecker set(double actualValue, double targetValue, double overrunPart, final double remaining) {
    gauge.setValues(actualValue, targetValue, overrunPart, remaining, "");
    Assert.assertEquals(1.0, gauge.getFillPercent() + gauge.getOverrunPercent() +
                             gauge.getEmptyPercent() + gauge.getBeginPercent(), 0.01);
    return this;
  }

  public GaugeChecker checkFill(double percentage) {
    Assert.assertEquals(percentage, gauge.getFillPercent(), 0.01);
    return this;
  }

  public GaugeChecker checkOverrun(double percentage, boolean isError) {
    Assert.assertEquals(percentage, gauge.getOverrunPercent(), 0.01);
    Assert.assertEquals(isError, gauge.isErrorOverrunShown());
    return this;
  }

  public GaugeChecker checkEmpty(double percentage) {
    Assert.assertEquals(percentage, gauge.getEmptyPercent(), 0.01);
    return this;
  }

  public GaugeChecker checkDescription(String text) {
    Assert.assertEquals(text, gauge.getDescription());
    Assert.assertEquals(text, gauge.getToolTipText());
    return this;
  }

  public GaugeChecker checkDescriptionContains(String text) {
    Assert.assertTrue(gauge.getDescription(), gauge.getDescription().contains(text));
    return this;
  }

  public GaugeChecker checkActualValue(double amount) {
    Assert.assertEquals(amount, gauge.getActualValue(), 0.01);
    return this;
  }

  public GaugeChecker checkTargetValue(double amount) {
    Assert.assertEquals(amount, gauge.getTargetValue(), 0.01);
    return this;
  }

  public GaugeChecker checkOnError(boolean isOnError) {
    Assert.assertTrue(isOnError == gauge.isErrorOverrunShown());
    return this;
  }

  public GaugeChecker checkBeginInError() {
    Assert.assertTrue(Amounts.isNotZero(gauge.getBeginPercent()));
    return this;
  }

  public GaugeChecker checkBegin(double value) {
    Assert.assertEquals(value, gauge.getBeginPercent(), 0.01);
    return this;
  }

  public GaugeChecker checkRemaining(double remaining) {
    Assert.assertEquals(remaining, gauge.getRemainder(), 0.01);
    return this;
  }

  public void click() {
    Mouse.click(new Panel(gauge));
  }
}
