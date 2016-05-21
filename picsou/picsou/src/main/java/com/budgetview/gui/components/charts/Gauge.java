package com.budgetview.gui.components.charts;

import com.budgetview.gui.components.ActionablePanel;
import com.budgetview.shared.gui.gauge.GaugeModel;
import com.budgetview.shared.gui.gauge.GaugeModelListener;
import org.globsframework.utils.Strings;

import java.awt.*;

public class Gauge extends ActionablePanel {

  private static final int ARC_WIDTH = 5;
  private static final int ARC_HEIGHT = 10;

  private static final int DEFAULT_BAR_HEIGHT = 12;
  private static final int HORIZONTAL_MARGIN = 1;
  private static final int VERTICAL_MARGIN = 0;

  private static final int HORIZONTAL_TEXT_MARGIN = 7;
  private static final int VERTICAL_TEXT_MARGIN = 1;

  private boolean active = true;

  private Color borderColor = Color.gray;
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

  private int barHeight = DEFAULT_BAR_HEIGHT;

  private String label;
  private FontMetrics fontMetrics;
  private int fontHeight;
  private int descent;

  private GaugeModel model;

  public Gauge() {
    this(new GaugeModel(new LangGaugeTextSource()));
  }
  
  public Gauge(GaugeModel model) {
    this.model = model;
    model.addListener(new GaugeModelListener() {
      public void modelUpdated() {
        repaint();
      }

      public void updateTooltip(String text) {
        setToolTipText(text);
      }
    });

    setMinimumSize(new Dimension(20, 28));
    setPreferredSize(new Dimension(200, 28));

    initFontMetrics(getFont());
  }

  public GaugeModel getModel() {
    return model;
  }

  public boolean shouldInvertAll() {
    return model.shouldInvertAll();
  }

  public void setFont(Font font) {
    initFontMetrics(font);
    super.setFont(font);
  }

  private void initFontMetrics(Font font) {
    this.fontMetrics = getFontMetrics(font);
    this.barHeight = fontMetrics.getHeight() + VERTICAL_TEXT_MARGIN * 2;
    this.fontHeight = fontMetrics.getHeight();
    this.descent = fontMetrics.getDescent();
  }

  public void setLabel(String label) {
    this.label = label;
    repaint();
  }

  public String getLabel() {
    return label;
  }

  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    if (isOpaque()) {
      g2.setColor(getBackground());
      g2.fillRect(0, 0, getWidth(), getHeight());
    }

    int totalWidth = getWidth() - 1 - 2 * HORIZONTAL_MARGIN;
    int width = totalWidth;
    int height = getHeight() - 1 - 2 * VERTICAL_MARGIN;
    int minX = HORIZONTAL_MARGIN;

    barHeight = Strings.isNotEmpty(label) ? height - 1 : DEFAULT_BAR_HEIGHT;

    int barTop = (height - barHeight) / 2 + VERTICAL_MARGIN;
    int barBottom = height - barTop;

    int beginWidth = (int)(width * getBeginPercent());
    int fillWidth = (int)(width * getFillPercent());
    int emptyWidth = (int)(width * getEmptyPercent());

    int overrunWidth = width - fillWidth - emptyWidth - beginWidth;
    int overrunEnd = beginWidth + fillWidth + overrunWidth;
    int overrunStart = beginWidth + fillWidth;

    if (getEmptyPercent() > 0) {
      fillBar(g2, emptyColorTop, emptyColorBottom, minX, overrunEnd + emptyWidth, barTop, barBottom);
    }

    if (getBeginPercent() > 0) {
      fillBar(g2, overrunErrorColorTop, overrunErrorColorBottom, minX, beginWidth, barTop, barBottom);
    }

    if (getOverrunPercent() > 0) {
      if (model.hasOverrunError()) {
        fillBar(g2, overrunErrorColorTop, overrunErrorColorBottom, minX, overrunStart + overrunWidth, barTop, barBottom);
      }
      else {
        fillBar(g2, overrunColorTop, overrunColorBottom, minX, overrunStart + overrunWidth, barTop, barBottom);
      }
    }

    if (getFillPercent() > 0) {
      fillBar(g2, filledColorTop, filledColorBottom, minX, fillWidth, barTop, barBottom);
    }

    drawBorder(g2, minX, barTop, width);

    drawText(g2);
  }

  private void drawBorder(Graphics2D g2, int barX, int barTop, int barWidth) {
    g2.setColor(isRolloverInProgress() ? rolloverBorderColor : borderColor);
    g2.drawRoundRect(barX, barTop, barWidth, barHeight, ARC_WIDTH, ARC_HEIGHT);
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
    else if (!active) {
      return inactiveLabelColor;
    }
    else {
      return getForeground();
    }
  }

  public double getActualValue() {
    return model.getActualValue();
  }

  public double getTargetValue() {
    return model.getTargetValue();
  }

  public double getOverrunPart() {
    return model.getOverrunPart();
  }

  public double getFillPercent() {
    return model.getFillPercent();
  }

  public double getOverrunPercent() {
    return model.getOverrunPercent();
  }

  public double getRemainder() {
    return model.getRemainder();
  }

  public double getEmptyPercent() {
    return model.getEmptyPercent();
  }

  public boolean isErrorOverrunShown() {
    return model.hasOverrunError() && model.getOverrunPercent() > 0;
  }

  public boolean isPositiveOverrunShown() {
    return !model.hasOverrunError() && model.getOverrunPercent() > 0;
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
    return model.getBeginPercent();
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public boolean isActive() {
    return active;
  }
}
