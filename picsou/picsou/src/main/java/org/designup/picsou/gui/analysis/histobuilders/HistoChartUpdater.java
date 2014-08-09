package org.designup.picsou.gui.analysis.histobuilders;

import org.designup.picsou.gui.time.TimeService;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import java.util.Set;
import java.util.SortedSet;

public abstract class HistoChartUpdater implements GlobSelectionListener, Disposable {
  private GlobRepository repository;
  private Directory directory;
  private GlobType selectionType;
  private IntegerField selectionMonthField;
  protected Integer currentMonthId;
  protected SortedSet<Integer> currentMonths;
  private int todayId;
  private ChangeSetListener changeSetListener;

  public HistoChartUpdater(GlobRepository repository,
                           Directory directory,
                           final GlobType selectionType,
                           final IntegerField selectionMonthField,
                           final GlobType... types) {
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
        if (!isDisposed()) {
          for (GlobType type : types) {
            if (changeSet.containsChanges(type)) {
              update(true);
              return;
            }
          }
        }
        else {
          return;
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (!isDisposed()) {
          for (GlobType type : Utils.list(selectionType, types)) {
            if (changedTypes.contains(type)) {
              update(true);
            }
          }
        }
      }
    };
    repository.addChangeListener(changeSetListener);
  }

  public void selectionUpdated(GlobSelection selection) {
    if (isDisposed()) {
      return;
    }
    updateCurrentMonth();
    update(true);
  }

  public void update(final boolean resetPosition) {
    if (currentMonthId == null) {
      updateCurrentMonth();
    }
    update(currentMonthId == null ? todayId : currentMonthId, resetPosition);
  }

  public Integer getCurrentMonthId() {
    if (currentMonths == null || currentMonths.isEmpty() || currentMonthId == null) {
      updateCurrentMonth();
    }
    return currentMonthId;
  }

  public void updateCurrentMonth() {
    if (isDisposed()) {
      return;
    }
    this.currentMonths = directory.get(SelectionService.class).getSelection(selectionType).getSortedSet(selectionMonthField);
    this.currentMonthId = currentMonths.isEmpty() ? null : currentMonths.last();
  }

  protected abstract void update(Integer currentMonthId, boolean resetPosition);

  public void dispose() {
    if (isDisposed()) {
      throw new RuntimeException("Double dispose");
    }
    repository.removeChangeListener(changeSetListener);
    directory.get(SelectionService.class).removeListener(this);
    repository = null;
    directory = null;
    changeSetListener = null;
  }

  public boolean isDisposed() {
    return HistoChartUpdater.this.repository == null;
  }
}
