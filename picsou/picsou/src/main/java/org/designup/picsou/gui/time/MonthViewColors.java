package org.designup.picsou.gui.time;

import org.designup.picsou.gui.utils.PicsouColors;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class MonthViewColors implements ColorChangeListener {

  public Color backgroundTop;
  public Color backgroundBottom;
  public Color pastBackgroundTop;
  public Color pastBackgroundBottom;
  public Color currentBackgroundTop;
  public Color currentBackgroundBottom;
  public Color futureBackgroundTop;
  public Color futureBackgroundBottom;
  public Color pastSelectedTop;
  public Color pastSelectedBottom;
  public Color currentSelectedTop;
  public Color currentSelectedBottom;
  public Color futureSelectedTop;
  public Color futureSelectedBottom;
  public Color selectedTop;
  public Color selectedBottom;
  public Color grid;
  public Color text;
  public Color textShadow;
  public Color yearSeparator;

  public MonthViewColors(Directory directory) {
    directory.get(ColorService.class).addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    backgroundTop = colorLocator.get(PicsouColors.PERIOD_BG_TOP);
    backgroundBottom = colorLocator.get(PicsouColors.PERIOD_BG_BOTTOM);
    pastBackgroundTop = colorLocator.get(PicsouColors.PERIOD_PAST_BG_TOP);
    pastBackgroundBottom = colorLocator.get(PicsouColors.PERIOD_PAST_BG_BOTTOM);
    currentBackgroundTop = colorLocator.get(PicsouColors.PERIOD_CURRENT_BG_TOP);
    currentBackgroundBottom = colorLocator.get(PicsouColors.PERIOD_CURRENT_BG_BOTTOM);
    futureBackgroundTop = colorLocator.get(PicsouColors.PERIOD_FUTURE_BG_TOP);
    futureBackgroundBottom = colorLocator.get(PicsouColors.PERIOD_FUTURE_BG_BOTTOM);
    grid = colorLocator.get(PicsouColors.PERIOD_GRID);
    yearSeparator = colorLocator.get(PicsouColors.PERIOD_YEAR_SEPARATOR);
    futureSelectedTop = colorLocator.get(PicsouColors.PERIOD_FUTURE_SELECTION_BG_TOP);
    futureSelectedBottom = colorLocator.get(PicsouColors.PERIOD_FUTURE_SELECTION_BG_BOTTOM);
    currentSelectedTop = colorLocator.get(PicsouColors.PERIOD_CURRENT_SELECTION_BG_TOP);
    currentSelectedBottom = colorLocator.get(PicsouColors.PERIOD_CURRENT_SELECTION_BG_BOTTOM);
    pastSelectedTop = colorLocator.get(PicsouColors.PERIOD_PAST_SELECTION_BG_TOP);
    pastSelectedBottom = colorLocator.get(PicsouColors.PERIOD_PAST_SELECTION_BG_BOTTOM);
    selectedBottom = colorLocator.get(PicsouColors.PERIOD_YEAR_SELECTION_BG_BOTTOM);
    selectedTop = colorLocator.get(PicsouColors.PERIOD_YEAR_SELECTION_BG_BOTTOM);

    text = colorLocator.get(PicsouColors.PERIOD_TEXT);
    textShadow = colorLocator.get(PicsouColors.PERIOD_TEXT_SHADOW);
  }
}
