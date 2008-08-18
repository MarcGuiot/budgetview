package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Key;

public class SeriesStatTriggerTest extends PicsouTriggerTestCase {

  public void testStandardCreation() throws Exception {
    createSeries(1, 150.0);
    listener.assertNoChanges(SeriesStat.TYPE);

    createMonth(200807);
    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<create type='seriesStat' series='1' month='200807'" +
                                    "        amount='0.0' plannedAmount='150.0'/>" +
                                    "<create type='seriesStat' series='0' month='200807'" +
                                    "        amount='0.0' plannedAmount='-150.0'/>" +
                                    "");
    createTransaction(10, 1, 200807, 10.0);
    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<update type='seriesStat' series='1' month='200807'" +
                                    "        amount='10.0' _amount='0.0'/>");
  }

  public void testUnassignedTransactionSeriesAndDeleteSeries() throws Exception {
    checker.parse(repository,
                  "<series id='1' amount='100.0' budgetAreaName='recurringExpenses' july='true'/>" +
                  "<series id='2' amount='100.0' budgetAreaName='recurringExpenses' july='true'/>" +
                  "<month id='200807'/>" +
                  "<transaction id='1' series='1' month='200807' bankMonth='200807' amount='10.0'/>" +
                  "<transaction id='2' series='2' month='200807' bankMonth='200807' amount='10.0'/>" +
                  "<transaction id='3' series='2' month='200807' bankMonth='200807' amount='10.0'/>");

    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<create type='seriesStat' series='1' month='200807'" +
                                    "        amount='10.0' plannedAmount='100.0'/>" +
                                    "<create type='seriesStat' series='2' month='200807'" +
                                    "        amount='20.0' plannedAmount='100.0'/>" +
                                    "<create type='seriesStat' series='0' month='200807'" +
                                    "        amount='0.0' plannedAmount='-200.0'/>" +
                                    "");

    repository.enterBulkDispatchingMode();
    repository.update(Key.create(Transaction.TYPE, 2), value(Transaction.SERIES, null));
    repository.delete(Key.create(Transaction.TYPE, 3));
    repository.delete(Key.create(Series.TYPE, 2));
    repository.completeBulkDispatchingMode();

    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<delete type='seriesStat' series='2' month='200807'" +
                                    "        _amount='20.0' _plannedAmount='100.0'/>" +
                                    "<update type='seriesStat' month='200807' series='0'" +
                                    "        plannedAmount='-100.0' _plannedAmount='-200.0'/>"
    );
  }

  public void testTransactionChangeAmount() throws Exception {
    createSeries(1, 150.0);
    createMonth(200807);
    createTransaction(10, 1, 200807, 10.0);
    updateTransactionAmount(10, 5.0);
    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<update type='seriesStat' series='1' month='200807'" +
                                    "        amount='5.0' _amount='10.0'/>");
  }

  public void testChangeSerie() throws Exception {
    createSeries(1, 150.0);
    createSeries(2, 50.0);
    createMonth(200807);
    createTransaction(10, 1, 200807, 10.0);
    repository.enterBulkDispatchingMode();
    updateTransactionAmount(10, 5.0);
    updateTransactionSeries(10, 2);
    repository.completeBulkDispatchingMode();
    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<update type='seriesStat' series='1' month='200807'" +
                                    "        amount='0.0' _amount='10.0'/>" +
                                    "<update type='seriesStat' series='2' month='200807'" +
                                    "        amount='5.0' _amount='0.0'/>");
  }

  public void testChangingMonths() throws Exception {
    checker.parse(repository,
                  "<series id='1' amount='100.0'  budgetAreaName='recurringExpenses' july='true'/>" +
                  "<month id='200807'/>" +
                  "<month id='200808'/>" +
                  "<transaction id='1' series='1' month='200807' bankMonth='200807' amount='10.0'/>" +
                  "<transaction id='2' series='1' month='200808' bankMonth='200808' amount='20.0'/>");

    repository.update(Key.create(Transaction.TYPE, 1), value(Transaction.MONTH, 200808));
    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<update type='seriesStat' series='1' month='200808'" +
                                    "        amount='30.0' _amount='20.0'/>" +
                                    "<update type='seriesStat' series='1' month='200807'" +
                                    "        amount='0.0' _amount='10.0'/>");
  }

  public void testUpdatingPlannedAmount() throws Exception {
    checker.parse(repository,
                  "<series id='1' amount='100.0' budgetAreaName='recurringExpenses' />" +
                  "<month id='200807'/>" +
                  "<transaction id='1' series='1' month='200807' bankMonth='200807' amount='10.0'/>");

    repository.update(Key.create(Series.TYPE, 1), value(Series.AMOUNT, 150.0));

    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    " <update type='seriesStat' series='1' month='200807'" +
                                    "         plannedAmount='150.0' _plannedAmount='100.0'/>" +
                                    " <update type='seriesStat' series='0' month='200807'" +
                                    "         plannedAmount='-150.0' _plannedAmount='-100.0'/>" +
                                    "");
  }

  public void testWithIncomeAndReccuring() throws Exception {
    checker.parse(repository,
                  "<series id='1' amount='100.0' budgetAreaName='recurringExpenses'/>" +
                  "<series id='2' amount='1000.0' budgetAreaName='income'/>" +
                  "<series id='3' amount='500.0' budgetAreaName='expensesEnvelope'/>" +
                  "<month id='200807'/>" +
                  "<transaction id='1' series='1' month='200807' bankMonth='200807' amount='90.0'/>" +
                  "<transaction id='2' series='3' month='200807' bankMonth='200807' amount='200.0'/>" +
                  "");
    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<create amount='90.0' month='200807' plannedAmount='100.0' series='1'" +
                                    "        type='seriesStat'/>" +
                                    "<create amount='0.0' month='200807' plannedAmount='1000.0' series='2'" +
                                    "        type='seriesStat'/>" +
                                    "<create amount='0.0' month='200807' plannedAmount='400.0' series='0'" +
                                    "        type='seriesStat'/>" +
                                    "<create amount='200.0' month='200807' plannedAmount='500.0' series='3'" +
                                    "        type='seriesStat'/>");
    createTransaction(10, 2, 200807, 750.);
    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<update _amount='0.0' amount='750.0' month='200807' series='2'" +
                                    "        type='seriesStat'/>" +
                                    "");
  }

  private void updateTransactionSeries(int transactionId, int seriesId) {
    repository.update(Key.create(Transaction.TYPE, transactionId), value(Transaction.SERIES, seriesId));
  }

  private void updateTransactionAmount(int transactionId, double amount) {
    repository.update(Key.create(Transaction.TYPE, transactionId), Transaction.AMOUNT, amount);
  }

  private void createTransaction(int transactionId, int seriesId, int monthId, double amount) {
    repository.create(Key.create(Transaction.TYPE, transactionId),
                      value(Transaction.SERIES, seriesId),
                      value(Transaction.MONTH, monthId),
                      value(Transaction.BANK_MONTH, monthId),
                      value(Transaction.AMOUNT, amount));
  }
}
