package com.budgetview.shared.gui.histochart;

import com.budgetview.shared.gui.TextMetrics;
import org.globsframework.utils.Utils;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class HistoChartMetrics {

  private static final int VERTICAL_CHART_PADDING = 5;
  private static final int MIN_SCALE_ZONE_WIDTH = 40;
  private static final int MIN_SCALE_ZONE_HEIGHT = 30;
  private static final int RIGHT_SCALE_MARGIN = 10;
  private static final int LABEL_ZONE_HEIGHT = 15;
  private static final int LABEL_BOTTOM_MARGIN = 2;
  private static final int SECTION_ZONE_HEIGHT = 20;
  private static final int SECTION_BOTTOM_MARGIN = 5;
  private static final int INNER_LABEL_HEIGHT = 18;
  private static final int INNER_LABEL_BOTTOM_MARGIN = 4;
  private static final int MIN_NEGATIVE_HEIGHT = 5;

  private final double[] SCALES = {0.25, 0.5, 1, 2, 2.5, 5};

  private final int left;
  private final int top;
  private int usablePanelWidth;
  private int usablePanelHeight;
  private TextMetrics textMetrics;
  private int columnCount;
  private HistoChartConfig config;
  private double maxPositiveValue;
  private double maxNegativeValue;

  private int positiveHeight;
  private int negativeHeight;
  private int columnWidth;
  private int scaleZoneWidth;
  private int chartWidth;
  private int chartHeight;
  private int sectionZoneHeight;

  private int labelZoneHeight;
  private int labelBottomMargin;
  private int innerLabelY;
  private int usableChartHeight;

  public HistoChartMetrics(int panelWidth,
                           int panelHeight,
                           Insets insets,
                           TextMetrics textMetrics,
                           int columnCount,
                           double maxNegativeValue,
                           double maxPositiveValue,
                           HistoChartConfig config,
                           boolean containsSections,
                           boolean snapToScale) {
    this.left = insets.left;
    this.top = insets.top;
    this.usablePanelWidth = panelWidth - insets.left - insets.right;
    this.usablePanelHeight = panelHeight - insets.top - insets.bottom;
    this.textMetrics = textMetrics;
    this.columnCount = columnCount;
    this.config = config;
    this.maxPositiveValue = adjustLimit(maxPositiveValue, snapToScale);
    this.maxNegativeValue = adjustLimit(maxNegativeValue, snapToScale);

    this.scaleZoneWidth = config.drawScale || config.keepScaleZone ? scaleZoneWidth() : 0;
    this.sectionZoneHeight = config.drawLabels && config.drawSections && containsSections ? SECTION_ZONE_HEIGHT : 0;
    this.chartWidth = usablePanelWidth - scaleZoneWidth;
    this.columnWidth = columnCount != 0 ? chartWidth / columnCount : 0;

    this.labelZoneHeight = config.drawLabels ? LABEL_ZONE_HEIGHT : 0;
    this.labelBottomMargin = config.drawLabels ? LABEL_BOTTOM_MARGIN : 0;

    this.chartHeight = this.usablePanelHeight - labelZoneHeight - sectionZoneHeight;
    this.usableChartHeight = chartHeight - (config.drawInnerLabels ? INNER_LABEL_HEIGHT : 0);
    if (maxNegativeValue != 0.0) {
      this.positiveHeight = (int)((usableChartHeight - 2 * VERTICAL_CHART_PADDING) * this.maxPositiveValue
                                  / (this.maxPositiveValue + this.maxNegativeValue));
      this.negativeHeight = usableChartHeight - 2 * VERTICAL_CHART_PADDING - positiveHeight;
    }
    else {
      this.positiveHeight = usableChartHeight - VERTICAL_CHART_PADDING - MIN_NEGATIVE_HEIGHT;
      this.negativeHeight = MIN_NEGATIVE_HEIGHT;
    }

    innerLabelY = columnTop() + chartHeight - INNER_LABEL_BOTTOM_MARGIN;
  }

  private double adjustLimit(double value, boolean snapToScale) {
    if (!snapToScale) {
      return value;
    }

    for (long power = 1; ; power *= 10) {
      for (double scale : SCALES) {
        double limit = scale * power;
        if (limit > value) {
          return limit;
        }
      }
    }
  }

  private int scaleZoneWidth() {
    double value = Math.max(maxPositiveValue, maxNegativeValue);
    int valueWidth = textMetrics.stringWidth(Integer.toString((int)value));
    return Math.max(MIN_SCALE_ZONE_WIDTH, valueWidth + RIGHT_SCALE_MARGIN);
  }

  public int middleX(int columnIndex) {
    checkIndex(columnIndex);
    return left + scaleZoneWidth + columnIndex * columnWidth + columnWidth / 2;
  }

  public int left(int columnIndex) {
    checkIndex(columnIndex);
    return left + scaleZoneWidth + columnIndex * columnWidth;
  }

  public int getColumnAt(int x) {
    if ((columnWidth == 0 || x < scaleZoneWidth)) {
      return -1;
    }
    return (x - left - scaleZoneWidth) / columnWidth;
  }

  public int right(int columnIndex) {
    checkIndex(columnIndex);
    if (columnIndex == columnCount - 1) {
      return left + usablePanelWidth;
    }

    return left(columnIndex) + columnWidth;
  }

  public int columnTop() {
    return top + sectionZoneHeight;
  }

  public int usableColumnBottom() {
    return columnTop() + usableChartHeight;
  }

  public int columnWidth() {
    return columnWidth;
  }

  public int columnHeight() {
    return chartHeight;
  }

  public int labelZoneHeightWithMargin() {
    return labelZoneHeight + 2;
  }

  public int y(double value) {
    if (value >= 0) {
      return (int)(positiveHeight * (1 - value / maxPositiveValue)) + columnTop() + VERTICAL_CHART_PADDING;
    }
    else {
      return positiveHeight + (int)(negativeHeight * Math.abs(value) / maxNegativeValue) + columnTop() + VERTICAL_CHART_PADDING;
    }
  }

  public int barTop(double value) {
    if (value >= 0) {
      return VERTICAL_CHART_PADDING + (int)(positiveHeight * (1 - value / maxPositiveValue)) + columnTop();
    }
    else {
      return VERTICAL_CHART_PADDING + positiveHeight + columnTop();
    }
  }

  public int barHeight(double value) {
    return Math.abs(y(value) - y(0));
  }

  public int chartX() {
    return left + scaleZoneWidth;
  }

  public int chartWidth() {
    return chartWidth;
  }

  public int chartHeight() {
    return chartHeight;
  }

  public int labelTop() {
    return top + sectionZoneHeight + columnHeight();
  }

  public int labelY() {
    return top + usablePanelHeight - labelBottomMargin;
  }

  public int labelX(String label, int index) {
    checkIndex(index);
    return left(index) + columnWidth / 2 - textMetrics.stringWidth(label) / 2;
  }

  public double[] scaleValues() {

    int scaleCount = chartHeight / MIN_SCALE_ZONE_HEIGHT;
    if (scaleCount <= 0) {
      return new double[0];
    }

    double span = maxPositiveValue + maxNegativeValue;
    int power = (int)Math.log10(span);

    double selectedScale = 1;
    for (double scale : SCALES) {
      double adjustedScale = scale * Math.pow(10, power);
      if (span / adjustedScale <= scaleCount) {
        selectedScale = adjustedScale;
        break;
      }
    }

    double[] result = new double[scaleCount];
    int index = 0;
    for (double value = 0; (value <= maxPositiveValue) && (index < result.length); value += selectedScale) {
      result[index++] = value;
    }
    for (double value = selectedScale; (value <= maxNegativeValue) && (index < result.length); value += selectedScale) {
      result[index++] = -value;
    }
    if (index < result.length) {
      double[] trimmedResult = new double[index];
      System.arraycopy(result, 0, trimmedResult, 0, index);
      result = trimmedResult;
    }

    return result;
  }

  public int scaleX(String label) {
    return left + scaleZoneWidth - RIGHT_SCALE_MARGIN - textMetrics.stringWidth(label);
  }

  public int scaleY(double value) {
    return y(value) + textMetrics.getAscent() / 2;
  }

  public boolean isDrawingInnerLabels() {
    return config.drawInnerLabels;
  }

  public int innerLabelY() {
    return innerLabelY;
  }

  public int labelTextWidth(String text) {
    return textMetrics.stringWidth(text);
  }

  private void checkIndex(int index) {
    if (index >= columnCount) {
      throw new InvalidParameter("Invalid index " + index + ", chart only contains " + columnCount + " columns");
    }
  }

  public static class Section {
    public final String text;
    public final int textX;
    public final int textY;
    public final int blockX;
    public final int blockWidth;
    public final int blockY;
    public final int blockHeight;
    public final int lineY;
    public final int lineHeight;

    public Section(String text,
                   int textX, int textY,
                   int blockX, int blockWidth,
                   int blockY, int blockHeight,
                   int lineY, int lineHeight) {
      this.text = text;
      this.textX = textX;
      this.textY = textY;
      this.blockX = blockX;
      this.blockWidth = blockWidth;
      this.blockY = blockY;
      this.blockHeight = blockHeight;
      this.lineY = lineY;
      this.lineHeight = lineHeight;
    }

    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("Section");
      builder.append("(text='").append(text).append('\'');
      builder.append(", textX=").append(textX);
      builder.append(", textY=").append(textY);
      builder.append(", blockX=").append(blockX);
      builder.append(", blockWidth=").append(blockWidth);
      builder.append(", blockY=").append(blockY);
      builder.append(", blockHeight=").append(blockHeight);
      builder.append(')');
      return builder.toString();
    }
  }

  public java.util.List<Section> getSections(HistoDataset dataset) {
    if (dataset.isEmpty()) {
      return Collections.emptyList();
    }

    java.util.List<Section> sections = new ArrayList<Section>();
    int blockLeft = left(0);
    String previousName = dataset.getSection(0);
    for (int i = 0; i < dataset.size(); i++) {
      String sectionName = dataset.getSection(i);
      if (!Utils.equal(sectionName, previousName)) {
        int blockRight = left(i);
        sections.add(createSection(previousName, blockLeft, blockRight));
        previousName = sectionName;
        blockLeft = blockRight;
      }
    }

    sections.add(createSection(previousName, blockLeft, usablePanelWidth));

    return sections;
  }

  private Section createSection(String previousName, int blockLeft, int blockRight) {
    int blockWidth = blockRight - blockLeft;
    int blockHeight = SECTION_ZONE_HEIGHT;
    int blockY = top + usablePanelHeight - blockHeight;
    int textX = blockLeft + blockWidth / 2 - textMetrics.stringWidth(previousName) / 2;
    int textY = sectionZoneHeight - SECTION_BOTTOM_MARGIN;
    int lineY = sectionZoneHeight / 2;
    int lineHeight = usablePanelHeight - lineY;
    return new Section(previousName, textX, textY, blockLeft, blockWidth, blockY, blockHeight, lineY, lineHeight);
  }
}
