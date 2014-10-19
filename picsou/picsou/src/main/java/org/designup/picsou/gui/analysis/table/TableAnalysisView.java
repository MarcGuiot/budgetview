package org.designup.picsou.gui.analysis.table;

import org.designup.picsou.gui.actions.SelectNextMonthAction;
import org.designup.picsou.gui.actions.SelectPreviousMonthAction;
import org.designup.picsou.gui.analysis.SeriesChartsColors;
import org.designup.picsou.gui.analysis.utils.AnalysisViewPanel;
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
