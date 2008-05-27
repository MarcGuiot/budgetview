package org.designup.picsou.gui.time;

import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.color.ColorChangeListener;
import org.crossbowlabs.splits.color.ColorService;
import org.crossbowlabs.splits.color.ColorSource;
import org.designup.picsou.gui.utils.PicsouColors;

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
