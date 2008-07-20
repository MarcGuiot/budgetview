package org.designup.picsou.triggers;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.PicsouTestCase;
import org.globsframework.model.FieldValue;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Key;
import org.globsframework.utils.Dates;

public class TransactionPlannedTriggerTest extends PicsouTestCase {
  private static final Key USER_PREFERENCES_KEY = Key.create(UserPreferences.TYPE, UserPreferences.SINGLETON_ID);

  protected void setUp() throws Exception {
    super.setUp();
    directory.add(new TimeService(Dates.parse("2008/07/05")));
  }

  public void testCreatesNoSummaryForOnlyOneAccount() throws Exception {
    listener.reset();
    repository.addTrigger(new FutureMonthTrigger(directory));
    repository.addTrigger(new SeriesUpdateTrigger(directory));
    repository.addTrigger(new BudgetStatComputer());
    repository.addTrigger(new TransactionPlannedTrigger());

    repository.create(USER_PREFERENCES_KEY, FieldValue.value(UserPreferences.FUTURE_MONTH_COUNT, 3));
    repository.enterBulkDispatchingMode();
    createSeries(value(Series.JULY, true),
                 value(Series.AUGUST, true),
                 value(Series.SEPTEMBER, true));
    createSeriesBudget(200807);
    createSeriesBudget(200808);
    createSeriesBudget(200809);
    repository.completeBulkDispatchingMode();
    listener.reset();
    Key transactionKey = Key.create(Transaction.TYPE, 10);
    repository.create(transactionKey,
                      value(Transaction.SERIES, 1),
                      value(Transaction.MONTH, 200807),
                      value(Transaction.DAY, 1),
                      value(Transaction.AMOUNT, -40.0),
                      value(Transaction.LABEL, "free"));
    listener.assertLastChangesEqual(Transaction.TYPE,
                                    "<create amount='-40.0' day='1' id='10' label='free'" +
                                    "        month='200807' planned='false' series='1' type='transaction'/>" +
                                    "<update _amount='-29.9' amount='10.1' id='0' type='transaction'/>");
    listener.reset();
    repository.update(transactionKey, value(Transaction.MONTH, 200808));
    listener.assertLastChangesEqual(Transaction.TYPE,
                                    "<update type='transaction' _month='200807' id='10' month='200808'/>" +
                                    "<update type='transaction' _amount='10.1' amount='-29.9' id='0'/>" +
                                    "<update type='transaction' _amount='-29.9' amount='10.1' id='1'/>");
  }

  private void createSeries(FieldValue... value) {
    Key seriesKey = Key.create(Series.TYPE, 1);
    repository.create(seriesKey,
                      value(Series.AMOUNT, 29.90),
                      value(Series.BUDGET_AREA, BudgetArea.RECURRING_EXPENSES.getId()),
                      value(Series.AUGUST, true),
                      value(Series.LABEL, "free telecom"),
                      value(Series.DEFAULT_CATEGORY, MasterCategory.TELECOMS.getId()));
    repository.update(seriesKey, value);
  }

  private void createSeriesBudget(int monthId) {
    repository.create(SeriesBudget.TYPE,
                      value(SeriesBudget.SERIES, 1),
                      value(SeriesBudget.AMOUNT, 29.9),
                      value(SeriesBudget.MONTH, monthId));
  }
}

