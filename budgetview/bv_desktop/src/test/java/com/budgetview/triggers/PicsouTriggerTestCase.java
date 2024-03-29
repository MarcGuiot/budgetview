package com.budgetview.triggers;

import com.budgetview.desktop.time.TimeService;
import com.budgetview.model.*;
import com.budgetview.shared.model.BudgetArea;
import com.budgetview.utils.PicsouTestCase;

import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.Dates;

public abstract class PicsouTriggerTestCase extends PicsouTestCase {
  private static final int ACCOUNT_ID = 100;
  protected static final int FREE_SERIES_ID = ACCOUNT_ID;
  protected static final int ENVELOPPE_SERIES_ID = 101;
  protected static final int INCOME_SERIES_ID = 102;

  protected void setUp() throws Exception {
    TimeService.setCurrentDate(Dates.parseMonth("2008/09"));
    super.setUp();
    directory.add(new TimeService());
    repository.addTrigger(new CurrentMonthTrigger());
    repository.addTrigger(new MonthsToSeriesBudgetTrigger(directory));
    repository.addTrigger(new SeriesBudgetTrigger(repository));
    repository.addTrigger(new SeriesDeletionTrigger());
    repository.addTrigger(new ActualSeriesStatTrigger());
    repository.addTrigger(new PastTransactionUpdateSeriesBudgetTrigger());
    repository.addTrigger(new PlannedTransactionCreationTrigger());
    repository.addTrigger(new PlannedSeriesStatTrigger());

    repository.create(CurrentMonth.KEY,
                      value(CurrentMonth.LAST_TRANSACTION_MONTH, 200808),
                      value(CurrentMonth.LAST_TRANSACTION_DAY, 1));
    repository.create(Account.TYPE,
                      value(Account.ID, ACCOUNT_ID),
                      value(Account.NAME, "Account n. 000111"));
    repository.create(Key.create(Series.TYPE, Series.UNCATEGORIZED_SERIES_ID),
                      value(Series.PROFILE_TYPE, ProfileType.IRREGULAR.getId()),
                      value(Series.IS_AUTOMATIC, false),
                      value(Series.BUDGET_AREA, BudgetArea.VARIABLE.getId()));
    repository.create(UserPreferences.KEY, value(UserPreferences.MONTH_FOR_PLANNED, 1),
                      value(UserPreferences.PERIOD_COUNT_FOR_PLANNED, 6));
  }

  protected void createSeries(int seriesId, double amount) {
    repository.create(Key.create(Series.TYPE, seriesId),
                      value(Series.INITIAL_AMOUNT, amount),
                      value(Series.PROFILE_TYPE, ProfileType.CUSTOM.getId()),
                      value(Series.IS_AUTOMATIC, false),
                      value(Series.NAME, "aSeries"),
                      value(Series.BUDGET_AREA, BudgetArea.RECURRING.getId()),
                      value(Series.TARGET_ACCOUNT, ACCOUNT_ID));
  }

  protected void createMonths(int... monthIds) {
    for (int monthId : monthIds) {
      repository.create(Key.create(Month.TYPE, monthId));
    }
  }

  protected Integer[] getBudgetId(int seriesId) {
    return repository.getAll(SeriesBudget.TYPE, fieldEquals(SeriesBudget.SERIES, seriesId))
      .sort(SeriesBudget.MONTH)
      .getValues(SeriesBudget.ID);
  }

  protected Integer[] getPlannedTransactions(Integer... seriesId) {
    GlobMatcher globMatcher = isTrue(Transaction.PLANNED);
    for (Integer series : seriesId) {
      globMatcher = and(globMatcher, fieldEquals(Transaction.SERIES, series));
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
                      value(Series.TARGET_ACCOUNT, ACCOUNT_ID),
                      value(Series.PROFILE_TYPE, ProfileType.CUSTOM.getId()),
                      value(Series.IS_AUTOMATIC, false));
  }

  protected void createEnveloppeSeries() {
    Key key = Key.create(Series.TYPE, ENVELOPPE_SERIES_ID);
    repository.create(key,
                      value(Series.INITIAL_AMOUNT, -1000.),
                      value(Series.BUDGET_AREA, BudgetArea.VARIABLE.getId()),
                      value(Series.DAY, 25),
                      value(Series.NAME, "courses"),
                      value(Series.TARGET_ACCOUNT, ACCOUNT_ID),
                      value(Series.PROFILE_TYPE, ProfileType.CUSTOM.getId()),
                      value(Series.IS_AUTOMATIC, false));
  }

  protected void createFreeSeries() {
    Key seriesKey = Key.create(Series.TYPE, FREE_SERIES_ID);
    repository.create(seriesKey,
                      value(Series.INITIAL_AMOUNT, -29.90),
                      value(Series.DAY, 7),
                      value(Series.BUDGET_AREA, BudgetArea.RECURRING.getId()),
                      value(Series.NAME, "free telecom"),
                      value(Series.PROFILE_TYPE, ProfileType.CUSTOM.getId()),
                      value(Series.TARGET_ACCOUNT, ACCOUNT_ID),
                      value(Series.IS_AUTOMATIC, false));
  }
}
