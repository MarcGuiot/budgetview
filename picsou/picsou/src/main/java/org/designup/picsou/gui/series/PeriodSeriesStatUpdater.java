package org.designup.picsou.gui.series;

import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.gui.model.SeriesType;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.series.utils.SeriesOrGroup;
import org.designup.picsou.model.*;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import static org.designup.picsou.gui.model.SeriesStat.isSeries;
import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.*;

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
    else if (changeSet.containsChanges(SeriesStat.TYPE) ||
             changeSet.containsChanges(SeriesGroup.TYPE) ||
             changeSet.containsChanges(CurrentMonth.KEY)) {
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
                           and(fieldContained(SeriesStat.MONTH, selectedMonths.getValueSet(Month.ID)),
                               isSeries()),
                           seriesStatFunctor);
      initToUpdateField();
      initGroups();
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
      SeriesOrGroup seriesOrGroup = SeriesOrGroup.getFromStat(stat);
      Double newValue = stat.get(SeriesStat.SUMMARY_AMOUNT);

      Glob previousStat = repository.find(seriesOrGroup.createSeriesStatKey(previousMonth));
      Double previousValue = previousStat == null ? null : previousStat.get(SeriesStat.SUMMARY_AMOUNT);

      Glob periodSeriesStat = PeriodSeriesStat.findOrCreate(seriesOrGroup.id, seriesOrGroup.type, repository);
      repository.update(periodSeriesStat.getKey(),
                        value(PeriodSeriesStat.PREVIOUS_SUMMARY_AMOUNT, previousValue),
                        value(PeriodSeriesStat.PREVIOUS_SUMMARY_MONTH, previousMonth),
                        value(PeriodSeriesStat.NEW_SUMMARY_AMOUNT, newValue),
                        value(PeriodSeriesStat.NEW_SUMMARY_MONTH, newMonth));
    }
  }

  private void initGroups() {
    for (Glob periodSeriesStat : repository.getAll(PeriodSeriesStat.TYPE,
                                                   fieldEquals(PeriodSeriesStat.TARGET_TYPE, SeriesType.SERIES.getId()))) {
      Glob series = PeriodSeriesStat.findTarget(periodSeriesStat, repository);
      Integer groupId = series.get(Series.GROUP);
      if (groupId != null) {
        Glob groupStat = PeriodSeriesStat.findUnique(groupId, SeriesType.SERIES_GROUP, repository);
        if (groupStat == null) {
          groupStat = repository.create(PeriodSeriesStat.TYPE,
                                        value(PeriodSeriesStat.TARGET, groupId),
                                        value(PeriodSeriesStat.TARGET_TYPE, SeriesType.SERIES_GROUP.getId()));
        }

        FieldValuesBuilder builder = new FieldValuesBuilder();
        addFields(builder, groupStat, periodSeriesStat,
                  PeriodSeriesStat.AMOUNT,
                  PeriodSeriesStat.PLANNED_AMOUNT,
                  PeriodSeriesStat.PAST_REMAINING,
                  PeriodSeriesStat.FUTURE_REMAINING,
                  PeriodSeriesStat.PAST_OVERRUN,
                  PeriodSeriesStat.FUTURE_OVERRUN,
                  PeriodSeriesStat.ABS_SUM_AMOUNT);
        builder.set(PeriodSeriesStat.VISIBLE, true);
        builder.set(PeriodSeriesStat.ACTIVE, groupStat.isTrue(PeriodSeriesStat.ACTIVE) || periodSeriesStat.isTrue(PeriodSeriesStat.ACTIVE));

        repository.update(groupStat.getKey(), builder.toArray());
      }
    }
  }

  private void addFields(FieldValuesBuilder builder,
                         Glob groupStat, Glob seriesStat,
                         DoubleField... fields) {
    for (DoubleField field : fields) {
      builder.set(field, Amounts.add(groupStat.get(field), seriesStat.get(field)));
    }
  }

  private void resetStats() {
    repository.deleteAll(PeriodSeriesStat.TYPE);
    for (Glob series : repository.getAll(Series.TYPE)) {
      repository.create(PeriodSeriesStat.TYPE,
                        value(PeriodSeriesStat.TARGET_TYPE, SeriesType.SERIES.getId()),
                        value(PeriodSeriesStat.TARGET, series.get(Series.ID)),
                        value(PeriodSeriesStat.VISIBLE, true),
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
      Integer seriesId = seriesStat.get(SeriesStat.TARGET);
      Glob series = remote.get(Key.create(Series.TYPE, seriesId));
      Glob group = remote.findLinkTarget(series, Series.GROUP);
      boolean visible = (group == null) || group.isTrue(SeriesGroup.EXPANDED);

      Glob periodStat = PeriodSeriesStat.findOrCreateForSeries(seriesId, repository);
      double amount = periodStat.get(PeriodSeriesStat.AMOUNT) +
                      Utils.zeroIfNull(seriesStat.get(SeriesStat.ACTUAL_AMOUNT));
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
                        value(PeriodSeriesStat.VISIBLE, visible),
                        value(PeriodSeriesStat.ACTIVE, isActive),
                        value(PeriodSeriesStat.TO_SET, false));

      stats.add(periodStat);
    }

    public GlobList getStats() {
      return new GlobList(stats);
    }
  }

  private void initToUpdateField() {
    boolean signpostCompleted = SignpostStatus.isInitialGuidanceCompleted(repository);
    for (Glob periodStat : repository.getAll(PeriodSeriesStat.TYPE)) {
      boolean value = PeriodSeriesStat.isForSeries(periodStat)
                      && !signpostCompleted
                      && isToUpdate(periodStat.get(PeriodSeriesStat.TARGET));
      repository.update(periodStat.getKey(), PeriodSeriesStat.TO_SET, value);
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
      hasObservedAmount |= Amounts.isNotZero(stat.get(SeriesStat.ACTUAL_AMOUNT));
      hasPlannedAmountToSet |= Amounts.isUnset(stat.get(SeriesStat.PLANNED_AMOUNT));
      if (hasObservedAmount && hasPlannedAmountToSet) {
        return true;
      }
    }
    return false;
  }
}
