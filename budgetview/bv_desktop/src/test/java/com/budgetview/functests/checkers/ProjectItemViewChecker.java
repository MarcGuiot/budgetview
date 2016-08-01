package com.budgetview.functests.checkers;

import com.budgetview.functests.checkers.components.GaugeChecker;
import com.budgetview.functests.checkers.components.MonthSliderChecker;
import com.budgetview.functests.checkers.components.PopupButton;
import org.globsframework.utils.TablePrinter;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.ToggleButton;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class ProjectItemViewChecker extends GuiChecker {
  private Panel panel;

  public ProjectItemViewChecker(Panel enclosingPanel) {
    this.panel = enclosingPanel.getPanel("projectItemViewPanel");
  }

  public void checkValues(String label, String month, double actual, double planned) {
    assertThat(panel.getButton("itemButton").textEquals(label));
    assertThat(panel.getPanel("monthSlider").getButton("month").textEquals(month));
    assertThat(panel.getButton("actualAmount").textEquals(toExpenseString(actual)));
    assertThat(panel.getButton("plannedAmount").textEquals(toExpenseString(planned)));
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

  public ProjectItemViewChecker setActive() {
    ToggleButton toggle = getActiveToggle();
    assertThat(toggle.isEnabled());
    toggle.click();
    return this;
  }

  public ProjectItemViewChecker setInactive() {
    ToggleButton toggle = getActiveToggle();
    assertThat(toggle.isEnabled());
    toggle.click();
    return this;
  }

  private ToggleButton getActiveToggle() {
    return panel.getToggleButton("activeToggle");
  }

  void write(TablePrinter printer) {
    printer.addRow(panel.getButton("itemButton").getLabel(),
                   MonthSliderChecker.init(panel, "monthSlider").getText(),
                   panel.getButton("actualAmount").getLabel(),
                   panel.getButton("plannedAmount").getLabel());
  }

  public void delete() {
    getItemButton().click("Delete");
  }

  public void modify() {
    panel.getButton("modify").click();
  }

  public ProjectItemViewChecker slideToPreviousMonth() {
    MonthSliderChecker.init(panel, "monthSlider").previous();
    return this;
  }

  public ProjectItemViewChecker slideToNextMonth() {
    MonthSliderChecker.init(panel, "monthSlider").next();
    return this;
  }

  public ProjectItemViewChecker checkCategorizationWarningNotShown() {
    checkComponentVisible(panel, JLabel.class, "categorizationWarning", false);
    return this;
  }

  public ProjectItemViewChecker checkCategorizationWarningShown() {
    checkComponentVisible(panel, JLabel.class, "categorizationWarning", true);
    assertThat(panel.getTextBox("categorizationWarning").textEquals("Transactions from other months have been assigned to this item"));
    return this;
  }

  public ProjectItemViewChecker clickCategorizationWarning() {
    panel.getButton("categorizationWarningAction").click();
    return this;
  }

  public ProjectItemViewChecker checkURL(String text, String browsedUrl) {
    Button linkButton = panel.getButton("link");
    assertThat(linkButton.textEquals(text));
    BrowsingChecker.checkDisplay(linkButton, browsedUrl);
    return this;
  }

  private PopupButton getItemButton() {
    final Button itemButton = panel.getButton("itemButton");
    return new PopupButton(itemButton);
  }
}
