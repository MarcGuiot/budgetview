package org.designup.picsou.gui.time;

import org.designup.picsou.gui.utils.PicsouColors;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.ColorSource;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class MonthViewColors implements ColorChangeListener {

  public Color backgroundTop;
  public Color backgroundBottom;
  public Color grid;
  public Color selectedTop;
  public Color selectedBottom;
  public Color text;

  public MonthViewColors(Directory directory) {
    directory.get(ColorService.class).addListener(this);
  }

  public void colorsChanged(ColorSource colorSource) {
    backgroundTop = colorSource.get(PicsouColors.PERIOD_BG_TOP);
    backgroundBottom = colorSource.get(PicsouColors.PERIOD_BG_BOTTOM);
    grid = colorSource.get(PicsouColors.PERIOD_GRID);
    selectedTop = colorSource.get(PicsouColors.PERIOD_SELECTION_BG_TOP);
    selectedBottom = colorSource.get(PicsouColors.PERIOD_SELECTION_BG_BOTTOM);
    text = colorSource.get(PicsouColors.PERIOD_TEXT);
  }
}
