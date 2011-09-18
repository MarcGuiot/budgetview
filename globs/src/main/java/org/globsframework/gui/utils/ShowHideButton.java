package org.globsframework.gui.utils;

import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.*;

public class ShowHideButton extends JButton {
  private JComponent component;
  private String showLabel;
  private String hideLabel;
  private Icon showIcon;
  private Icon hideIcon;

  public ShowHideButton(final JComponent component, String showLabel, String hideLabel) {
    this.component = component;
    this.showLabel = showLabel;
    this.hideLabel = hideLabel;
    setText(this.component.isVisible() ? this.hideLabel : this.showLabel);
    this.component.addComponentListener(new ComponentAdapter() {
      public void componentShown(ComponentEvent e) {
        setText(ShowHideButton.this.hideLabel);
        setIcon(ShowHideButton.this.hideIcon);
      }

      public void componentHidden(ComponentEvent e) {
        setText(ShowHideButton.this.showLabel);
        setIcon(ShowHideButton.this.showIcon);
      }
    });
    addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final boolean visible = !component.isVisible();
        setComponentVisible(visible);
      }
    });
  }

  public void setShowIcon(Icon showIcon) {
    this.showIcon = showIcon;
    setIcon(this.component.isVisible() ? this.hideIcon : this.showIcon);
  }

  public void setHideIcon(Icon hideIcon) {
    this.hideIcon = hideIcon;
    setIcon(this.component.isVisible() ? this.hideIcon : this.showIcon);
  }

  public void setShown() {
    setComponentVisible(true);
  }

  public void setHidden() {
    setComponentVisible(false);
  }

  private void setComponentVisible(boolean visible) {
    component.setVisible(visible);
  }

  public void lock() {
    setEnabled(false);
  }

  public void unlock() {
    setEnabled(true);
  }
}
