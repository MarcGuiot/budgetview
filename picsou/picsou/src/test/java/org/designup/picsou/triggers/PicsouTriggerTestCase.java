package org.designup.picsou.triggers;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.PicsouTestCase;
import org.globsframework.model.FieldValue;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Dates;

public abstract class PicsouTriggerTestCase extends PicsouTestCase {
  protected static final int FREE_SERIES_ID = 100;
  protected static final int ENVELOPPE_SERIES_ID = 101;
  protected static final int INCOME_SERIES_ID = 102;

  protected void setUp() throws Exception {
    TimeService.setCurrentDate(Dates.parseMonth("2008/09"));
    super.setUp();
    directory.add(new TimeService());
    repository.addTrigger(new CurrentMonthTrigger());
    repository.addTrigger(new MonthsToSeriesBudgetTrigger(directory));
    repository.addTrigger(new SeriesBudgetTrigger());
    repository.addTrigger(new ObservedSeriesStatTrigger());
    repository.addTrigger(new PastTransactionUpdateSeriesBudgetTrigger());
    repository.addTrigger(new TransactionPlannedTrigger());
    repository.addTrigger(new MonthStatTrigger());
    repository.addTrigger(new PlannedSeriesStatTrigger());
    repository.addTrigger(new OccasionalSeriesStatTrigger());
    repository.create(CurrentMonth.KEY,
                      FieldValue.value(CurrentMonth.LAST_TRANSACTION_MONTH, 200808),
                      FieldValue.value(CurrentMonth.LAST_TRANSACTION_DAY, 1)
    );
    repository.create(Key.create(Series.TYPE, Series.OCCASIONAL_SERIES_ID),
                      value(Series.PROFILE_TYPE, ProfileType.IRREGULAR.getId()),
                      value(Series.IS_AUTOMATIC, false),
                      value(Series.BUDGET_AREA, BudgetArea.OCCASIONAL.getId()));
    repository.create(Key.create(Series.TYPE, Series.UNCATEGORIZED_SERIES_ID),
                      value(Series.PROFILE_TYPE, ProfileType.IRREGULAR.getId()),
                      value(Series.IS_AUTOMATIC, false),
                      value(Series.BUDGET_AREA, BudgetArea.OCCASIONAL.getId()));
  }

  protected void createSeries(int seriesId, double amount) {
    repository.create(Key.create(Series.TYPE, seriesId),
                      value(Series.INITIAL_AMOUNT, amount),
                      value(Series.PROFILE_TYPE, ProfileType.CUSTOM.getId()),
                      value(Series.IS_AUTOMATIC, false),
                      value(Series.NAME, "aSeries"),
                      value(Series.BUDGET_AREA, BudgetArea.RECURRING.getId()));
  }

  protected void createMonth(int... monthId) {
    for (int i : monthId) {
      repository.create(Key.create(Month.TYPE, i));
    }
  }

  protected Integer[] getBudgetId(int seriesId) {
    return repository.getAll(SeriesBudget.TYPE, GlobMatchers.fieldEquals(SeriesBudget.SERIES, seriesId))
      .sort(SeriesBudget.MONTH)
      .getValues(SeriesBudget.ID);
  }

  protected Integer[] getPlannedTransaction(Integer... seriesId) {
    GlobMatcher globMatcher = GlobMatchers.fieldEquals(Transaction.PLANNED, true);
    for (Integer series : seriesId) {
      globMatcher = GlobMatchers.and(globMatcher, GlobMatchers.fieldEquals(Transaction.SERIES, series));
    }
    return repository.getAll(Transaction.TYPE, globMatcher).sort(Transaction.MONTH).getValues(Transaction.ID);
  }

  protected void createIncomeSeries() {
    Key seriesKey = Key.create(Series.TYPE, INCOME_SERIES_ID);
    repository.create(seriesKey,
                      value(Series.INITIAL_AMOUNT, 2000.),
                      value(Series.DAY, 4),
                      value(Series.BUDGET_AREA, BudgetArea.INCOME.getId()),
                      value(Series.NAME, "salaire"),
                      value(Series.PROFILE_TYPE, ProfileType.CUSTOM.getId()),
                      value(Series.IS_AUTOMATIC, false),
                      value(Series.DEFAULT_CATEGORY, MasterCategory.INCOME.getId()));
  }

  protected void createEnveloppeSeries() {
    Key key = Key.create(Series.TYPE, ENVELOPPE_SERIES_ID);
    repository.create(key,
                      value(Series.INITIAL_AMOUNT, -1000.),
                      value(Series.BUDGET_AREA, BudgetArea.ENVELOPES.getId()),
                      value(Series.DAY, 25),
                      value(Series.NAME, "courses"),
                      value(Series.PROFILE_TYPE, ProfileType.CUSTOM.getId()),
                      value(Series.IS_AUTOMATIC, false),
                      value(Series.DEFAULT_CATEGORY, MasterCategory.FOOD.getId()));
  }

  protected void createFreeSeries() {
    Key seriesKey = Key.create(Series.TYPE, FREE_SERIES_ID);
    repository.create(seriesKey,
                      value(Series.INITIAL_AMOUNT, -29.90),
                      value(Series.DAY, 7),
                      value(Series.BUDGET_AREA, BudgetArea.RECURRING.getId()),
                      value(Series.NAME, "free telecom"),
                      value(Series.PROFILE_TYPE, ProfileType.CUSTOM.getId()),
                      value(Series.IS_AUTOMATIC, false),
                      value(Series.DEFAULT_CATEGORY, MasterCategory.TELECOMS.getId()));
  }
}
