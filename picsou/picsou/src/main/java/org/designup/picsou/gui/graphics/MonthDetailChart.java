package org.designup.picsou.gui.graphics;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.model.MonthStat;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorUpdater;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobFieldMatcher;
import org.globsframework.utils.directory.Directory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;

import java.awt.*;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class MonthDetailChart extends View implements GlobSelectionListener, ChangeSetListener {
  private static int step = 6;
  private static int interval = 31 / step + 1;
  private ChartPanel panel;
  private XYPlot plot;
  private JFreeChart chart;
  private Set<Integer> selectedCategories = Collections.emptySet();
  private Set<Integer> selectedMonths = Collections.emptySet();

  private Color markerOutlineColor;
  private Color markerColor;

  public static final String INCOME_ROW = "Revenus";
  public static final String AVERAGE_INCOME_ROW = "Revenus moyens";
  public static final String EXPENSES_ROW = "Depenses";
  public static final String AVERAGE_EXPENSES_ROW = "Depenses moyennes";

  public MonthDetailChart(GlobRepository repository, Directory directory) {
    super(repository, directory);
    repository.addChangeListener(this);
    selectionService.addListener(this, Category.TYPE, Month.TYPE);
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository globRepository) {
    if (changeSet.containsChanges(MonthStat.TYPE)) {
      updateChart();
      return;
    }
  }

  public void globsReset(GlobRepository globRepository, java.util.List<GlobType> changedTypes) {
    updateChart();
  }

  public void selectionUpdated(GlobSelection selection) {
    boolean update = false;
    if (selection.isRelevantForType(Category.TYPE)) {
      selectedCategories = selection.getAll(Category.TYPE).getValueSet(Category.ID);
      update = true;
    }
    if (selection.isRelevantForType(Month.TYPE)) {
      selectedMonths = selection.getAll(Month.TYPE).getSortedSet(Month.ID);
      update = true;
    }
    if (update) {
      updateChart();
    }
  }

  public void colorsChanged(ColorLocator colorLocator) {
    markerOutlineColor = colorLocator.get(PicsouColors.CHART_MARKER_OUTLINE);
    markerColor = colorLocator.get(PicsouColors.CHART_MARKER);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("monthDetailChart", getPanel());
  }

  private ChartPanel getPanel() {
    if (panel == null) {
      panel = createPanel();
    }
    return panel;
  }

  public ChartPanel createPanel() {
    ChartPanel panel = new ChartPanel(createChart());
    panel.setFont(Gui.getDefaultFont());
    panel.setDomainZoomable(false);
    panel.setDisplayToolTips(true);
    panel.setInitialDelay(100);
    panel.setFillZoomRectangle(false);
    panel.setMouseZoomable(false);
    panel.setMinimumSize(new Dimension(100, 180));
    panel.setPopupMenu(null);
    return panel;
  }

  private void updateChart() {
    getPanel().setChart(createChart());
  }

  private JFreeChart createChart() {
    return createHistoricalChart(createDataset());
  }

  private JFreeChart createHistoricalChart(DefaultCategoryDataset dataset) {
    chart = ChartFactory.createBarChart(
      null,  // chart title
      null,  // xAxisLabel
      null,  // yAxisLabel
      dataset,
      PlotOrientation.VERTICAL,
      false, // include legend
      true,  // tooltips
      false  // urls
    );

//    chart.setBackgroundPaint(Color.white);

//    plot = (XYPlot)chart.getPlot();
//    plot.setBackgroundPaint(Color.white);
//    plot.setRangeGridlinePaint(Color.lightGray);

//    plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

//    plot.setDomainCrosshairVisible(false);
//    plot.setRangeCrosshairVisible(false);

//    colorService.addListener(new ColorService.Listener() {
//      public void colorsChanged(ColorSource colorLocator) {
//        plot.setBackgroundPaint(new GradientPaint(100.0f, 0.0f, colorLocator.get(PicsouColors.CHART_BG_TOP),
//                                                  100.0f, 200.0f, colorLocator.get(PicsouColors.CHART_BG_BOTTOM)));
//      }
//    });

//    NumberAxis domainAxis = (NumberAxis)plot.getDomainAxis();
//    domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
//    domainAxis.setNumberFormatOverride(new MonthDetailChart.MonthFormat());

//    NumberAxis rangeAxis = (NumberAxis)plot.getRangeAxis();
//    rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

//    XYBarRenderer renderer = (XYBarRenderer)plot.getRenderer();
//    renderer.setSeriesShapesVisible(0, true);
//    renderer.setSeriesShapesVisible(1, false);
//    renderer.setSeriesShapesVisible(2, true);
//    renderer.setSeriesShapesVisible(3, false);
//
//    renderer.setSeriesLinesVisible(0, true);
//    renderer.setSeriesLinesVisible(2, true);
//    renderer.setSeriesStroke(0, new BasicStroke(1.0f));
//    renderer.setSeriesStroke(2, new BasicStroke(1.0f));
//    renderer.setSeriesStroke(1, new BasicStroke(2.0f));
//    renderer.setSeriesStroke(3, new BasicStroke(2.0f));

//    renderer.setSeriesToolTipGenerator(0, new MonthDetailChart.ToolTipGenerator(Lang.get("expenses")));
//    renderer.setSeriesToolTipGenerator(1, new MonthDetailChart.ToolTipGenerator(Lang.get("average.expenses")));
//    renderer.setSeriesToolTipGenerator(2, new MonthDetailChart.ToolTipGenerator(Lang.get("income")));
//    renderer.setSeriesToolTipGenerator(3, new MonthDetailChart.ToolTipGenerator(Lang.get("average.income")));

//    setRowColor(INCOME_ROW, PicsouColors.CHART_INCOME_LINE, renderer, dataset);
//    setRowColor(AVERAGE_INCOME_ROW, PicsouColors.CHART_INCOME_AVERAGE_LINE, renderer, dataset);
//    setRowColor(EXPENSES_ROW, PicsouColors.CHART_EXPENSES_LINE, renderer, dataset);
//    setRowColor(AVERAGE_EXPENSES_ROW, PicsouColors.CHART_EXPENSES_AVERAGE_LINE, renderer, dataset);
//
//    setShapeColor(INCOME_ROW, PicsouColors.CHART_INCOME_SHAPE, renderer, dataset);
//    setShapeColor(EXPENSES_ROW, PicsouColors.CHART_EXPENSES_SHAPE, renderer, dataset);
//
//    renderer.setSeriesShape(2, ShapeUtilities.createDiamond(4.0f));
//    renderer.setSeriesShape(3, ShapeUtilities.createDiamond(4.0f));
//
//    renderer.setDrawOutlines(true);
//    renderer.setUseFillPaint(true);

    return chart;
  }

  private void setRowColor(String row, PicsouColors color, final XYBarRenderer renderer, XYDataset dataset) {
    final int rowIndex = dataset.indexOf(row);
    if (rowIndex >= 0) {
      colorService.install(color.toString(), new ColorUpdater() {
        public void updateColor(Color color) {
          renderer.setSeriesPaint(rowIndex, color);
        }
      });
    }
  }

  private void setShapeColor(String row, PicsouColors color, final XYBarRenderer renderer, XYDataset dataset) {
    final int rowIndex = dataset.indexOf(row);
    if (rowIndex >= 0) {
      colorService.install(color.toString(), new ColorUpdater() {
        public void updateColor(Color color) {
          renderer.setSeriesFillPaint(rowIndex, color);
        }
      });
    }
  }

  private DefaultCategoryDataset createDataset() {

    DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset();
    boolean hasExpenses = false;
    boolean hasIncome = false;
    double[] expenses = new double[interval];
    double[] incommes = new double[interval];
    for (Integer selectedMonth : selectedMonths) {
      updateExpenseFor(selectedMonth, expenses, incommes);
    }
    for (int i = 0; i < expenses.length; i++) {
      categoryDataset.addValue(((Number)new Double(-expenses[i] / selectedMonths.size())), "expense", i);
    }
    for (int i = 0; i < incommes.length; i++) {
      expenses[i] = 0;
    }

    Set<Integer> comparesMonth = getMonthToCompareTo();
    for (Integer selectedMonth : comparesMonth) {
      updateExpenseFor(selectedMonth, expenses, incommes);
    }
    for (int i = 0; i < expenses.length; i++) {
      double expense = expenses[i];
      categoryDataset.addValue((Number)new Double(-expense / selectedMonths.size()), "moyenne", i);
    }

    return categoryDataset;
  }

  private Set<Integer> getMonthToCompareTo() {
    Set<Integer> monthForAverage = new TreeSet<Integer>();
    if (selectedMonths.size() >= 1) {
      Integer lastMonth = selectedMonths.iterator().next();
      addIfExist(lastMonth - 1, monthForAverage);
      addIfExist(lastMonth - 2, monthForAverage);
    }
    return monthForAverage;
  }

  private void addIfExist(int month, Set<Integer> monthForAverage) {
    if (repository.find(Key.create(Month.TYPE, month)) != null) {
      monthForAverage.add(month);
    }
  }

  private void updateExpenseFor(final int monthId, double[] expense, double[] income) {
    for (Glob transaction : repository.getAll(Transaction.TYPE, new GlobFieldMatcher(Transaction.MONTH, monthId))) {
      Integer day = transaction.get(Transaction.DAY);
      if (selectedCategories.contains(Category.ALL) || selectedCategories.contains(transaction.get(Transaction.CATEGORY))) {
        int index = day / step;
        Double value = transaction.get(Transaction.AMOUNT);
        if (value < 0) {
          expense[index] += value;
        }
        else {
          income[index] += value;
        }
      }
    }
  }
}
