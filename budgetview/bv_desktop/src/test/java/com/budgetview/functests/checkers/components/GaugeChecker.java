package com.budgetview.functests.checkers.components;

import com.budgetview.gui.components.charts.Gauge;
import com.budgetview.shared.utils.Amounts;
import junit.framework.Assert;
import org.globsframework.utils.Strings;
import org.uispec4j.AbstractUIComponent;
import org.uispec4j.Mouse;
import org.uispec4j.Panel;
import org.uispec4j.Trigger;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;

public class GaugeChecker extends AbstractUIComponent {

  public static final String TYPE_NAME = "gauge";
  public static final Class[] SWING_CLASSES = {Gauge.class};

  private Gauge gauge;

  public GaugeChecker(Panel panel, String componentName) {
    this(panel.findSwingComponent(Gauge.class, componentName));
  }

  public GaugeChecker(Gauge gauge) {
    this.gauge = gauge;
    Assert.assertNotNull(gauge);
  }

  public Panel getPanel() {
    return new Panel(gauge);
  }

  public GaugeChecker set(double actualValue, double targetValue) {
    gauge.getModel().setValues(actualValue, targetValue);
    Assert.assertEquals(1.0, gauge.getFillPercent() + gauge.getOverrunPercent() +
                             gauge.getEmptyPercent() + gauge.getBeginPercent(), 0.01);
    return this;
  }

  public GaugeChecker set(double actualValue, double targetValue, double overrunPart, final double remaining) {
    gauge.getModel().setValues(actualValue, targetValue, overrunPart, remaining, "", false);
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

  public GaugeChecker checkTooltip(String text) {
    Assert.assertEquals(text, cleanup(gauge.getToolTipText()));
    Assert.assertEquals(text, cleanup(gauge.getToolTipText()));
    return this;
  }

  private Object cleanup(String text) {
    return text.replace("<html>", "").replace("</html>", "");
  }

  public GaugeChecker checkDescriptionContains(final String text) {
    UISpecAssert.assertThat(new Assertion() {
      public void check() {
        if (!gauge.getToolTipText().contains(text)) {
          Assert.fail("Toolip: " + gauge.getToolTipText() + "' does not contain\n" +
                      "        " + text);
        }
      }
    });
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

  public String getLabel() {
    return gauge.getLabel();
  }

  public void click() {
    Mouse.click(new Panel(gauge));
  }

  public Trigger triggerClick() {
    return new Trigger() {
      public void run() throws Exception {
        click();
      }
    };
  }

  public Panel getContainer() {
    return new Panel(gauge.getParent());
  }

  public Gauge getAwtComponent() {
    return gauge;
  }

  public String getDescriptionTypeName() {
    return TYPE_NAME;
  }

  public Assertion tooltipContains(final String tooltipText) {
    return new Assertion() {
      public void check() {
        String actualTooltip = gauge.getToolTipText();
        if (Strings.isNullOrEmpty(actualTooltip)) {
          Assert.fail("Actual tooltip is empty");
        }
        if (!actualTooltip.contains(tooltipText)) {
          Assert.fail("Actual tooltip '" + actualTooltip + "' does not contain: " + tooltipText);
        }
      }
    };
  }
}
