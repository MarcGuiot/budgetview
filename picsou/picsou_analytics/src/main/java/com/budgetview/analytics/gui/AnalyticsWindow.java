package com.budgetview.analytics.gui;

import com.budgetview.analytics.model.Experiment;
import com.budgetview.analytics.model.OnboardingStats;
import com.budgetview.analytics.model.User;
import com.budgetview.analytics.model.WeekStats;
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
import static org.globsframework.model.utils.GlobComparators.ascending;
import static org.globsframework.model.utils.GlobComparators.descending;

public class AnalyticsWindow {

  private static final List<Field> COHORT_CHART_FIELDS =
    Arrays.asList(WeekStats.NEW_USERS,
                  WeekStats.ACTIVATION_RATIO,
                  WeekStats.RETENTION_RATIO,
                  WeekStats.REVENUE_RATIO);

  private static final List<Field> VOLUME_CHART_FIELDS =
    Arrays.asList((Field) WeekStats.NEW_USERS,
                  WeekStats.TOTAL_ACTIVE_USERS,
                  WeekStats.TOTAL_PAID_ACTIVE_USERS,
                  WeekStats.NEW_PURCHASES);

  private static final List<Field> ONBOARDING_FIELDS =
    Arrays.asList((Field) OnboardingStats.FIRST_TRY_COUNT,
                  OnboardingStats.FIRST_TRY_COMPLETION_RATIO);

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
    addCohortElements(builder);
    addVolumeElements(builder);
    addWeekElements(builder);
    addUserElements(builder);
    addOnboardingElements(builder);

    setupSelectionListeners();

    return builder.load();
  }

  private void setupSelectionListeners() {

    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobList experiments = selection.getAll(Experiment.TYPE);
        if (experiments.isEmpty()) {
          selectionService.clear(WeekStats.TYPE);
          selectionService.clear(OnboardingStats.TYPE);
          selectionService.clear(User.TYPE);
          return;
        }

        int weekId = experiments.getFirst().get(Experiment.WEEK);
        selectStat(weekId, WeekStats.TYPE);
        selectStat(weekId, OnboardingStats.TYPE);

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

  private void addCohortElements(GlobsPanelBuilder builder) {
    builder.addRepeat("cohortCharts", COHORT_CHART_FIELDS,
                      new FieldRepeatComponentFactory(WeekStats.ID, COHORT_CHART_FIELDS,
                                                      repository, directory));
  }

  private void addVolumeElements(GlobsPanelBuilder builder) {
    builder.addRepeat("volumeCharts", VOLUME_CHART_FIELDS,
                      new FieldRepeatComponentFactory(WeekStats.ID, VOLUME_CHART_FIELDS,
                                                      repository, directory));
  }

  private void addWeekElements(GlobsPanelBuilder builder) {

    builder.addTable("weekTable", WeekStats.TYPE, ascending(WeekStats.ID))
      .addColumn(WeekStats.ID)
      .addColumn(WeekStats.NEW_USERS)
      .addColumn(WeekStats.ACTIVATION_COUNT)
      .addColumn(WeekStats.ACTIVATION_RATIO)
      .addColumn(WeekStats.RETENTION_COUNT)
      .addColumn(WeekStats.RETENTION_RATIO)
      .addColumn(WeekStats.REVENUE_COUNT)
      .addColumn(WeekStats.REVENUE_RATIO)
      .addColumn(WeekStats.NEW_PURCHASES)
      .addColumn(WeekStats.TOTAL_ACTIVE_USERS)
      .addColumn(WeekStats.TOTAL_PAID_ACTIVE_USERS);
  }

  private void addUserElements(GlobsPanelBuilder builder) {
    builder.addTable("users", User.TYPE, descending(User.FIRST_DATE))
      .addColumn(User.ID)
      .addColumn(User.EMAIL)
      .addColumn(User.FIRST_DATE)
      .addColumn(User.LAST_DATE)
      .addColumn(User.PING_COUNT)
      .addColumn(User.ACTIVATED)
      .addColumn(User.RETAINED)
      .addColumn(User.DAYS_BEFORE_PURCHASE)
      .addColumn(User.PURCHASE_DATE)
      .addColumn(User.LOST);
  }

  private void addOnboardingElements(GlobsPanelBuilder builder) {
    builder.addRepeat("onboardingCharts", ONBOARDING_FIELDS,
                      new FieldRepeatComponentFactory(OnboardingStats.ID, ONBOARDING_FIELDS,
                                                      repository, directory));

    builder.addTable("onboarding", OnboardingStats.TYPE, descending(OnboardingStats.LAST_DAY))
      .addColumn(OnboardingStats.LAST_DAY)
      .addColumn(OnboardingStats.FIRST_TRY_COUNT)
      .addColumn(OnboardingStats.IMPORT_STARTED_ON_FIRST_TRY)
      .addColumn(OnboardingStats.CATEGORIZATION_STARTED_ON_FIRST_TRY)
      .addColumn(OnboardingStats.CATEGORIZATION_FINISHED_ON_FIRST_TRY)
      .addColumn(OnboardingStats.ONBOARDING_COMPLETED_ON_FIRST_TRY)
      .addColumn(OnboardingStats.FIRST_TRY_COMPLETION_RATIO)
      .addColumn(OnboardingStats.BOUNCE_BEFORE_IMPORT_RATIO)
      .addColumn(OnboardingStats.COMPLETE_IMPORT_RATIO)
      .addColumn(OnboardingStats.COMPLETE_CATEGORIZATION_RATIO);

  }
}
