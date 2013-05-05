package org.designup.picsou.gui.printing.utils;

import org.designup.picsou.gui.printing.PrintStyle;

import java.awt.*;

public class EmptyBlock implements PageBlock {
  
  private int height;

  public EmptyBlock(int height) {
    this.height = height;
  }

  public int getNeededHeight() {
    return height;
  }

  public int getHeight() {
    return height;
  }

  public void print(Dimension area, Graphics2D g2, PrintStyle style) {
  }
}
