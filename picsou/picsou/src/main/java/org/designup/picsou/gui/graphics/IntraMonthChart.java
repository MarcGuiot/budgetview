package org.designup.picsou.gui.graphics;

import org.crossbowlabs.globs.gui.GlobSelection;
import org.crossbowlabs.globs.model.ChangeSet;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.model.utils.GlobMatcher;
import static org.crossbowlabs.globs.model.utils.GlobMatchers.and;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.SplitsBuilder;
import org.crossbowlabs.splits.color.ColorChangeListener;
import org.crossbowlabs.splits.color.ColorSource;
import org.designup.picsou.gui.model.MonthStat;
import org.designup.picsou.gui.transactions.TransactionSelection;
import org.designup.picsou.gui.utils.PicsouColors;
import static org.designup.picsou.gui.utils.PicsouMatchers.*;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.util.ShapeUtilities;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class IntraMonthChart extends AbstractLineChart {

  public static final String CURRENT_ROW = "Current Month";
  public static final String PREVIOUS_ROW = "Previous Month";

  private TransactionSelection transactionSelection;

  public IntraMonthChart(GlobRepository repository, Directory directory, TransactionSelection transactionSelection) {
    super(repository, directory);
    this.transactionSelection = transactionSelection;
    transactionSelection.addListener(this);
  }

  public void registerComponents(SplitsBuilder builder) {
    builder.add("intraMonthChart", getPanel());
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository globRepository) {
    if (changeSet.containsChanges(MonthStat.TYPE)) {
      updateChart();
    }
  }

  public void selectionUpdated(GlobSelection selection) {
    updateChart();
  }

  protected void updateChart() {

    dataset.removeAllSeries();

    Set<Integer> selectedMonths = transactionSelection.getCurrentMonths();
    if (selectedMonths.size() != 1) {
      return;
    }

    int currentMonth = selectedMonths.iterator().next();
    Integer previousMonth = Month.previous(currentMonth);

    boolean hasData = false;

    double[] currentValues = new double[31];
    double[] previousValues = new double[31];
    Arrays.fill(currentValues, 0);
    Arrays.fill(previousValues, 0);

    GlobMatcher matcher = getMatcher(previousMonth);

    int maxCurrentIndex = 0;
    int maxPreviousIndex = 0;

    for (Glob transaction : repository.getAll(Transaction.TYPE, matcher)) {
      Integer month = transaction.get(Transaction.MONTH);
      Integer day = transaction.get(Transaction.DAY);
      if (month.equals(currentMonth)) {
        Double amount = transaction.get(Transaction.AMOUNT);
        if (amount < 0) {
          currentValues[day - 1] += Math.abs(amount);
          hasData = true;
          maxCurrentIndex = Math.max(maxCurrentIndex, day - 1);
        }
      }
      else if (month.equals(previousMonth)) {
        Double amount = transaction.get(Transaction.AMOUNT);
        if (amount < 0) {
          previousValues[day - 1] += Math.abs(amount);
          hasData = true;
          maxPreviousIndex = Math.max(maxPreviousIndex, day - 1);
        }
      }
    }

    if (!hasData) {
      return;
    }

    computeCumulatedValues(currentValues, maxCurrentIndex);
    computeCumulatedValues(previousValues, maxPreviousIndex);

    XYSeries currentSeries = new XYSeries(IntraMonthChart.CURRENT_ROW);
    XYSeries previousSeries = new XYSeries(IntraMonthChart.PREVIOUS_ROW);
    for (int i = 0; i < 31; i++) {
      if (i < maxCurrentIndex) {
        currentSeries.add(i + 1, currentValues[i]);
      }
      if (i < maxPreviousIndex) {
        previousSeries.add(i + 1, previousValues[i]);
      }
    }

    dataset.addSeries(currentSeries);
    dataset.addSeries(previousSeries);

    configureSeries();
  }

  private void computeCumulatedValues(double[] values, int maxIndex) {
    double sum = 0;
    for (int i = 0; (i < 31) && (i < maxIndex); i++) {
      sum += values[i];
      values[i] = sum;
    }
  }

  private GlobMatcher getMatcher(Integer previousMonth) {
    Set<Integer> currentAccounts = transactionSelection.getCurrentAccounts();
    Set<Integer> currentCategories = new HashSet<Integer>(transactionSelection.getCurrentCategories());
    for (Iterator iter = currentCategories.iterator(); iter.hasNext();) {
      if (repository.find(Key.create(Category.TYPE, iter.next())) == null) {
        iter.remove();
      }
    }

    Set<Integer> currentMonths = new HashSet<Integer>(transactionSelection.getCurrentMonths());
    if (previousMonth != null) {
      currentMonths.add(previousMonth);
    }

    return and(transactionsForMonths(currentMonths),
               transactionsForCategories(currentCategories, repository),
               transactionsForAccounts(currentAccounts));
  }

  protected void configurePanel(ChartPanel panel) {
    panel.setMinimumSize(new Dimension(400, 140));
  }

  protected void configureChart() {
    colorService.addListener(new ColorChangeListener() {
      public void colorsChanged(ColorSource colorSource) {
        plot.setBackgroundPaint(new GradientPaint(100.0f, 0.0f, colorSource.get(PicsouColors.CHART_BG_TOP),
                                                  100.0f, 200.0f, colorSource.get(PicsouColors.CHART_BG_BOTTOM)));
      }
    });

    NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
    domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    domainAxis.setAutoRangeIncludesZero(false);

    NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
    rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    rangeAxis.setAutoRangeIncludesZero(true);
  }

  private void configureSeries() {
    XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
    renderer.setSeriesShapesVisible(0, true);
    renderer.setSeriesShapesVisible(1, false);

    renderer.setSeriesLinesVisible(0, true);
    renderer.setSeriesLinesVisible(1, true);
    renderer.setSeriesStroke(0, new BasicStroke(1.5f));
    renderer.setSeriesStroke(1, new BasicStroke(
      1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
      1.0f, new float[]{6.0f, 6.0f}, 0.0f
    ));

    setRowColor(CURRENT_ROW, PicsouColors.INTRAMONTH_CURRENT_LINE, renderer, dataset);
    setRowColor(PREVIOUS_ROW, PicsouColors.INTRAMONTH_PREVIOUS_LINE, renderer, dataset);

    setShapeColor(CURRENT_ROW, PicsouColors.CHART_EXPENSES_SHAPE, renderer, dataset);
    renderer.setSeriesShape(0, ShapeUtilities.createDiamond(2.0f));

    renderer.setDrawOutlines(true);
    renderer.setUseFillPaint(true);
  }
}
