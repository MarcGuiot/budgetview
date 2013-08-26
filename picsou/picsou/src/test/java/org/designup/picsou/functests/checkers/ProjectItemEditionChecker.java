package org.designup.picsou.functests.checkers;

import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.functests.checkers.components.AmountEditorChecker;
import org.designup.picsou.functests.checkers.components.MonthSliderChecker;
import org.designup.picsou.gui.description.Formatting;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;

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
    TextBox textBox = panel.getInputTextBox("nameField");
    textBox.setText(name, false);
    textBox.focusLost();
    return this;
  }

  public ProjectItemEditionChecker setMonth(int monthId) {
    MonthSliderChecker.init(panel, "month").setMonth(monthId);
    return this;
  }

  public ProjectItemEditionChecker checkMonth(String text) {
    MonthSliderChecker.init(panel, "month").checkText(text);
    return this;
  }

  public ProjectItemEditionChecker setAmount(double amount) {
    AmountEditorChecker.init(panel, "amountEditor").set(amount);
    return this;
  }

  public ProjectItemEditionChecker setMonthCount(int numberOfMonths) {
    TextBox textBox = panel.getInputTextBox("monthCountEditor");
    textBox.setText(Integer.toString(numberOfMonths), false);
    textBox.focusLost();
    return this;
  }

  public ProjectItemEditionChecker checkMonthCount(int numberOfMonths) {
    assertThat(panel.getInputTextBox("monthCountEditor").textEquals(Integer.toString(numberOfMonths)));
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

  public ProjectItemEditionChecker validateAndCheckMonthCountTip(String message) {
    panel.getButton("validate").click();
    assertTrue(enclosingPanel.containsSwingComponent(JPanel.class, "projectItemEditionPanel"));
    checkTipVisible(enclosingPanel, panel.getInputTextBox("monthCountEditor"), message);
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
