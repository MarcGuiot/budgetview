package org.designup.picsou.gui.series.evolution.histobuilders;

import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.model.CurrentMonth;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.Set;
import java.util.SortedSet;

public abstract class HistoChartUpdater implements GlobSelectionListener, Disposable {
  private HistoChartBuilder histoChartBuilder;
  private GlobRepository repository;
  private Directory directory;
  private GlobType selectionType;
  private IntegerField selectionMonthField;
  protected Integer currentMonthId;
  protected SortedSet<Integer> currentMonths;
  private int todayId;
  private ChangeSetListener changeSetListener;

  public HistoChartUpdater(HistoChartBuilder histoChartBuilder,
                           GlobRepository repository,
                           Directory directory,
                           final GlobType selectionType,
                           final IntegerField selectionMonthField,
                           final GlobType... types) {
    this.histoChartBuilder = histoChartBuilder;
    this.repository = repository;
    this.directory = directory;
    this.selectionType = selectionType;
    this.selectionMonthField = selectionMonthField;
    this.todayId = directory.get(TimeService.class).getCurrentMonthId();
    directory.get(SelectionService.class).addListener(this, selectionType);
    registerChangeSetListener(repository, types);
  }

  private void registerChangeSetListener(GlobRepository repository, final GlobType[] types) {
    this.changeSetListener = new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (repository == null) {
          return;
        }
        for (GlobType type : types) {
          if (changeSet.containsChanges(type)) {
            update(true);
            return;
          }
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        update(true);
      }
    };
    repository.addChangeListener(changeSetListener);
  }

  public void selectionUpdated(GlobSelection selection) {
    this.currentMonths = selection.getAll(selectionType).getSortedSet(selectionMonthField);
    this.currentMonthId = currentMonths.isEmpty() ? null : currentMonths.last();
    update(true);
  }

  public void update(final boolean resetPosition) {
    if ((histoChartBuilder != null) && (repository.contains(CurrentMonth.TYPE))) {
      update(histoChartBuilder, currentMonthId == null ? todayId : currentMonthId, resetPosition);
    }
  }

  public Integer getCurrentMonthId() {
    return currentMonthId;
  }

  public void dispose() {
    if (repository != null) {
      repository.removeChangeListener(changeSetListener);
    }
    if (directory != null) {
      directory.get(SelectionService.class).removeListener(this);
    }
    histoChartBuilder = null;
    repository = null;
    directory = null;
    changeSetListener = null;
  }

  protected abstract void update(HistoChartBuilder histoChartBuilder, Integer currentMonthId, boolean resetPosition);
}
