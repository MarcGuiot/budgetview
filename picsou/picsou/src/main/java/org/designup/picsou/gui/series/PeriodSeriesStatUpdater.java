package org.designup.picsou.gui.series;

import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.Utils;
import org.globsframework.utils.Log;
import org.designup.picsou.model.Month;
import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.gui.model.SeriesStat;

import java.util.Set;
import java.util.HashSet;
import java.awt.*;

public class PeriodSeriesStatUpdater implements GlobSelectionListener, ChangeSetListener {

  private GlobList selectedMonths = GlobList.EMPTY;
  private GlobRepository repository;
  private Directory directory;

  public static void init(GlobRepository repository, Directory directory) {
    new PeriodSeriesStatUpdater(repository, directory);
  }

  private PeriodSeriesStatUpdater(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    directory.get(SelectionService.class).addListener(this, Month.TYPE);
    repository.addTrigger(this);
  }

  public void selectionUpdated(GlobSelection selection) {
    selectedMonths = selection.getAll(Month.TYPE);
    updateSelection();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsCreationsOrDeletions(Month.TYPE)) {
      selectedMonths.removeAll(changeSet.getDeleted(Month.TYPE));
      updateSelection();
    }
    else if (changeSet.containsChanges(SeriesStat.TYPE)) {
      updateSelection();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    selectedMonths = GlobList.EMPTY;
    updateSelection();
  }

  private void updateSelection() {
    repository.startChangeSet();
    PeriodSeriesStatFunctor seriesStatFunctor = new PeriodSeriesStatFunctor(repository);
    try {
      repository.deleteAll(PeriodSeriesStat.TYPE);
      Set<Integer> monthIds = selectedMonths.getValueSet(Month.ID);

      repository.safeApply(SeriesStat.TYPE, GlobMatchers.fieldContained(SeriesStat.MONTH, monthIds),
                           seriesStatFunctor);
    }
    finally {
      repository.completeChangeSet();
    }

    SelectionService localSelectionService = directory.get(SelectionService.class);
    GlobList seriesStats = seriesStatFunctor.getStats();
    localSelectionService.select(GlobSelectionBuilder.init()
      .add(seriesStats, PeriodSeriesStat.TYPE).get());

    Utils.releaseBeginRemove();
    for (Glob seriesStat : seriesStats) {
      if (repository.findLinkTarget(seriesStat, PeriodSeriesStat.SERIES) == null){
        Log.write("Error : SeriesStat without series ");
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        toolkit.beep();
        break;
      }
    }
    Utils.releaseEndRemove();
  }

  private static class PeriodSeriesStatFunctor implements GlobFunctor {
    private Set<Glob> stats = new HashSet<Glob>();
    private GlobRepository repository;

    public PeriodSeriesStatFunctor(GlobRepository repository) {
      this.repository = repository;
    }

    public void run(Glob glob, GlobRepository remote) throws Exception {
      Glob stat =
        repository.findOrCreate(Key.create(PeriodSeriesStat.TYPE, glob.get(SeriesStat.SERIES)));
      double amount = stat.get(PeriodSeriesStat.AMOUNT) + glob.get(SeriesStat.AMOUNT);
      double plannedAmount = stat.get(PeriodSeriesStat.PLANNED_AMOUNT) + glob.get(SeriesStat.PLANNED_AMOUNT);
      repository.update(stat.getKey(),
                        FieldValue.value(PeriodSeriesStat.AMOUNT, amount),
                        FieldValue.value(PeriodSeriesStat.PLANNED_AMOUNT, plannedAmount),
                        FieldValue.value(PeriodSeriesStat.ABS_SUM_AMOUNT, 
                                         Math.abs(plannedAmount) > Math.abs(amount) ? Math.abs(plannedAmount) : Math.abs(amount)));
      stats.add(stat);
    }

    public GlobList getStats() {
      return new GlobList(stats);
    }
  }

}
