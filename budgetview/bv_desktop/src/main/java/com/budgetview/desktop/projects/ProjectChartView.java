package com.budgetview.desktop.projects;

import com.budgetview.desktop.View;
import com.budgetview.desktop.analysis.histobuilders.HistoButtonDatasetBuilder;
import com.budgetview.desktop.analysis.histobuilders.HistoChartRangeListener;
import com.budgetview.desktop.analysis.histobuilders.HistoChartUpdater;
import com.budgetview.desktop.analysis.histobuilders.HistoLabelUpdater;
import com.budgetview.desktop.analysis.histobuilders.range.HistoChartAdjustableRange;
import com.budgetview.desktop.analysis.histobuilders.range.HistoChartRange;
import com.budgetview.desktop.browsing.BrowsingAction;
import com.budgetview.desktop.components.charts.histo.HistoChart;
import com.budgetview.desktop.components.charts.histo.HistoChartColors;
import com.budgetview.desktop.components.charts.histo.HistoSelection;
import com.budgetview.desktop.components.charts.histo.button.HistoButtonColors;
import com.budgetview.desktop.components.charts.histo.button.HistoButtonPainter;
import com.budgetview.desktop.components.charts.histo.utils.HistoChartListenerAdapter;
import com.budgetview.desktop.description.Formatting;
import com.budgetview.desktop.model.ProjectStat;
import com.budgetview.desktop.projects.actions.CreateProjectAction;
import com.budgetview.desktop.projects.components.ProjectPopupMenuFactory;
import com.budgetview.shared.model.BudgetArea;
import com.budgetview.model.Month;
import com.budgetview.model.Project;
import com.budgetview.shared.gui.histochart.HistoChartConfig;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.utils.BooleanListener;
import org.globsframework.gui.utils.TypePresenceListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.Utils;
import org.globsframework.utils.collections.Range;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

public class ProjectChartView extends View implements HistoChartAdjustableRange {
  private static final float BUTTON_FONT_SIZE = 12.0f;
  private HistoChartUpdater updater;
  private HistoChart histoChart;
  private HistoChartRange range;
  private HistoButtonColors colors;
  private FontMetrics buttonFontMetrics;
  private final ProjectPopupMenuFactory popupMenuFactory;
  private HistoChartRangeListener rangeListener;
  private CardHandler cards;
  private CreateProjectAction createProjectAction;

  public ProjectChartView(final HistoChartRange range, final GlobRepository repository, final Directory directory) {
    super(repository, directory);
    this.range = range;
    this.histoChart = new HistoChart(new HistoChartConfig(true, false, true, false, true, true, false, true, true, true),
                                     new HistoChartColors("sidebar.histo", directory));
    this.updater = new HistoChartUpdater(repository, directory,
                                         Month.TYPE, Month.ID, Project.TYPE, ProjectStat.TYPE) {
      protected void update(Integer currentMonthId, boolean resetPosition) {
        updateChart(currentMonthId, resetPosition);
      }
    };
    this.popupMenuFactory = new ProjectPopupMenuFactory(repository, directory);
    this.popupMenuFactory.setShowMainActionsOnly(true);
    this.histoChart.addListener(new HistoChartListenerAdapter() {
      public void processClick(HistoSelection selection, Set<Key> objectKeys) {
        if (objectKeys.size() == 1) {
          Glob project = repository.find(objectKeys.iterator().next());
          if (project != null) {
            selectionService.select(project);
          }
          else {
            selectionService.clear(Project.TYPE);
          }
          updateChart();
          return;
        }

        GlobList months = new GlobList();
        for (Integer monthId : selection.getColumnIds()) {
          months.add(repository.get(Key.create(Month.TYPE, monthId)));
        }
        selectionService.select(months, Month.TYPE);
      }

      public void processRightClick(HistoSelection selection, Set<Key> objectKeys, final Point mouseLocation) {
        processClick(selection, objectKeys);
        if (objectKeys.size() == 1) {
          Key projectKey = objectKeys.iterator().next();
          popupMenuFactory.updateSelection(projectKey);
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              JPopupMenu menu = popupMenuFactory.createPopup();
              if (!menu.isShowing()) {
                menu.show(histoChart, mouseLocation.x, mouseLocation.y);
              }
            }
          });
        }
      }

      public void scroll(int count) {
        range.scroll(count);
      }
    });
    this.rangeListener = new HistoChartRangeListener() {
      public void rangeUpdated() {
        updateChart();
      }
    };
    this.range.addListener(rangeListener);
    this.colors = new HistoButtonColors(
      "histo.button.bg.top",
      "histo.button.bg.bottom",
      "histo.button.label",
      "histo.button.label.shadow",
      "histo.button.border",
      "histo.button.rollover.bg.top",
      "histo.button.rollover.bg.bottom",
      "histo.button.rollover.label",
      "histo.button.rollover.border",
      "histo.button.disabled.bg.top",
      "histo.button.disabled.bg.bottom",
      "histo.button.disabled.label",
      "histo.button.disabled.border",
      directory
    );

    Font buttonFont = histoChart.getFont().deriveFont(BUTTON_FONT_SIZE);
    buttonFontMetrics = histoChart.getFontMetrics(buttonFont);
  }

  public void setRange(HistoChartRange newRange) {
    this.range.removeListener(rangeListener);
    this.range = newRange;
    this.range.addListener(rangeListener);
    updateChart();
  }

  private void updateChart() {
    Integer currentMonthId = updater.getCurrentMonthId();
    if (currentMonthId != null) {
      updateChart(currentMonthId, false);
    }
  }

  private void updateChart(Integer currentMonthId, boolean resetPosition) {
    if (resetPosition) {
      range.reset();
    }

    java.util.List<Integer> monthIds = range.getMonthIds(currentMonthId);
    HistoButtonDatasetBuilder dataset = new HistoButtonDatasetBuilder(histoChart, new JLabel(), repository, HistoLabelUpdater.get(histoChart, monthIds.size()));
    for (Integer monthId : monthIds) {
      dataset.addColumn(monthId, Utils.equal(monthId, currentMonthId));
    }
    GlobList selectedProjects = selectionService.getSelection(Project.TYPE);
    for (Glob project : repository.getAll(Project.TYPE)) {
      Range<Integer> range = Project.getMonthRange(project, repository);
      if (range == null) {
        continue;
      }
      dataset.addButton(range.getMin(), range.getMax(),
                        project.get(Project.NAME),
                        project.getKey(),
                        getTooltip(project),
                        selectedProjects.contains(project),
                        project.isTrue(Project.ACTIVE));
    }

    histoChart.update(new HistoButtonPainter(dataset.get(), buttonFontMetrics, colors));
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/projects/projectChartView.splits",
                                                      repository, directory);

    cards = builder.addCardHandler("projectChartCards");

    builder.add("createFirstProject", new CreateProjectAction(repository, directory));
    createProjectAction = new CreateProjectAction(repository, directory);

    builder.add("openProjectGuide", new BrowsingAction(Lang.get("projectView.creation.learnmore"), directory) {
      protected String getUrl() {
        return Lang.get("projectView.creation.site.url.projects");
      }
    });

    builder.add("projectChart", histoChart);


    parentBuilder.add("projectChartPanel", builder);

    parentBuilder.addLoader(new SplitsLoader() {
      public void load(Component component, SplitsNode node) {
        TypePresenceListener.install(Project.TYPE, repository, new BooleanListener() {
          public void apply(boolean containsProjects) {
            cards.show(containsProjects ? "chart" : "creation");
            histoChart.setVisible(containsProjects);
            createProjectAction.setEnabled(containsProjects);
          }
        });
      }
    });
  }

  private String getTooltip(Glob project) {
    Glob stat = repository.find(Key.create(ProjectStat.TYPE, project.get(Project.ID)));
    if (stat != null) {
      return Lang.get("projectView.chart.element.tooltip",
                      project.get(Project.NAME),
                      Formatting.toString(stat.get(ProjectStat.ACTUAL_AMOUNT), BudgetArea.EXTRAS),
                      Formatting.toString(stat.get(ProjectStat.PLANNED_AMOUNT), BudgetArea.EXTRAS));
    }
    return null;
  }

  public Action getCreateProjectAction() {
    return createProjectAction;
  }
}
