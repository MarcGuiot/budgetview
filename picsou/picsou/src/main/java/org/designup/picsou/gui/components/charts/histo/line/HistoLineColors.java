package org.designup.picsou.gui.components.charts.histo.line;

import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class HistoLineColors implements ColorChangeListener {

  private String lineKey;
  private String positiveFillKey;
  private String negativeFillKey;

  private Color lineColor;
  private Color positiveFillColor;
  private Color negativeFillColor;

  public HistoLineColors(String lineKey, String positiveFillKey, String negativeFillKey, Directory directory) {
    this.lineKey = lineKey;
    this.positiveFillKey = positiveFillKey;
    this.negativeFillKey = negativeFillKey;
    directory.get(ColorService.class).addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    this.lineColor = colorLocator.get(lineKey);
    this.positiveFillColor = colorLocator.get(positiveFillKey);
    this.negativeFillColor = colorLocator.get(negativeFillKey);
  }

  public Color getLineColor() {
    return lineColor;
  }

  public Color getPositiveFillColor() {
    return positiveFillColor;
  }

  public Color getNegativeFillColor() {
    return negativeFillColor;
  }
}
