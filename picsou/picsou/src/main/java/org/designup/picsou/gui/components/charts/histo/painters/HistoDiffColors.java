package org.designup.picsou.gui.components.charts.histo.painters;

import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class HistoDiffColors implements ColorChangeListener {

  private String referenceLineKey;
  private String actualLineKey;
  private String fillKey;

  private Color referenceLineColor;
  private Color actualLineColor;

  private Color fillColor;

  public HistoDiffColors(String referenceLineKey,
                         String actualLineKey,
                         String fillKey,
                         Directory directory) {
    this.referenceLineKey = referenceLineKey;
    this.actualLineKey = actualLineKey;
    this.fillKey = fillKey;

    directory.get(ColorService.class).addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    referenceLineColor = colorLocator.get(referenceLineKey);
    actualLineColor = colorLocator.get(actualLineKey);
    fillColor = colorLocator.get(fillKey);
  }

  public Color getReferenceLineColor() {
    return referenceLineColor;
  }

  public Color getActualLineColor() {
    return actualLineColor;
  }

  public Color getFillColor() {
    return fillColor;
  }
}
