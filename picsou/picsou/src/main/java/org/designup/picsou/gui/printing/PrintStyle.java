package org.designup.picsou.gui.printing;

import org.designup.picsou.gui.components.charts.Gauge;
import org.globsframework.gui.splits.color.Colors;

import java.awt.*;

public class PrintStyle implements PrintFonts {

  private final Font titleFont = new Font("Arial", Font.BOLD, 20);
  private final Font textFont = new Font("Arial", Font.PLAIN, 9);
  private final Font selectedTextFont = new Font("Arial", Font.BOLD, 9);
  private final Font sectionTitleFont = new Font("Arial", Font.BOLD, 11);

  private final Color titleColor = Color.BLACK;
  private final Color textColor = Color.BLACK;
  private final Color tableRowColor = Colors.toColor("EEEEEE");
  private final Color tableLineColor = Colors.toColor("BBBBBB");
  private final Color sectionTextColor = Colors.toColor("FFFFFF");
  private final Color sectionBackgroundColor = Colors.toColor("999999");

  public Font getTitleFont() {
    return titleFont;
  }

  public Color getTitleColor() {
    return titleColor;
  }

  public Font getSectionTitleFont() {
    return sectionTitleFont;
  }

  public Color getSectionTextColor() {
    return sectionTextColor;
  }

  public Color getSectionBackgroundColor() {
    return sectionBackgroundColor;
  }

  public Font getTextFont(boolean selected) {
    return selected ? selectedTextFont : textFont;
  }

  public Color getTextColor() {
    return textColor;
  }

  public Color getTableRowColor() {
    return tableRowColor;
  }

  public Color getDividerColor() {
    return tableLineColor;
  }

  public void setColors(Gauge gauge) {
    gauge.setBorderColor(Colors.toColor("AAAAAA"));
    gauge.setEmptyColorTop(Color.WHITE);
    gauge.setEmptyColorBottom(Color.WHITE);
    gauge.setFilledColorTop(Colors.toColor("EEEEEE"));
    gauge.setFilledColorBottom(Colors.toColor("BBBBBB"));
    gauge.setOverrunColorTop(Colors.toColor("FF3333"));
    gauge.setOverrunColorBottom(Colors.toColor("FF3333"));
    gauge.setOverrunErrorColorTop(Colors.toColor("CC0000"));
    gauge.setOverrunErrorColorBottom(Colors.toColor("CC0000"));
  }
}
