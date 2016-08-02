package com.budgetview.desktop.analysis.budget;

import com.budgetview.desktop.analysis.SeriesChartsColors;
import com.budgetview.desktop.analysis.SeriesChartsPanel;
import com.budgetview.desktop.analysis.histobuilders.range.ScrollableHistoChartRange;
import com.budgetview.desktop.analysis.utils.AnalysisViewPanel;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.SortedSet;

public class BudgetAnalysisView extends AnalysisViewPanel {
  private SeriesChartsPanel charts;
  private JPanel chartsPanel;

  public BudgetAnalysisView(String name, GlobRepository repository, Directory parentDirectory, Directory directory, SeriesChartsColors seriesChartsColors) {
    super(name, "/layout/analysis/budgetAnalysisView.splits", repository, parentDirectory, directory, seriesChartsColors);
  }

  protected void registerComponents(GlobsPanelBuilder builder) {
    BudgetAnalysisBreadcrumb breadcrumb = new BudgetAnalysisBreadcrumb(repository, directory);
    builder.add("breadcrumb", breadcrumb.getEditor());

    this.charts = new SeriesChartsPanel(new ScrollableHistoChartRange(12, 6, false, repository), repository, directory, parentDirectory.get(SelectionService.class));
    this.charts.registerCharts(builder);
    this.chartsPanel = new JPanel();
    builder.add("chartsPanel", chartsPanel);

  }

  public void monthSelected(Integer referenceMonthId, SortedSet<Integer> monthIds) {
    charts.monthSelected(referenceMonthId, monthIds);
  }

  public void reset() {
    charts.reset();
  }
}
