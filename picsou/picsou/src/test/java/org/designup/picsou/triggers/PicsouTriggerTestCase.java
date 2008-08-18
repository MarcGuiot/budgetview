package org.designup.picsou.triggers;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.utils.PicsouTestCase;
import org.globsframework.model.Key;
import org.globsframework.utils.Dates;

public abstract class PicsouTriggerTestCase extends PicsouTestCase {

  protected void setUp() throws Exception {
    TimeService.setCurrentDate(Dates.parseMonth("2008/08"));
    TimeService.setLastAvailableTransactionMonthId(200808);
    super.setUp();
    directory.add(new TimeService());
    repository.addTrigger(new LastTransactionToTimeServiceTrigger(directory));
    repository.addTrigger(new MonthsToSeriesBudgetTrigger());
    repository.addTrigger(new SeriesBudgetTrigger());
    repository.addTrigger(new SeriesBudgetUpdateOccasionnalTrigger());
    repository.addTrigger(new SeriesBudgetUpdateTransactionTrigger(directory));
    repository.addTrigger(new TransactionPlannedTrigger(directory));
    repository.addTrigger(new MonthStatTrigger(repository));
    final SeriesStatTrigger seriesStatTrigger = new SeriesStatTrigger();
    repository.addTrigger(seriesStatTrigger);
    repository.addTrigger(new OccasionalSeriesStatTrigger());
    repository.create(Key.create(Series.TYPE, 0),
                      org.globsframework.model.FieldValue.value(Series.AMOUNT, 0.),
                      org.globsframework.model.FieldValue.value(Series.BUDGET_AREA, BudgetArea.OCCASIONAL_EXPENSES.getId()));
  }

  protected void createSeries(int seriesId, double amount) {
    repository.create(Key.create(Series.TYPE, seriesId),
                      org.globsframework.model.FieldValue.value(Series.AMOUNT, amount),
                      org.globsframework.model.FieldValue.value(Series.BUDGET_AREA, BudgetArea.RECURRING_EXPENSES.getId()));
  }

  protected void createMonth(int... monthId) {
    for (int i : monthId) {
      repository.create(Key.create(Month.TYPE, i));
    }
  }
}
