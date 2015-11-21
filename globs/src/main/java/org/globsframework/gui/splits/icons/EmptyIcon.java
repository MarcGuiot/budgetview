package org.globsframework.gui.splits.icons;

import javax.swing.*;
import java.awt.*;

public class EmptyIcon implements Icon {

  private int width;
  private int height;

  public EmptyIcon(int width, int height) {
    this.width = width;
    this.height = height;
  }

  public EmptyIcon(Dimension size) {
    this(size.width, size.height);
  }

  public void paintIcon(Component component, Graphics graphics, int i, int i1) {
  }

  public int getIconWidth() {
    return width;
  }

  public int getIconHeight() {
    return height;
  }
}
