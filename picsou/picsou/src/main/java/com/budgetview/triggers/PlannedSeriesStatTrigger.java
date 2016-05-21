package com.budgetview.triggers;

import com.budgetview.gui.accounts.utils.AccountMatchers;
import com.budgetview.gui.model.SeriesStat;
import com.budgetview.model.Account;
import com.budgetview.model.Month;
import com.budgetview.model.Series;
import com.budgetview.model.SeriesBudget;
import com.budgetview.shared.utils.Amounts;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.collections.Pair;

import java.util.Set;

import static org.globsframework.model.FieldValue.value;

public class PlannedSeriesStatTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(SeriesBudget.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        for (Integer accountId : repository.getAll(Account.TYPE, AccountMatchers.userOrAllAccounts()).getValues(Account.ID)) {
          Key seriesStatKey = SeriesStat.createKeyForSeries(accountId, values.get(SeriesBudget.SERIES), values.get(SeriesBudget.MONTH));
          Glob seriesStat = repository.findOrCreate(seriesStatKey);

          Boolean isActive = values.isTrue(SeriesBudget.ACTIVE);
          Pair<Double, Double> remainingAndOverrun = computeRemainingAndOverrun(values, isActive, seriesStat.get(SeriesStat.ACTUAL_AMOUNT));
          repository.update(seriesStatKey,
                            value(SeriesStat.PLANNED_AMOUNT, getActiveAmount(values, isActive)),
                            value(SeriesStat.REMAINING_AMOUNT, remainingAndOverrun.getFirst()),
                            value(SeriesStat.OVERRUN_AMOUNT, remainingAndOverrun.getSecond()),
                            value(SeriesStat.ACTIVE, isActive));
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(SeriesBudget.PLANNED_AMOUNT) || values.contains(SeriesBudget.ACTIVE)) {
          updateSeriesStat(repository.get(key), repository);
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
       SeriesStat.deleteAllForSeriesAndMonth(previousValues.get(SeriesBudget.SERIES), previousValues.get(SeriesBudget.MONTH), repository);
      }
    });

    changeSet.safeVisit(SeriesStat.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Glob seriesBudget = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, key.get(SeriesStat.TARGET))
          .findByIndex(SeriesBudget.MONTH, key.get(SeriesStat.MONTH)).getGlobs().getFirst();
        if (seriesBudget != null) {
          updateSeriesStat(seriesBudget, repository);
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
  }

  private void updateSeriesStat(final Glob seriesBudget, GlobRepository repository) {
    Integer seriesId = seriesBudget.get(SeriesBudget.SERIES);
    if (!repository.contains(Key.create(Series.TYPE, seriesId))) {
      return;
    }

    for (Integer accountId : repository.getAll(Account.TYPE, AccountMatchers.userOrAllAccounts()).getValues(Account.ID)) {
      doUpdateStat(seriesBudget, repository, seriesId, accountId);
    }
  }

  private void doUpdateStat(Glob seriesBudget, GlobRepository repository, Integer seriesId, Integer accountId) {
    Key seriesStatKey = SeriesStat.createKeyForSeries(accountId, seriesId, seriesBudget.get(SeriesBudget.MONTH));
    Glob seriesStat = repository.findOrCreate(seriesStatKey);

    Boolean isActive = seriesBudget.isTrue(SeriesBudget.ACTIVE);
    Pair<Double, Double> remainingAndOverrun = computeRemainingAndOverrun(seriesBudget, isActive, seriesStat.get(SeriesStat.ACTUAL_AMOUNT));
    repository.update(seriesStatKey,
                      value(SeriesStat.PLANNED_AMOUNT, getActiveAmount(seriesBudget, isActive)),
                      value(SeriesStat.REMAINING_AMOUNT, remainingAndOverrun.getFirst()),
                      value(SeriesStat.OVERRUN_AMOUNT, remainingAndOverrun.getSecond()),
                      value(SeriesStat.ACTIVE, isActive));
  }

  // Do not allow autoboxing - amount can be null
  private Double getActiveAmount(FieldValues seriesBudget, Boolean active) {
    return active ? seriesBudget.get(SeriesBudget.PLANNED_AMOUNT) : new Double(0.00);
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    for (Glob month : repository.getAll(Month.TYPE)) {
      for (Glob series : repository.getAll(Series.TYPE)) {
        repository.findOrCreate(SeriesStat.createKeyForSeries(series.get(Series.ID), month.get(Month.ID)));
      }
    }
    GlobList seriesBudgets = repository.getAll(SeriesBudget.TYPE);
    for (Glob seriesBudget : seriesBudgets) {
      for (Integer accountId : repository.getAll(Account.TYPE, AccountMatchers.userOrAllAccounts()).getValues(Account.ID)) {
        doUpdateStat(seriesBudget, repository, seriesBudget.get(SeriesBudget.SERIES), accountId);
      }
    }
  }

  private Pair<Double, Double> computeRemainingAndOverrun(FieldValues seriesBudget, Boolean isActive, Double actualValue) {
    Double plannedAmount = isActive ? seriesBudget.get(SeriesBudget.PLANNED_AMOUNT, 0) : 0.00;
    Double obervedAmount = Amounts.zeroIfNull(actualValue);
    double remaining = 0;
    double overrun = 0;

    if (plannedAmount > 0) {
      if (obervedAmount > plannedAmount) {
        overrun = obervedAmount - plannedAmount;
      }
      else {
        remaining = plannedAmount - obervedAmount;
      }
    }
    else if (plannedAmount < 0) {
      if (obervedAmount < plannedAmount) {
        overrun = obervedAmount - plannedAmount;
      }
      else {
        remaining = plannedAmount - obervedAmount;
      }
    }
    else {
      overrun = obervedAmount;
    }
    return new Pair<Double, Double>(remaining, overrun);
  }
}
