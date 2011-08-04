package org.designup.picsou.gui.components.charts;

import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.model.util.Amounts;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

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
  private boolean active = true;

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
  private Color rolloverLabelColor = Color.BLUE;
  private Color inactiveLabelColor = Color.GRAY.brighter();
  private Color labelShadowColor = Color.gray.darker();

  private static final int DEFAULT_BAR_HEIGHT = 10;

  private static final int HORIZONTAL_TEXT_MARGIN = 4;
  private static final int VERTICAL_TEXT_MARGIN = 1;

  private int barHeight = DEFAULT_BAR_HEIGHT;
  private double remainder;

  private String tooltip;
  private String description;

  private boolean targetValueUnset = false;
  private String text;
  private FontMetrics fontMetrics;
  private int fontHeight;
  private int descent;
  private Double maxValue;

  private java.util.List<ActionListener> actionListeners = new ArrayList<ActionListener>();

  public Gauge() {
    this(false);
  }

  public Gauge(boolean invertAll) {
    this.invertAll = invertAll;

    setMinimumSize(new Dimension(20, 28));
    setPreferredSize(new Dimension(200, 28));

    setTooltipKey("gauge.unset");

    initFontMetrics(getFont());
  }

  public void setActionListener(final ActionListener listener) {

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
          listener.actionPerformed(new ActionEvent(Gauge.this, 0, "action"));
          e.consume();
        }
      }
    });
  }

  public boolean shouldInvertAll() {
    return invertAll;
  }

  public void setFont(Font font) {
    initFontMetrics(font);
    super.setFont(font);
  }

  private void initFontMetrics(Font font) {
    this.fontMetrics = getFontMetrics(font);
    this.barHeight = fontMetrics.getHeight() + VERTICAL_TEXT_MARGIN * 2;
    fontHeight = fontMetrics.getHeight();
    descent = fontMetrics.getDescent();
  }

  public void setText(String text) {
    this.text = text;
    repaint();
  }

  public String getText() {
    return text;
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
      setTooltipKey("gauge.unset");
    }
    else if (Amounts.isNearZero(this.targetValue - this.actualValue)) { // passer par remaining et overrun
      fillPercent = 1;
      emptyPercent = 0;
      setTooltipKey("gauge.complete");
    }
    else if (this.targetValue > 0) {
      if (this.actualValue > this.targetValue) { // if (overrun == 0 && remaining != 0)
        fillPercent = absTarget / absActual;  //==> differencié passe et future
        overrunPercent = 1 - fillPercent;
        setTooltipKey("gauge.overrun.ok", Math.abs(remainder));
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
        setTooltipKey("gauge.expected", Math.abs(remainder));
      }
    }
    else if (this.targetValue < 0) {
      if (this.actualValue < this.targetValue) {
        fillPercent = absTarget / absActual;
        overrunPercent = 1 - fillPercent;
        overrunError = true;
        emptyPercent = 0;
        setTooltipKey("gauge.overrun.error", Math.abs(remainder));
      }
      else {
        if (!sameSign && !Amounts.isNearZero(this.actualValue)) {
          fillPercent = absActual / (absActual + absTarget);
        }
        else {
          fillPercent = absActual / absTarget;
        }
        emptyPercent = 1 - fillPercent;
        setTooltipKey("gauge.partial", Math.abs(remainder));
      }
    }
    else {
      if (this.actualValue != 0) {
        fillPercent = 0;
        overrunPercent = 1;
        emptyPercent = 0;
        if (this.actualValue > 0) {
          setTooltipKey("gauge.overrun.ok", Math.abs(remainder));
        }
        else {
          overrunError = true;
          setTooltipKey("gauge.overrun.error", Math.abs(remainder));
        }
      }
    }
    repaint();
  }

  public void setValues(double actualValue, double targetValue, double partialOverrun, double remaining, String text,
                        boolean targetValueUnset) {
    this.targetValueUnset = targetValueUnset;
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

    if (targetValueUnset) {
      text = Lang.get("gauge.plannetUnset");
    }
    else if (Amounts.isNearZero(this.targetValue) && Amounts.isNearZero(this.actualValue)) {
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

    this.tooltip = text;
    updateTooltip();
    repaint();
  }

  private void setTooltipKey(String key, Double... values) {
    String[] formattedValues = new String[values.length];
    for (int i = 0; i < values.length; i++) {
      formattedValues[i] = Formatting.DECIMAL_FORMAT.format(values[i]);

    }
    this.tooltip = Strings.isNotEmpty(key) ? Lang.get(key, formattedValues) : "";
    updateTooltip();
  }

  public void setDescription(String text) {
    this.description = text;
    updateTooltip();
  }

  private void updateTooltip() {
    StringBuilder builder = new StringBuilder("<html>");
    if (Strings.isNotEmpty(description)) {
      builder.append(description).append("<br/>");
    }
    builder.append(tooltip);
    builder.append("</html>");
    setToolTipText(builder.toString());
  }

  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    if (isOpaque()) {
      g2.setColor(getBackground());
      g2.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
    }

    int width = (int)(getWidthRatio() * (getWidth() - 1));
    int height = getHeight() - 1;

    barHeight = Strings.isNotEmpty(text) ? getHeight() - 1 : DEFAULT_BAR_HEIGHT;

    int barTop = (height - barHeight) / 2;
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

    drawBorder(g2, width, barTop, barHeight);

    drawText(g2);
  }

  public double getWidthRatio() {
    if ((maxValue == null) || (maxValue == 0.0)) {
      return 1.0;
    }
    Double value = Utils.max(Math.abs(actualValue), Math.abs(targetValue));
    return Math.abs(value / maxValue);
  }

  private void drawBorder(Graphics2D g2, int width, int barTop, int barHeight) {
    if (rolloverInProgress) {
      g2.setColor(rolloverBorderColor);
      g2.drawRect(0, barTop, getWidth() - 1, barHeight);
    }
    else {
      g2.setColor(borderColor);
      g2.drawRect(0, barTop, width, barHeight);
    }
  }

  private void fillBar(Graphics2D g2, Color topColor, Color bottomColor, int barX, int barWidth, int barTop, int barBottom) {
    g2.setPaint(new GradientPaint(0, barTop, topColor, 0, barBottom, bottomColor));
    g2.fillRect(barX, barTop, barWidth, barHeight);
  }

  private void drawText(Graphics2D g2) {
    if (Strings.isNullOrEmpty(text)) {
      return;
    }

    g2.setFont(getFont());
    int x = HORIZONTAL_TEXT_MARGIN;
    int y = (getHeight() + fontHeight) / 2 - descent - VERTICAL_TEXT_MARGIN + 1;

    g2.setColor(labelShadowColor);
    g2.drawString(text, x + 1, y + 1);

    g2.setColor(getLabelColor());
    g2.drawString(text, x, y);
  }

  private Color getLabelColor() {
    if (rolloverInProgress) {
      return rolloverLabelColor;
    }
    else if (!active) {
      return inactiveLabelColor;
    }
    else {
      return getForeground();
    }
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

  public String getTooltip() {
    return tooltip;
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

  public void setLabelShadowColor(Color shadowColor) {
    this.labelShadowColor = shadowColor;
  }

  public void setRolloverLabelColor(Color rolloverLabelColor) {
    this.rolloverLabelColor = rolloverLabelColor;
  }

  public void setInactiveLabelColor(Color inactiveLabelColor) {
    this.inactiveLabelColor = inactiveLabelColor;
  }

  public double getBeginPercent() {
    return beginPercent;
  }

  public void setMaxValue(Double maxValue) {
    this.maxValue = maxValue;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public boolean isActive() {
    return active;
  }
}
