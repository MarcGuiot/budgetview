package org.designup.picsou.gui.components.charts.histo.daily;

import org.designup.picsou.gui.components.charts.histo.line.HistoLineColors;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class HistoDailyColors  {
  
  public final HistoLineColors line;

  private String currentDayKey;
  private String positiveInnerLabelKey;
  private String negativeInnerLabelKey;

  private Color currentDayColor;
  private Color positiveInnerLabelColor;
  private Color negativeInnerLabelColor;
  
  public HistoDailyColors(HistoLineColors line,
                          String currentDayKey,
                          String positiveInnerLabelKey,
                          String negativeInnerLabelKey,
                          Directory directory) {
    this.line = line;
    this.currentDayKey = currentDayKey;
    this.positiveInnerLabelKey = positiveInnerLabelKey;
    this.negativeInnerLabelKey = negativeInnerLabelKey;

    directory.get(ColorService.class).addListener(new ColorUpdater());
  }

  private class ColorUpdater implements ColorChangeListener {
    public void colorsChanged(ColorLocator colorLocator) {
      currentDayColor = colorLocator.get(currentDayKey);
      positiveInnerLabelColor = colorLocator.get(positiveInnerLabelKey);
      negativeInnerLabelColor = colorLocator.get(negativeInnerLabelKey);
    }
  }

  public Color getCurrentDayColor() {
    return currentDayColor;
  }

  public Color getInnerLabelColor(double value) {
    return value >= 0 ? positiveInnerLabelColor : negativeInnerLabelColor;
  }
}
