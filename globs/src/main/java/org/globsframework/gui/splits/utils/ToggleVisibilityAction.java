package org.globsframework.gui.splits.utils;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ToggleVisibilityAction extends AbstractAction {

  private JComponent component;

  public ToggleVisibilityAction(JComponent component) {
    this.component = component;
  }

  public void actionPerformed(ActionEvent e) {
    component.setVisible(!component.isVisible());
  }
}
