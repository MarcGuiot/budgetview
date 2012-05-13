package org.designup.picsou.gui.series;

import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.model.util.Amounts;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.fieldContained;

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
    else if (changeSet.containsChanges(CurrentMonth.KEY)) {
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
      resetStats();
      repository.safeApply(SeriesStat.TYPE,
                           fieldContained(SeriesStat.MONTH, selectedMonths.getValueSet(Month.ID)),
                           seriesStatFunctor);
      initToUpdateField();
      initEvolutionFields();
    }
    finally {
      repository.completeChangeSet();
    }

    SelectionService localSelectionService = directory.get(SelectionService.class);
    GlobList seriesStats = seriesStatFunctor.getStats();
    localSelectionService.select(GlobSelectionBuilder.init()
                                   .add(seriesStats, PeriodSeriesStat.TYPE)
                                   .get());
  }

  private void initEvolutionFields() {
    SortedSet<Integer> monthIds = selectedMonths.getSortedSet(Month.ID);
    if (monthIds.isEmpty()) {
      return;
    }

    if (monthIds.size() > 1) {
      initEvolutionFields(monthIds.first(), monthIds.last());
    }
    else {
      int lastMonth = monthIds.first();
      initEvolutionFields(Month.previous(lastMonth), lastMonth);
    }
  }

  private void initEvolutionFields(int previousMonth, int newMonth) {
    for (Glob stat : repository.findByIndex(SeriesStat.MONTH_INDEX, newMonth)) {
      Integer seriesId = stat.get(SeriesStat.SERIES);
      Double newValue = stat.get(SeriesStat.SUMMARY_AMOUNT);

      Glob previousStat = repository.find(SeriesStat.createKey(seriesId, previousMonth));
      Double previousValue = previousStat == null ? null : previousStat.get(SeriesStat.SUMMARY_AMOUNT);

      repository.update(Key.create(PeriodSeriesStat.TYPE, seriesId),
                        value(PeriodSeriesStat.PREVIOUS_SUMMARY_AMOUNT, previousValue),
                        value(PeriodSeriesStat.PREVIOUS_SUMMARY_MONTH, previousMonth),
                        value(PeriodSeriesStat.NEW_SUMMARY_AMOUNT, newValue),
                        value(PeriodSeriesStat.NEW_SUMMARY_MONTH, newMonth));
    }
  }

  private void resetStats() {
    repository.deleteAll(PeriodSeriesStat.TYPE);
    for (Glob series : repository.getAll(Series.TYPE)) {
      repository.create(Key.create(PeriodSeriesStat.TYPE, series.get(Series.ID)),
                        value(PeriodSeriesStat.ACTIVE, false));
    }
  }

  private static class PeriodSeriesStatFunctor implements GlobFunctor {
    private Set<Glob> stats = new HashSet<Glob>();
    private GlobRepository repository;
    private Integer monthId = 0;

    public PeriodSeriesStatFunctor(GlobRepository repository) {
      this.repository = repository;
      if (repository.contains(CurrentMonth.KEY)) {
        monthId = CurrentMonth.getCurrentMonth(repository);
      }
    }

    public void run(Glob seriesStat, GlobRepository remote) throws Exception {
      Glob periodStat =
        repository.findOrCreate(Key.create(PeriodSeriesStat.TYPE, seriesStat.get(SeriesStat.SERIES)));
      double amount = periodStat.get(PeriodSeriesStat.AMOUNT) +
                      Utils.zeroIfNull(seriesStat.get(SeriesStat.AMOUNT));
      Double plannedAmount;
      if (periodStat.get(PeriodSeriesStat.PLANNED_AMOUNT) == null && seriesStat.get(SeriesStat.PLANNED_AMOUNT) == null) {
        plannedAmount = null;
      }
      else {
        plannedAmount = periodStat.get(PeriodSeriesStat.PLANNED_AMOUNT, 0) +
                        seriesStat.get(SeriesStat.PLANNED_AMOUNT, 0);
      }
      double pastRemaining = periodStat.get(PeriodSeriesStat.PAST_REMAINING);
      double futureRemaining = periodStat.get(PeriodSeriesStat.FUTURE_REMAINING);
      double pastOverrun = periodStat.get(PeriodSeriesStat.PAST_OVERRUN);
      double futureOverrun = periodStat.get(PeriodSeriesStat.FUTURE_OVERRUN);
      if (seriesStat.get(SeriesStat.MONTH) < monthId) {
        pastRemaining += seriesStat.get(SeriesStat.REMAINING_AMOUNT);
        pastOverrun += seriesStat.get(SeriesStat.OVERRUN_AMOUNT);
      }
      else {
        futureRemaining += seriesStat.get(SeriesStat.REMAINING_AMOUNT);
        futureOverrun += seriesStat.get(SeriesStat.OVERRUN_AMOUNT);
      }

      boolean isActive = seriesStat.isTrue(SeriesStat.ACTIVE) || periodStat.isTrue(PeriodSeriesStat.ACTIVE);

      repository.update(periodStat.getKey(),
                        value(PeriodSeriesStat.AMOUNT, amount),
                        value(PeriodSeriesStat.PLANNED_AMOUNT, plannedAmount),
                        value(PeriodSeriesStat.PAST_REMAINING, pastRemaining),
                        value(PeriodSeriesStat.FUTURE_REMAINING, futureRemaining),
                        value(PeriodSeriesStat.PAST_OVERRUN, pastOverrun),
                        value(PeriodSeriesStat.FUTURE_OVERRUN, futureOverrun),
                        value(PeriodSeriesStat.ABS_SUM_AMOUNT,
                              Math.abs(plannedAmount == null ? 0 : plannedAmount) > Math.abs(amount) ?
                              Math.abs(plannedAmount == null ? 0 : plannedAmount) : Math.abs(amount)),
                        value(PeriodSeriesStat.ACTIVE, isActive));

      stats.add(periodStat);
    }

    public GlobList getStats() {
      return new GlobList(stats);
    }
  }

  private void initToUpdateField() {
    boolean signpostCompleted = SignpostStatus.isInitialGuidanceCompleted(repository);
    for (Glob periodStat : repository.getAll(PeriodSeriesStat.TYPE)) {
      boolean value = !signpostCompleted && isToUpdate(periodStat.get(PeriodSeriesStat.SERIES));
      repository.update(periodStat.getKey(),
                        PeriodSeriesStat.TO_SET,
                        value);
    }
  }

  private boolean isToUpdate(Integer seriesId) {
    if (Series.UNCATEGORIZED_SERIES_ID.equals(seriesId)) {
      return false;
    }

    boolean hasObservedAmount = false;
    boolean hasPlannedAmountToSet = false;
    GlobList activeStats =
      repository.findByIndex(SeriesStat.SERIES_INDEX, seriesId);
    if (!selectedMonths.isEmpty()) {
      activeStats.filterSelf(fieldContained(SeriesStat.MONTH, selectedMonths.getValueSet(Month.ID)), repository);
    }

    for (Glob stat : activeStats) {
      hasObservedAmount |= Amounts.isNotZero(stat.get(SeriesStat.AMOUNT));
      hasPlannedAmountToSet |= Amounts.isUnset(stat.get(SeriesStat.PLANNED_AMOUNT));
      if (hasObservedAmount && hasPlannedAmountToSet) {
        return true;
      }
    }
    return false;
  }
}
