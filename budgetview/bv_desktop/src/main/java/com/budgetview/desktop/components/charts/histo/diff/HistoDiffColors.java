package com.budgetview.desktop.components.charts.histo.diff;

import com.budgetview.shared.utils.Amounts;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class HistoDiffColors implements ColorChangeListener, Disposable {

  private String positiveFillKey;
  private String negativeFillKey;
  private String positiveLineKey;
  private String negativeLineKey;

  private Color positiveFillColor;
  private Color positiveLineColor;
  private Color negativeFillColor;
  private Color negativeLineColor;

  private final Directory directory;

  public HistoDiffColors(String lineKey,
                         String fillKey,
                         Directory directory) {
    this(lineKey, lineKey, fillKey, fillKey, directory);
  }

  public HistoDiffColors(String positiveLineKey,
                         String negativeLineKey,
                         String positiveFillKey,
                         String negativeFillKey,
                         Directory directory) {
    this.positiveLineKey = positiveLineKey;
    this.positiveFillKey = positiveFillKey;
    this.negativeLineKey = negativeLineKey;
    this.negativeFillKey = negativeFillKey;
    this.directory = directory;

    directory.get(ColorService.class).addListener(this);
  }

  public void dispose() {
    directory.get(ColorService.class).removeListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    positiveLineColor = colorLocator.get(positiveLineKey);
    positiveFillColor = colorLocator.get(positiveFillKey);
    negativeLineColor = colorLocator.get(negativeLineKey);
    negativeFillColor = colorLocator.get(negativeFillKey);
  }

  public Color getPositiveFillColor() {
    return positiveFillColor;
  }

  public Color getPositiveLineColor() {
    return positiveLineColor;
  }

  public Color getNegativeFillColor() {
    return negativeFillColor;
  }

  public Color getNegativeLineColor() {
    return negativeLineColor;
  }

  public Color getLineColor(double value, boolean inverted) {
    if (Amounts.isNearZero(value)) {
      return inverted ? negativeLineColor : positiveLineColor;
    }
    return ((value > 0) && !inverted) ? positiveLineColor : negativeLineColor;
  }

  public Color getFillColor(double value, boolean inverted) {
    return (value >= 0 && !inverted) ? positiveFillColor : negativeFillColor;
  }
}
