package org.designup.picsou.triggers;

import org.designup.picsou.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatchers;

public class SeriesUpdateTriggerTest extends PicsouTriggerTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    createMonth(200807, 200808, 200809);
    createSeries();
  }

  public void testCreateSeriesGeneratesSeriesBudgetsAndTransactions() throws Exception {
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "  <update _amount='0.0' amount='29.9' id='0' type='seriesBudget'/>" +
      "  <update _amount='0.0' amount='29.9' id='1' type='seriesBudget'/>" +
      "  <update _amount='0.0' amount='29.9' id='2' type='seriesBudget'/>" +
      "  <create active='true' amount='-29.9' day='30' id='5'" +
      "          month='200809' series='1' type='seriesBudget'/>" +
      "  <create active='true' amount='-29.9' day='31' id='4'" +
      "          month='200808' series='1' type='seriesBudget'/>" +
      "  <create active='true' amount='-29.9' day='31' id='3'" +
      "          month='200807' series='1' type='seriesBudget'/>");
    Integer[] ids = repository.getAll(Transaction.TYPE, GlobMatchers.fieldEquals(Transaction.PLANNED, true))
      .sort(Transaction.MONTH).getValues(Transaction.ID);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <create account='-1' amount='-29.9' bankDay='31' bankMonth='200808'" +
      "          category='8' day='31' id='" + ids[0] + "' label='free telecom' month='200808'" +
      "          planned='true' series='1' transactionType='11' type='transaction'/>" +
      "  <create account='-1' amount='-29.9' bankDay='30' bankMonth='200809'" +
      "          category='8' day='30' id='" + ids[1] + "' label='free telecom' month='200809'" +
      "          planned='true' series='1' transactionType='11' type='transaction'/>" +
      "");

  }

  public void testDeleteSeriesDeleteBudgetSerieAndSetTransactionSeries() throws Exception {
    repository.create(Key.create(Transaction.TYPE, 10),
                      value(Transaction.SERIES, 1),
                      value(Transaction.MONTH, 200807),
                      value(Transaction.BANK_MONTH, 200807),
                      value(Transaction.AMOUNT, -40.));
    listener.reset();
    Integer[] ids = repository.getAll(Transaction.TYPE, GlobMatchers.fieldEquals(Transaction.PLANNED, true))
      .sort(Transaction.MONTH).getValues(Transaction.ID);
    repository.delete(Key.create(Series.TYPE, 1));
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "  <update _amount='29.9' amount='0.0' id='0' type='seriesBudget'/>" +
      "  <update _amount='29.9' amount='0.0' id='1' type='seriesBudget'/>" +
      "  <update _amount='29.9' amount='0.0' id='2' type='seriesBudget'/>" +
      "  <delete _active='true' _amount='29.9' _day='30' _month='200809'" +
      "          _series='1' id='5' type='seriesBudget'/>" +
      "  <delete _active='true' _amount='29.9' _day='31' _month='200808'" +
      "          _series='1' id='4' type='seriesBudget'/>" +
      "  <delete _active='true' _amount='29.9' _day='31' _month='200807'" +
      "          _series='1' id='3' _overBurnAmount='-10.1' type='seriesBudget'/>");

    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <delete _account='-1' _amount='-29.9' _bankDay='30' _bankMonth='200809'" +
      "          _category='8' _day='30' _label='free telecom' _month='200809' _planned='true'" +
      "          _series='1' _transactionType='11' id='" + ids[1] + "' type='transaction'/>" +
      "  <delete _account='-1' _amount='-29.9' _bankDay='31' _bankMonth='200808'" +
      "          _category='8' _day='31' _label='free telecom' _month='200808' _planned='true'" +
      "          _series='1' _transactionType='11' id='" + ids[0] + "' type='transaction'/>");
  }


  private void createSeries() {
    Key seriesKey = Key.create(Series.TYPE, 1);
    repository.create(seriesKey,
                      value(Series.AMOUNT, -29.90),
                      value(Series.BUDGET_AREA, BudgetArea.RECURRING_EXPENSES.getId()),
                      value(Series.LABEL, "free telecom"),
                      value(Series.DEFAULT_CATEGORY, MasterCategory.TELECOMS.getId()));
  }
}
