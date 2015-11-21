package org.globsframework.gui.splits.icons;

import javax.swing.*;
import java.awt.*;

public abstract class BorderColorIcon implements Icon {
  private Color backgroundColor = Color.GRAY;
  private Color borderColor = Color.BLACK;

  public Color getBackgroundColor() {
    return backgroundColor;
  }

  public void setBackgroundColor(Color backgroundColor) {
    this.backgroundColor = backgroundColor;
  }

  public Color getBorderColor() {
    return borderColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }
}
