package org.designup.picsou.gui.savings;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.series.evolution.HistoChartBuilder;
import org.designup.picsou.gui.components.charts.histo.painters.HistoLineColors;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.model.SavingsBudgetStat;
import org.designup.picsou.model.Month;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.Set;
import java.util.SortedSet;

public class SavingsChartView extends View implements GlobSelectionListener {

  private HistoChartBuilder histoChartBuilder;
  private HistoLineColors accountColors;
  private Integer currentMonthId;

  protected SavingsChartView(GlobRepository repository, Directory directory) {
    super(repository, directory);
    selectionService.addListener(this, Month.TYPE);

    histoChartBuilder = new HistoChartBuilder(repository, directory, selectionService, 12, 12);
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(BudgetStat.TYPE)
            || changeSet.containsChanges(SavingsBudgetStat.TYPE)) {
          update();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        update();
      }
    });
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("histoChart", histoChartBuilder.getChart());
  }

  public void selectionUpdated(GlobSelection selection) {
    SortedSet<Integer> months = selection.getAll(Month.TYPE).getSortedSet(Month.ID);
    this.currentMonthId = months.isEmpty() ? null : months.first();
    update();
  }

  private void update() {
    if (currentMonthId == null) {
      histoChartBuilder.clear();
      return;
    }

    histoChartBuilder.showSavingsAccountsHisto(currentMonthId);
  }
}
