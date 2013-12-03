package org.globsframework.gui.splits.utils;

import org.globsframework.utils.Strings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class ToggleVisibilityAction extends AbstractAction {

  private JComponent component;
  private String shownLabel;
  private String hiddenLabel;
  private String parentName;

  public ToggleVisibilityAction(JComponent component, String shownLabel, String hiddenLabel) {
    this.component = component;
    this.shownLabel = shownLabel;
    this.hiddenLabel = hiddenLabel;
    this.component.addComponentListener(new ComponentAdapter() {
      public void componentHidden(ComponentEvent e) {
        updateLabel();
      }

      public void componentShown(ComponentEvent e) {
        updateLabel();
      }
    });
    updateLabel();
  }

  public void setParentName(String parentName) {
    this.parentName = parentName;
  }

  public void setHidden() {
    component.setVisible(false);
  }

  private void updateLabel() {
    putValue(Action.NAME, component.isVisible() ? shownLabel : hiddenLabel);
  }

  public void actionPerformed(ActionEvent e) {
    component.setVisible(!component.isVisible());
    if (Strings.isNotEmpty(parentName)) {
      JComponent parent = GuiUtils.getEnclosingComponent(component, JComponent.class, parentName);
      if (parent != null) {
        GuiUtils.revalidate(parent);
      }
    }
  }
}
