package org.designup.picsou.gui.components.charts.histo.daily;

import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class HistoDailyColors implements ColorChangeListener {

  private String pastPositiveLineKey;
  private String pastNegativeLineKey;
  private String pastPositiveFillKey;
  private String pastNegativeFillKey;
  private String futurePositiveLineKey;
  private String futureNegativeLineKey;
  private String futurePositiveFillKey;
  private String futureNegativeFillKey;
  private String verticalDividerKey;

  private Color pastPositiveLineColor;
  private Color pastNegativeLineColor;
  private Color pastPositiveFillColor;
  private Color pastNegativeFillColor;
  private Color futurePositiveLineColor;
  private Color futureNegativeLineColor;
  private Color futurePositiveFillColor;
  private Color futureNegativeFillColor;
  private Color verticalDividerColor;

  public HistoDailyColors(String pastPositiveLineKey, String pastNegativeLineKey,
                          String pastPositiveFillKey, String pastNegativeFillKey,
                          String futurePositiveLineKey, String futureNegativeLineKey,
                          String futurePositiveFillKey, String futureNegativeFillKey,
                          String verticalDividerKey,
                          Directory directory) {
    this.pastPositiveLineKey = pastPositiveLineKey;
    this.pastNegativeLineKey = pastNegativeLineKey;
    this.pastPositiveFillKey = pastPositiveFillKey;
    this.pastNegativeFillKey = pastNegativeFillKey;
    this.futurePositiveLineKey = futurePositiveLineKey;
    this.futureNegativeLineKey = futureNegativeLineKey;
    this.futurePositiveFillKey = futurePositiveFillKey;
    this.futureNegativeFillKey = futureNegativeFillKey;
    this.verticalDividerKey = verticalDividerKey;

    directory.get(ColorService.class).addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    this.pastPositiveLineColor = colorLocator.get(pastPositiveLineKey);
    this.pastNegativeLineColor = colorLocator.get(pastNegativeLineKey);
    this.pastPositiveFillColor = colorLocator.get(pastPositiveFillKey);
    this.pastNegativeFillColor = colorLocator.get(pastNegativeFillKey);
    this.futurePositiveLineColor = colorLocator.get(futurePositiveLineKey);
    this.futureNegativeLineColor = colorLocator.get(futureNegativeLineKey);
    this.futurePositiveFillColor = colorLocator.get(futurePositiveFillKey);
    this.futureNegativeFillColor = colorLocator.get(futureNegativeFillKey);
    this.verticalDividerColor = colorLocator.get(verticalDividerKey);
  }

  public Color getPastPositiveLineColor() {
    return pastPositiveLineColor;
  }

  public Color getPastNegativeLineColor() {
    return pastNegativeLineColor;
  }

  public Color getPastPositiveFillColor() {
    return pastPositiveFillColor;
  }

  public Color getPastNegativeFillColor() {
    return pastNegativeFillColor;
  }

  public Color getFuturePositiveLineColor() {
    return futurePositiveLineColor;
  }

  public Color getFutureNegativeLineColor() {
    return futureNegativeLineColor;
  }

  public Color getFuturePositiveFillColor() {
    return futurePositiveFillColor;
  }

  public Color getFutureNegativeFillColor() {
    return futureNegativeFillColor;
  }

  public Color getVerticalDividerColor() {
    return verticalDividerColor;
  }
}
