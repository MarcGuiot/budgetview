package org.designup.picsou.gui.components.charts.histo.line;

import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class HistoLineColors implements ColorChangeListener {

  private String positiveLineKey;
  private String negativeLineKey;
  private String positiveFillKey;
  private String negativeFillKey;
  private String verticalDividerKey;

  private Color positiveLineColor;
  private Color negativeLineColor;
  private Color positiveFillColor;
  private Color negativeFillColor;
  private Color verticalDividerColor;

  public HistoLineColors(String positiveLineKey, String negativeLineKey,
                         String positiveFillKey, String negativeFillKey,
                         String verticalDividerKey,
                         Directory directory) {
    this.positiveLineKey = positiveLineKey;
    this.negativeLineKey = negativeLineKey;
    this.positiveFillKey = positiveFillKey;
    this.negativeFillKey = negativeFillKey;
    this.verticalDividerKey = verticalDividerKey;

    directory.get(ColorService.class).addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    this.positiveLineColor = colorLocator.get(positiveLineKey);
    this.negativeLineColor = colorLocator.get(negativeLineKey);
    this.positiveFillColor = colorLocator.get(positiveFillKey);
    this.negativeFillColor = colorLocator.get(negativeFillKey);
    this.verticalDividerColor = colorLocator.get(verticalDividerKey);
  }

  public void setVerticalDividerStyle(Graphics2D g2) {
    g2.setComposite(AlphaComposite.Src);
    g2.setColor(verticalDividerColor);
  }

  public void setLineStyle(Graphics2D g2, boolean positive, boolean future) {
    if (future) {
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.35f));
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
    if (selected) {
      return future ? 0.3f : 0.7f;
    }
    if (future) {
      return 0.2f;
    }
    if (current) {
      return 0.9f;
    }
    return 0.5f;
  }
}
