package org.designup.picsou.triggers;

import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.model.Transaction;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatchers;

public class SeriesUpdateTriggerTest extends PicsouTriggerTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    createMonth(200807, 200808, 200809);
    createFreeSeries();
  }

  public void testCreateSeriesGeneratesSeriesBudgetsAndTransactions() throws Exception {
    Integer[] occasional = getBudgetId(0);
    Integer[] free = getBudgetId(FREE_SERIES_ID);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "  <create active='true' amount='-29.9' day='7' id='" + free[2] + "'" +
      "          month='200809' series='100' type='seriesBudget' overrunAmount='0.0'/>" +
      "  <create active='true' amount='-29.9' day='7' id='" + free[1] + "'" +
      "          month='200808' series='100' type='seriesBudget' overrunAmount='0.0'/>" +
      "  <create active='true' amount='-29.9' day='7' id='" + free[0] + "'" +
      "          month='200807' series='100' type='seriesBudget' overrunAmount='0.0'/>");
    Integer[] ids = repository.getAll(Transaction.TYPE, GlobMatchers.fieldEquals(Transaction.PLANNED, true))
      .sort(Transaction.MONTH).getValues(Transaction.ID);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <create account='-1' amount='-29.9' bankDay='7' bankMonth='200808' mirror='false'" +
      "          category='8' day='7' id='" + ids[0] + "' label='Planned: free telecom' month='200808'" +
      "          planned='true' series='100' transactionType='5' type='transaction'/>" +
      "  <create account='-1' amount='-29.9' bankDay='7' bankMonth='200809' mirror='false'" +
      "          category='8' day='7' id='" + ids[1] + "' label='Planned: free telecom' month='200809'" +
      "          planned='true' series='100' transactionType='5' type='transaction'/>" +
      "");

  }

  public void testDeleteSeriesDeleteBudgetSerieAndSetTransactionSeries() throws Exception {
    repository.create(Key.create(Transaction.TYPE, 10),
                      value(Transaction.SERIES, 100),
                      value(Transaction.MONTH, 200808),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.BANK_DAY, 1),
                      value(Transaction.AMOUNT, -40.));
    Integer[] ids = repository.getAll(Transaction.TYPE, GlobMatchers.fieldEquals(Transaction.PLANNED, true))
      .sort(Transaction.MONTH).getValues(Transaction.ID);
    Integer[] free = getBudgetId(FREE_SERIES_ID);
    repository.delete(Key.create(Series.TYPE, FREE_SERIES_ID));
    Integer[] occasional = getBudgetId(0);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "  <delete _active='true' _amount='-29.9' _day='7' _month='200809'" +
      "          _series='100' id='" + free[2] + "' type='seriesBudget' _overrunAmount='0.0'/>" +
      "  <delete _active='true' _amount='-29.9' _day='7' _month='200808'" +
      "          _series='100' id='" + free[1] + "' _overrunAmount='-10.1' type='seriesBudget'/>" +
      "  <delete _active='true' _amount='-29.9' _day='7' _month='200807'" +
      "          _series='100' id='" + free[0] + "' type='seriesBudget' _overrunAmount='0.0'/>");

    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <delete _account='-1' _amount='-29.9' _bankDay='7' _bankMonth='200809' _mirror='false'\n" +
      "          _category='8' _day='7' _label='Planned: free telecom' _month='200809' _planned='true'\n" +
      "          _series='100' _transactionType='5' id='" + ids[0] + "' type='transaction'/>");
  }
}
