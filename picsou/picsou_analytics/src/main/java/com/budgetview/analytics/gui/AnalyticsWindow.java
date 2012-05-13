package com.budgetview.analytics.gui;

import com.budgetview.analytics.model.Experiment;
import com.budgetview.analytics.model.User;
import com.budgetview.analytics.model.WeekPerfStat;
import com.budgetview.analytics.model.WeekUsageStat;
import com.budgetview.analytics.utils.Weeks;
import org.designup.picsou.gui.utils.Gui;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.SplitsEditor;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

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
                  WeekPerfStat.POTENTIAL_BUYERS,
                  WeekPerfStat.REVENUE_RATIO,
                  WeekPerfStat.PURCHASES);

  private static final List<Field> USER_PROGRESS_FIELDS =
    Arrays.asList((Field)WeekUsageStat.COMPLETION_RATE_ON_FIRST_TRY,
                  WeekUsageStat.LOSS_BEFORE_FIRST_IMPORT,
                  WeekUsageStat.LOSS_DURING_FIRST_IMPORT,
                  WeekUsageStat.LOSS_DURING_FIRST_CATEGORIZATION,
                  WeekUsageStat.LOSS_AFTER_FIRST_CATEGORIZATION);

  private GlobRepository repository;
  private Directory directory;
  private JFrame frame;
  private SelectionService selectionService;

  public AnalyticsWindow(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    this.selectionService = directory.get(SelectionService.class);

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
    addUserProgressElements(builder);

    setupSelectionListeners();

    return builder.load();
  }

  private void setupSelectionListeners() {

    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobList experiments = selection.getAll(Experiment.TYPE);
        if (experiments.isEmpty()) {
          selectionService.clear(WeekPerfStat.TYPE);
          selectionService.clear(WeekUsageStat.TYPE);
          selectionService.clear(User.TYPE);
          return;
        }

        int weekId = experiments.getFirst().get(Experiment.WEEK);
        selectStat(weekId, WeekPerfStat.TYPE);
        selectStat(weekId, WeekUsageStat.TYPE);

        GlobList users =
          repository.getAll(User.TYPE,
                            GlobMatchers.fieldAfter(User.FIRST_DATE, Weeks.getFirstDay(weekId)));
        selectionService.select(users, User.TYPE);
      }

      private void selectStat(int weekId, GlobType type) {
        Glob stat = repository.find(Key.create(type, weekId));
        if (stat != null) {
          selectionService.select(stat);
        }
        else {
          selectionService.clear(type);
        }
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
    builder.addRepeat("performanceCharts", PERF_CHART_FIELDS,
                      new FieldRepeatComponentFactory(WeekPerfStat.ID, PERF_CHART_FIELDS,
                                                      repository, directory));
  }

  private void addUserProgressElements(GlobsPanelBuilder builder) {
    builder.addRepeat("userProgressCharts", USER_PROGRESS_FIELDS,
                      new FieldRepeatComponentFactory(WeekUsageStat.ID, USER_PROGRESS_FIELDS,
                                                      repository, directory));
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
