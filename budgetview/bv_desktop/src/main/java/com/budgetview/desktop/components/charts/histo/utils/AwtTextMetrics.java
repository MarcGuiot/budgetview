package com.budgetview.desktop.components.charts.histo.utils;

import com.budgetview.shared.gui.TextMetrics;

import java.awt.*;

public class AwtTextMetrics implements TextMetrics {

  private FontMetrics fontMetrics;

  public AwtTextMetrics(Graphics graphics, Font font) {
    fontMetrics = graphics.getFontMetrics(font);
  }

  public int getAscent() {
    return fontMetrics.getAscent();
  }

  public int stringWidth(String text) {
    return fontMetrics.stringWidth(text);
  }
}
