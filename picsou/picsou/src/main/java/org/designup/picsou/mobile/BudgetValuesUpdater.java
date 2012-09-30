package org.designup.picsou.mobile;

import com.budgetview.shared.model.BudgetAreaEntity;
import com.budgetview.shared.model.BudgetAreaValues;
import com.budgetview.shared.model.SeriesValues;
import org.designup.picsou.gui.budget.summary.TotalBudgetAreaAmounts;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Series;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;

import static org.globsframework.model.FieldValue.value;

public class BudgetValuesUpdater {
  public static void process(GlobRepository sourceRepository, GlobRepository targetRepository) {
    targetRepository.startChangeSet();
    try {
      targetRepository.deleteAll(BudgetAreaValues.TYPE, SeriesValues.TYPE);

      final int currentMonthId = CurrentMonth.getCurrentMonth(sourceRepository);
      TotalBudgetAreaAmounts totalAmounts = new TotalBudgetAreaAmounts() {
        protected Integer getCurrentMonths() {
          return currentMonthId;
        }
      };

      for (BudgetArea budgetArea : BudgetArea.INCOME_AND_EXPENSES_AREAS) {
        targetRepository.create(BudgetAreaEntity.TYPE,
                                value(BudgetAreaEntity.ID, budgetArea.getId()),
                                value(BudgetAreaEntity.LABEL, budgetArea.getLabel()));
      }

      for (Glob budgetStat : sourceRepository.getAll(BudgetStat.TYPE)) {
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

      for (Glob seriesStat : sourceRepository.getAll(SeriesStat.TYPE)) {
        Glob series = sourceRepository.findLinkTarget(seriesStat, SeriesStat.SERIES);

        if (seriesStat.isTrue(SeriesStat.ACTIVE)) {
          targetRepository.create(SeriesValues.TYPE,
                                  value(SeriesValues.NAME, series.get(Series.NAME)),
                                  value(SeriesValues.MONTH, seriesStat.get(SeriesStat.MONTH)),
                                  value(SeriesValues.BUDGET_AREA, series.get(Series.BUDGET_AREA)),
                                  value(SeriesValues.AMOUNT, seriesStat.get(SeriesStat.AMOUNT)),
                                  value(SeriesValues.PLANNED_AMOUNT, seriesStat.get(SeriesStat.PLANNED_AMOUNT)),
                                  value(SeriesValues.OVERRUN_AMOUNT, seriesStat.get(SeriesStat.OVERRUN_AMOUNT)),
                                  value(SeriesValues.REMAINING_AMOUNT, seriesStat.get(SeriesStat.REMAINING_AMOUNT))
                                  );
        }
      }
    }
    finally {
      targetRepository.completeChangeSet();
    }
  }
}
