package org.designup.picsou.gui.projects;

import com.budgetview.shared.gui.histochart.HistoChartConfig;
import org.designup.picsou.gui.View;
import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoChartColors;
import org.designup.picsou.gui.components.charts.histo.HistoSelection;
import org.designup.picsou.gui.components.charts.histo.button.HistoButtonColors;
import org.designup.picsou.gui.components.charts.histo.button.HistoButtonPainter;
import org.designup.picsou.gui.components.charts.histo.utils.HistoChartListenerAdapter;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.ProjectStat;
import org.designup.picsou.gui.projects.actions.CreateProjectAction;
import org.designup.picsou.gui.series.analysis.histobuilders.HistoButtonDatasetBuilder;
import org.designup.picsou.gui.series.analysis.histobuilders.HistoChartRangeListener;
import org.designup.picsou.gui.series.analysis.histobuilders.HistoChartUpdater;
import org.designup.picsou.gui.series.analysis.histobuilders.range.HistoChartRange;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Project;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
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

public class ProjectChartView extends View {
  private static final float BUTTON_FONT_SIZE = 12.0f;
  private HistoChartUpdater updater;
  private HistoChart histoChart;
  private HistoChartRange range;
  private HistoButtonColors colors;
  private FontMetrics buttonFontMetrics;

  public ProjectChartView(final HistoChartRange range, final GlobRepository repository, final Directory directory) {
    super(repository, directory);
    this.range = range;
    this.histoChart = new HistoChart(new HistoChartConfig(true, false, true, false, true, true, false, true, true, true), new HistoChartColors(directory));
    this.updater = new HistoChartUpdater(repository, directory,
                                         Month.TYPE, Month.ID, Project.TYPE, ProjectStat.TYPE) {
      protected void update(Integer currentMonthId, boolean resetPosition) {
        updateChart(currentMonthId, resetPosition);
      }
    };
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

      public void scroll(int count) {
        range.scroll(count);
      }
    });
    this.range.addListener(new HistoChartRangeListener() {
      public void rangeUpdated() {
        updateChart();
      }
    });
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
      directory
    );

    Font buttonFont = histoChart.getFont().deriveFont(BUTTON_FONT_SIZE);
    buttonFontMetrics = histoChart.getFontMetrics(buttonFont);
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

    HistoButtonDatasetBuilder dataset = new HistoButtonDatasetBuilder(histoChart, new JLabel(), repository);
    for (Integer monthId : range.getMonthIds(currentMonthId)) {
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
                        selectedProjects.contains(project));
    }

    histoChart.update(new HistoButtonPainter(dataset.get(), buttonFontMetrics, colors));
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("projectChart", histoChart);

    builder.add("createProject", new CreateProjectAction(directory));
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

}
