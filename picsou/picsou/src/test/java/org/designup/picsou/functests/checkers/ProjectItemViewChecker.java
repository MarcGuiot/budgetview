package org.designup.picsou.functests.checkers;

import org.designup.picsou.functests.checkers.components.GaugeChecker;
import org.designup.picsou.functests.checkers.components.PopupButton;
import org.designup.picsou.functests.checkers.components.PopupChecker;
import org.uispec4j.*;
import org.uispec4j.interception.PopupMenuInterceptor;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class ProjectItemViewChecker extends GuiChecker {
  private Panel panel;

  public ProjectItemViewChecker(Panel enclosingPanel) {
    this.panel = enclosingPanel.getPanel("projectItemViewPanel");
  }

  public void checkValues(String label, String month, double actual, double planned) {
    assertThat(panel.getButton("itemButton").textEquals(label));
    assertThat(panel.getTextBox("monthLabel").textEquals(month));
    assertThat(panel.getTextBox("actualLabel").textEquals(toString(actual)));
    assertThat(panel.getTextBox("plannedLabel").textEquals(toString(planned)));
    checkGauge(actual, planned);
  }

  public void checkGauge(double actual, double planned) {
    GaugeChecker gauge = new GaugeChecker(panel, "itemGauge");
    gauge.checkActualValue(actual);
    gauge.checkTargetValue(planned);
  }

  void write(StringBuilder builder) {
    builder.append(panel.getButton("itemButton").getLabel());
    builder.append(" | ");
    builder.append(panel.getTextBox("monthLabel").getText());
    builder.append(" | ");
    builder.append(panel.getTextBox("actualLabel").getText());
    builder.append(" | ");
    builder.append(panel.getTextBox("plannedLabel").getText());
  }

  public void delete() {
    final Button itemButton = panel.getButton("itemButton");
    PopupButton button = new PopupButton(itemButton);
    button.click("Delete");
  }

  public void modify() {
    panel.getButton("modify").click();
  }
}
