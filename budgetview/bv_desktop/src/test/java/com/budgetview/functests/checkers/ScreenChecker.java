package com.budgetview.functests.checkers;

import org.globsframework.gui.splits.color.Colors;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import org.uispec4j.utils.ColorUtils;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.assertTrue;

public class ScreenChecker {
  private Panel mainWindow;
  private TextBox sectionText;
  private Button periodText;

  public ScreenChecker(Window mainWindow) {
    this.mainWindow = mainWindow;
  }

  public void checkContent(String section, String period) {
    assertTrue(getSectionText().textEquals(section));
    assertTrue(getPeriodText().textEquals(period));
  }

  private TextBox getSectionText() {
    if (sectionText == null) {
      sectionText = mainWindow.getTextBox("sectionTitle");
    }
    return sectionText;
  }

  private Button getPeriodText() {
    if (periodText == null) {
      periodText = mainWindow.getButton("periodTitle");
    }
    return periodText;
  }

  public void checkBackgroundColorIsStandard() {
    checkBackgroundTopColor("FFFFFF");
  }

  public void checkBackgroundColorIsBlack() {
    checkBackgroundTopColor("707070");
  }

  private void checkBackgroundTopColor(String color) {
    JPanel panel = (JPanel) mainWindow.getPanel("mainPanel").getPanel("timeviewHeader").getAwtComponent();
    ColorUtils.assertEquals(Colors.toColor(color), panel.getBackground());
  }
}
