package org.designup.picsou.gui.analysis.budget;

import org.designup.picsou.gui.analysis.SeriesChartsColors;
import org.designup.picsou.gui.analysis.SeriesChartsPanel;
import org.designup.picsou.gui.analysis.histobuilders.range.ScrollableHistoChartRange;
import org.designup.picsou.gui.analysis.utils.AnalysisViewPanel;
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
