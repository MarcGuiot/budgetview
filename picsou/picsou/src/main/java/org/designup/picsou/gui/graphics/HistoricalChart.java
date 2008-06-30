package org.designup.picsou.gui.graphics;

import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.designup.picsou.gui.model.MonthStat;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.Layer;

import java.awt.*;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class HistoricalChart extends AbstractLineChart {

  private java.util.List<Integer> months = new ArrayList<Integer>();
  private GlobList selectedCategories = GlobList.EMPTY;

  private Color markerOutlineColor;
  private Color markerColor;

  public static final String INCOME_ROW = "Revenus";
  public static final String EXPENSES_ROW = "Depenses";
  private Integer[] selectedMonths = {};

  public HistoricalChart(GlobRepository repository, Directory directory) {
    super(repository, directory);
    selectionService.addListener(this, Category.TYPE, Month.TYPE);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("historicalChart", getPanel());
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository globRepository) {
    if (changeSet.containsChanges(MonthStat.TYPE)) {
      updateChart();
    }
  }

  public void selectionUpdated(GlobSelection selection) {
    if (selection.isRelevantForType(Category.TYPE)) {
      selectedCategories = selection.getAll(Category.TYPE);
      updateChart();
    }
    if (selection.isRelevantForType(Month.TYPE)) {
      selectedMonths = selection.getAll(Month.TYPE).getSortedArray(Month.ID);
      updateMonthMarkers();
    }
  }

  public void colorsChanged(ColorLocator colorLocator) {
    markerOutlineColor = colorLocator.get(PicsouColors.CHART_MARKER_OUTLINE);
    markerColor = colorLocator.get(PicsouColors.CHART_MARKER);
  }

  protected void updateChart() {
    updateDataset();
    updateMonthMarkers();
  }

  private void updateMonthMarkers() {
    plot.clearDomainMarkers();
    if (selectedMonths.length == 0) {
      return;
    }
    java.util.List<Integer> indexes = getSortedIndexes(selectedMonths);
    Iterator<Integer> iter = indexes.iterator();
    int start = iter.next();
    int last = start;
    while (iter.hasNext()) {
      int current = iter.next();
      if (current - last == 1) {
        last = current;
      }
      else {
        addMarker(start - 0.5, last + 0.5);
        start = current;
        last = current;
      }
    }
    addMarker(start - 0.5, last + 0.5);
  }

  private void addMarker(double start, double end) {
    IntervalMarker marker = new IntervalMarker(start, end);
    marker.setOutlinePaint(markerOutlineColor);
    marker.setPaint(markerColor);
    marker.setAlpha(0.2f);
    plot.addDomainMarker(marker, Layer.BACKGROUND);
  }

  protected void configurePanel(ChartPanel panel) {
    panel.setMinimumSize(new Dimension(400, 180));
  }

  protected void configureChart() {
    colorService.addListener(new ColorChangeListener() {
      public void colorsChanged(ColorLocator colorLocator) {
        plot.setBackgroundPaint(new GradientPaint(100.0f, 0.0f, colorLocator.get(PicsouColors.CHART_BG_TOP),
                                                  100.0f, 200.0f, colorLocator.get(PicsouColors.CHART_BG_BOTTOM)));

        updateMonthMarkers();
      }
    });

    Color labelColor = colorService.get(PicsouColors.CHART_LABEL);

    NumberAxis domainAxis = (NumberAxis)plot.getDomainAxis();
    domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    domainAxis.setNumberFormatOverride(new MonthFormat());
    domainAxis.setTickLabelPaint(labelColor);
    domainAxis.setTickMarkPaint(labelColor);
    domainAxis.setTickLabelFont(fontLocator.get("chart.historical.label"));

    NumberAxis rangeAxis = (NumberAxis)plot.getRangeAxis();
    rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    rangeAxis.setAutoRangeIncludesZero(false);
    rangeAxis.setTickLabelPaint(labelColor);
    rangeAxis.setTickMarkPaint(labelColor);
    rangeAxis.setTickLabelFont(fontLocator.get("chart.historical.label"));
  }

  private void configureSeries() {
    AbstractXYItemRenderer renderer = (AbstractXYItemRenderer)plot.getRenderer();
    renderer.setSeriesStroke(0, new BasicStroke(1.0f));
    renderer.setSeriesStroke(2, new BasicStroke(1.0f));

    renderer.setSeriesToolTipGenerator(0, new ToolTipGenerator(Lang.get("expenses")));
    renderer.setSeriesToolTipGenerator(2, new ToolTipGenerator(Lang.get("income")));

    setRowColor(INCOME_ROW,
                PicsouColors.CHART_INCOME_BAR_TOP,
                PicsouColors.CHART_INCOME_BAR_BOTTOM,
                renderer, dataset);
    setRowColor(EXPENSES_ROW,
                PicsouColors.CHART_EXPENSES_BAR_TOP,
                PicsouColors.CHART_EXPENSES_BAR_BOTTOM,
                renderer, dataset);
  }

  private int getMonthIndex(Integer yyyymm) {
    int index = months.indexOf(yyyymm);
    if (index < 0) {
      return -1;
    }
    return index + 1;
  }

  private java.util.List<Integer> getSortedIndexes(Integer[] yyyymms) {
    java.util.List<Integer> indexes = new ArrayList<Integer>();
    for (Integer month : yyyymms) {
      indexes.add(getMonthIndex(month));
    }
    Collections.sort(indexes);
    return indexes;
  }

  private int getYyyymmMonth(double number) {
    int index = (int)number - 1;
    if (index < 0) {
      return -1;
    }
    return months.get(index);
  }

  private void updateDataset() {

    dataset.removeAllSeries();

    XYSeries expenseSeries = new XYSeries(EXPENSES_ROW);
    XYSeries incomeSeries = new XYSeries(INCOME_ROW);

    months.clear();
    boolean hasExpenses = false;
    boolean hasIncome = false;
    int index = 1;
    for (Glob month : repository.getAll(Month.TYPE).sort(Month.ID)) {
      months.add(month.get(Month.ID));
      double expenses = 0;
      double income = 0;
      for (Glob category : selectedCategories) {
        if (!category.exists()) {
          continue;
        }
        Key key = MonthStat.getKey(month.get(Month.ID), category.get(Category.ID), Account.SUMMARY_ACCOUNT_ID);
        Glob stat = repository.get(key);
        expenses += stat.get(MonthStat.EXPENSES);
        income += stat.get(MonthStat.INCOME);
      }
      hasExpenses |= expenses > 0;
      expenseSeries.add(index, expenses);
      hasIncome |= income > 0;
      incomeSeries.add(index, income);
      index++;
    }

    if (hasExpenses) {
      dataset.addSeries(expenseSeries);
    }
    if (hasIncome) {
      dataset.addSeries(incomeSeries);
    }

    configureSeries();
  }

  private class MonthFormat extends NumberFormat {
    public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
      if ((number <= 1) || (number > months.size())) {
        return toAppendTo;
      }

      int month = getYyyymmMonth(number);
      toAppendTo.append(Lang.get("month." + Month.toMonth(month) + ".medium"));
      if (Month.toMonth(month) == 1) {
        toAppendTo.append('/');
        toAppendTo.append(Month.toYearString(month));
      }
      return toAppendTo;
    }

    public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
      return toAppendTo;
    }

    public Number parse(String source, ParsePosition parsePosition) {
      return null;
    }
  }

  private class ToolTipGenerator implements XYToolTipGenerator {
    private String prefix;

    public ToolTipGenerator(String prefix) {
      this.prefix = prefix;
    }

    public String generateToolTip(XYDataset dataset, int series, int item) {
      double value = dataset.getYValue(series, item);
      StringBuilder builder = new StringBuilder();
      return builder.append(prefix)
        .append(": ")
        .append(PicsouDescriptionService.DECIMAL_FORMAT.format(value))
        .append(Gui.EURO)
        .toString();
    }
  }
}
