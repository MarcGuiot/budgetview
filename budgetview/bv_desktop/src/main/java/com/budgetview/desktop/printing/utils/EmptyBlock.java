package com.budgetview.desktop.printing.utils;

import com.budgetview.desktop.printing.PrintStyle;

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
