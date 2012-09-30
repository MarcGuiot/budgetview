package org.designup.picsou.gui.components;

import javax.swing.*;
import java.awt.*;

public abstract class ComponentTextDisplay implements TextDisplay {

  protected JComponent component;

  public TextDisplay create(final JButton button) {
    return new ComponentTextDisplay(button) {
      public void setText(String text) {
        button.setText(text);
      }
    };
  }

  public static TextDisplay create(final JLabel label) {
    return new ComponentTextDisplay(label) {
      public void setText(String text) {
        label.setText(text);
      }
    };
  }

  public ComponentTextDisplay(JComponent component) {
    this.component = component;
  }

  public void setToolTipText(String text) {
    component.setToolTipText(text);
  }

  public void setForeground(Color color) {
    component.setForeground(color);
  }

  public void setVisible(boolean visible) {
    component.setVisible(visible);
  }
}
