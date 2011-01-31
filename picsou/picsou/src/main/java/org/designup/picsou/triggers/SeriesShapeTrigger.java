package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.SeriesShape;
import org.designup.picsou.model.*;
import org.designup.picsou.model.util.Amounts;
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
    private Boolean positif;

    SeriesToReCompute(Integer seriesId, int monthId1, int monthId2, int monthId3, Boolean isPositif) {
      this.seriesId = seriesId;
      this.monthId1 = monthId1;
      this.monthId2 = monthId2;
      this.monthId3 = monthId3;
      positif = isPositif;
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
    if (changeSet.containsChanges(UserPreferences.KEY, UserPreferences.MONTH_FOR_PLANNED, UserPreferences.PERIOD_COUNT_FOR_PLANNED)) {
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
          if (budgetMonthId >= lastTransactionMonthId && previousMonthId >= lastTransactionMonthId) {
            return;
          }
          Glob glob = repository.get(key);
          addSeries(budgetMonthId, glob.get(Transaction.SERIES), seriesToReCompute, repository, lastTransactionMonthId, monthCount);
          addSeries(previousMonthId, values.contains(Transaction.SERIES) ?
                                     values.getPrevious(Transaction.SERIES) :
                                     glob.get(Transaction.SERIES),
                    seriesToReCompute, repository, lastTransactionMonthId, monthCount);
        }
        else if (values.contains(Transaction.SERIES)) {
          Glob glob = repository.get(key);
          Integer budgetMonthId = glob.get(Transaction.BUDGET_MONTH);
          if (budgetMonthId >= lastTransactionMonthId) {
            return;
          }
          addSeries(budgetMonthId, glob.get(Transaction.SERIES), seriesToReCompute, repository, lastTransactionMonthId, monthCount);
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Integer budgetMonthId = previousValues.get(Transaction.BUDGET_MONTH);
        if (budgetMonthId >= lastTransactionMonthId) {
          return;
        }
        addSeries(budgetMonthId, previousValues.get(Transaction.SERIES), seriesToReCompute,
                  repository, lastTransactionMonthId, monthCount);
      }
    });

    Integer periodInMonth = userPreference.get(UserPreferences.PERIOD_COUNT_FOR_PLANNED);
    for (SeriesToReCompute series : seriesToReCompute.values()) {
      final Glob seriesShape = repository.findOrCreate(Key.create(SeriesShape.TYPE, series.seriesId));
      TransactionGlobFunctor transactionGlobFunctor =
        new TransactionGlobFunctor(periodInMonth, monthCount, series);
      repository
        .findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, series.seriesId)
        .saveApply(transactionGlobFunctor, repository);
      Field[] fields = SeriesShape.TYPE.getFields();
      repository.startChangeSet();

      if (!transactionGlobFunctor.hasData()) {
        repository.delete(seriesShape.getKey());
      }
      else {
        for (int i = 1; i < fields.length - 1; i++) {
          repository.update(seriesShape.getKey(), fields[i], transactionGlobFunctor.getPercent(i));
        }
        repository.update(seriesShape.getKey(), SeriesShape.TOTAL, transactionGlobFunctor.getTotal());
      }
      repository.completeChangeSet();
    }
  }

  private void addSeries(Integer budgetMonthId, Integer seriesId, Map<Integer, SeriesToReCompute> seriesToReCompute,
                         GlobRepository repository, Integer lastTransactionMonthId, Integer monthCount) {
    if (!seriesToReCompute.containsKey(seriesId)) {
      SeriesToReCompute toReCompute = findValidMonth(repository, seriesId, lastTransactionMonthId, monthCount);
      if (budgetMonthId >= toReCompute.monthId3 && budgetMonthId <= toReCompute.monthId1) {
        seriesToReCompute.put(seriesId, toReCompute);
      }
    }
  }

  // 1 1 1 => 

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
//    if (changedTypes.contains(Series.TYPE)) {
//      Glob userPreference = repository.find(UserPreferences.KEY);
//      if (userPreference == null) {
//        return;
//      }
//      recomputeAll(repository, userPreference);
//    }
    // force compute all
  }

  private void recomputeAll(GlobRepository repository, Glob userPreference) {
    Glob currentMonth = repository.get(CurrentMonth.KEY);
    Integer lastTransactionMonthId = currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH);

    Integer periodInMonth = userPreference.get(UserPreferences.PERIOD_COUNT_FOR_PLANNED);
    Integer monthCount = userPreference.get(UserPreferences.MONTH_FOR_PLANNED);
    for (Glob series : repository.getAll(Series.TYPE)) {
      final Glob seriesShape = repository.findOrCreate(Key.create(SeriesShape.TYPE, series.get(Series.ID)));
      SeriesToReCompute seriesToReCompute = findValidMonth(repository, series.get(Series.ID), lastTransactionMonthId, userPreference.get(UserPreferences.MONTH_FOR_PLANNED));
      TransactionGlobFunctor transactionGlobFunctor =
        new TransactionGlobFunctor(periodInMonth, monthCount, seriesToReCompute);
      repository
        .findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, series.get(Series.ID))
        .saveApply(transactionGlobFunctor, repository);
      Field[] fields = SeriesShape.TYPE.getFields();
      repository.startChangeSet();

      for (int i = 1; i < fields.length - 1; i++) {
        repository.update(seriesShape.getKey(), fields[i], transactionGlobFunctor.getPercent(i));
      }
      repository.update(seriesShape.getKey(), SeriesShape.TOTAL, transactionGlobFunctor.getTotal());
      repository.completeChangeSet();
    }
  }

  private SeriesToReCompute findValidMonth(GlobRepository repository, Integer seriesId,
                                           Integer lastTransactionMonthId, int monthCount) {
    SelectActiveGlobFunctor globFunctor = new SelectActiveGlobFunctor(lastTransactionMonthId);
    repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
      .saveApply(globFunctor, repository);
    if (monthCount == 1) {
      return new SeriesToReCompute(seriesId, globFunctor.lastId1, globFunctor.lastId1, globFunctor.lastId1, globFunctor.isPositif);
    }
    if (monthCount == 2) {
      return new SeriesToReCompute(seriesId, globFunctor.lastId1, globFunctor.lastId2, globFunctor.lastId2, globFunctor.isPositif);
    }
    if (monthCount == 3) {
      return new SeriesToReCompute(seriesId, globFunctor.lastId1, globFunctor.lastId1, globFunctor.lastId3, globFunctor.isPositif);
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
    private int periodCount;
    private SeriesToReCompute series;

    public TransactionGlobFunctor(int periodCount, int monthCount, SeriesToReCompute series) {
      this.periodCount = periodCount;
      this.monthCount = monthCount;
      this.series = series;
      amountForMonth1 = new double[32];
      amountForMonth2 = new double[32];
      amountForMonth3 = new double[32];
    }


    public void run(Glob glob, GlobRepository repository) throws Exception {
      if (glob.isTrue(Transaction.MIRROR)){
        return;
      }
      int budgetMonthId = glob.get(Transaction.BUDGET_MONTH);
      double amount = glob.get(Transaction.AMOUNT, 0);
      if (series.positif != null && (amount > 0 ? !series.positif : series.positif)){
        return;
      }
      if (budgetMonthId == series.monthId1) {
        amountForMonth1[glob.get(Transaction.BUDGET_DAY)] += amount;
        total1 += amount;
      }
      else if (budgetMonthId == series.monthId2) {
        amountForMonth2[glob.get(Transaction.BUDGET_DAY)] += amount;
        total2 += amount;
      }
      else if (budgetMonthId == series.monthId1) {
        amountForMonth3[glob.get(Transaction.BUDGET_DAY)] += amount;
        total3 += amount;
      }
    }

    double getTotal(){
      double total = 0;
      if (monthCount >= 1){
        total = total1;
      }
      if (monthCount >= 2){
        total += total2;
      }
      if (monthCount >= 3){
        total += total3;
      }
      return total / monthCount;
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
      return (int)(100. * (percent) / monthCount);
    }

    public boolean hasData() {
      return total1 != 0 || total2 != 0 || total3 != 0;
    }
  }

  private static class SelectActiveGlobFunctor implements GlobFunctor {
    private Integer lastTransactionMonthId;
    private Boolean isPositif;
    private int lastId1 = -1;
    private int lastId2 = -1;
    private int lastId3 = -1;

    public SelectActiveGlobFunctor(Integer lastTransactionMonthId) {
      this.lastTransactionMonthId = lastTransactionMonthId;
    }

    public void run(Glob glob, GlobRepository repository) throws Exception {
      Integer monthId = glob.get(SeriesBudget.MONTH);
      if (monthId < lastTransactionMonthId && glob.isTrue(SeriesBudget.ACTIVE)) {
        if (monthId > lastId1) {
          lastId3 = lastId2;
          lastId2 = lastId1;
          lastId1 = monthId;
          isPositif = glob.get(SeriesBudget.AMOUNT) != null ? Boolean.valueOf(glob.get(SeriesBudget.AMOUNT) >= 0) : isPositif;
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
}
