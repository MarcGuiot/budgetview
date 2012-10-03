package org.globsframework.gui.splits.layout;

import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;

public class DefaultTabHandler implements TabHandler {
  private JTabbedPane tabbedPane;

  public static TabHandler init(JTabbedPane tabbedPane) {
    return new DefaultTabHandler(tabbedPane);
  }

  private DefaultTabHandler(JTabbedPane tabbedPane) {
    this.tabbedPane = tabbedPane;
  }

  public void select(int tabIndex) {
    if (tabIndex >= tabbedPane.getTabCount()) {
      throw new InvalidParameter("Invalid index " + tabIndex +
                                 " - should be between 0 and " + (tabbedPane.getTabCount() - 1));
    }
    tabbedPane.setSelectedIndex(tabIndex);
  }
}
