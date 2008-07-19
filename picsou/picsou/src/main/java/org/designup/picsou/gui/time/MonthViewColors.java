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
  public Color grid;
  public Color selectedTop;
  public Color selectedBottom;
  public Color text;
  public Color textShadow;
  public Color yearSeparator;

  public MonthViewColors(Directory directory) {
    directory.get(ColorService.class).addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    backgroundTop = colorLocator.get(PicsouColors.PERIOD_BG_TOP);
    backgroundBottom = colorLocator.get(PicsouColors.PERIOD_BG_BOTTOM);
    grid = colorLocator.get(PicsouColors.PERIOD_GRID);
    yearSeparator = colorLocator.get(PicsouColors.PERIOD_YEAR_SEPARATOR);
    selectedTop = colorLocator.get(PicsouColors.PERIOD_SELECTION_BG_TOP);
    selectedBottom = colorLocator.get(PicsouColors.PERIOD_SELECTION_BG_BOTTOM);
    text = colorLocator.get(PicsouColors.PERIOD_TEXT);
    textShadow = colorLocator.get(PicsouColors.PERIOD_TEXT_SHADOW);
  }
}
