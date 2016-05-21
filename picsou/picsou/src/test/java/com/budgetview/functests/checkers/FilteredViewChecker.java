package com.budgetview.functests.checkers;

import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Window;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.*;

public abstract class FilteredViewChecker<T> extends ViewChecker {
  private String containerMessage;
  private String filterMessageName;
  private Panel filterMessagePanel;

  protected FilteredViewChecker(Window mainWindow, String filterContainerName, String filterMessagePanelName) {
    super(mainWindow);
    this.containerMessage = filterContainerName;
    this.filterMessageName = filterMessagePanelName;
  }

  public T checkFilterMessage(String message) {
    views.selectData();
    Panel panel = getFilterMessagePanel();
    assertThat(panel.isVisible());
    TextBox label = panel.getTextBox("filterLabel");
    assertThat(label.textEquals(message));
    return (T)this;
  }

  public T checkNoFilterMessageShown() {
    views.selectData();
    checkComponentVisible(mainWindow.getPanel(containerMessage), JPanel.class, filterMessageName, false);
    return (T)this;
  }

  public T clearCurrentFilter() {
    Panel panel = getFilterMessagePanel();
    assertTrue(panel.isVisible());
    Button button = panel.getButton();
    button.click();
    assertFalse(panel.isVisible());
    return (T)this;
  }

  private Panel getFilterMessagePanel() {
    if (filterMessagePanel == null) {
      filterMessagePanel = mainWindow.getPanel(containerMessage).getPanel(filterMessageName);
    }
    return filterMessagePanel;
  }

}
