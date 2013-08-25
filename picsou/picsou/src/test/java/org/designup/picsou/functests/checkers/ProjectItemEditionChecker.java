package org.designup.picsou.functests.checkers;

import org.designup.picsou.functests.checkers.components.AmountEditorChecker;
import org.designup.picsou.functests.checkers.components.MonthSliderChecker;
import org.uispec4j.Panel;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.*;

public class ProjectItemEditionChecker extends GuiChecker {
  private Panel panel;
  private Panel enclosingPanel;

  public ProjectItemEditionChecker(Panel enclosingPanel) {
    this.enclosingPanel = enclosingPanel;
    this.panel = enclosingPanel.getPanel("projectItemEditionPanel");
  }

  public ProjectItemEditionChecker setLabel(String name) {
    panel.getInputTextBox("nameField").setText(name);
    return this;
  }

  public ProjectItemEditionChecker setMonth(int monthId) {
    MonthSliderChecker.init(panel, "month").setMonth(monthId);
    return this;
  }

  public ProjectItemEditionChecker setAmount(double amount) {
    AmountEditorChecker.init(panel, "amountEditor").set(amount);
    return this;
  }

  public void validate() {
    panel.getButton("validate").click();
    assertFalse(enclosingPanel.containsSwingComponent(JPanel.class, "projectItemEditionPanel"));
  }

  public ProjectItemEditionChecker validateAndCheckNameTip(String message) {
    panel.getButton("validate").click();
    assertTrue(enclosingPanel.containsSwingComponent(JPanel.class, "projectItemEditionPanel"));
    checkTipVisible(enclosingPanel, panel.getInputTextBox("nameField"), message);
    return this;
  }

  public ProjectItemEditionChecker checkNoTipShown() {
    checkNoTipVisible(enclosingPanel);
    return this;
  }

  public void cancel() {
    panel.getButton("cancel").click();
  }
}
