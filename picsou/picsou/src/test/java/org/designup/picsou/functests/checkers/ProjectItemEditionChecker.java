package org.designup.picsou.functests.checkers;

import org.designup.picsou.functests.checkers.components.AmountEditorChecker;
import org.designup.picsou.functests.checkers.components.MonthSliderChecker;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.UIComponent;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.*;

public abstract class ProjectItemEditionChecker<T extends ProjectItemEditionChecker> extends GuiChecker {
  protected Panel panel;
  protected Panel enclosingPanel;

  public ProjectItemEditionChecker(Panel enclosingPanel) {
    this.enclosingPanel = enclosingPanel;
    this.panel = enclosingPanel.getPanel("projectItemEditionPanel");
  }

  public T setLabel(String name) {
    TextBox textBox = panel.getInputTextBox("nameField");
    textBox.setText(name, false);
    textBox.focusLost();
    return (T)this;
  }

  public T checkLabel(String label) {
    assertThat(panel.getInputTextBox("nameField").textEquals(label));
    return (T)this;
  }

  public T setMonth(int monthId) {
    MonthSliderChecker.init(panel, "month").setMonth(monthId);
    return (T)this;
  }

  public T checkMonth(String text) {
    MonthSliderChecker.init(panel, "month").checkText(text);
    return (T)this;
  }

  public T setMonthCount(int numberOfMonths) {
    TextBox textBox = panel.getInputTextBox("monthCountEditor");
    textBox.setText(Integer.toString(numberOfMonths), false);
    textBox.focusLost();
    return (T)this;
  }

  public T checkMonthCount(int numberOfMonths) {
    assertThat(panel.getInputTextBox("monthCountEditor").textEquals(Integer.toString(numberOfMonths)));
    return (T)this;
  }

  public void validate() {
    panel.getButton("validate").click();
    checkNoTipVisible(enclosingPanel);
    assertFalse(enclosingPanel.containsSwingComponent(JPanel.class, "projectItemEditionPanel"));
  }

  public T validateAndCheckNameTip(String message) {
    doValidateAndCheckError(message, panel.getInputTextBox("nameField"));
    return (T)this;
  }

  public T validateAndCheckMonthCountTip(String message) {
    doValidateAndCheckError(message, panel.getInputTextBox("monthCountEditor"));
    return (T)this;
  }

  protected void doValidateAndCheckError(String message, UIComponent component) {
    panel.getButton("validate").click();
    assertTrue(enclosingPanel.containsSwingComponent(JPanel.class, "projectItemEditionPanel"));
    checkTipVisible(enclosingPanel, component, message);
  }

  public T checkNoTipShown() {
    checkNoTipVisible(enclosingPanel);
    return (T)this;
  }

  public void cancel() {
    panel.getButton("cancel").click();
  }
}
