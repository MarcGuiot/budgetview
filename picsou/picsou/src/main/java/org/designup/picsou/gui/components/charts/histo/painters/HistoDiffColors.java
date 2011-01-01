package org.designup.picsou.gui.components.charts.histo.painters;

import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class HistoDiffColors implements ColorChangeListener {

  private String fillKey;
  private String lineKey;

  private Color fillColor;
  private Color lineColor;

  public HistoDiffColors(String lineKey,
                         String fillKey,
                         Directory directory) {
    this.lineKey = lineKey;
    this.fillKey = fillKey;

    directory.get(ColorService.class).addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    lineColor = colorLocator.get(lineKey);
    fillColor = colorLocator.get(fillKey);
  }

  public Color getFillColor() {
    return fillColor;
  }

  public Color getLineColor() {
    return lineColor;
  }
}
