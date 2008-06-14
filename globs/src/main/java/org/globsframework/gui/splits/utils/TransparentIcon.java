package org.globsframework.gui.splits.utils;

import javax.swing.*;
import java.awt.*;

/**
 * A transparent icon used for instance to create margins in labels
 */
public class TransparentIcon implements Icon {

  private int height;
  private int width;

  public TransparentIcon(int height, int width) {
    this.height = height;
    this.width = width;
  }

  public int getIconHeight() {
    return height;
  }

  public int getIconWidth() {
    return width;
  }

  /**
   * Empty implementation
   */
  public void paintIcon(Component c, Graphics g, int x, int y) {
  }
}
