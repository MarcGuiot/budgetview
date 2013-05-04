package com.budgetview.android.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import com.budgetview.shared.gui.histochart.HistoChartConfig;
import com.budgetview.shared.gui.histochart.HistoChartMetrics;
import com.budgetview.shared.gui.histochart.HistoDataset;

public class DailyChartView extends View {

  public static final int HEIGHT = 150;

  private DailyChartStyles styles;
  private DailyChartPainter painter = null;
  private HistoChartMetrics metrics;
  private HistoChartConfig config;

  public DailyChartView(Context context) {
    super(context);
    init();
  }

  public DailyChartView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public DailyChartView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  private void init() {
    setWillNotDraw(false);
    this.config = new HistoChartConfig(false, false, true, true, true, false, false, true, false, true);
    this.styles = new DailyChartStyles(getResources());
  }

  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), HEIGHT);
  }

  public void update(DailyChartPainter painter) {
    this.painter = painter;
    this.metrics = null;
  }

  protected void onDraw(Canvas canvas) {

    int panelWidth = getWidth() - 1;
    int panelHeight = getHeight() - 1;

    canvas.drawRect(0, 0, panelWidth, panelHeight, styles.getBackgroundPaint());

    HistoDataset dataset = painter.getDataset();
    if (dataset == null) {
      Paint errorPaint = new Paint();
      errorPaint.setColor(Color.RED);
      errorPaint.setStyle(Paint.Style.FILL);
      canvas.drawRect(0, 0, panelWidth, panelHeight, errorPaint);
      return;
    }

    if (metrics == null) {
      metrics = new HistoChartMetrics(panelWidth, panelHeight,
                                      styles.getTypefaceMetrics(),
                                      dataset.size(),
                                      dataset.getMaxNegativeValue(), dataset.getMaxPositiveValue(),
                                      config,
                                      dataset.containsSections(),
                                      true);
    }

    paintBg(canvas);
    paintLabels(canvas, dataset);
    paintScale(canvas, panelWidth);
    paintSelectionBorder(canvas, dataset);
    paintBorder(canvas);

    painter.paint(canvas, metrics);

    paintSections(canvas, dataset);

  }

  public int getX(int columnIndex) {
    if (metrics == null) {
      return -1;
    }
    return (metrics.left(columnIndex) + metrics.right(columnIndex)) / 2;
  }

  private void paintBg(Canvas canvas) {
    canvas.drawRect(metrics.chartX(), metrics.columnTop(), metrics.chartX() + metrics.chartWidth(), metrics.columnTop() + metrics.chartHeight(), styles.getChartBgPaint());
  }

  private void paintBorder(Canvas canvas) {
    canvas.drawRect(metrics.chartX(), metrics.columnTop(), metrics.chartX() + metrics.chartWidth(), metrics.columnTop()+ metrics.chartHeight(), styles.getChartBorderPaint());
  }

  private void paintLabels(Canvas canvas, HistoDataset dataset) {
    for (int i = 0; i < dataset.size(); i++) {

      int columnHeight = metrics.columnHeight();
      int left = metrics.left(i);
      int right = metrics.right(i);

      canvas.drawRect(left, metrics.columnTop(), left + metrics.columnWidth(), metrics.columnTop() + columnHeight, styles.getChartBgPaint());
      canvas.drawRect(left, metrics.labelTop(), left + metrics.columnWidth(), metrics.labelTop() + metrics.labelZoneHeightWithMargin(), styles.getBackgroundPaint());

      if (config.drawColumnDividers) {
        canvas.drawLine(right, metrics.columnTop(), right, metrics.columnTop() + columnHeight, styles.getColumnDividerPaint());
      }

      if (config.drawLabels) {
        String label = dataset.getLabel(i);
        canvas.drawText(label, metrics.labelX(label, i), metrics.labelY(), styles.getLabelPaint());
      }
    }
  }

  private void paintSelectionBorder(Canvas canvas, HistoDataset dataset) {
    for (int i = 0; i < dataset.size(); i++) {
      if (dataset.isSelected(i)) {

        int columnHeight = metrics.columnHeight();
        int left = metrics.left(i);

        canvas.drawRect(left, metrics.columnTop(), metrics.columnWidth(), columnHeight + metrics.labelZoneHeightWithMargin(), styles.getSelectedColumnBorderPaint());
      }
    }
  }

  private void paintSections(Canvas canvas, HistoDataset dataset) {
    if (!config.drawLabels) {
      return;
    }
    boolean firstBlock = true;
    for (HistoChartMetrics.Section section : metrics.getSections(dataset)) {
      if (!firstBlock) {
        canvas.drawLine(section.blockX, section.lineY, section.blockX, section.lineHeight, styles.getSectionLinePaint())
        ;
      }

      canvas.drawText(section.text, section.textX, section.textY, styles.getLabelPaint());

      firstBlock = false;
    }
  }

  private void paintScale(Canvas canvas, int panelWidth) {
    if (!config.drawScale) {
      return;
    }
    double[] scaleValues = metrics.scaleValues();
    for (double scaleValue : scaleValues) {
      Paint paint = null;
      if (scaleValue == 0) {
        paint = styles.getScaleOriginLinePaint();
      }
      else {
        paint = styles.getScaleLinePaint();
      }
      canvas.drawLine(metrics.chartX(), metrics.y(scaleValue), panelWidth, metrics.y(scaleValue), paint);

      String label = Integer.toString((int)scaleValue);
      canvas.drawText(label, metrics.scaleX(label), metrics.scaleY(scaleValue), styles.getLabelPaint());
    }
  }
}
