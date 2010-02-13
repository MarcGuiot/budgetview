package org.designup.picsou.gui.series.evolution;

import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.model.SavingsBudgetStat;
import org.designup.picsou.model.Month;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.Set;
import java.util.SortedSet;

public abstract class AccountHistoChartUpdater implements GlobSelectionListener {
  private HistoChartBuilder histoChartBuilder;
  private Integer currentMonthId;

  public AccountHistoChartUpdater(HistoChartBuilder histoChartBuilder, GlobRepository repository, Directory directory) {
    this.histoChartBuilder = histoChartBuilder;
    directory.get(SelectionService.class).addListener(this, Month.TYPE);

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

    update(histoChartBuilder, currentMonthId);
  }

  protected abstract void update(HistoChartBuilder histoChartBuilder, Integer currentMonthId);
}
