package org.designup.picsou.gui.components.charts.histo;

import org.globsframework.utils.Utils;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class HistoChartMetrics {

  private static final int VERTICAL_CHART_PADDING = 10;
  private static final int MIN_SCALE_ZONE_WIDTH = 25;
  private static final int MIN_SCALE_ZONE_HEIGHT = 30;
  private static final int RIGHT_SCALE_MARGIN = 10;
  private static final int LABEL_ZONE_HEIGHT = 15;
  private static final int LABEL_BOTTOM_MARGIN = 2;
  private static final int SECTION_ZONE_HEIGHT = 20;
  private static final int SECTION_BOTTOM_MARGIN = 5;

  private final double[] SCALES = {0.25, 0.5, 1, 2, 2.5, 5};

  private int panelWidth;
  private int panelHeight;
  private FontMetrics fontMetrics;
  private int columnCount;
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

  public HistoChartMetrics(int panelWidth,
                           int panelHeight,
                           FontMetrics fontMetrics,
                           int columnCount,
                           double maxNegativeValue,
                           double maxPositiveValue,
                           boolean drawLabels,
                           boolean drawSections,
                           boolean snapToScale) {
    this.panelWidth = panelWidth;
    this.panelHeight = panelHeight;
    this.fontMetrics = fontMetrics;
    this.columnCount = columnCount;
    this.maxPositiveValue = adjustLimit(maxPositiveValue, snapToScale);
    this.maxNegativeValue = adjustLimit(maxNegativeValue, snapToScale);

    this.scaleZoneWidth = drawLabels ? scaleZoneWidth() : 0;
    this.sectionZoneHeight = drawLabels && drawSections ? SECTION_ZONE_HEIGHT : 0;
    this.chartWidth = panelWidth - scaleZoneWidth;

    this.labelZoneHeight = drawLabels ? LABEL_ZONE_HEIGHT : 0;
    this.labelBottomMargin = drawLabels ? LABEL_BOTTOM_MARGIN : 0;

    this.chartHeight = panelHeight - labelZoneHeight - sectionZoneHeight;
    if (maxNegativeValue != 0.0) {
      this.positiveHeight = (int)((chartHeight - 2 * VERTICAL_CHART_PADDING) * this.maxPositiveValue
                                  / (this.maxPositiveValue + this.maxNegativeValue));
      this.negativeHeight = chartHeight - 2 * VERTICAL_CHART_PADDING - positiveHeight;
    }
    else {
      this.positiveHeight = chartHeight - VERTICAL_CHART_PADDING;
      this.negativeHeight = 0;
    }
    this.columnWidth = columnCount != 0 ? chartWidth / columnCount : 0;
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
    int valueWidth = fontMetrics.stringWidth(Integer.toString((int)value));
    return Math.max(MIN_SCALE_ZONE_WIDTH, valueWidth + RIGHT_SCALE_MARGIN);
  }

  public int middleX(int columnIndex) {
    checkIndex(columnIndex);
    return scaleZoneWidth + columnIndex * columnWidth + columnWidth / 2;
  }

  public int left(int columnIndex) {
    checkIndex(columnIndex);
    return scaleZoneWidth + columnIndex * columnWidth;
  }

  public int getColumnAt(int x) {
    if (columnWidth == 0) {
      return -1;
    }
    return (x - scaleZoneWidth) / columnWidth;
  }

  public int right(int columnIndex) {
    checkIndex(columnIndex);
    if (columnIndex == columnCount - 1) {
      return panelWidth;
    }

    return left(columnIndex) + columnWidth;
  }

  public int columnTop() {
    return sectionZoneHeight;
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
      return VERTICAL_CHART_PADDING + (int)(positiveHeight * (1 - value / maxPositiveValue)) + columnTop();
    }
    else {
      return VERTICAL_CHART_PADDING + positiveHeight + (int)(negativeHeight * Math.abs(value) / maxNegativeValue) + columnTop();
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
    return scaleZoneWidth;
  }

  public int chartWidth() {
    return chartWidth;
  }

  public int chartHeight() {
    return chartHeight;
  }

  public int labelTop() {
    return sectionZoneHeight + columnHeight();
  }

  public int labelY() {
    return panelHeight - labelBottomMargin;
  }

  public int labelX(String label, int index) {
    checkIndex(index);
    return left(index) + columnWidth / 2 - fontMetrics.stringWidth(label) / 2;
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
    return scaleZoneWidth - RIGHT_SCALE_MARGIN - fontMetrics.stringWidth(label);
  }

  public int scaleY(double value) {
    return y(value) + fontMetrics.getAscent() / 2;
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

    sections.add(createSection(previousName, blockLeft, panelWidth));

    return sections;
  }

  private Section createSection(String previousName, int blockLeft, int blockRight) {
    int blockWidth = blockRight - blockLeft;
    int blockHeight = SECTION_ZONE_HEIGHT;
    int blockY = panelHeight - blockHeight;
    int textX = blockLeft + blockWidth / 2 - fontMetrics.stringWidth(previousName) / 2;
    int textY = sectionZoneHeight - SECTION_BOTTOM_MARGIN;
    int lineY = sectionZoneHeight / 2;
    int lineHeight = chartHeight - lineY;
    return new Section(previousName, textX, textY, blockLeft, blockWidth, blockY, blockHeight, lineY, lineHeight);
  }
}
