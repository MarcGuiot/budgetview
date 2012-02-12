package com.budgetview.analytics.gui;

import com.budgetview.analytics.AnalyticsApp;
import com.budgetview.analytics.model.Experiment;
import com.budgetview.analytics.model.User;
import com.budgetview.analytics.model.WeekPerfStat;
import com.budgetview.analytics.utils.Weeks;
import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoChartConfig;
import org.designup.picsou.gui.components.charts.histo.line.HistoBarPainter;
import org.designup.picsou.gui.components.charts.histo.line.HistoLineColors;
import org.designup.picsou.gui.components.charts.histo.line.HistoLineDataset;
import org.designup.picsou.gui.utils.Gui;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.SplitsEditor;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

import static org.globsframework.gui.views.utils.LabelCustomizers.ALIGN_CENTER;
import static org.globsframework.model.utils.GlobComparators.descending;

public class AnalyticsWindow {
  private static final List<Field> PERF_CHART_FIELDS =
    Arrays.asList(WeekPerfStat.NEW_USERS,
                  WeekPerfStat.RETENTION_RATIO,
                  WeekPerfStat.EVALUATIONS_RESULT,
                  WeekPerfStat.REVENUE_RATIO,
                  WeekPerfStat.PURCHASES);
  private GlobRepository repository;
  private Directory directory;
  private JFrame frame;
  private HistoLineColors chartColors;
  private SelectionService selectionService;

  public AnalyticsWindow(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    this.selectionService = directory.get(SelectionService.class);

    this.chartColors = new HistoLineColors(
      "histo.line.positive",
      "histo.line.negative",
      "histo.fill.positive",
      "histo.fill.negative",
      directory
    );

    this.frame = new JFrame();
    JPanel panel = createPanel();
    this.frame.setContentPane(panel);
    this.frame.pack();
  }

  public void show() {
    SplitsEditor.show(frame, directory);
    GuiUtils.showFullSize(frame);
  }

  private JPanel createPanel() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/analyticsWindow.splits",
                                                      repository, directory);

    addExperimentElements(builder);
    addPerfElements(builder);
    addUserElements(builder);

    setupSelectionListeners();

    return builder.load();
  }

  private void setupSelectionListeners() {

    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobList experiments = selection.getAll(Experiment.TYPE);
        if (experiments.isEmpty()) {
          return;
        }
        int weekId = experiments.getFirst().get(Experiment.WEEK);
        Glob stat = repository.find(Key.create(WeekPerfStat.TYPE, weekId));
        if (stat != null) {
          selectionService.select(stat);
        }

        GlobList users =
          repository.getAll(User.TYPE,
                            GlobMatchers.fieldAfter(User.FIRST_DATE, Weeks.getFirstDay(weekId)));
        selectionService.select(users, User.TYPE);
      }
    }, Experiment.TYPE);
  }

  private void addExperimentElements(GlobsPanelBuilder builder) {
    GlobTableView table =
      builder.addTable("experiments",
                       Experiment.TYPE,
                       descending(Experiment.WEEK))
        .addColumn(Experiment.WEEK, ALIGN_CENTER)
        .addColumn(Experiment.ACTION);
    Gui.setColumnSizes(table.getComponent(), new int[]{10});
  }

  private void addPerfElements(GlobsPanelBuilder builder) {
    builder.addRepeat("charts", PERF_CHART_FIELDS, new FieldRepeatComponentFactory());
  }

  private class FieldRepeatComponentFactory implements RepeatComponentFactory<Field> {
    public void registerComponents(RepeatCellBuilder cellBuilder, Field field) {
      cellBuilder.add("chartTitle", new JLabel(field.getName()));
      cellBuilder.add("chart", createPerfChart(field));
    }

    private HistoChart createPerfChart(final Field field) {
      HistoChartConfig chartConfig =
        new HistoChartConfig(true, field == PERF_CHART_FIELDS.get(0),
                             false, true, true, true, false, false, false);
      final HistoChart chart = new HistoChart(chartConfig, directory);

      selectionService.addListener(new GlobSelectionListener() {
        public void selectionUpdated(GlobSelection selection) {
          updateChart(field, chart);
        }
      }, WeekPerfStat.TYPE);

      updateChart(field, chart);

      return chart;
    }

    private void updateChart(Field field, HistoChart chart) {

      GlobList selection = selectionService.getSelection(WeekPerfStat.TYPE);

      HistoLineDataset dataset = new HistoLineDataset("histo.tooltip");
      for (Glob stat : getWeekStat().sort(new GlobFieldComparator(WeekPerfStat.ID))) {
        Integer weekId = stat.get(WeekPerfStat.ID);
        dataset.add(weekId,
                    getValue(stat, field),
                    Integer.toString(weekId % 100),
                    Integer.toString(weekId),
                    Integer.toString(weekId / 100),
                    true,
                    false,
                    selection.contains(stat));
      }
      chart.update(new HistoBarPainter(dataset, chartColors));
    }

    private GlobList getWeekStat() {
      return repository.getAll(WeekPerfStat.TYPE,
                                         GlobMatchers.fieldGreaterOrEqual(WeekPerfStat.ID, AnalyticsApp.MIN_WEEK));
    }
  }

  private Double getValue(Glob stat, Field field) {
    if (field instanceof DoubleField) {
      return (Double)stat.getValue(field);
    }
    else if (field instanceof IntegerField) {
      Integer value = (Integer)stat.getValue(field);
      if (value == null) {
        return null;
      }
      return value.doubleValue();
    }
    throw new InvalidParameter("Unexpected field type: " + field.getFullName());
  }

  private void addUserElements(GlobsPanelBuilder builder) {
    builder.addTable("users", User.TYPE, descending(User.FIRST_DATE))
      .setFilter(GlobMatchers.isNotNull(User.EMAIL))
      .addColumn(User.EMAIL)
      .addColumn(User.FIRST_DATE)
      .addColumn(User.LAST_DATE)
      .addColumn(User.PING_COUNT)
      .addColumn(User.DAYS_BEFORE_PURCHASE)
      .addColumn(User.PURCHASE_DATE);
  }
}
