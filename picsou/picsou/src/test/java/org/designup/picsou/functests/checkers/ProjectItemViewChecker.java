package org.designup.picsou.functests.checkers;

import org.designup.picsou.functests.checkers.components.GaugeChecker;
import org.designup.picsou.functests.checkers.components.PopupButton;
import org.uispec4j.Button;
import org.uispec4j.Panel;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class ProjectItemViewChecker extends GuiChecker {
  private Panel panel;

  public ProjectItemViewChecker(Panel enclosingPanel) {
    this.panel = enclosingPanel.getPanel("projectItemViewPanel");
  }

  public void checkValues(String label, String month, double actual, double planned) {
    assertThat(panel.getButton("itemButton").textEquals(label));
    assertThat(panel.getTextBox("monthLabel").textEquals(month));
    assertThat(panel.getButton("actualAmount").textEquals(toString(actual)));
    assertThat(panel.getButton("plannedAmount").textEquals(toString(planned)));
    checkGauge(actual, planned);
  }

  public void checkGauge(double actual, double planned) {
    GaugeChecker gauge = new GaugeChecker(panel, "itemGauge");
    gauge.checkActualValue(actual);
    gauge.checkTargetValue(planned);
  }

  public void showTransactionsThroughActual() {
    panel.getButton("actualAmount").click();
  }

  public void showTransactionsThroughMenu() {
    getItemButton().click("Show transactions");
  }

  void write(StringBuilder builder) {
    builder.append(panel.getButton("itemButton").getLabel());
    builder.append(" | ");
    builder.append(panel.getTextBox("monthLabel").getText());
    builder.append(" | ");
    builder.append(panel.getButton("actualAmount").getLabel());
    builder.append(" | ");
    builder.append(panel.getButton("plannedAmount").getLabel());
  }

  public void delete() {
    getItemButton().click("Delete");
  }

  public void modify() {
    panel.getButton("modify").click();
  }

  private PopupButton getItemButton() {
    final Button itemButton = panel.getButton("itemButton");
    return new PopupButton(itemButton);
  }
}
