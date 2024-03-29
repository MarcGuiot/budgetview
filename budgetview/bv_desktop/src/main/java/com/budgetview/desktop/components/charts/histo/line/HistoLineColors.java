package com.budgetview.desktop.components.charts.histo.line;

import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class HistoLineColors implements Disposable {

  private String positiveLineKey;
  private String negativeLineKey;
  private String positiveFillKey;
  private String negativeFillKey;

  private Color positiveLineColor;
  private Color negativeLineColor;
  private Color positiveFillColor;
  private Color negativeFillColor;

  private Directory directory;
  private HistoLineColors.ColorUpdater colorUpdater;

  public HistoLineColors(String positiveLineKey, String negativeLineKey,
                         String positiveFillKey, String negativeFillKey,
                         Directory directory) {
    this.positiveLineKey = positiveLineKey;
    this.negativeLineKey = negativeLineKey;
    this.positiveFillKey = positiveFillKey;
    this.negativeFillKey = negativeFillKey;
    this.directory = directory;

    colorUpdater = new ColorUpdater();
    directory.get(ColorService.class).addListener(colorUpdater);
  }

  public void dispose() {
    directory.get(ColorService.class).removeListener(colorUpdater);
    directory = null;
    colorUpdater = null;
  }

  private class ColorUpdater implements ColorChangeListener {
    public void colorsChanged(ColorLocator colorLocator) {
      positiveLineColor = colorLocator.get(positiveLineKey);
      negativeLineColor = colorLocator.get(negativeLineKey);
      positiveFillColor = colorLocator.get(positiveFillKey);
      negativeFillColor = colorLocator.get(negativeFillKey);
    }
  }

  public void setLineStyle(Graphics2D g2, boolean positive, boolean future) {
    if (future) {
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.8f));
    }
    else {
      g2.setComposite(AlphaComposite.Src);
    }
    g2.setColor(getLineColor(positive));
  }

  private Color getLineColor(boolean positive) {
    return positive ? positiveLineColor : negativeLineColor;
  }

  public void setFillStyle(Graphics2D g2, boolean positive, boolean current,
                           boolean future, boolean selected, boolean rollover) {
    g2.setColor(positive ? positiveFillColor : negativeFillColor);
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,
                                               getFillAlpha(current, future, selected, rollover)));
  }

  private float getFillAlpha(boolean current, boolean future, boolean selected, boolean rollover) {
    if (rollover) {
      return future ? 0.4f : 0.8f;
    }
    if (future) {
      return 0.4f;
    }
    if (current) {
      return 0.9f;
    }
    return 0.5f;
  }
}
