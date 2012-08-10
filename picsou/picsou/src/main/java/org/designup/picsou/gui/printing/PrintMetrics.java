package org.designup.picsou.gui.printing;

import java.awt.*;
import java.awt.print.PageFormat;

public class PrintMetrics {

  private static final int TITLE_ZONE_HEIGHT = 20;

  private int x0;
  private int y0;
  private int width;
  private int height;

  public PrintMetrics(PageFormat format) {
    x0 = (int)format.getImageableX() - 20;
    y0 = (int)format.getImageableY() - 25;
    width = (int)format.getImageableWidth() - 46;
    height = (int)format.getImageableHeight() - 20;
  }

  public int titleX() {
    return x0;
  }

  public int titleY() {
    return y0 + TITLE_ZONE_HEIGHT;
  }

  public int titleLineX() {
    return x0;
  }

  public int getTitleLineXEnd() {
    return x0 + width;
  }

  public int titleLineY() {
    return titleY() + 3;
  }

  public Rectangle getContentArea() {
    int top = getContentTop();
    return new Rectangle(x0, top, width, getContentHeight());
  }

  private int getContentTop() {
    return titleLineY() + 10;
  }

  public int getContentHeight() {
    return height - getContentTop();
  }
}
