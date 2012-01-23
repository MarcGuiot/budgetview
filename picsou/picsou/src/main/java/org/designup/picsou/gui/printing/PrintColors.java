package org.designup.picsou.gui.printing;

import org.globsframework.gui.splits.color.Colors;

import java.awt.*;

public class PrintColors {
  private final Color titleColor = Color.BLACK;
  private final Color tableTextColor = Color.BLACK;
  private final Color tableRowColor = Colors.toColor("EEEEEE");
  private final Color tableLineColor = Colors.toColor("999999");

  public Color getTitleColor() {
    return titleColor;
  }

  public Color getTableTextColor() {
    return tableTextColor;
  }

  public Color getTableRowColor() {
    return tableRowColor;
  }

  public Color getTableLineColor() {
    return tableLineColor;
  }
}
