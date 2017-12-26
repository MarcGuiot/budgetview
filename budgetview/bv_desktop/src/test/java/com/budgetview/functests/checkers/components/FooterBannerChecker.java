package com.budgetview.functests.checkers.components;

import com.budgetview.functests.checkers.GuiChecker;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Window;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class FooterBannerChecker extends GuiChecker {
  private Window mainWindow;
  private String panelName;
  private Panel panel;

  public FooterBannerChecker(Window mainWindow, String panelName) {
    this.mainWindow = mainWindow;
    this.panelName = panelName;
  }

  public void checkVisible(String message) {
    checkComponentVisible(mainWindow, JPanel.class, panelName, true);
    Panel panel = getPanel();
    TextBox textBox = panel.getTextBox("message");
    assertThat(textBox.textContains(message));
  }

  public void checkHidden() {
    checkComponentVisible(mainWindow, JPanel.class, panelName, false);
  }

  public Button getActionButton() {
    Panel panel = getPanel();
    assertThat(panel.isVisible());
    return panel.getButton("action");
  }

  public void hide() {
    getPanel().getButton("hide").click();
  }

  private Panel getPanel() {
    if (panel == null) {
      panel = mainWindow.getPanel(panelName);
    }
    return panel;
  }

}
