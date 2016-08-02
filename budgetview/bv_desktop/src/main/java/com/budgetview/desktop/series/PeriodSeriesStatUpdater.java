package com.budgetview.desktop.series;

import com.budgetview.desktop.model.PeriodSeriesStat;
import com.budgetview.desktop.model.SeriesStat;
import com.budgetview.desktop.model.SeriesType;
import com.budgetview.desktop.series.utils.SeriesMatchers;
import com.budgetview.desktop.series.utils.SeriesOrGroup;
import com.budgetview.model.*;
import com.budgetview.shared.utils.Amounts;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import static com.budgetview.desktop.model.SeriesStat.isSeriesForAccount;
import static com.budgetview.desktop.model.SeriesStat.isSummaryForSeries;
import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.*;

public class PeriodSeriesStatUpdater implements GlobSelectionListener, ChangeSetListener {

  private GlobList selectedMonths = GlobList.EMPTY;
  private Integer selectedAccountId = null;
  private GlobMatcher selectedAccountSeriesMatcher = GlobMatchers.ALL;
  private GlobRepository repository;
  private Directory directory;

  public static void init(GlobRepository repository, Directory directory) {
    new PeriodSeriesStatUpdater(repository, directory);
  }

  private PeriodSeriesStatUpdater(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    directory.get(SelectionService.class).addListener(this, Month.TYPE, Account.TYPE);
    repository.addTrigger(this);
  }

  public void selectionUpdated(GlobSelection selection) {
    if (selection.isRelevantForType(Month.TYPE)) {
      selectedMonths = selection.getAll(Month.TYPE);
    }
    if (selection.isRelevantForType(Account.TYPE)) {
      GlobList accounts = selection.getAll(Account.TYPE);
      if (accounts.isEmpty()) {
        filterAllAccounts();
      }
      else {
        filterSingleAccount(accounts.getFirst().get(Account.ID));
      }
    }
    updateSelection();
  }

  public void filterAllAccounts() {
    selectedAccountId = null;
    selectedAccountSeriesMatcher = SeriesMatchers.seriesForGlobalBudget();
  }

  public void filterSingleAccount(final Integer accountId) {
    selectedAccountId = accountId;
    selectedAccountSeriesMatcher = SeriesMatchers.seriesForAccount(selectedAccountId);
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    boolean update = false;
    if (changeSet.containsDeletions(Account.TYPE)) {
      filterAllAccounts();
      update = true;
    }
    if (changeSet.containsCreationsOrDeletions(Month.TYPE)) {
      selectedMonths.removeAll(changeSet.getDeleted(Month.TYPE));
      update = true;
    }
    if (changeSet.containsUpdates(Series.GROUP) ||
        changeSet.containsUpdates(Series.TARGET_ACCOUNT) ||
        changeSet.containsUpdates(Series.BUDGET_AREA) ||
        changeSet.containsChanges(SeriesStat.TYPE) ||
        changeSet.containsChanges(SeriesGroup.TYPE) ||
        changeSet.containsChanges(CurrentMonth.KEY)) {
      update = true;
    }
    if (changeSet.containsUpdates(Account.ACCOUNT_TYPE)) {
      filterAllAccounts();
      update = true;
    }
    if (update) {
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
                               selectedAccountId != null ? isSeriesForAccount(selectedAccountId) : isSummaryForSeries()),
                           seriesStatFunctor
      );
      initToSetField();
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
    GlobList stats =
      repository.findByIndex(SeriesStat.MONTH_INDEX, newMonth)
        .filterSelf(fieldEquals(SeriesStat.ACCOUNT, selectedAccountId == null ? Account.ALL_SUMMARY_ACCOUNT_ID : selectedAccountId), repository);
    for (Glob stat : stats) {
      SeriesOrGroup seriesOrGroup = SeriesOrGroup.getFromStat(stat);
      Glob periodSeriesStat = PeriodSeriesStat.findUnique(seriesOrGroup.id, seriesOrGroup.type, repository);
      if (periodSeriesStat == null) {
        continue;
      }

      Double newValue = stat.get(SeriesStat.SUMMARY_AMOUNT);
      Glob previousStat = repository.find(seriesOrGroup.createSeriesStatKey(previousMonth));
      Double previousValue = previousStat == null ? null : previousStat.get(SeriesStat.SUMMARY_AMOUNT);
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
                                        value(PeriodSeriesStat.TARGET_TYPE, SeriesType.SERIES_GROUP.getId()),
                                        value(PeriodSeriesStat.BUDGET_AREA, series.get(Series.BUDGET_AREA)));
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
    for (Glob series : repository.getAll(Series.TYPE, selectedAccountSeriesMatcher)) {
      repository.create(PeriodSeriesStat.TYPE,
                        value(PeriodSeriesStat.TARGET_TYPE, SeriesType.SERIES.getId()),
                        value(PeriodSeriesStat.TARGET, series.get(Series.ID)),
                        value(PeriodSeriesStat.BUDGET_AREA, series.get(Series.BUDGET_AREA)),
                        value(PeriodSeriesStat.VISIBLE, true),
                        value(PeriodSeriesStat.ACTIVE, false));
    }
  }

  private class PeriodSeriesStatFunctor implements GlobFunctor {
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
      if (!selectedAccountSeriesMatcher.matches(series, remote)) {
        return;
      }

      Glob group = remote.findLinkTarget(series, Series.GROUP);
      boolean visible = (group == null) || group.isTrue(SeriesGroup.EXPANDED);
      double signModifier = Series.shouldInvertAmounts(series, selectedAccountId, repository) ? -1 : 1;

      Glob periodStat = PeriodSeriesStat.findOrCreateForSeries(seriesId, repository);
      double amount = (periodStat.get(PeriodSeriesStat.AMOUNT) +
                       Utils.zeroIfNull(getActual(seriesStat))) * signModifier;
      Double plannedAmount;
      if (periodStat.get(PeriodSeriesStat.PLANNED_AMOUNT) == null && getPlanned(seriesStat) == null) {
        plannedAmount = null;
      }
      else {
        plannedAmount = (periodStat.get(PeriodSeriesStat.PLANNED_AMOUNT, 0) +
                         seriesStat.get(SeriesStat.PLANNED_AMOUNT, 0)) * signModifier;
      }
      double pastRemaining = periodStat.get(PeriodSeriesStat.PAST_REMAINING);
      double futureRemaining = periodStat.get(PeriodSeriesStat.FUTURE_REMAINING);
      double pastOverrun = periodStat.get(PeriodSeriesStat.PAST_OVERRUN);
      double futureOverrun = periodStat.get(PeriodSeriesStat.FUTURE_OVERRUN);
      if (seriesStat.get(SeriesStat.MONTH) < monthId) {
        pastRemaining += seriesStat.get(SeriesStat.REMAINING_AMOUNT) * signModifier;
        pastOverrun += seriesStat.get(SeriesStat.OVERRUN_AMOUNT) * signModifier;
      }
      else {
        futureRemaining += seriesStat.get(SeriesStat.REMAINING_AMOUNT) * signModifier;
        futureOverrun += seriesStat.get(SeriesStat.OVERRUN_AMOUNT) * signModifier;
      }

      boolean isActive = Amounts.isNotZero(amount) || seriesStat.isTrue(SeriesStat.ACTIVE) || periodStat.isTrue(PeriodSeriesStat.ACTIVE);

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
                        value(PeriodSeriesStat.TO_SET, false)
      );

      stats.add(periodStat);
    }

    public GlobList getStats() {
      return new GlobList(stats);
    }
  }

  private void initToSetField() {
    boolean signpostCompleted = SignpostStatus.isOnboardingCompleted(repository);
    for (Glob periodStat : repository.getAll(PeriodSeriesStat.TYPE)) {
      boolean toSet = PeriodSeriesStat.isForSeries(periodStat)
                      && !signpostCompleted
                      && isToSet(periodStat.get(PeriodSeriesStat.TARGET));
      repository.update(periodStat.getKey(), PeriodSeriesStat.TO_SET, toSet);
    }
  }

  private boolean isToSet(Integer seriesId) {
    if (Series.UNCATEGORIZED_SERIES_ID.equals(seriesId)) {
      return false;
    }

    boolean hasActualAmount = false;
    boolean hasPlannedAmountToSet = false;
    GlobList activeStats =
      repository.findByIndex(SeriesStat.SERIES_INDEX, seriesId);
    if (!selectedMonths.isEmpty()) {
      activeStats.filterSelf(fieldContained(SeriesStat.MONTH, selectedMonths.getValueSet(Month.ID)), repository);
    }

    for (Glob stat : activeStats) {
      hasActualAmount |= Amounts.isNotZero(getActual(stat));
      hasPlannedAmountToSet |= Amounts.isUnset(getPlanned(stat));
      if (hasActualAmount && hasPlannedAmountToSet) {
        return true;
      }
    }
    return false;
  }

  private Double getPlanned(Glob stat) {
    return stat.get(SeriesStat.PLANNED_AMOUNT);
  }

  public Double getActual(Glob seriesStat) {
    return seriesStat.get(SeriesStat.ACTUAL_AMOUNT);
  }
}
