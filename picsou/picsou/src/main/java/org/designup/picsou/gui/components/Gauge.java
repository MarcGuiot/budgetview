package org.designup.picsou.gui.components;

import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.model.util.Amounts;
import org.designup.picsou.utils.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Gauge extends JPanel {

  private boolean overrunIsAnError;
  private boolean invertedSignIsAnError;

  private double actualValue;
  private double targetValue;
  private double overrunPart = 0;

  private double fillPercent = 0;
  private double overrunPercent = 0;
  private double emptyPercent = 1.0;
  private boolean overrunError = false;

  private Color borderColor = Color.DARK_GRAY;
  private Color filledColorTop = Color.BLUE.brighter();
  private Color filledColorBottom = Color.BLUE.darker();
  private Color emptyColorTop = Color.LIGHT_GRAY;
  private Color emptyColorBottom = Color.LIGHT_GRAY.brighter();
  private Color overrunColorTop = Color.CYAN.brighter();
  private Color overrunColorBottom = Color.CYAN.darker();
  private Color overrunErrorColorTop = Color.RED.brighter();
  private Color overrunErrorColorBottom = Color.RED.darker();

  private static final int BAR_HEIGHT = 10;
  private static final float TRIANGLE_HEIGHT = 16f;
  private static final float TRIANGLE_WIDTH = 16f;

  public Gauge() {
    this(true, true);
  }

  public Gauge(boolean overrunIsAnError, boolean invertedSignIsAnError) {
    this.overrunIsAnError = overrunIsAnError;
    this.invertedSignIsAnError = invertedSignIsAnError;

    setMinimumSize(new Dimension(60, 28));
    setPreferredSize(new Dimension(200, 28));

    setToolTip("gauge.unset");
  }

  public static void showToolTip(JComponent comp) {
    Action action = comp.getActionMap().get("postTip");
    if (action == null) {
      return;
    }
    ActionEvent event = new ActionEvent(comp, ActionEvent.ACTION_PERFORMED,
                                        "postTip", EventQueue.getMostRecentEventTime(), 0);
    action.actionPerformed(event);
  }

  public static void hideToolTip(JComponent comp) {
    Action action = comp.getActionMap().get("hideTip");
    if (action == null) {
      return;
    }
    ActionEvent event = new ActionEvent(comp, ActionEvent.ACTION_PERFORMED,
                                        "hideTip", EventQueue.getMostRecentEventTime(), 0);
    action.actionPerformed(event);
  }

  public void setValues(double actualValue, double targetValue) {
    this.actualValue = actualValue;
    this.targetValue = targetValue;

    boolean sameSign = Amounts.sameSign(this.actualValue, this.targetValue);
    double absActual = Math.abs(this.actualValue);
    double absTarget = Math.abs(this.targetValue);

    if ((absTarget == 0) && (absActual == 0)) {
      fillPercent = 0;
      overrunPercent = 0;
      emptyPercent = 1;
      overrunError = false;
      setToolTip("gauge.unset");
    }
    else if (Amounts.isNearZero(absTarget)) {
      fillPercent = 0;
      overrunPercent = 1;
      emptyPercent = 0;
      overrunError = overrunIsAnError;
      setToolTip("gauge.overrun." + (overrunIsAnError ? "error" : "ok"), absActual);
    }
    else if (!sameSign) {
      fillPercent = 0;
      overrunPercent = absActual / (absActual + absTarget);
      emptyPercent = 1 - overrunPercent;
      overrunError = invertedSignIsAnError;
      setToolTip("gauge.inverted." + (invertedSignIsAnError ? "error" : "ok"), absActual);
    }
    else if (absActual - absTarget >= 0.01) {
      fillPercent = absTarget / absActual;
      overrunPercent = 1 - fillPercent;
      emptyPercent = 0;
      overrunError = overrunIsAnError;
      setToolTip("gauge.overrun." + (overrunIsAnError ? "error" : "ok"), absActual - absTarget);
    }
    else if (Amounts.isNearZero(absTarget - absActual)) {
      fillPercent = 1;
      overrunPercent = 0;
      emptyPercent = 0;
      overrunError = false;
      setToolTip("gauge.complete");
    }
    else {
      fillPercent = absActual / absTarget;
      if (fillPercent > 1) {
        fillPercent = 1;
      }
      overrunPercent = 0;
      emptyPercent = 1 - fillPercent;
      overrunError = false;
      setToolTip("gauge.partial", absTarget - absActual);
    }
    repaint();
  }

  public void setValues(double actualValue, double targetValue, double partialOverrun) {
    if (Amounts.isNearZero(partialOverrun) || (Math.abs(actualValue) > Math.abs(targetValue))) {
      setValues(actualValue, targetValue);
      return;
    }

    this.actualValue = actualValue;
    this.targetValue = targetValue;
    this.overrunPart = partialOverrun;

    double fillValue = actualValue - partialOverrun;
    fillPercent = Math.abs(fillValue / targetValue);
    overrunPercent = Math.abs(partialOverrun / targetValue);
    emptyPercent = 1 - overrunPercent - fillPercent;
    overrunError = overrunIsAnError;
    double remainingValue = targetValue - actualValue;
    setToolTip("gauge.partial.overrun." + (overrunIsAnError ? "error" : "ok"),
               remainingValue, Math.abs(partialOverrun));

    repaint();
  }

  private void setToolTip(String key, Double... values) {
    String[] formattedValues = new String[values.length];
    for (int i = 0; i < values.length; i++) {
      formattedValues[i] = Formatting.DECIMAL_FORMAT.format(values[i]);

    }
    this.setToolTipText(Lang.get(key, formattedValues));
  }

  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int width = getWidth() - 1;
    int height = getHeight() - 1;

    if (isOpaque()) {
      g2.setColor(getBackground());
      g2.fillRect(0, 0, width, height);
    }

    int barTop = (height - BAR_HEIGHT) / 2;
    int barBottom = height - barTop;

    int fillWidth = (int)(width * fillPercent);
    int emptyWidth = (int)(width * emptyPercent);

    int overrunWidth = width - fillWidth - emptyWidth;
    int overrunEnd = fillWidth + overrunWidth;

    if (fillPercent > 0) {
      fillBar(g2, filledColorTop, filledColorBottom, 0, fillWidth, barTop, barBottom);
    }

    if (overrunPercent > 0) {
      if (overrunError) {
        fillBar(g2, overrunErrorColorTop, overrunErrorColorBottom, fillWidth, overrunWidth, barTop, barBottom);
      }
      else {
        fillBar(g2, overrunColorTop, overrunColorBottom, fillWidth, overrunWidth, barTop, barBottom);
      }
    }

    if (emptyPercent > 0) {
      fillBar(g2, emptyColorTop, emptyColorBottom, overrunEnd, emptyWidth, barTop, barBottom);
    }

    drawBorder(g2, width, barTop, BAR_HEIGHT);
  }

  private void drawBorder(Graphics2D g2, int width, int barTop, int barHeight) {
    g2.setColor(borderColor);
    g2.drawRect(0, barTop, width, barHeight);
  }

  private void fillBar(Graphics2D g2, Color topColor, Color bottomColor, int barX, int barWidth, int barTop, int barBottom) {
    g2.setPaint(new GradientPaint(0, barTop, topColor, 0, barBottom, bottomColor));
    g2.fillRect(barX, barTop, barWidth, BAR_HEIGHT);
  }

  public double getActualValue() {
    return actualValue;
  }

  public double getTargetValue() {
    return targetValue;
  }

  public double getOverrunPart() {
    return overrunPart;
  }

  public double getFillPercent() {
    return fillPercent;
  }

  public double getOverrunPercent() {
    return overrunPercent;
  }

  public double getEmptyPercent() {
    return emptyPercent;
  }

  public void setOverrunIsAnError(boolean value) {
    this.overrunIsAnError = value;
  }

  public boolean isErrorOverrunShown() {
    return overrunIsAnError && (overrunPercent > 0);
  }

  public boolean isPositiveOverrunShown() {
    return !overrunIsAnError && (overrunPercent > 0);
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  public void setFilledColorTop(Color filledColorTop) {
    this.filledColorTop = filledColorTop;
  }

  public void setFilledColorBottom(Color filledColorBottom) {
    this.filledColorBottom = filledColorBottom;
  }

  public void setEmptyColorTop(Color emptyColorTop) {
    this.emptyColorTop = emptyColorTop;
  }

  public void setEmptyColorBottom(Color emptyColorBottom) {
    this.emptyColorBottom = emptyColorBottom;
  }

  public void setOverrunColorTop(Color overrunColorTop) {
    this.overrunColorTop = overrunColorTop;
  }

  public void setOverrunColorBottom(Color overrunColorBottom) {
    this.overrunColorBottom = overrunColorBottom;
  }

  public void setOverrunErrorColorTop(Color overrunErrorColorTop) {
    this.overrunErrorColorTop = overrunErrorColorTop;
  }

  public void setOverrunErrorColorBottom(Color overrunErrorColorBottom) {
    this.overrunErrorColorBottom = overrunErrorColorBottom;
  }

  public void setInvertedSignIsAnError(boolean invertedSignIsAnError) {
    this.invertedSignIsAnError = invertedSignIsAnError;
  }
}
