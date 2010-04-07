package org.designup.picsou.gui.components;

import javax.swing.*;
import java.awt.*;

public abstract class TextDisplay {

  public static final TextDisplay NULL = new TextDisplay(new JLabel()) {
    public void setText(String text) {
    }
  };

  protected JComponent component;

  public static TextDisplay create(final JButton button) {
    return new TextDisplay(button) {
      public void setText(String text) {
        button.setText(text);
      }
    };
  }

  public static TextDisplay create(final JLabel label) {
    return new TextDisplay(label) {
      public void setText(String text) {
        label.setText(text);
      }
    };
  }

  public abstract void setText(String text);

  public void setToolTipText(String text) {
    component.setToolTipText(text);
  }

  public void setForeground(Color color) {
    component.setForeground(color);
  }

  public void setVisible(boolean visible) {
    component.setVisible(visible);
  }

  protected TextDisplay(JComponent component) {
    this.component = component;
  }
}
