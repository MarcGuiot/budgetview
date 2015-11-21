package org.globsframework.gui.splits.icons;

import javax.swing.*;
import java.awt.*;

public abstract class SingleColorIcon implements Icon {

  private Color color = Color.BLACK;

  public void setColor(Color color) {
    this.color = color;
  }

  public Color getColor() {
    return color;
  }

}
