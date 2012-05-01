package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.model.util.Amounts;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.Utils;

import java.util.Iterator;
import java.util.Set;

public class PastTransactionUpdateSeriesBudgetTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {

    changeSet.safeVisit(SeriesStat.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Integer currentMonthId = repository.get(CurrentMonth.KEY).get(CurrentMonth.LAST_TRANSACTION_MONTH);
        Glob series = repository.find(Key.create(Series.TYPE, key.get(SeriesStat.SERIES)));
        if (series == null) {
          return;
        }
        Integer seriesId = values.get(SeriesStat.SERIES);
        Integer statMonthId = values.get(SeriesStat.MONTH);
        Double amount = values.get(SeriesStat.AMOUNT);

        updateSeriesBudget(currentMonthId, series, seriesId, statMonthId, amount, repository);
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Integer currentMonthId = repository.get(CurrentMonth.KEY).get(CurrentMonth.LAST_TRANSACTION_MONTH);
        Glob series = repository.find(Key.create(Series.TYPE, key.get(SeriesStat.SERIES)));
        if (series == null) {
          return;
        }
        Glob seriesStat = repository.get(key);
        Integer seriesId = seriesStat.get(SeriesStat.SERIES);
        Integer statMonthId = seriesStat.get(SeriesStat.MONTH);
        Double amount = seriesStat.get(SeriesStat.AMOUNT);
        updateSeriesBudget(currentMonthId, series, seriesId, statMonthId, amount, repository);
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
            Glob seriesStat = repository.findOrCreate(Key.create(SeriesStat.SERIES, oneSeries.get(Series.ID),
                                                                 SeriesStat.MONTH, currentMonthId));
            Double amount = seriesStat.get(SeriesStat.AMOUNT);
            updateSeriesBudget(currentMonthId, oneSeries, seriesId, currentMonthId, amount, repository);
          }
          else {
            Glob seriesStat = repository.findOrCreate(Key.create(SeriesStat.SERIES, oneSeries.get(Series.ID),
                                                                 SeriesStat.MONTH, previousActiveMonth));
            Double amount = seriesStat.get(SeriesStat.AMOUNT);
            updateSeriesBudget(currentMonthId, oneSeries, seriesId, previousActiveMonth, amount, repository);
          }
        }

      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
  }

  private void updateSeriesBudget(Integer currentMonthId, Glob series, Integer seriesId, Integer statMonthId,
                                  Double observedAmount, GlobRepository repository) {
    ReadOnlyGlobRepository.MultiFieldIndexed budgetIndex =
      repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId);
    Glob currentBudget = budgetIndex.findByIndex(SeriesBudget.MONTH, statMonthId).getGlobs().getFirst();
    if (currentBudget == null || !currentBudget.isTrue(SeriesBudget.ACTIVE)) {
      return;
    }
    repository.update(currentBudget.getKey(), SeriesBudget.OBSERVED_AMOUNT, observedAmount);

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
            repository.findOrCreate(Key.create(SeriesStat.SERIES, seriesId,
                                               SeriesStat.MONTH, budget.get(SeriesBudget.MONTH)));
          if (firstMonthWithObserved == Integer.MAX_VALUE && Amounts.isNotZero(currentSeriesStat.get(SeriesStat.AMOUNT))) {
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
        Glob previousStat = repository.findOrCreate(Key.create(SeriesStat.SERIES, seriesId,
                                                               SeriesStat.MONTH, previousMonth));
        // Si on a un changement de signe : ex on passe de -10 a 5 on propage le changement vers 5
        Double futureAmount;
        if ((previousStat.get(SeriesStat.AMOUNT) == null
             || Amounts.isNullOrZero(previousStat.get(SeriesStat.AMOUNT))
             || (Amounts.isNotZero(observedAmount)
                 && Amounts.isNotZero(previousStat.get(SeriesStat.AMOUNT))
                 && !Amounts.sameSign(previousStat.get(SeriesStat.AMOUNT), observedAmount))
             || Math.abs(Utils.zeroIfNull(observedAmount)) > Math.abs(previousStat.get(SeriesStat.AMOUNT)))) {
          futureAmount = observedAmount;
        }
        else {
          futureAmount = previousStat.get(SeriesStat.AMOUNT);
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
            repository.update(budget.getKey(), SeriesBudget.PLANNED_AMOUNT, Utils.zeroIfNull(observedAmount));
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
        repository.findOrCreate(Key.create(SeriesStat.SERIES, seriesId,
                                           SeriesStat.MONTH, budget.get(SeriesBudget.MONTH)));
      if (firstMonthWithObserved == 0 && Amounts.isNotZero(currentSeriesStat.get(SeriesStat.AMOUNT))) {
        firstMonthWithObserved = budget.get(SeriesBudget.MONTH);
        firstMonthAmout = currentSeriesStat.get(SeriesStat.AMOUNT);
      }

      if (firstMonthWithObserved == statMonthId && statMonthId.equals(budget.get(SeriesBudget.MONTH))) {
        repository.update(budget.getKey(), SeriesBudget.PLANNED_AMOUNT, Utils.zeroIfNull(firstMonthAmout));
        observedAmount = firstMonthAmout;
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
          repository.update(budget.getKey(), SeriesBudget.PLANNED_AMOUNT, Utils.zeroIfNull(observedAmount));
        }
        Double futureAmount = observedAmount;
        if (budget.get(SeriesBudget.MONTH).equals(currentMonthId)) {
          if (Amounts.isNullOrZero(observedAmount)
              || (Amounts.isNotZero(currentSeriesStat.get(SeriesStat.AMOUNT))
                  && !Amounts.sameSign(currentSeriesStat.get(SeriesStat.AMOUNT), observedAmount))
              || Math.abs(Utils.zeroIfNull(currentSeriesStat.get(SeriesStat.AMOUNT))) > Math.abs(observedAmount)) {
            futureAmount = currentSeriesStat.get(SeriesStat.AMOUNT);
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

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
