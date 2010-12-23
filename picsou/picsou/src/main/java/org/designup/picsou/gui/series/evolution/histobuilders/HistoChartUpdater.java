package org.designup.picsou.gui.series.evolution.histobuilders;

import org.designup.picsou.gui.time.TimeService;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.Set;
import java.util.SortedSet;

public abstract class HistoChartUpdater implements GlobSelectionListener {
  private HistoChartBuilder histoChartBuilder;
  private GlobType selectionType;
  private IntegerField selectionMonthField;
  protected Integer currentMonthId;
  protected SortedSet<Integer> currentMonths;
  private int todayId;

  public HistoChartUpdater(HistoChartBuilder histoChartBuilder,
                           GlobRepository repository,
                           Directory directory,
                           final GlobType selectionType,
                           final IntegerField selectionMonthField,
                           final GlobType... types) {
    this.histoChartBuilder = histoChartBuilder;
    this.selectionType = selectionType;
    this.selectionMonthField = selectionMonthField;
    todayId = directory.get(TimeService.class).getCurrentMonthId();
    directory.get(SelectionService.class).addListener(this, selectionType);

    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        for (GlobType type : types) {
          if (changeSet.containsChanges(type)) {
            update();
            return;
          }
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        update();
      }
    });
  }

  public void selectionUpdated(GlobSelection selection) {
    this.currentMonths = selection.getAll(selectionType).getSortedSet(selectionMonthField);
    this.currentMonthId = currentMonths.isEmpty() ? null : currentMonths.first();
    update();
  }

  public void update() {
//    if (currentMonthId == null) {
//      histoChartBuilder.clear();
//      return;
//    }

    update(histoChartBuilder, currentMonthId == null ? todayId : currentMonthId);
  }

  protected abstract void update(HistoChartBuilder histoChartBuilder, Integer currentMonthId);
}