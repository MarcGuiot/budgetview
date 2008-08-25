package org.globsframework.wicket.table;

import org.apache.wicket.Component;

public class GlobTableRowEditor {
  private final Component switcher;
  private final Component editionPanel;

  public GlobTableRowEditor(Component switcher, Component editionPanel) {
    this.switcher = switcher;
    this.editionPanel = editionPanel;
  }

  public Component getSwitcher() {
    return switcher;
  }

  public Component getEditionPanel() {
    return editionPanel;
  }
}
