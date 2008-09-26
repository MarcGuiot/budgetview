package org.designup.picsou.triggers;

import org.designup.picsou.model.*;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import java.util.Iterator;
import java.util.Set;

public class AutomaticSeriesBudgetTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {

    changeSet.safeVisit(Series.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(Series.IS_AUTOMATIC)) {
          if (values.get(Series.IS_AUTOMATIC)) {
            updateSeriesBudget(key, repository);
          }
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
  }

  public static void updateSeriesBudget(Key seriesKey, GlobRepository repository) {
    final Glob currentMonth = repository.get(CurrentMonth.KEY);
    Integer seriesId = seriesKey.get(Series.ID);
    GlobList seriesBudgets =
      repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
        .getGlobs().sort(SeriesBudget.MONTH);
    Iterator<Glob> transactions = repository.findByIndex(Transaction.SERIES_INDEX, seriesId)
      .sort(TransactionComparator.ASCENDING).iterator();
    Glob currentTransaction = transactions.hasNext() ? transactions.next() : null;
    Double amount = 0.;
    boolean firstUpdate = false;
    for (Glob seriesBudget : seriesBudgets) {
      if (!seriesBudget.get(SeriesBudget.ACTIVE)) {
        repository.update(seriesBudget.getKey(),
                          FieldValue.value(SeriesBudget.AMOUNT, 0.),
                          FieldValue.value(SeriesBudget.OVERRUN_AMOUNT, 0.));
      }
      else {
        repository.update(seriesBudget.getKey(),
                          FieldValue.value(SeriesBudget.AMOUNT, amount),
                          FieldValue.value(SeriesBudget.OVERRUN_AMOUNT, 0.));
        Double previousAmount = amount;
        if (seriesBudget.get(SeriesBudget.MONTH) <= currentMonth.get(CurrentMonth.MONTH_ID)) {
          amount = 0.;
          while (currentTransaction != null &&
                 currentTransaction.get(Transaction.MONTH).equals(seriesBudget.get(SeriesBudget.MONTH))) {
            amount += currentTransaction.get(Transaction.AMOUNT);
            currentTransaction = transactions.hasNext() ? transactions.next() : null;
          }
        }
        if (seriesBudget.get(SeriesBudget.MONTH).equals(currentMonth.get(CurrentMonth.MONTH_ID))) {
          int multi = -1;
          if (BudgetArea.get(repository.get(seriesKey).get(Series.BUDGET_AREA)).isIncome()) {
            multi = 1;
          }
          if (multi * amount < multi * previousAmount) {
            amount = previousAmount;
          }
        }
        if (!firstUpdate) {
          repository.update(seriesBudget.getKey(),
                            FieldValue.value(SeriesBudget.AMOUNT, amount),
                            FieldValue.value(SeriesBudget.OVERRUN_AMOUNT, 0.));
          firstUpdate = true;
        }
      }
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
