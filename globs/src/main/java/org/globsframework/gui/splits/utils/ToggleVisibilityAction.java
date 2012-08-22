package org.globsframework.gui.splits.utils;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ToggleVisibilityAction extends AbstractAction {

  private JComponent component;
  private String shownLabel;
  private String hiddenLabel;

  public ToggleVisibilityAction(JComponent component) {
    this(component, null, null);
  }

  public ToggleVisibilityAction(JComponent component, String shownLabel, String hiddenLabel) {
    this.component = component;
    this.shownLabel = shownLabel;
    this.hiddenLabel = hiddenLabel;
    updateLabel();
  }
  
  public void setHidden() {
    component.setVisible(false);
    updateLabel();
  }

  private void updateLabel() {
    putValue(Action.NAME, component.isVisible() ? shownLabel : hiddenLabel);
  }

  public void actionPerformed(ActionEvent e) {
    component.setVisible(!component.isVisible());
    updateLabel();
  }
}
