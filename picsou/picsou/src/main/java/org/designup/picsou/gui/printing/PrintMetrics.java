package org.designup.picsou.gui.printing;

import java.awt.*;
import java.awt.print.PageFormat;

public class PrintMetrics {

  private static final int TITLE_ZONE_HEIGHT = 20;
  private static final int FOOTER_ZONE_HEIGHT = 20;

  private int x0;
  private int y0;
  private int pageWidth;
  private int contentHeight;
  private int pageHeight;

  public PrintMetrics(PageFormat format) {
    x0 = (int)format.getImageableX() - 20;
    y0 = (int)format.getImageableY() - 25;
    pageWidth = (int)format.getImageableWidth() - 15;
    pageHeight = (int)format.getImageableHeight();
    contentHeight = pageHeight - FOOTER_ZONE_HEIGHT;
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
    return x0 + pageWidth;
  }

  public int titleLineY() {
    return titleY() + 3;
  }

  public Rectangle getContentArea() {
    int top = getContentTop();
    return new Rectangle(x0, top, pageWidth, getContentHeight());
  }

  private int getContentTop() {
    return titleLineY() + 10;
  }

  public int getContentHeight() {
    return contentHeight - getContentTop();
  }
  
  public int getFooterX(String text, FontMetrics fontMetrics) {
    return pageWidth - fontMetrics.stringWidth(text);
  }
  
  public int getFooterY() {
    return pageHeight - 4;
  }
}
