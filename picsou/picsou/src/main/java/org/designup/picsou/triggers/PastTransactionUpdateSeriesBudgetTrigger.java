package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import java.util.Iterator;
import java.util.Set;

public class PastTransactionUpdateSeriesBudgetTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {

    changeSet.safeVisit(SeriesStat.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Integer currentMonthId = repository.get(CurrentMonth.KEY).get(CurrentMonth.MONTH_ID);
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
        Integer currentMonthId = repository.get(CurrentMonth.KEY).get(CurrentMonth.MONTH_ID);
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
        if (!values.contains(CurrentMonth.MONTH_ID)) {
          return;
        }
        GlobList series = repository.getAll(Series.TYPE);
        for (Glob oneSeries : series) {
          if (!oneSeries.get(Series.IS_AUTOMATIC)) {
            continue;
          }
          Integer currentMonthId = values.get(CurrentMonth.MONTH_ID);
          ReadOnlyGlobRepository.MultiFieldIndexed seriesBudgets =
            repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, oneSeries.get(Series.ID));
          Glob currentSeriesBudget = seriesBudgets.findByIndex(SeriesBudget.MONTH, currentMonthId)
            .getGlobs().getFirst();
          if (currentSeriesBudget == null || !currentSeriesBudget.get(SeriesBudget.ACTIVE)) {
            continue;
          }
          Integer previousActiveMonth = null;
          for (Glob seriesBudget : seriesBudgets.getGlobs().sort(SeriesBudget.MONTH)) {
            if (seriesBudget.get(SeriesBudget.ACTIVE) && seriesBudget.get(SeriesBudget.MONTH) < currentMonthId) {
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

  private void updateSeriesBudget(Integer currentMonthId, Glob series, Integer seriesId, Integer statMonthId, Double amount, GlobRepository repository) {
    if (!series.get(Series.IS_AUTOMATIC) || statMonthId > currentMonthId) {
      return;
    }

    Glob currentBudget = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
      .findByIndex(SeriesBudget.MONTH, statMonthId).getGlobs().getFirst();
    if (currentBudget == null || !currentBudget.get(SeriesBudget.ACTIVE)) {
      return;
    }


    if (statMonthId.equals(currentMonthId)) {
      ReadOnlyGlobRepository.MultiFieldIndexed index =
        repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId);
      GlobList seriesBudgets = index.getGlobs().sort(SeriesBudget.MONTH);
      Integer previousMonth = null;
      for (Glob glob : seriesBudgets) {
        if (glob.get(SeriesBudget.ACTIVE) && glob.get(SeriesBudget.MONTH) < statMonthId) {
          previousMonth = glob.get(SeriesBudget.MONTH);
        }
        if (glob.get(SeriesBudget.MONTH) > statMonthId) {
          break;
        }
      }
      if (previousMonth != null) {
        Glob previousStat =
          repository.findOrCreate(Key.create(SeriesStat.SERIES, seriesId,
                                             SeriesStat.MONTH, previousMonth));
        int multi = -1;
        if (BudgetArea.get(series.get(Series.BUDGET_AREA)).isIncome()) {
          multi = 1;
        }

        Double futureAmount;
        if (multi * amount > multi * previousStat.get(SeriesStat.AMOUNT)) {
          futureAmount = amount;
        }
        else {
          futureAmount = previousStat.get(SeriesStat.AMOUNT);
        }
        for (Glob budget : seriesBudgets) {
          if (budget.get(SeriesBudget.ACTIVE) && budget.get(SeriesBudget.MONTH) > currentMonthId) {
            repository.update(budget.getKey(), SeriesBudget.AMOUNT, futureAmount);
          }
        }
      }
      else {
        // c'est le premier
        for (Glob budget : seriesBudgets) {
          if (budget.get(SeriesBudget.ACTIVE) && budget.get(SeriesBudget.MONTH) >= currentMonthId) {
            repository.update(budget.getKey(), SeriesBudget.AMOUNT, amount);
          }
        }
      }
      return;
    }

    GlobList serieBudgets = repository
      .findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID))
      .getGlobs().sort(SeriesBudget.MONTH);

    boolean first = true;
    for (Iterator it = serieBudgets.iterator(); it.hasNext();) {
      Glob budget = (Glob)it.next();
      if (!budget.get(SeriesBudget.ACTIVE)) {
        continue;
      }

      if (first && statMonthId.equals(budget.get(SeriesBudget.MONTH))) {
        repository.update(budget.getKey(), SeriesBudget.AMOUNT, amount);
      }
      first = false;

      if (budget.get(SeriesBudget.MONTH) > statMonthId) {
        repository.update(budget.getKey(), SeriesBudget.AMOUNT, amount);
        Double futureAmount = amount;
        if (budget.get(SeriesBudget.MONTH).equals(currentMonthId)) {
          Glob currentSeriesStat =
            repository.findOrCreate(Key.create(SeriesStat.SERIES, seriesId,
                                               SeriesStat.MONTH, currentMonthId));
          int multi = -1;
          if (BudgetArea.get(series.get(Series.BUDGET_AREA)).isIncome()) {
            multi = 1;
          }
          if (multi * currentSeriesStat.get(SeriesStat.AMOUNT) > multi * amount) {
            futureAmount = currentSeriesStat.get(SeriesStat.AMOUNT);
          }
        }
        if (budget.get(SeriesBudget.MONTH) >= currentMonthId) {
          while (it.hasNext()) {
            Glob futurBudget = (Glob)it.next();
            if (futurBudget.get(SeriesBudget.ACTIVE)) {
              repository.update(futurBudget.getKey(), SeriesBudget.AMOUNT, futureAmount);
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
