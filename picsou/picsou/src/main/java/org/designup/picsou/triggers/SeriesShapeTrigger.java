package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.SeriesShape;
import org.designup.picsou.model.*;
import com.budgetview.shared.utils.Amounts;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobFunctor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SeriesShapeTrigger implements ChangeSetListener {

  static class SeriesToReCompute {
    Integer seriesId;
    Integer monthId1;
    Integer monthId2;
    Integer monthId3;

    SeriesToReCompute(Integer seriesId, int monthId1, int monthId2, int monthId3) {
      this.seriesId = seriesId;
      this.monthId1 = monthId1;
      this.monthId2 = monthId2;
      this.monthId3 = monthId3;
    }
  }

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    Glob userPreference = repository.find(UserPreferences.KEY);
    if (userPreference == null) {
      return;
    }
    final Glob currentMonth = repository.get(CurrentMonth.KEY);
    final Integer lastTransactionMonthId = currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH);
    if (lastTransactionMonthId == 0) {
      return;
    }
    if (changeSet.containsChanges(UserPreferences.KEY, UserPreferences.MONTH_FOR_PLANNED, UserPreferences.PERIOD_COUNT_FOR_PLANNED, UserPreferences.MULTIPLE_PLANNED)) {
      recomputeAll(repository, userPreference);
      return;
    }

    final Map<Integer, SeriesToReCompute> seriesToReCompute = new HashMap<Integer, SeriesToReCompute>();
    final Integer monthCount = userPreference.get(UserPreferences.MONTH_FOR_PLANNED);
    changeSet.safeVisit(Transaction.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Integer budgetMonthId = values.get(Transaction.BUDGET_MONTH);
        if (budgetMonthId >= lastTransactionMonthId) {
          return;
        }
        Integer seriesId = values.get(Transaction.SERIES);
        if (Series.UNCATEGORIZED_SERIES_ID.equals(seriesId)) {
          return;
        }
        addSeries(budgetMonthId, seriesId, seriesToReCompute, repository, lastTransactionMonthId, monthCount);
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(Transaction.BUDGET_MONTH)) {
          Integer budgetMonthId = values.get(Transaction.BUDGET_MONTH);
          Integer previousMonthId = values.getPrevious(Transaction.BUDGET_MONTH);
          Glob glob = repository.get(key);
          addSeries(budgetMonthId, glob.get(Transaction.SERIES), seriesToReCompute, repository, lastTransactionMonthId, monthCount);
          addSeries(previousMonthId, values.contains(Transaction.SERIES) ?
                                     values.getPrevious(Transaction.SERIES) :
                                     glob.get(Transaction.SERIES),
                    seriesToReCompute, repository, lastTransactionMonthId, monthCount);
        }
        else if (values.contains(Transaction.SERIES) || values.contains(Transaction.AMOUNT)) {
          Glob glob = repository.get(key);
          Integer budgetMonthId = glob.get(Transaction.BUDGET_MONTH);
          if (budgetMonthId > lastTransactionMonthId) {
            return;
          }
          if (budgetMonthId.equals(lastTransactionMonthId)) {
            Integer previousId = glob.get(Transaction.SERIES);
            if (values.contains(Transaction.SERIES)) {
              previousId = values.getPrevious(Transaction.SERIES);
            }
            for (Integer seriesId : new Integer[]{glob.get(Transaction.SERIES),
                                                  previousId}) {
              Glob budget = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
                .findByIndex(SeriesBudget.MONTH, budgetMonthId)
                .getGlobs().getFirst();
              Glob seriesShape = repository.find(Key.create(SeriesShape.TYPE, seriesId));
              if (budget != null) {
                boolean smallDiff = isSmallDiff(budget);
                if ((smallDiff ||(seriesShape != null && lastTransactionMonthId.equals(seriesShape.get(SeriesShape.LAST_MONTH))))
                    && budget.get(SeriesBudget.ACTIVE)) {
                  addSeries(budgetMonthId, glob.get(Transaction.SERIES), seriesToReCompute, repository,
                            lastTransactionMonthId, monthCount);
                }
                if (!smallDiff && seriesShape != null && lastTransactionMonthId.equals(seriesShape.get(SeriesShape.LAST_MONTH))) {
                  repository.delete(seriesShape);
                }
              }
            }
          }
          else {
            addSeries(budgetMonthId, glob.get(Transaction.SERIES), seriesToReCompute, repository, lastTransactionMonthId, monthCount);
            if (values.contains(Transaction.SERIES)) {
              addSeries(budgetMonthId, values.getPrevious(Transaction.SERIES), seriesToReCompute, repository, lastTransactionMonthId, monthCount);
            }
          }
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Integer budgetMonthId = previousValues.get(Transaction.BUDGET_MONTH);
        if (budgetMonthId > lastTransactionMonthId) {
          return;
        }
        addSeries(budgetMonthId, previousValues.get(Transaction.SERIES), seriesToReCompute,
                  repository, lastTransactionMonthId, monthCount);
      }
    });

    changeSet.safeVisit(SeriesBudget.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        addFromSeriesBudget(key, repository, lastTransactionMonthId, seriesToReCompute, monthCount);
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(SeriesBudget.PLANNED_AMOUNT)
            || values.contains(SeriesBudget.ACTIVE)
            || values.contains(SeriesBudget.ACTUAL_AMOUNT)) {
          addFromSeriesBudget(key, repository, lastTransactionMonthId, seriesToReCompute, monthCount);
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        addSeries(previousValues.get(SeriesBudget.MONTH),
                  previousValues.get(SeriesBudget.SERIES),
                  seriesToReCompute, repository, lastTransactionMonthId, monthCount);
      }
    });
    changeSet.safeVisit(Series.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        addSeries(-1, key.get(Series.ID), seriesToReCompute,
                  repository, lastTransactionMonthId, monthCount);
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(Series.FORCE_SINGLE_OPERATION) || values.contains(Series.FORCE_SINGLE_OPERATION_DAY)) {
          addSeries(-1, key.get(Series.ID), seriesToReCompute,
                    repository, lastTransactionMonthId, monthCount);
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });

    Integer periodInMonth = userPreference.get(UserPreferences.PERIOD_COUNT_FOR_PLANNED);
    for (SeriesToReCompute series : seriesToReCompute.values()) {
      Glob seriesData = repository.find(Key.create(Series.TYPE, series.seriesId));
      if (seriesData != null) {
        final Glob seriesShape = repository.findOrCreate(Key.create(SeriesShape.TYPE, series.seriesId));
        if (seriesData.isTrue(Series.FORCE_SINGLE_OPERATION)) {
          setFixedDateToSeriesShape(repository, seriesData, seriesShape);
        }
        else {
          TransactionGlobFunctor transactionGlobFunctor =
            new TransactionGlobFunctor(repository, periodInMonth, monthCount, series);
          repository
            .findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, series.seriesId)
            .saveApply(transactionGlobFunctor, repository);
          Field[] fields = SeriesShape.TYPE.getFields();
          repository.startChangeSet();

          if (!transactionGlobFunctor.hasData()) {
            repository.delete(seriesShape);
          }
          else {
            transactionGlobFunctor.updateForSingleOp();

            for (int i = 1; i < SeriesShape.TOTAL.getIndex(); i++) {
              repository.update(seriesShape.getKey(), fields[i], transactionGlobFunctor.getPercent(i));
            }
            repository.update(seriesShape.getKey(), SeriesShape.TOTAL, transactionGlobFunctor.getTotal());
            repository.update(seriesShape.getKey(), SeriesShape.LAST_MONTH, series.monthId1);
            repository.update(seriesShape.getKey(), SeriesShape.FIXED_DATE, null);
          }
          repository.completeChangeSet();
        }
      }
    }
  }

  private void addFromSeriesBudget(Key key, GlobRepository repository, Integer lastTransactionMonthId,
                                   Map<Integer, SeriesToReCompute> seriesToReCompute, Integer monthCount) {
    Glob budget = repository.get(key);
    Integer budgetMonthId = budget.get(SeriesBudget.MONTH);
    if (budgetMonthId > lastTransactionMonthId) {
      return;
    }
    if (budgetMonthId.equals(lastTransactionMonthId)) {
      Glob seriesShape = repository.find(Key.create(SeriesShape.TYPE, budget.get(SeriesBudget.SERIES)));
      boolean smallDiff = isSmallDiff(budget);
      if ((smallDiff || (seriesShape != null && lastTransactionMonthId.equals(seriesShape.get(SeriesShape.LAST_MONTH)))) &&
          budget.get(SeriesBudget.ACTIVE)) {
        addSeries(budgetMonthId, budget.get(SeriesBudget.SERIES), seriesToReCompute, repository, lastTransactionMonthId, monthCount);
      }
      // la series doit etre supprimé, elle sera recreer si besoin par le add
      if (!smallDiff && seriesShape != null && lastTransactionMonthId.equals(seriesShape.get(SeriesShape.LAST_MONTH))) {
        repository.delete(seriesShape);
      }
    }
    else {
      addSeries(budgetMonthId, budget.get(SeriesBudget.SERIES), seriesToReCompute, repository, lastTransactionMonthId, monthCount);
    }
  }

  private void addSeries(Integer budgetMonthId, Integer seriesId, Map<Integer, SeriesToReCompute> seriesToReCompute,
                         GlobRepository repository, Integer lastTransactionMonthId, Integer monthCount) {
    if (!seriesToReCompute.containsKey(seriesId)) {
      SeriesToReCompute toReCompute = findValidMonth(repository, seriesId, lastTransactionMonthId, monthCount);
      if (budgetMonthId == -1 || (budgetMonthId >= toReCompute.monthId3 && budgetMonthId <= toReCompute.monthId1)) {
        seriesToReCompute.put(seriesId, toReCompute);
      }
    }
  }

  // 1 1 1 => 

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Series.TYPE)) {
      Glob userPreference = repository.find(UserPreferences.KEY);
      if (userPreference == null) {
        return;
      }
      recomputeAll(repository, userPreference);
    }
    // force compute all
  }

  private void recomputeAll(GlobRepository repository, Glob userPreference) {
    repository.startChangeSet();
    try {
      Glob currentMonth = repository.get(CurrentMonth.KEY);
      Integer lastTransactionMonthId = currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH);

      Integer periodInMonth = userPreference.get(UserPreferences.PERIOD_COUNT_FOR_PLANNED);
      Integer monthCount = userPreference.get(UserPreferences.MONTH_FOR_PLANNED);
      repository.deleteAll(SeriesShape.TYPE);
      for (Glob series : repository.getAll(Series.TYPE)) {
        if (series.isTrue(Series.FORCE_SINGLE_OPERATION)) {
          final Glob seriesShape = repository.findOrCreate(Key.create(SeriesShape.TYPE, series.get(Series.ID)));
          setFixedDateToSeriesShape(repository, series, seriesShape);
        }
        else {
          SeriesToReCompute seriesToReCompute = findValidMonth(repository, series.get(Series.ID), lastTransactionMonthId, userPreference.get(UserPreferences.MONTH_FOR_PLANNED));
          TransactionGlobFunctor transactionGlobFunctor =
            new TransactionGlobFunctor(repository, periodInMonth, monthCount, seriesToReCompute);
          // utiliser le mois dans l'index?
          repository
            .findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, series.get(Series.ID))
            .saveApply(transactionGlobFunctor, repository);
          Field[] fields = SeriesShape.TYPE.getFields();

          if (transactionGlobFunctor.hasData()) {
            transactionGlobFunctor.updateForSingleOp();
            final Glob seriesShape = repository.findOrCreate(Key.create(SeriesShape.TYPE, series.get(Series.ID)));
            for (int i = 1; i < SeriesShape.TOTAL.getIndex(); i++) {
              repository.update(seriesShape.getKey(), fields[i], transactionGlobFunctor.getPercent(i));
            }
            repository.update(seriesShape.getKey(), SeriesShape.TOTAL, transactionGlobFunctor.getTotal());
            repository.update(seriesShape.getKey(), SeriesShape.LAST_MONTH, seriesToReCompute.monthId1);
          }
        }
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }

  private void setFixedDateToSeriesShape(GlobRepository repository, Glob series, Glob seriesShape) {
    Field[] fields = SeriesShape.TYPE.getFields();
    for (int i = 1; i < SeriesShape.TOTAL.getIndex(); i++) {
      repository.update(seriesShape.getKey(), fields[i], 0);
    }
    repository.update(seriesShape.getKey(), SeriesShape.FIXED_DATE, series.get(Series.FORCE_SINGLE_OPERATION_DAY));
  }

  private SeriesToReCompute findValidMonth(GlobRepository repository, Integer seriesId,
                                           Integer lastTransactionMonthId, int monthCount) {
    SelectActiveGlobFunctor globFunctor = new SelectActiveGlobFunctor(lastTransactionMonthId);
    repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
      .saveApply(globFunctor, repository);
    if (monthCount == 1) {
      return new SeriesToReCompute(seriesId, globFunctor.lastId1, globFunctor.lastId1, globFunctor.lastId1);
    }
    if (monthCount == 2) {
      return new SeriesToReCompute(seriesId, globFunctor.lastId1, globFunctor.lastId2, globFunctor.lastId2);
    }
    if (monthCount == 3) {
      return new SeriesToReCompute(seriesId, globFunctor.lastId1, globFunctor.lastId2, globFunctor.lastId3);
    }
    throw new RuntimeException("only 3 month max");
  }

  private static class TransactionGlobFunctor implements GlobFunctor {
    private int monthCount;
    private final double[] amountForMonth1;
    private final double[] amountForMonth2;
    private final double[] amountForMonth3;
    private double total1;
    private double total2;
    private double total3;
    private int opCount1;
    private int opCount2;
    private int opCount3;
    private int periodCount;
    private SeriesToReCompute seriesToReCompute;
    private boolean positif;

    public TransactionGlobFunctor(GlobRepository repository, int periodCount, int monthCount, SeriesToReCompute seriesToReCompute) {
      Glob series = repository.find(Key.create(Series.TYPE, seriesToReCompute.seriesId));
      BudgetArea budgetArea = BudgetArea.get(series.get(Series.BUDGET_AREA));
      if (budgetArea == BudgetArea.SAVINGS) {
        positif = series.get(Series.TO_ACCOUNT).equals(series.get(Series.TARGET_ACCOUNT));
      }
      else {
        positif = budgetArea.isIncome();
      }
      this.periodCount = periodCount;
      this.monthCount = monthCount;
      this.seriesToReCompute = seriesToReCompute;
      amountForMonth1 = new double[32];
      amountForMonth2 = new double[32];
      amountForMonth3 = new double[32];
    }


    public void run(Glob glob, GlobRepository repository) throws Exception {
      if (glob.isTrue(Transaction.PLANNED)) {
        return;
      }
      int budgetMonthId = glob.get(Transaction.BUDGET_MONTH);
      double amount = glob.get(Transaction.AMOUNT, 0);
      if ((amount > 0 != positif)) {
        return;
      }
      if (budgetMonthId == seriesToReCompute.monthId1) {
        amountForMonth1[glob.get(Transaction.BUDGET_DAY)] += amount;
        total1 += amount;
        opCount1++;
      }
      else if (budgetMonthId == seriesToReCompute.monthId2) {
        amountForMonth2[glob.get(Transaction.BUDGET_DAY)] += amount;
        total2 += amount;
        opCount2++;
      }
      else if (budgetMonthId == seriesToReCompute.monthId3) {
        amountForMonth3[glob.get(Transaction.BUDGET_DAY)] += amount;
        total3 += amount;
        opCount3++;
      }
    }

    double getTotal() {
      double total = 0;
      if (monthCount >= 1) {
        total = total1;
      }
      if (monthCount >= 2) {
        total += total2;
      }
      if (monthCount >= 3) {
        total += total3;
      }
      return total / getMonthCount();
    }

    int getPercent(int period) {
      double percent = 0;
      for (int j = SeriesShape.getBegin(periodCount, period);
           j <= SeriesShape.getEnd(periodCount, period) && period <= periodCount; j++) {
        if (monthCount >= 1) {
          percent += total1 == 0 ? 0. : amountForMonth1[j] / total1;
        }
        if (monthCount >= 2) {
          percent += total2 == 0 ? 0. : amountForMonth2[j] / total2;
        }
        if (monthCount >= 3) {
          percent += total3 == 0 ? 0. : amountForMonth3[j] / total3;
        }
      }
      return (int)(100. * (percent) / getMonthCount());
    }

    private double getMonthCount() {
      if (monthCount == 1) {
        return 1;
      }
      if (monthCount == 2) {
        return (total1 == 0 ? 0 : 1) + (total2 == 0 ? 0 : 1);
      }
      if (monthCount == 3) {
        return (total1 == 0 ? 0 : 1) + (total2 == 0 ? 0 : 1) + (total3 == 0 ? 0 : 1);
      }
      throw new RuntimeException("no " + monthCount);
    }

    public boolean hasData() {
      return total1 != 0 || total2 != 0 || total3 != 0;
    }

    public void updateForSingleOp() {
      if (monthCount != 1) {
        if (opCount1 <= 1 && opCount2 <= 1 && opCount3 <= 1) {
          int day1 = getDay(amountForMonth1);
          int day2 = getDay(amountForMonth2);
          int day3 = getDay(amountForMonth3);
          int day = (day1 + day2 + day3) / (opCount1 + opCount2 + opCount3);
          if (day1 != 0) {
            if (day != day1) {
              amountForMonth1[day] = amountForMonth1[day1];
              amountForMonth1[day1] = 0;
            }
          }
          else if (day2 != 0) {
            amountForMonth1[day] = amountForMonth2[day2];
          }
          else if (day3 != 0) {
            amountForMonth1[day] = amountForMonth2[day3];
          }
          monthCount = 1;
        }
      }
    }

    private int getDay(final double[] days) {
      for (int j = 0; j < days.length; j++) {
        if (!Amounts.isNearZero(days[j])) {
          return j;
        }
      }
      return 0;
    }
  }

  private static class SelectActiveGlobFunctor implements GlobFunctor {
    private Integer lastTransactionMonthId;
    private int lastId1 = -1;
    private int lastId2 = -1;
    private int lastId3 = -1;

    public SelectActiveGlobFunctor(Integer lastTransactionMonthId) {
      this.lastTransactionMonthId = lastTransactionMonthId;
    }

    public void run(Glob glob, GlobRepository repository) throws Exception {
      Integer monthId = glob.get(SeriesBudget.MONTH);
      if ((monthId < lastTransactionMonthId ||
           (monthId.equals(lastTransactionMonthId) && isSmallDiff(glob))) && glob.isTrue(SeriesBudget.ACTIVE)) {
        if (monthId > lastId1) {
          lastId3 = lastId2;
          lastId2 = lastId1;
          lastId1 = monthId;
          return;
        }
        if (monthId > lastId2) {
          lastId3 = lastId2;
          lastId2 = monthId;
          return;
        }
        if (monthId > lastId3) {
          lastId3 = monthId;
        }
      }
    }
  }

  static private boolean isSmallDiff(Glob budget) {
    if (Amounts.isNearZero(Amounts.zeroIfNull(budget.get(SeriesBudget.PLANNED_AMOUNT)))) {
      return false;
    }
    double v = Amounts.zeroIfNull(budget.get(SeriesBudget.ACTUAL_AMOUNT)) / (budget.get(SeriesBudget.PLANNED_AMOUNT));
    return Math.abs(v) > 0.90;
  }
}
