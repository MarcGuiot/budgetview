package com.budgetview.desktop.analysis.table;

import com.budgetview.desktop.actions.SelectNextMonthAction;
import com.budgetview.desktop.actions.SelectPreviousMonthAction;
import com.budgetview.desktop.analysis.SeriesChartsColors;
import com.budgetview.desktop.analysis.utils.AnalysisViewPanel;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.SortedSet;

public class TableAnalysisView extends AnalysisViewPanel {

  public TableAnalysisView(String name, GlobRepository repository,
                           Directory parentDirectory,
                           Directory directory, SeriesChartsColors seriesChartsColors) {
    super(name, "/layout/analysis/tableAnalysisView.splits",
          repository, parentDirectory, directory, seriesChartsColors);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    SeriesEvolutionTableView tableView = new SeriesEvolutionTableView("analysisTable", repository, seriesChartsColors,
                                                                      directory, parentDirectory);
    tableView.registerComponents(builder);

    builder.add("previousMonth", new SelectPreviousMonthAction(repository, parentDirectory));
    builder.add("nextMonth", new SelectNextMonthAction(repository, parentDirectory));
  }

  public void monthSelected(Integer referenceMonthId, SortedSet<Integer> monthIds) {
  }

  public void reset() {
  }
}
