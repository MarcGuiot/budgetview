package org.designup.picsou.gui.series;

import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.util.Amounts;
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
    else if (changeSet.containsChanges(CurrentMonth.KEY)){
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

    Utils.beginRemove();
    for (Glob seriesStat : seriesStats) {
      if (repository.findLinkTarget(seriesStat, PeriodSeriesStat.SERIES) == null) {
        Log.write("Error : SeriesStat without series ");
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        toolkit.beep();
        break;
      }
    }
    Utils.endRemove();
  }

  private static class PeriodSeriesStatFunctor implements GlobFunctor {
    private Set<Glob> stats = new HashSet<Glob>();
    private GlobRepository repository;
    private Integer monthId = 0;

    public PeriodSeriesStatFunctor(GlobRepository repository) {
      this.repository = repository;
      if (repository.contains(CurrentMonth.KEY)){
        monthId = CurrentMonth.getCurrentMonth(repository);
      }
    }

    public void run(Glob seriesStat, GlobRepository remote) throws Exception {
      Glob periodStat =
        repository.findOrCreate(Key.create(PeriodSeriesStat.TYPE, seriesStat.get(SeriesStat.SERIES)));
      double amount = periodStat.get(PeriodSeriesStat.AMOUNT) +
                      Utils.zeroIfNull(seriesStat.get(SeriesStat.AMOUNT));
      double plannedAmount = periodStat.get(PeriodSeriesStat.PLANNED_AMOUNT) +
                             Utils.zeroIfNull(seriesStat.get(SeriesStat.PLANNED_AMOUNT));
      double pastRemaining = periodStat.get(PeriodSeriesStat.PAST_REMAINING);
      double futureRemaining = periodStat.get(PeriodSeriesStat.FUTURE_REMAINING);
      double pastOverrun = periodStat.get(PeriodSeriesStat.PAST_OVERRUN);
      double futureOverrun = periodStat.get(PeriodSeriesStat.FUTURE_OVERRUN);
      if (seriesStat.get(SeriesStat.MONTH) < monthId){
        pastRemaining += seriesStat.get(SeriesStat.REMAINING_AMOUNT);
        pastOverrun += seriesStat.get(SeriesStat.OVERRUN_AMOUNT);
      }
      else {
        futureRemaining += seriesStat.get(SeriesStat.REMAINING_AMOUNT);
        futureOverrun += seriesStat.get(SeriesStat.OVERRUN_AMOUNT);
      }
      repository.update(periodStat.getKey(),
                        value(PeriodSeriesStat.AMOUNT, amount),
                        value(PeriodSeriesStat.PLANNED_AMOUNT, plannedAmount),
                        value(PeriodSeriesStat.PAST_REMAINING, pastRemaining),
                        value(PeriodSeriesStat.FUTURE_REMAINING, futureRemaining),
                        value(PeriodSeriesStat.PAST_OVERRUN, pastOverrun),
                        value(PeriodSeriesStat.FUTURE_OVERRUN, futureOverrun),
                        value(PeriodSeriesStat.ABS_SUM_AMOUNT,
                              Math.abs(plannedAmount) > Math.abs(amount) ? Math.abs(plannedAmount) : Math.abs(amount)));
      stats.add(periodStat);
    }

    public GlobList getStats() {
      return new GlobList(stats);
    }
  }

}
