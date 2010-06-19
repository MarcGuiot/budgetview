package org.designup.picsou.gui.components.charts;

import org.designup.picsou.gui.components.tips.DetailsTip;
import org.designup.picsou.gui.components.tips.DetailsTipFactory;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.model.util.Amounts;
import org.designup.picsou.utils.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Gauge extends JPanel {

  private boolean invertAll;

  private double actualValue;
  private double targetValue;
  private double overrunPart = 0;

  private double fillPercent = 0;
  private double overrunPercent = 0;
  private double emptyPercent = 1.0;
  private double beginPercent = 0.;
  private boolean overrunError = false;

  private boolean rolloverInProgress;

  private Color borderColor = Color.DARK_GRAY;
  private Color rolloverBorderColor = Color.DARK_GRAY;
  private Color filledColorTop = Color.BLUE.brighter();
  private Color filledColorBottom = Color.BLUE.darker();
  private Color emptyColorTop = Color.LIGHT_GRAY;
  private Color emptyColorBottom = Color.LIGHT_GRAY.brighter();
  private Color overrunColorTop = Color.CYAN.brighter();
  private Color overrunColorBottom = Color.CYAN.darker();
  private Color overrunErrorColorTop = Color.RED.brighter();
  private Color overrunErrorColorBottom = Color.RED.darker();

  private static final int BAR_HEIGHT = 10;
  private double remainder;

  private String description;

  public Gauge() {
    this(false);
  }

  public Gauge(boolean invertAll) {
    this.invertAll = invertAll;

    setMinimumSize(new Dimension(20, 28));
    setPreferredSize(new Dimension(200, 28));

    setDescription("gauge.unset");
  }

  public void enableDetailsTips(final DetailsTipFactory detailsTipFactory) {

    setToolTipText(Lang.get("gauge.tooltip"));
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {
        rolloverInProgress = true;
        repaint();
      }

      public void mouseExited(MouseEvent e) {
        rolloverInProgress = false;
        repaint();
      }

      public void mousePressed(MouseEvent e) {
        if (!e.isConsumed()) {
          detailsTipFactory.show(Gauge.this, getDescription());
          e.consume();
        }
      }
    });
  }

  public boolean shouldInvertAll() {
    return invertAll;
  }

  public void setValues(double actualValue, double targetValue) {

    this.actualValue = actualValue * (invertAll ? -1. : 1.);
    this.targetValue = targetValue * (invertAll ? -1. : 1.);

    boolean sameSign = Amounts.sameSign(this.actualValue, this.targetValue);
    double absActual = Math.abs(this.actualValue);
    double absTarget = Math.abs(this.targetValue);
    remainder = this.targetValue - this.actualValue;

    fillPercent = 0;
    overrunPercent = 0;
    emptyPercent = 1;
    beginPercent = 0;
    overrunError = false;

    if (Amounts.isNearZero(this.targetValue) && Amounts.isNearZero(this.actualValue)) {
      setDescription("gauge.unset");
    }
    else if (Amounts.isNearZero(this.targetValue - this.actualValue)) { // passer par remaining et overrun
      fillPercent = 1;
      emptyPercent = 0;
      setDescription("gauge.complete");
    }
    else if (this.targetValue > 0) {
      if (this.actualValue > this.targetValue) { // if (overrun == 0 && remaining != 0)
        fillPercent = absTarget / absActual;  //==> differencié passe et future
        overrunPercent = 1 - fillPercent;
        setDescription("gauge.overrun.ok", Math.abs(remainder));
        emptyPercent = 0;
      }
      else {
        if (!sameSign && !Amounts.isNearZero(this.actualValue)) {
          beginPercent = absActual / (absActual + absTarget);
        }
        else {
          fillPercent = absActual / absTarget;
        }
        emptyPercent = 1 - fillPercent - beginPercent;
        setDescription("gauge.expected", Math.abs(remainder));
      }
    }
    else if (this.targetValue < 0) {
      if (this.actualValue < this.targetValue) {
        fillPercent = absTarget / absActual;
        overrunPercent = 1 - fillPercent;
        overrunError = true;
        emptyPercent = 0;
        setDescription("gauge.overrun.error", Math.abs(remainder));
      }
      else {
        if (!sameSign && !Amounts.isNearZero(this.actualValue)) {
          fillPercent = absActual / (absActual + absTarget);
        }
        else {
          fillPercent = absActual / absTarget;
        }
        emptyPercent = 1 - fillPercent;
        setDescription("gauge.partial", Math.abs(remainder));
      }
    }
    else {
      if (this.actualValue != 0) {
        fillPercent = 0;
        overrunPercent = 1;
        emptyPercent = 0;
        if (this.actualValue > 0) {
          setDescription("gauge.overrun.ok", Math.abs(remainder));
        }
        else {
          overrunError = true;
          setDescription("gauge.overrun.error", Math.abs(remainder));
        }
      }
    }
    repaint();
  }

  public void setValues(double actualValue, double targetValue, double partialOverrun, double remaining, String text) {
    fillPercent = 0;
    overrunPercent = 0;
    emptyPercent = 1;
    beginPercent = 0;
    overrunError = false;

    this.actualValue = actualValue;
    this.targetValue = targetValue;
    this.overrunPart = partialOverrun;
    this.remainder = remaining;
    boolean sameSign = Amounts.sameSign(this.actualValue, this.targetValue);

    if (Amounts.isNearZero(this.targetValue) && Amounts.isNearZero(this.actualValue)) {
      text = Lang.get("gauge.unset");
    }
    else if (Amounts.isNearZero(this.targetValue - this.actualValue) && Amounts.isNearZero(partialOverrun)
             && Amounts.isNearZero(remaining)) { // passer par remaining et overrun
      fillPercent = 1;
      emptyPercent = 0;
      text = Lang.get("gauge.complete");
    }
    else if (Math.abs(actualValue) > Math.abs(targetValue) && sameSign) {
      double total = Math.abs(remaining) + Math.abs(actualValue);
      fillPercent = (Math.abs(actualValue) - Math.abs(partialOverrun)) / total;
      overrunPercent = Math.abs(partialOverrun / total);
    }
    else if (!sameSign && !Amounts.isNearZero(this.actualValue)) {
      double total = Math.abs(remaining) + Math.abs(actualValue) + Math.abs(targetValue);
      beginPercent = (Math.abs(actualValue) - Math.abs(partialOverrun)) / total;
      overrunPercent = Math.abs(partialOverrun / total);
    }
    else {
      double total = Math.abs(targetValue);
      fillPercent = (Math.abs(actualValue) - Math.abs(partialOverrun)) / total;
      overrunPercent = Math.abs(partialOverrun / total);
    }
    emptyPercent = 1 - overrunPercent - fillPercent - beginPercent;

    if (Amounts.isNearZero(targetValue)) {
      if (actualValue > 0) {
        overrunError = invertAll;
      }
      else {
        overrunError = !invertAll;
      }
    }
    else {
      if (partialOverrun > 0) {
        overrunError = invertAll;
      }
      else {
        overrunError = !invertAll;
      }
    }

    this.description = text;
    repaint();
  }

  protected void setDescription(String key, Double... values) {
    String[] formattedValues = new String[values.length];
    for (int i = 0; i < values.length; i++) {
      formattedValues[i] = Formatting.DECIMAL_FORMAT.format(values[i]);

    }
    this.description = Lang.get(key, formattedValues);
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

    int beginWidth = (int)(width * beginPercent);
    int fillWidth = (int)(width * fillPercent);
    int emptyWidth = (int)(width * emptyPercent);

    int overrunWidth = width - fillWidth - emptyWidth - beginWidth;
    int overrunEnd = beginWidth + fillWidth + overrunWidth;

    int overrunStart = beginWidth + fillWidth;

    if (fillPercent > 0) {
      fillBar(g2, filledColorTop, filledColorBottom, 0, fillWidth, barTop, barBottom);
    }

    if (beginPercent > 0) {
      fillBar(g2, overrunErrorColorTop, overrunErrorColorBottom, 0, beginWidth, barTop, barBottom);
    }

    if (overrunPercent > 0) {
      if (overrunError) {
        fillBar(g2, overrunErrorColorTop, overrunErrorColorBottom, overrunStart, overrunWidth, barTop, barBottom);
      }
      else {
        fillBar(g2, overrunColorTop, overrunColorBottom, overrunStart, overrunWidth, barTop, barBottom);
      }
    }

    if (emptyPercent > 0) {
      fillBar(g2, emptyColorTop, emptyColorBottom, overrunEnd, emptyWidth, barTop, barBottom);
    }

    drawBorder(g2, width, barTop, BAR_HEIGHT);
  }

  private void drawBorder(Graphics2D g2, int width, int barTop, int barHeight) {
    g2.setColor(rolloverInProgress ? rolloverBorderColor : borderColor);
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

  public double getRemainder() {
    return remainder;
  }

  public double getEmptyPercent() {
    return emptyPercent;
  }

  public boolean isErrorOverrunShown() {
    return overrunError && overrunPercent > 0;
  }

  public boolean isPositiveOverrunShown() {
    return !overrunError && overrunPercent > 0;
  }

  public String getDescription() {
    return description;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  public void setRolloverBorderColor(Color borderColor) {
    this.rolloverBorderColor = borderColor;
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

  public double getBeginPercent() {
    return beginPercent;
  }
}
