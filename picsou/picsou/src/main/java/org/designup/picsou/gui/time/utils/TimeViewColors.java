package org.designup.picsou.gui.time.utils;

import org.designup.picsou.gui.utils.AmountColors;
import org.designup.picsou.gui.utils.ApplicationColors;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class TimeViewColors implements ColorChangeListener {

  public Color yearBackground;
  public Color yearSeparator;

  public Color futureBackgroundTop;
  public Color futureBackgroundBottom;
  public Color monthTop;
  public Color monthBottom;
  public Color selectedMonthTop;
  public Color selectedMonthBottom;

  public Color currentText;
  public Color futureText;
  public Color pastText;
  public Color textShadow;
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
    monthTop = colorLocator.get(ApplicationColors.TIMEVIEW_MONTH_TOP);
    monthBottom = colorLocator.get(ApplicationColors.TIMEVIEW_MONTH_BOTTOM);
    futureBackgroundTop = colorLocator.get(ApplicationColors.TIMEVIEW_FUTURE_MONTH_TOP);
    futureBackgroundBottom = colorLocator.get(ApplicationColors.TIMEVIEW_FUTURE_MONTH_BOTTOM);
    selectedMonthBottom = colorLocator.get(ApplicationColors.TIMEVIEW_SELECTED_MONTH_BOTTOM);
    selectedMonthTop = colorLocator.get(ApplicationColors.TIMEVIEW_SELECTED_MONTH_TOP);

    yearBackground = monthBottom;
    yearSeparator = colorLocator.get(ApplicationColors.TIMEVIEW_YEAR_SEPARATOR);

    futureText = colorLocator.get(ApplicationColors.TIMEVIEW_TEXT_FUTURE);
    pastText = colorLocator.get(ApplicationColors.TIMEVIEW_TEXT_PAST);
    currentText = colorLocator.get(ApplicationColors.TIMEVIEW_TEXT_CURRENT);
    yearText = colorLocator.get(ApplicationColors.TIMEVIEW_TEXT_YEAR);
    textShadow = colorLocator.get(ApplicationColors.TIMEVIEW_TEXT_SHADOW);
  }

  public Font getYearFont() {
    return yearFont;
  }

  public Font getMonthFont() {
    return monthFont;
  }

  public Color getMonthTextColor(int month, int currentMonth) {
    if (month < currentMonth) {
      return pastText;
    }
    else if (month > currentMonth) {
      return futureText;
    }
    else {
      return currentText;
    }
  }

  public Color getAmountColor(double diff) {
    return amountColors.getIndicatorColor(diff);
  }

  public Color getAmountTextColor(double diff, Color normalColor) {
    return amountColors.getTextColor(diff, normalColor);
  }
}
