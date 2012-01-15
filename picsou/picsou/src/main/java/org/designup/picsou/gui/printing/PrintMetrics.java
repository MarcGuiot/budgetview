package org.designup.picsou.gui.printing;

import java.awt.*;
import java.awt.print.PageFormat;

public class PrintMetrics {
  private int x0;
  private int y0;
  private int width;
  private int height;
  private FontMetrics titleFontMetrics;

  public PrintMetrics(PageFormat format, Graphics2D g2, PrintFonts fonts) {
    x0 = (int)format.getImageableX() - 20;
    y0 = (int)format.getImageableY() - 25;
    width = (int)format.getImageableWidth() - 46;
    height = (int)format.getImageableHeight() - 20;

    titleFontMetrics = g2.getFontMetrics(fonts.getTitleFont());
  }

  public int titleX() {
    return x0;
  }

  public int titleY() {
    return y0 + titleFontMetrics.getHeight() + 2;
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
    int top = titleLineY() + 10;
    return new Rectangle(x0, top, width, height - top);
  }
}
