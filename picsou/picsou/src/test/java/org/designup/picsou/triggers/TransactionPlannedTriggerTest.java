package org.designup.picsou.triggers;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatchers;

public class TransactionPlannedTriggerTest extends PicsouTriggerTestCase {

  public void test() throws Exception {
    repository.enterBulkDispatchingMode();
    createSeries();
    createMonth(200807);
    createMonth(200808);
    repository.completeBulkDispatchingMode();
    Key transactionKey = Key.create(Transaction.TYPE, 10);
    repository.create(transactionKey,
                      value(Transaction.SERIES, 1),
                      value(Transaction.MONTH, 200807),
                      value(Transaction.DAY, 1),
                      value(Transaction.AMOUNT, -40.0),
                      value(Transaction.LABEL, "free"));
    Integer trId = repository.getAll(Transaction.TYPE, GlobMatchers.and(GlobMatchers.fieldEquals(Transaction.PLANNED, true),
                                                                        GlobMatchers.fieldEquals(Transaction.MONTH, 200807)))
      .get(0).get(Transaction.ID);
    listener.assertLastChangesEqual(Transaction.TYPE,
                                    "<update type='transaction' _amount='-29.9' amount='10.1' id='" + trId + "'/>" +
                                    "<create type='transaction' amount='-40.0' day='1' id='10' label='free'" +
                                    "        month='200807' planned='false' series='1'/>" +
                                    "");
    listener.reset();
    repository.update(transactionKey, value(Transaction.MONTH, 200808));
    Integer Tr08 = repository.getAll(Transaction.TYPE, GlobMatchers.and(GlobMatchers.fieldEquals(Transaction.PLANNED, true),
                                                                        GlobMatchers.fieldEquals(Transaction.MONTH, 200808)))
      .get(0).get(Transaction.ID);
    listener.assertLastChangesEqual(Transaction.TYPE,
                                    "<update type='transaction' _month='200807' id='10' month='200808'/>" +
                                    "<update type='transaction' _amount='10.1' amount='-29.9' id='" + trId + "'/>" +
                                    "<update type='transaction' _amount='-29.9' amount='10.1' id='" + Tr08 + "'/>");
  }

  private void createSeries() {
    Key seriesKey = Key.create(Series.TYPE, 1);
    repository.create(seriesKey,
                      value(Series.AMOUNT, 29.90),
                      value(Series.BUDGET_AREA, BudgetArea.RECURRING_EXPENSES.getId()),
                      value(Series.AUGUST, true),
                      value(Series.LABEL, "free telecom"),
                      value(Series.DEFAULT_CATEGORY, MasterCategory.TELECOMS.getId()));
  }

}

