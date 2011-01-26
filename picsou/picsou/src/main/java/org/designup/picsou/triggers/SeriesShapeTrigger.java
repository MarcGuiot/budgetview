package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.SeriesShape;
import org.designup.picsou.model.*;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobFunctor;

import java.util.HashSet;
import java.util.Set;

public class SeriesShapeTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    Glob userPreference = repository.find(UserPreferences.KEY);
    if (userPreference == null) {
      return;
    }

    final Set<Integer> seriesList = new HashSet<Integer>();
    if (changeSet.containsChanges(UserPreferences.KEY, UserPreferences.MONTH_FOR_PLANNED, UserPreferences.PERIOD_COUNT_FOR_PLANNED)){
      seriesList.addAll(repository.getAll(Series.TYPE).getSortedSet(Series.ID));
    }

    Glob currentMonth = repository.get(CurrentMonth.KEY);
    if (currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH) == 0){
      return;
    }
    final Integer lastMonthId = Month.offset(currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH), -1);
    final Integer firstMonthId = Month.offset(currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH),
                                              -(userPreference.get(UserPreferences.MONTH_FOR_PLANNED)));
    changeSet.safeVisit(Transaction.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Integer budgetMonthId = values.get(Transaction.BUDGET_MONTH);
        if (budgetMonthId >= firstMonthId && budgetMonthId <= lastMonthId) {
          seriesList.add(values.get(Transaction.SERIES));
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(Transaction.BUDGET_MONTH)) {
          Integer budgetMonthId = values.get(Transaction.BUDGET_MONTH);
          Integer previousMonthId = values.getPrevious(Transaction.BUDGET_MONTH);
          if (budgetMonthId >= lastMonthId && budgetMonthId <= lastMonthId ||
              previousMonthId >= lastMonthId && previousMonthId <= lastMonthId) {
            Glob glob = repository.get(key);
            seriesList.add(glob.get(Transaction.SERIES));
            if (values.contains(Transaction.SERIES)) {
              seriesList.add(values.getPrevious(Transaction.SERIES));
            }
          }
        }
        else if (values.contains(Transaction.SERIES)) {
          Glob glob = repository.get(key);
          Integer budgetMonthId = glob.get(Transaction.BUDGET_MONTH);
          if (budgetMonthId >= firstMonthId && budgetMonthId <= lastMonthId) {
            seriesList.add(values.get(Transaction.SERIES));
            seriesList.add(values.getPrevious(Transaction.SERIES));
          }
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Integer budgetMonthId = previousValues.get(Transaction.BUDGET_MONTH);
        if (budgetMonthId >= firstMonthId && budgetMonthId <= lastMonthId) {
          seriesList.add(previousValues.get(Transaction.SERIES));
        }
      }
    });

    Integer periodInMonth = userPreference.get(UserPreferences.PERIOD_COUNT_FOR_PLANNED);
    for (Integer series : seriesList) {
      final Glob seriesShape = repository.findOrCreate(Key.create(SeriesShape.TYPE, series));
      TransactionGlobFunctor transactionGlobFunctor = new TransactionGlobFunctor(periodInMonth, firstMonthId, lastMonthId);
      repository
        .findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, series)
        .saveApply(transactionGlobFunctor, repository);
      Field[] fields = SeriesShape.TYPE.getFields();
      repository.startChangeSet();

      for (int i = 1; i < fields.length; i++) {
        repository.update(seriesShape.getKey(), fields[i], transactionGlobFunctor.getPercent(i));
      }
      repository.completeChangeSet();
    }
  }

  // 1 1 1 => 

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Series.TYPE)) {
      Glob userPreference = repository.find(UserPreferences.KEY);
      if (userPreference == null) {
        return;
      }
      Glob currentMonth = repository.get(CurrentMonth.KEY);
      final Integer lastMonthId = Month.offset(currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH), -1);
      final Integer firstMonthId = Month.offset(currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH),
                                                -(userPreference.get(UserPreferences.MONTH_FOR_PLANNED)));

      Integer periodInMonth = userPreference.get(UserPreferences.PERIOD_COUNT_FOR_PLANNED);
      for (Glob series : repository.getAll(Series.TYPE)) {
        final Glob seriesShape = repository.findOrCreate(Key.create(SeriesShape.TYPE, series.get(Series.ID)));
        TransactionGlobFunctor transactionGlobFunctor = new TransactionGlobFunctor(periodInMonth, firstMonthId, lastMonthId);
        repository
          .findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, series.get(Series.ID))
          .saveApply(transactionGlobFunctor, repository);
        Field[] fields = SeriesShape.TYPE.getFields();
        repository.startChangeSet();

        for (int i = 1; i < fields.length; i++) {
          repository.update(seriesShape.getKey(), fields[i], transactionGlobFunctor.getPercent(i));
        }
        repository.completeChangeSet();
      }
    }
    // force compute all
  }

  private static class TransactionGlobFunctor implements GlobFunctor {
    double[] total;
    private int monthCount;
    private int firstMonthId;
    private int lastMonthId;
    private final double[][] amountForMonth;
    private int periodCount;

    public TransactionGlobFunctor(int periodCount, int firstMonthId, int lastMonthId) {
      this.periodCount = periodCount;
      this.monthCount = Month.distance(firstMonthId, lastMonthId) + 1;
      this.firstMonthId = firstMonthId;
      this.lastMonthId = lastMonthId;
      total = new double[monthCount];
      amountForMonth = new double[monthCount][32];
    }

    public void run(Glob glob, GlobRepository repository) throws Exception {
      Integer budgetMonthId = glob.get(Transaction.BUDGET_MONTH);
      if (budgetMonthId >= firstMonthId && budgetMonthId <= lastMonthId) {
        double amount = glob.get(Transaction.AMOUNT, 0);
        amountForMonth[budgetMonthId - firstMonthId][glob.get(Transaction.DAY)] += amount;
        total[budgetMonthId - firstMonthId] += amount;
      }
    }

    int getPercent(int period) {
      double percent = 0;
      for (int j = SeriesShape.getBegin(periodCount, period);
           j <= SeriesShape.getEnd(periodCount, period) && period <= periodCount; j++) {
        for (int i = 0; i < monthCount; i++) {
          percent += total[i] == 0 ? 0. : amountForMonth[i][j] / total[i];
        }
      }
      return (int)(100. * (percent) / monthCount);
    }
  }
}
