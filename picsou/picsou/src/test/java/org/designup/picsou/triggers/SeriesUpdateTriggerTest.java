package org.designup.picsou.triggers;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.PicsouTestCase;
import org.globsframework.model.FieldValue;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Key;
import org.globsframework.utils.Dates;

public class SeriesUpdateTriggerTest extends PicsouTestCase {
  private static final Key USER_PREFERENCES_KEY = Key.create(UserPreferences.TYPE, UserPreferences.SINGLETON_ID);

  protected void setUp() throws Exception {
    super.setUp();
    directory.add(new TimeService(Dates.parse("2008/07/09")));
  }

  public void testCreateSeriesGeneratesSeriesBudgetsAndTransactions() throws Exception {
    listener.reset();
    repository.addTrigger(new FutureMonthTrigger(directory));
    repository.addTrigger(new SeriesUpdateTrigger(directory));
    repository.create(USER_PREFERENCES_KEY, FieldValue.value(UserPreferences.FUTURE_MONTH_COUNT, 3));

    createSeries(value(Series.JULY, true));
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<create type='seriesBudget' id='0' month='200807' series='1' amount='29.9'/>" +
      "<create type='seriesBudget' id='1' month='200808' series='1' amount='29.9'/>" +
      "");
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "" +
      "<create type='transaction' id='0' account='-1' month='200807' series='1' planned='true' " +
      "        amount='-29.9' bankDay='31' transactionType='11' bankMonth='200807' category='8' day='31' label='free telecom'/>" +
      "<create type='transaction' id='1' account='-1' month='200808' series='1' planned='true' " +
      "        amount='-29.9' bankDay='31' transactionType='11' bankMonth='200808' category='8' day='31' label='free telecom'/>" +
      "");

  }

  public void testDeleteSeriesDeleteBudgetSerieAndSetTransactionSeries() throws Exception {
    repository.addTrigger(new FutureMonthTrigger(directory));
    repository.addTrigger(new SeriesUpdateTrigger(directory));
    repository.create(USER_PREFERENCES_KEY, FieldValue.value(UserPreferences.FUTURE_MONTH_COUNT, 3));

    createSeries(value(Series.JULY, true));
    repository.create(Key.create(Transaction.TYPE, 10),
                      value(Transaction.SERIES, 1),
                      value(Transaction.MONTH, 200807));
    listener.reset();
    repository.delete(Key.create(Series.TYPE, 1));
    listener.assertLastChangesEqual(SeriesBudget.TYPE,
                                    "<delete type='seriesBudget' id='0' _month='200807' _series='1' _amount='29.9'/>" +
                                    "<delete type='seriesBudget' id='1' _month='200808' _series='1' _amount='29.9'/>");
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "<update type='transaction' id='10' series='(null)' _series='1'/>" +
      "<delete _account='-1' _amount='-29.9' _bankDay='31' _bankMonth='200807' _transactionType='11' \n" +
      "        _category='8' _day='31' _label='free telecom' _month='200807' _planned='true'\n" +
      "        _series='1' id='0' type='transaction'/>\n" +
      "<delete _account='-1' _amount='-29.9' _bankDay='31' _bankMonth='200808' _transactionType='11' \n" +
      "        _category='8' _day='31' _label='free telecom' _month='200808' _planned='true'\n" +
      "        _series='1' id='1' type='transaction'/>" +
      "");
  }

  public void testReduceMonthDeleteTransactionAndSeriesBudget() throws Exception {
    repository.addTrigger(new FutureMonthTrigger(directory));
    repository.addTrigger(new SeriesUpdateTrigger(directory));
    repository.create(USER_PREFERENCES_KEY, FieldValue.value(UserPreferences.FUTURE_MONTH_COUNT, 3));
    createSeries(value(Series.JULY, true));
    repository.create(Key.create(Transaction.TYPE, 10),
                      value(Transaction.SERIES, 1),
                      value(Transaction.MONTH, 200808));
    repository.create(Key.create(Transaction.TYPE, 11),
                      value(Transaction.SERIES, 1),
                      value(Transaction.MONTH, 200807));
    listener.reset();
    repository.update(USER_PREFERENCES_KEY, value(UserPreferences.FUTURE_MONTH_COUNT, 0));
    listener.assertLastChangesEqual(SeriesBudget.TYPE,
                                    "<delete type='seriesBudget' id='1' _month='200808' _series='1' _amount='29.9'/>");
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "<delete _account='-1' _amount='-29.9' _bankDay='31' _bankMonth='200808' _transactionType='11'" +
      "        _category='8' _day='31' _label='free telecom' _month='200808' _planned='true'" +
      "        _series='1' id='1' type='transaction'/>" +
      "");
  }


  private void createSeries(FieldValue... value) {
    Key seriesKey = Key.create(Series.TYPE, 1);
    repository.enterBulkDispatchingMode();
    repository.create(seriesKey,
                      value(Series.AMOUNT, 29.90),
                      value(Series.BUDGET_AREA, BudgetArea.RECURRING_EXPENSES.getId()),
                      value(Series.AUGUST, true),
                      value(Series.LABEL, "free telecom"),
                      value(Series.DEFAULT_CATEGORY, MasterCategory.TELECOMS.getId()));
    repository.update(seriesKey, value);
    repository.completeBulkDispatchingMode();

  }
}
