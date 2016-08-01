package com.budgetview.functests.checkers.components;

import com.budgetview.gui.components.charts.DeltaGauge;
import com.budgetview.gui.description.Formatting;
import junit.framework.Assert;
import org.uispec4j.AbstractUIComponent;
import org.uispec4j.Mouse;
import org.uispec4j.Panel;
import org.uispec4j.Trigger;

public class DeltaGaugeChecker extends AbstractUIComponent {

  public static final String TYPE_NAME = "deltaGauge";
  public static final Class[] SWING_CLASSES = {DeltaGauge.class};

  private DeltaGauge gauge;

  public DeltaGaugeChecker(Panel panel, String componentName) {
    this(panel.findSwingComponent(DeltaGauge.class, componentName));
  }

  public DeltaGaugeChecker(DeltaGauge gauge) {
    this.gauge = gauge;
    Assert.assertNotNull(gauge);
  }

  Panel getPanel() {
    return new Panel(gauge);
  }

  public DeltaGaugeChecker check(Double previousValue, Double newValue, double ratio, String tooltip) {
    String expected = getDescription(previousValue, newValue, ratio, tooltip);
    String actual = getDescription(gauge.getPreviousValue(), gauge.getNewValue(), gauge.getRatio(),
                                   org.uispec4j.utils.Utils.cleanupHtml(gauge.getToolTipText()));
    Assert.assertEquals(expected, actual);
    return this;
  }

  private String getDescription(Double previousValue, Double newValue, double ratio, String tooltip) {
    return "[" + Formatting.toString(previousValue) + "/" + Formatting.toString(newValue) + "]=> " +
           Formatting.toString(ratio) + " - " + tooltip;
  }

  public String getLabel() {
    return "";
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

  public DeltaGauge getAwtComponent() {
    return gauge;
  }

  public String getDescriptionTypeName() {
    return TYPE_NAME;
  }
}
