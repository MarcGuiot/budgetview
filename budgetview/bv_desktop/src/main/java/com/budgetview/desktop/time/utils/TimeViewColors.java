package com.budgetview.desktop.time.utils;

import com.budgetview.desktop.utils.AmountColors;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class TimeViewColors implements ColorChangeListener {

  public Color selectedMonthBg;
  private Color selectedMonthText;

  public Color monthText;
  public Color yearText;

  public Font monthFont;
  public Font yearFont;

  private AmountColors amountColors;

  public TimeViewColors(Directory directory, Font yearFont, Font monthFont) {
    this.monthFont = monthFont;
    this.yearFont = yearFont;
    this.amountColors = new AmountColors(directory);
    directory.get(ColorService.class).addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    selectedMonthBg = colorLocator.get("timeview.selected.month.bg");
    selectedMonthText = colorLocator.get("timeview.selected.month.text");
    monthText = colorLocator.get("timeview.text.month");
    yearText = colorLocator.get("timeview.text.year");
  }

  public Font getYearFont() {
    return yearFont;
  }

  public Font getMonthFont() {
    return monthFont;
  }

  public Color getMonthTextColor(boolean selected) {
      return selected ? selectedMonthText : monthText;
  }

  public Color getAmountColor(double diff) {
    return amountColors.getIndicatorColor(diff);
  }

  public Color getAmountTextColor(double diff, Color normalColor) {
    return amountColors.getTextColor(diff, normalColor);
  }
}
