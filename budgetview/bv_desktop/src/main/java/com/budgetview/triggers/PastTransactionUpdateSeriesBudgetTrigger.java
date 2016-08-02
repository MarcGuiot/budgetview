package com.budgetview.triggers;

import com.budgetview.desktop.model.SeriesStat;
import com.budgetview.desktop.model.SeriesType;
import com.budgetview.model.Account;
import com.budgetview.model.CurrentMonth;
import com.budgetview.model.Series;
import com.budgetview.model.SeriesBudget;
import com.budgetview.shared.utils.Amounts;
import org.globsframework.model.*;
import org.globsframework.utils.Utils;

import java.util.Iterator;

public class PastTransactionUpdateSeriesBudgetTrigger extends AbstractChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {

    changeSet.safeVisit(SeriesStat.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Integer currentMonthId = repository.get(CurrentMonth.KEY).get(CurrentMonth.LAST_TRANSACTION_MONTH);
        Glob series = repository.find(Key.create(Series.TYPE, key.get(SeriesStat.TARGET)));
        if (series == null) {
          return;
        }
        if (values.get(SeriesStat.ACCOUNT) == Account.ALL_SUMMARY_ACCOUNT_ID) {
          Integer seriesId = values.get(SeriesStat.TARGET);
          Integer statMonthId = values.get(SeriesStat.MONTH);
          Double amount = values.get(SeriesStat.ACTUAL_AMOUNT);
          updateSeriesBudget(currentMonthId, series, seriesId, statMonthId, amount, repository);
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Integer currentMonthId = repository.get(CurrentMonth.KEY).get(CurrentMonth.LAST_TRANSACTION_MONTH);
        Glob series = repository.find(Key.create(Series.TYPE, key.get(SeriesStat.TARGET)));
        if (series == null) {
          return;
        }
        Glob seriesStat = repository.get(key);
        if (values.get(SeriesStat.ACCOUNT) == Account.ALL_SUMMARY_ACCOUNT_ID){
          Integer seriesId = seriesStat.get(SeriesStat.TARGET);
          Integer statMonthId = seriesStat.get(SeriesStat.MONTH);
          Double amount = seriesStat.get(SeriesStat.ACTUAL_AMOUNT);
          updateSeriesBudget(currentMonthId, series, seriesId, statMonthId, amount, repository);
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });

    changeSet.safeVisit(CurrentMonth.KEY, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (!values.contains(CurrentMonth.LAST_TRANSACTION_MONTH)) {
          return;
        }
        GlobList series = repository.getAll(Series.TYPE);
        for (Glob oneSeries : series) {
          if (!oneSeries.isTrue(Series.IS_AUTOMATIC)) {
            continue;
          }
          Integer currentMonthId = values.get(CurrentMonth.LAST_TRANSACTION_MONTH);
          ReadOnlyGlobRepository.MultiFieldIndexed seriesBudgets =
            repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, oneSeries.get(Series.ID));
          Glob currentSeriesBudget = seriesBudgets.findByIndex(SeriesBudget.MONTH, currentMonthId)
            .getGlobs().getFirst();
          if (currentSeriesBudget == null || !currentSeriesBudget.isTrue(SeriesBudget.ACTIVE)) {
            continue;
          }
          Integer previousActiveMonth = null;
          for (Glob seriesBudget : seriesBudgets.getGlobs().sort(SeriesBudget.MONTH)) {
            if (seriesBudget.isTrue(SeriesBudget.ACTIVE) && seriesBudget.get(SeriesBudget.MONTH) < currentMonthId) {
              previousActiveMonth = seriesBudget.get(SeriesBudget.MONTH);
            }
          }
            Integer seriesId = oneSeries.get(Series.ID);
            if (previousActiveMonth == null) {
              Glob seriesStat = SeriesStat.findOrCreateForSeries(oneSeries.get(Series.ID), currentMonthId, repository);
              if (seriesStat.get(SeriesStat.ACCOUNT) == Account.ALL_SUMMARY_ACCOUNT_ID) {
                Double amount = seriesStat.get(SeriesStat.ACTUAL_AMOUNT);
                updateSeriesBudget(currentMonthId, oneSeries, seriesId, currentMonthId, amount, repository);
              }
            }
            else {
              Glob seriesStat = SeriesStat.findOrCreateForSeries(oneSeries.get(Series.ID), previousActiveMonth, repository);
              if (seriesStat.get(SeriesStat.ACCOUNT) == Account.ALL_SUMMARY_ACCOUNT_ID) {
                Double amount = seriesStat.get(SeriesStat.ACTUAL_AMOUNT);
                updateSeriesBudget(currentMonthId, oneSeries, seriesId, previousActiveMonth, amount, repository);
              }
            }
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
  }

  private void updateSeriesBudget(Integer currentMonthId, Glob series, Integer seriesId, Integer statMonthId,
                                  Double actualAmount, GlobRepository repository) {
    ReadOnlyGlobRepository.MultiFieldIndexed budgetIndex =
      repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId);
    Glob currentBudget = budgetIndex.findByIndex(SeriesBudget.MONTH, statMonthId).getGlobs().getFirst();
    if (currentBudget == null || !currentBudget.isTrue(SeriesBudget.ACTIVE)) {
      return;
    }
    repository.update(currentBudget.getKey(), SeriesBudget.ACTUAL_AMOUNT, actualAmount);

    if (!series.isTrue(Series.IS_AUTOMATIC) || statMonthId > currentMonthId) {
      return;
    }

    if (statMonthId.equals(currentMonthId)) {
      GlobList seriesBudgets = budgetIndex.getGlobs().sort(SeriesBudget.MONTH);
      Integer previousMonth = null;
      int firstMonthWithObserved = Integer.MAX_VALUE;
      for (Glob budget : seriesBudgets) {
        {
          Glob currentSeriesStat =
            SeriesStat.findOrCreateForSeries(seriesId, budget.get(SeriesBudget.MONTH), repository);
          if (firstMonthWithObserved == Integer.MAX_VALUE && Amounts.isNotZero(currentSeriesStat.get(SeriesStat.ACTUAL_AMOUNT))) {
            firstMonthWithObserved = budget.get(SeriesBudget.MONTH);
          }
        }
        if (budget.isTrue(SeriesBudget.ACTIVE) && budget.get(SeriesBudget.MONTH) < statMonthId) {
          previousMonth = budget.get(SeriesBudget.MONTH);
        }
        if (budget.get(SeriesBudget.MONTH) > statMonthId) {
          break;
        }
      }
      if (previousMonth != null && firstMonthWithObserved < statMonthId) {
        Glob previousStat = repository.findOrCreate(Key.create(SeriesStat.ACCOUNT, Account.ALL_SUMMARY_ACCOUNT_ID,
                                                               SeriesStat.TARGET, seriesId,
                                                               SeriesStat.TARGET_TYPE, SeriesType.SERIES.getId(),
                                                               SeriesStat.MONTH, previousMonth));
        // Si on a un changement de signe : ex on passe de -10 a 5 on propage le changement vers 5
        Double futureAmount;
        if ((previousStat.get(SeriesStat.ACTUAL_AMOUNT) == null
             || Amounts.isNullOrZero(previousStat.get(SeriesStat.ACTUAL_AMOUNT))
             || (Amounts.isNotZero(actualAmount)
                 && Amounts.isNotZero(previousStat.get(SeriesStat.ACTUAL_AMOUNT))
                 && !Amounts.sameSign(previousStat.get(SeriesStat.ACTUAL_AMOUNT), actualAmount))
             || Math.abs(Utils.zeroIfNull(actualAmount)) > Math.abs(previousStat.get(SeriesStat.ACTUAL_AMOUNT)))) {
          futureAmount = actualAmount;
        }
        else {
          futureAmount = previousStat.get(SeriesStat.ACTUAL_AMOUNT);
        }
        for (Glob budget : seriesBudgets) {
          if (budget.isTrue(SeriesBudget.ACTIVE) && budget.get(SeriesBudget.MONTH) > currentMonthId) {
            repository.update(budget.getKey(), SeriesBudget.PLANNED_AMOUNT, Utils.zeroIfNull(futureAmount));
          }
        }
      }
      else {
        // c'est le premier
        for (Glob budget : seriesBudgets) {
          if (budget.isTrue(SeriesBudget.ACTIVE) && budget.get(SeriesBudget.MONTH) >= currentMonthId) {
            repository.update(budget.getKey(), SeriesBudget.PLANNED_AMOUNT, Utils.zeroIfNull(actualAmount));
          }
        }
      }
      return;
    }

    GlobList serieBudgets = repository
      .findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID))
      .getGlobs().sort(SeriesBudget.MONTH);

    double firstMonthAmout = 0;
    int firstMonthWithObserved = 0;
    for (Iterator it = serieBudgets.iterator(); it.hasNext(); ) {
      Glob budget = (Glob)it.next();
      if (!budget.isTrue(SeriesBudget.ACTIVE)) {
        continue;
      }

      Glob currentSeriesStat =
        repository.findOrCreate(Key.create(SeriesStat.ACCOUNT, Account.ALL_SUMMARY_ACCOUNT_ID,
                                           SeriesStat.TARGET, seriesId,
                                           SeriesStat.TARGET_TYPE, SeriesType.SERIES.getId(),
                                           SeriesStat.MONTH, budget.get(SeriesBudget.MONTH)));
      if (firstMonthWithObserved == 0 && Amounts.isNotZero(currentSeriesStat.get(SeriesStat.ACTUAL_AMOUNT))) {
        firstMonthWithObserved = budget.get(SeriesBudget.MONTH);
        firstMonthAmout = currentSeriesStat.get(SeriesStat.ACTUAL_AMOUNT);
      }

      if (firstMonthWithObserved == statMonthId && statMonthId.equals(budget.get(SeriesBudget.MONTH))) {
        repository.update(budget.getKey(), SeriesBudget.PLANNED_AMOUNT, Utils.zeroIfNull(firstMonthAmout));
        actualAmount = firstMonthAmout;
      }

      if (firstMonthWithObserved == 0) {
        repository.update(budget.getKey(), SeriesBudget.PLANNED_AMOUNT, 0.);
        continue;
      }
      // Le premier mois reel de la series, c-a-d celui dans laquel il y a un observedAmount != 0

      if (budget.get(SeriesBudget.MONTH) > statMonthId) {
        if (firstMonthWithObserved == budget.get(SeriesBudget.MONTH)) {
          repository.update(budget.getKey(), SeriesBudget.PLANNED_AMOUNT, Utils.zeroIfNull(firstMonthAmout));
        }
        else {
          repository.update(budget.getKey(), SeriesBudget.PLANNED_AMOUNT, Utils.zeroIfNull(actualAmount));
        }
        Double futureAmount = actualAmount;
        if (budget.get(SeriesBudget.MONTH).equals(currentMonthId)) {
          if (Amounts.isNullOrZero(actualAmount)
              || (Amounts.isNotZero(currentSeriesStat.get(SeriesStat.ACTUAL_AMOUNT))
                  && !Amounts.sameSign(currentSeriesStat.get(SeriesStat.ACTUAL_AMOUNT), actualAmount))
              || Math.abs(Utils.zeroIfNull(currentSeriesStat.get(SeriesStat.ACTUAL_AMOUNT))) > Math.abs(actualAmount)) {
            futureAmount = currentSeriesStat.get(SeriesStat.ACTUAL_AMOUNT);
          }
        }
        if (budget.get(SeriesBudget.MONTH) >= currentMonthId) {
          while (it.hasNext()) {
            Glob futureBudget = (Glob)it.next();
            if (futureBudget.isTrue(SeriesBudget.ACTIVE)) {
              repository.update(futureBudget.getKey(), SeriesBudget.PLANNED_AMOUNT, Utils.zeroIfNull(futureAmount));
            }
          }
        }
        return;
      }
    }
  }
}
