package org.designup.picsou.gui.components.charts.histo.painters;

import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class HistoDiffColors implements ColorChangeListener {

  private String referenceLineKey;
  private String referenceOverrunKey;
  private String actualLineKey;
  private String actualOverrunKey;
  private String fillKey;

  private Color referenceLineColor;
  private Color referenceOverrunColor;
  private Color actualLineColor;
  private Color actualOverrunColor;

  private Color fillColor;

  public HistoDiffColors(String referenceLineKey,
                         String referenceOverrunKey,
                         String actualLineKey,
                         String actualOverrunKey,
                         String fillKey,
                         Directory directory) {
    this.referenceLineKey = referenceLineKey;
    this.referenceOverrunKey = referenceOverrunKey;
    this.actualLineKey = actualLineKey;
    this.actualOverrunKey = actualOverrunKey;
    this.fillKey = fillKey;

    directory.get(ColorService.class).addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    referenceLineColor = colorLocator.get(referenceLineKey);
    referenceOverrunColor = colorLocator.get(referenceOverrunKey);
    actualLineColor = colorLocator.get(actualLineKey);
    actualOverrunColor = colorLocator.get(actualOverrunKey);
    fillColor = colorLocator.get(fillKey);
  }

  public Color getReferenceLineColor() {
    return referenceLineColor;
  }

  public Color getReferenceOverrunColor() {
    return referenceOverrunColor;
  }

  public Color getActualLineColor() {
    return actualLineColor;
  }

  public Color getActualOverrunColor() {
    return actualOverrunColor;
  }

  public Color getFillColor() {
    return fillColor;
  }
}
