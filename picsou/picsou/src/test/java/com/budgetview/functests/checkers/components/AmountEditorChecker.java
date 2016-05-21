package com.budgetview.functests.checkers.components;

import com.budgetview.functests.checkers.GuiChecker;
import junit.framework.Assert;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.ToggleButton;
import org.uispec4j.assertion.UISpecAssert;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class AmountEditorChecker extends GuiChecker {
  private Panel editorPanel;

  public static AmountEditorChecker init(Panel container, String componentName) {
    return new AmountEditorChecker(container.getPanel(componentName));
  }

  private AmountEditorChecker(Panel panel) {
    this.editorPanel = panel;
  }

  public void set(double amount) {
    ToggleButton toggle = editorPanel.getToggleButton();
    if (((amount > 0) && !toggle.isSelected().isTrue()) ||
        ((amount < 0) && toggle.isSelected().isTrue())) {
      toggle.click();
    }
    TextBox textBox = editorPanel.getInputTextBox("amountEditionField");
    textBox.setText(toString(Math.abs(amount)), false);
    textBox.focusLost();
  }

  public void setText(String text) {
    editorPanel.getInputTextBox("amountEditionField").setText(text);
  }

  public void clear() {
    editorPanel.getInputTextBox("amountEditionField").setText("");
  }

  public void checkAmount(double amount) {
    assertThat(editorPanel.getInputTextBox("amountEditionField").textEquals(toString(Math.abs(amount))));
    ToggleButton toggle = editorPanel.getToggleButton();
    if ((amount < 0) && toggle.isSelected().isTrue()) {
      Assert.fail("Text is OK but toggle should not be selected");
    }
    if ((amount > 0) && !toggle.isSelected().isTrue()) {
      Assert.fail("Text is OK but toggle should be selected");
    }
  }

  public void selectPlus() {
    ToggleButton toggle = editorPanel.getToggleButton();
    if (!toggle.isSelected().isTrue()) {
      toggle.click();
    }
  }

  public void selectMinus() {
    ToggleButton toggle = editorPanel.getToggleButton();
    if (toggle.isSelected().isTrue()) {
      toggle.click();
    }
  }

  public void checkPlusSelected() {
    ToggleButton toggle = editorPanel.getToggleButton();
    UISpecAssert.assertTrue(toggle.isSelected());
  }

  public void checkMinusSelected() {
    ToggleButton toggle = editorPanel.getToggleButton();
    UISpecAssert.assertFalse(toggle.isSelected());
  }

  public void checkSignSelectorShown() {
    ToggleButton toggle = editorPanel.getToggleButton();
    UISpecAssert.assertTrue(toggle.isVisible());
  }

  public void checkSignSelectorHidden() {
    ToggleButton toggle = editorPanel.getToggleButton();
    UISpecAssert.assertFalse(toggle.isVisible());
  }
}
