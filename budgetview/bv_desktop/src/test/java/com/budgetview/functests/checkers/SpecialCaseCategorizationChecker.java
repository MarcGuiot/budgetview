package com.budgetview.functests.checkers;

import junit.framework.Assert;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;

import javax.swing.*;

public class SpecialCaseCategorizationChecker<T extends SpecialCaseCategorizationChecker> extends GuiChecker {
  protected Panel panel;

  public SpecialCaseCategorizationChecker(Panel panel) {
    this.panel = panel;
  }

  public T toggle() {
    panel.getButton("showHide").click();
    return (T) this;
  }

  public T checkShown() {
    checkComponentVisible(panel, JPanel.class, "specialCasePanel", true);
    return (T) this;
  }

  public T checkHidden() {
    UISpecAssert.assertThat(new Assertion() {
      public void check() {
        Panel specialCase = panel.getPanel("specialCasePanel");
        if (specialCase.isVisible().isTrue()) {
          TextBox textBox = getSpecialCasePanel().getTextBox("message");
          Assert.fail("Special case panel unexpectedly shown with: " + textBox.getText());
        }
      }
    });
    return (T) this;
  }

  protected Panel getSpecialCasePanel() {
    return panel.getPanel("specialCasePanel");
  }

  protected void checkSpecialCaseMessage(String text) {
    UISpecAssert.assertThat(getSpecialCasePanel().getTextBox("message").textEquals(text));
  }
}
