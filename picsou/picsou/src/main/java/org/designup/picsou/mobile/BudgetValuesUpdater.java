package org.designup.picsou.mobile;

import com.budgetview.shared.model.BudgetAreaEntity;
import com.budgetview.shared.model.BudgetAreaValues;
import com.budgetview.shared.model.SeriesValues;
import com.budgetview.shared.model.TransactionValues;
import org.designup.picsou.gui.budget.summary.TotalBudgetAreaAmounts;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.util.HashMap;
import java.util.Map;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.fieldIn;

public class BudgetValuesUpdater {

  private static BudgetArea[] BUDGET_AREAS;

  static {
    BUDGET_AREAS = new BudgetArea[BudgetArea.INCOME_AND_EXPENSES_AREAS.length + 1];
    for (int i = 0; i < BudgetArea.INCOME_AND_EXPENSES_AREAS.length; i++) {
      BUDGET_AREAS[i] = BudgetArea.INCOME_AND_EXPENSES_AREAS[i];
    }
    BUDGET_AREAS[BUDGET_AREAS.length - 1] = BudgetArea.UNCATEGORIZED;
  }

  public static void process(GlobRepository sourceRepository, GlobRepository targetRepository) {
    targetRepository.startChangeSet();
    try {
      targetRepository.deleteAll(BudgetAreaValues.TYPE, SeriesValues.TYPE);

      final int currentMonthId = CurrentMonth.getCurrentMonth(sourceRepository);
      Integer[] months = new Integer[]{
        Month.previous(currentMonthId),
        currentMonthId,
        Month.next(currentMonthId)
      };
      TotalBudgetAreaAmounts totalAmounts = new TotalBudgetAreaAmounts() {
        protected Integer getCurrentMonths() {
          return currentMonthId;
        }
      };

      for (BudgetArea budgetArea : BUDGET_AREAS) {
        targetRepository.create(BudgetAreaEntity.TYPE,
                                value(BudgetAreaEntity.ID, budgetArea.getId()),
                                value(BudgetAreaEntity.LABEL, budgetArea.getLabel()));
      }

      for (Glob budgetStat : sourceRepository.getAll(BudgetStat.TYPE, fieldIn(BudgetStat.MONTH, months))) {
        for (BudgetArea budgetArea : BudgetArea.INCOME_AND_EXPENSES_AREAS) {
          totalAmounts.update(new GlobList(budgetStat), budgetArea);
          targetRepository.create(BudgetAreaValues.TYPE,
                                  value(BudgetAreaValues.BUDGET_AREA, budgetArea.getId()),
                                  value(BudgetAreaValues.MONTH, budgetStat.get(BudgetStat.MONTH)),
                                  value(BudgetAreaValues.REMAINDER, totalAmounts.getFutureRemaining() + totalAmounts.getPastRemaining()),
                                  value(BudgetAreaValues.OVERRUN, totalAmounts.getFutureOverrun() + totalAmounts.getPastOverrun()),
                                  value(BudgetAreaValues.INITIALLY_PLANNED, totalAmounts.getInitiallyPlanned()),
                                  value(BudgetAreaValues.ACTUAL, totalAmounts.getActual()));
        }
      }

      Map<SeriesValueKey, Integer> seriesValuesMap = new HashMap<SeriesValueKey, Integer>();

      for (Glob seriesStat : sourceRepository.getAll(SeriesStat.TYPE, fieldIn(SeriesStat.MONTH, months))) {
        Glob series = sourceRepository.findLinkTarget(seriesStat, SeriesStat.SERIES);

        if (seriesStat.isTrue(SeriesStat.ACTIVE)) {
          Glob seriesValues = targetRepository.create(SeriesValues.TYPE,
                                                      value(SeriesValues.NAME, series.get(Series.NAME)),
                                                      value(SeriesValues.MONTH, seriesStat.get(SeriesStat.MONTH)),
                                                      value(SeriesValues.BUDGET_AREA, series.get(Series.BUDGET_AREA)),
                                                      value(SeriesValues.AMOUNT, seriesStat.get(SeriesStat.AMOUNT)),
                                                      value(SeriesValues.PLANNED_AMOUNT, seriesStat.get(SeriesStat.PLANNED_AMOUNT)),
                                                      value(SeriesValues.OVERRUN_AMOUNT, seriesStat.get(SeriesStat.OVERRUN_AMOUNT)),
                                                      value(SeriesValues.REMAINING_AMOUNT, seriesStat.get(SeriesStat.REMAINING_AMOUNT)));
          seriesValuesMap.put(new SeriesValueKey(series.get(Series.ID), seriesStat.get(SeriesStat.MONTH)),
                              seriesValues.get(SeriesValues.ID));
        }
      }

      GlobList transactions = sourceRepository
        .getAll(Transaction.TYPE, fieldIn(Transaction.BUDGET_MONTH, months))
        .sort(TransactionComparator.DESCENDING_BANK_SPLIT_AFTER);

      int sequenceNumber = 0;
      for (Glob transaction : transactions) {
        Integer seriesValuesId = seriesValuesMap.get(new SeriesValueKey(transaction.get(Transaction.SERIES),
                                                                        transaction.get(Transaction.BUDGET_MONTH)));
        if (seriesValuesId == null) {
          throw new UnexpectedApplicationState("No series values found for " + transaction);
        }

        targetRepository.create(TransactionValues.TYPE,
                                value(TransactionValues.AMOUNT, transaction.get(Transaction.AMOUNT)),
                                value(TransactionValues.LABEL, transaction.get(Transaction.LABEL)),
                                value(TransactionValues.BANK_DAY, transaction.get(Transaction.BANK_DAY)),
                                value(TransactionValues.BANK_MONTH, transaction.get(Transaction.BANK_MONTH)),
                                value(TransactionValues.SERIES_VALUES, seriesValuesId),
                                value(TransactionValues.SEQUENCE_NUMBER, sequenceNumber++)
        );
      }
    }
    finally {
      targetRepository.completeChangeSet();
    }
  }

  private static class SeriesValueKey {
    private Integer seriesId;
    private Integer monthId;

    private SeriesValueKey(Integer seriesId, Integer monthId) {
      this.seriesId = seriesId;
      this.monthId = monthId;
    }

    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      SeriesValueKey that = (SeriesValueKey)o;

      if (monthId != null ? !monthId.equals(that.monthId) : that.monthId != null) {
        return false;
      }
      if (seriesId != null ? !seriesId.equals(that.seriesId) : that.seriesId != null) {
        return false;
      }

      return true;
    }

    public int hashCode() {
      int result = seriesId != null ? seriesId.hashCode() : 0;
      result = 31 * result + (monthId != null ? monthId.hashCode() : 0);
      return result;
    }
  }
}
