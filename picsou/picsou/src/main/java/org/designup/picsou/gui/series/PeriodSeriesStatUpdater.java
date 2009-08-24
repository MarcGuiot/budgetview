package org.designup.picsou.gui.series;

import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.Month;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Log;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

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
      if (repository.findLinkTarget(seriesStat, PeriodSeriesStat.SERIES) == null) {
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

    public void run(Glob seriesStat, GlobRepository remote) throws Exception {
      Glob periodStat =
        repository.findOrCreate(Key.create(PeriodSeriesStat.TYPE, seriesStat.get(SeriesStat.SERIES)));
      double amount = periodStat.get(PeriodSeriesStat.AMOUNT) + seriesStat.get(SeriesStat.AMOUNT);
      double plannedAmount = periodStat.get(PeriodSeriesStat.PLANNED_AMOUNT) + seriesStat.get(SeriesStat.PLANNED_AMOUNT);
      repository.update(periodStat.getKey(),
                        value(PeriodSeriesStat.AMOUNT, amount),
                        value(PeriodSeriesStat.PLANNED_AMOUNT, plannedAmount),
                        value(PeriodSeriesStat.ABS_SUM_AMOUNT,
                              Math.abs(plannedAmount) > Math.abs(amount) ? Math.abs(plannedAmount) : Math.abs(amount)));
      stats.add(periodStat);
    }

    public GlobList getStats() {
      return new GlobList(stats);
    }
  }

}
