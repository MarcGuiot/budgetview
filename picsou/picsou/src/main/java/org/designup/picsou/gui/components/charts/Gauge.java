package org.designup.picsou.gui.components.charts;

import org.designup.picsou.gui.components.ActionablePanel;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.model.util.Amounts;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;

import java.awt.*;

public class Gauge extends ActionablePanel {

  private static final int ARC_WIDTH = 5;
  private static final int ARC_HEIGHT = 10;

  private double actualValue;
  private double targetValue;

  private boolean invertAll;
  private double overrunPart = 0;
  private double fillPercent = 0;
  private double overrunPercent = 0;
  private double emptyPercent = 1.0;
  private double beginPercent = 0.;
  private boolean overrunError = false;
  private boolean active = true;

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
  private Color highlightedLabelColor = Color.YELLOW.darker();
  private Color labelShadowColor = Color.gray.darker();
  private Color highlightedBackgroundColor = Color.YELLOW.brighter();

  private static final int DEFAULT_BAR_HEIGHT = 10;
  private static final int HORIZONTAL_MARGIN = 2;
  private static final double FIXED_WIDTH_RATIO = 0.1;

  private static final int HORIZONTAL_TEXT_MARGIN = 5;
  private static final int VERTICAL_TEXT_MARGIN = 1;

  private int barHeight = DEFAULT_BAR_HEIGHT;
  private double remainder;

  private String detailsTooltip;
  private String description;

  private String label;
  private FontMetrics fontMetrics;
  private int fontHeight;
  private int descent;
  private Double maxValue;
  private boolean highlighted;

  public Gauge() {
    this(false);
  }

  public Gauge(boolean invertAll) {
    this.invertAll = invertAll;

    setMinimumSize(new Dimension(20, 28));
    setPreferredSize(new Dimension(200, 28));

    setDetailsTooltipKey("gauge.unset");

    initFontMetrics(getFont());
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

  public void setLabel(String label) {
    this.label = label;
    repaint();
  }

  public String getLabel() {
    return label;
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
      setDetailsTooltipKey("gauge.unset");
    }
    else if (Amounts.isNearZero(this.targetValue - this.actualValue)) { // passer par remaining et overrun
      fillPercent = 1;
      emptyPercent = 0;
      setDetailsTooltipKey("gauge.complete");
    }
    else if (this.targetValue > 0) {
      if (this.actualValue > this.targetValue) { // if (overrun == 0 && remaining != 0)
        fillPercent = absTarget / absActual;  //==> differencié passe et future
        overrunPercent = 1 - fillPercent;
        setDetailsTooltipKey("gauge.overrun.ok", Math.abs(remainder));
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
        setDetailsTooltipKey("gauge.expected", Math.abs(remainder));
      }
    }
    else if (this.targetValue < 0) {
      if (this.actualValue < this.targetValue) {
        fillPercent = absTarget / absActual;
        overrunPercent = 1 - fillPercent;
        overrunError = true;
        emptyPercent = 0;
        setDetailsTooltipKey("gauge.overrun.error", Math.abs(remainder));
      }
      else {
        if (!sameSign && !Amounts.isNearZero(this.actualValue)) {
          fillPercent = absActual / (absActual + absTarget);
        }
        else {
          fillPercent = absActual / absTarget;
        }
        emptyPercent = 1 - fillPercent;
        setDetailsTooltipKey("gauge.partial", Math.abs(remainder));
      }
    }
    else {
      if (this.actualValue != 0) {
        fillPercent = 0;
        overrunPercent = 1;
        emptyPercent = 0;
        if (this.actualValue > 0) {
          setDetailsTooltipKey("gauge.overrun.ok", Math.abs(remainder));
        }
        else {
          overrunError = true;
          setDetailsTooltipKey("gauge.overrun.error", Math.abs(remainder));
        }
      }
    }
    repaint();
  }

  public void setValues(double actualValue, double targetValue, double partialOverrun, double remaining,
                        String detailsTooltipText, boolean targetValueUnset) {
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

    this.detailsTooltip = detailsTooltipText;
    if (targetValueUnset) {
      detailsTooltip = Lang.get("gauge.plannetUnset");
    }
    else if (Amounts.isNearZero(this.targetValue) && Amounts.isNearZero(this.actualValue)) {
      detailsTooltip = Lang.get("gauge.unset");
    }
    else if (Amounts.isNearZero(this.targetValue - this.actualValue) && Amounts.isNearZero(partialOverrun)
             && Amounts.isNearZero(remaining)) { // passer par remaining et overrun
      fillPercent = 1;
      emptyPercent = 0;
      detailsTooltip = Lang.get("gauge.complete");
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

    updateTooltip();
    repaint();
  }

  private void setDetailsTooltipKey(String key, Double... values) {
    String[] formattedValues = new String[values.length];
    for (int i = 0; i < values.length; i++) {
      formattedValues[i] = Formatting.DECIMAL_FORMAT.format(values[i]);

    }
    this.detailsTooltip = Strings.isNotEmpty(key) ? Lang.get(key, formattedValues) : "";
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
    builder.append(detailsTooltip);
    builder.append("</html>");
    setToolTipText(builder.toString());
  }

  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    if (isOpaque()) {
      g2.setColor(highlighted ? highlightedBackgroundColor : getBackground());
      g2.fillRect(0, 0, getWidth(), getHeight());
    }

    int totalWidth = getWidth() - 1 - 2 * HORIZONTAL_MARGIN;
    int width = getAdjustedWidth(totalWidth);
    int height = getHeight() - 1;
    int minX = HORIZONTAL_MARGIN;

    barHeight = Strings.isNotEmpty(label) ? getHeight() - 1 : DEFAULT_BAR_HEIGHT;

    int barTop = (height - barHeight) / 2;
    int barBottom = height - barTop;

    int beginWidth = (int)(width * beginPercent);
    int fillWidth = (int)(width * fillPercent);
    int emptyWidth = (int)(width * emptyPercent);

    int overrunWidth = width - fillWidth - emptyWidth - beginWidth;
    int overrunEnd = beginWidth + fillWidth + overrunWidth;

    int overrunStart = beginWidth + fillWidth;

    if (emptyPercent > 0) {
      fillBar(g2, emptyColorTop, emptyColorBottom, minX, overrunEnd + emptyWidth, barTop, barBottom);
    }

    if (beginPercent > 0) {
      fillBar(g2, overrunErrorColorTop, overrunErrorColorBottom, minX, beginWidth, barTop, barBottom);
    }

    if (overrunPercent > 0) {
      if (overrunError) {
        fillBar(g2, overrunErrorColorTop, overrunErrorColorBottom, minX, overrunStart + overrunWidth, barTop, barBottom);
      }
      else {
        fillBar(g2, overrunColorTop, overrunColorBottom, minX, overrunStart + overrunWidth, barTop, barBottom);
      }
    }

    if (fillPercent > 0) {
      fillBar(g2, filledColorTop, filledColorBottom, minX, fillWidth, barTop, barBottom);
    }

    drawBorder(g2, totalWidth, barTop, barHeight);

    drawText(g2);
  }

  private int getAdjustedWidth(int totalWidth) {
    Double value = Utils.max(Math.abs(actualValue), Math.abs(targetValue));
    if (Math.abs(value) < 0.1) {
      return 0;
    }
    int fixedWidth = (int)(FIXED_WIDTH_RATIO * totalWidth);
    return fixedWidth + (int)((totalWidth - fixedWidth) * getWidthRatio());
  }

  public double getWidthRatio() {
    if ((maxValue == null) || (maxValue == 0.0)) {
      return 1.0;
    }
    Double value = Utils.max(Math.abs(actualValue), Math.abs(targetValue));
    return Math.abs(value / maxValue);
  }


  private void drawBorder(Graphics2D g2, int width, int barTop, int barHeight) {
    if (isRolloverInProgress()) {
      g2.setColor(rolloverBorderColor);
      g2.drawRect(0, barTop, getWidth() - 1, barHeight);
    }
    else if (!highlighted) {
      g2.setColor(borderColor);
      g2.drawRect(0, barTop, getWidth() - 1, barHeight);
    }
  }

  private void fillBar(Graphics2D g2, Color topColor, Color bottomColor, int barX, int barWidth, int barTop, int barBottom) {
    g2.setPaint(new GradientPaint(0, barTop, topColor, 0, barBottom, bottomColor));
    g2.fillRoundRect(barX, barTop, barWidth, barHeight, ARC_WIDTH, ARC_HEIGHT);
  }

  private void drawText(Graphics2D g2) {
    if (Strings.isNullOrEmpty(label)) {
      return;
    }

    g2.setFont(getFont());
    int x = HORIZONTAL_TEXT_MARGIN;
    int y = (getHeight() + fontHeight) / 2 - descent - VERTICAL_TEXT_MARGIN + 1;

    g2.setColor(labelShadowColor);
    g2.drawString(label, x, y - 1);
    g2.drawString(label, x, y + 1);
    g2.drawString(label, x + 1, y - 1);
    g2.drawString(label, x + 1, y);
    g2.drawString(label, x + 1, y + 1);

    g2.setColor(getLabelColor());
    g2.drawString(label, x, y);
  }

  private Color getLabelColor() {
    if (isRolloverInProgress()) {
      return rolloverLabelColor;
    }
    else if (highlighted) {
      return highlightedLabelColor;
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
    return detailsTooltip;
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

  public void setHighlightedLabelColor(Color highlightedLabelColor) {
    this.highlightedLabelColor = highlightedLabelColor;
  }

  public void setInactiveLabelColor(Color inactiveLabelColor) {
    this.inactiveLabelColor = inactiveLabelColor;
  }

  public void setHighlightedBackgroundColor(Color highlightedBackgroundColor) {
    this.highlightedBackgroundColor = highlightedBackgroundColor;
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

  public void setHighlighted(boolean highlighted) {
    this.highlighted = highlighted;
    repaint();
  }
}
