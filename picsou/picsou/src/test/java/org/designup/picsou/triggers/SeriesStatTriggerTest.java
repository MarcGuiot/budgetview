package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.PicsouTestCase;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Key;
import org.globsframework.model.KeyBuilder;

public class SeriesStatTriggerTest extends PicsouTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    repository.addTrigger(new SeriesStatTrigger());
  }

  public void testStandardCreation() throws Exception {
    createSeries(1, 150.0);
    listener.assertNoChanges(SeriesStat.TYPE);

    createMonth(200807);
    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<create type='seriesStat' series='1' month='200807'" +
                                    "        amount='0.0' plannedAmount='0.0'/>");
    createTransaction(10, 1, 200807, 10.0);
    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<update type='seriesStat' series='1' month='200807'" +
                                    "        amount='10.0' _amount='0.0'/>");
  }

  public void testChangingSeries() throws Exception {
    checker.parse(repository,
                  "<series id='1' amount='100.0'/>" +
                  "<series id='2' amount='100.0'/>" +
                  "<month id='200807'/>" +
                  "<transaction id='1' series='1' month='200807' amount='10.0'/>" +
                  "<transaction id='2' series='2' month='200807' amount='10.0'/>" +
                  "<transaction id='3' series='2' month='200807' amount='10.0'/>");

    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<create type='seriesStat' series='1' month='200807'" +
                                    "        amount='10.0' plannedAmount='100.0'/>" +
                                    "<create type='seriesStat' series='2' month='200807'" +
                                    "        amount='20.0' plannedAmount='100.0'/>");

    repository.enterBulkDispatchingMode();
    repository.delete(KeyBuilder.newKey(Series.TYPE, 2));
    repository.update(KeyBuilder.newKey(Series.TYPE, 1), value(Series.AMOUNT, 200.0));
    createSeries(3, 150.0);
    repository.update(KeyBuilder.newKey(Transaction.TYPE, 2), value(Transaction.SERIES, null));
    repository.delete(KeyBuilder.newKey(Transaction.TYPE, 3));
    repository.completeBulkDispatchingMode();

    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<create type='seriesStat' series='3' month='200807'" +
                                    "        amount='0.0' plannedAmount='150.0'/>" +
                                    "<update type='seriesStat' month='200807' series='1'" +
                                    "        plannedAmount='200.0' _plannedAmount='100.0'/>" +
                                    "<delete type='seriesStat' series='2' month='200807'" +
                                    "        _amount='20.0' _plannedAmount='100.0'/>"
    );
  }

  public void testTransactionChangeAmount() throws Exception {
    createSeries(1, 150.0);
    listener.assertNoChanges(SeriesStat.TYPE);

    createMonth(200807);
    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<create type='seriesStat' series='1' month='200807'" +
                                    "        amount='0.0' plannedAmount='0.0'/>");

    createTransaction(10, 1, 200807, 10.0);
    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<update type='seriesStat' series='1' month='200807'" +
                                    "        amount='10.0' _amount='0.0'/>");
    updateTransactionAmount(10, 5.0);

    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<update type='seriesStat' series='1' month='200807'" +
                                    "        amount='5.0' _amount='10.0'/>");
  }

  public void testChangeSerie() throws Exception {
    createSeries(1, 150.0);
    createSeries(2, 50.0);
    listener.assertNoChanges(SeriesStat.TYPE);

    createMonth(200807);
    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<create type='seriesStat' series='1' month='200807'" +
                                    "        amount='0.0' plannedAmount='0.0'/>" +
                                    "<create type='seriesStat' series='2' month='200807'" +
                                    "        amount='0.0' plannedAmount='0.0'/>");

    createTransaction(10, 1, 200807, 10.0);
    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<update type='seriesStat' series='1' month='200807'" +
                                    "        amount='10.0' _amount='0.0'/>");
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

  private void updateTransactionSeries(int transactionId, int seriesId) {
    repository.update(Key.create(Transaction.TYPE, transactionId), value(Transaction.SERIES, seriesId));
  }

  private void updateTransactionAmount(int transactionId, double amount) {
    repository.update(Key.create(Transaction.TYPE, transactionId), Transaction.AMOUNT, amount);
  }

  private void createMonth(int monthId) {
    repository.create(KeyBuilder.newKey(Month.TYPE, monthId));
  }

  private void createSeries(int seriesId, double amount) {
    repository.create(KeyBuilder.newKey(Series.TYPE, seriesId), value(Series.AMOUNT, amount));
  }

  private void createTransaction(int transactionId, int seriesId, int monthId, double amount) {
    repository.create(KeyBuilder.newKey(Transaction.TYPE, transactionId),
                      value(Transaction.SERIES, seriesId),
                      value(Transaction.MONTH, monthId),
                      value(Transaction.AMOUNT, amount));
  }

  public void testChangingMonths() throws Exception {
    checker.parse(repository,
                  "<series id='1' amount='100.0'/>" +
                  "<month id='200807'/>" +
                  "<month id='200808'/>" +
                  "<transaction id='1' series='1' month='200807' amount='10.0'/>" +
                  "<transaction id='2' series='1' month='200808' amount='20.0'/>");

    repository.enterBulkDispatchingMode();
    repository.delete(KeyBuilder.newKey(Month.TYPE, 200807));
    createMonth(200809);
    repository.update(KeyBuilder.newKey(Transaction.TYPE, 1), value(Transaction.MONTH, 200808));
    repository.completeBulkDispatchingMode();

    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<update type='seriesStat' series='1' month='200808'" +
                                    "        amount='30.0' _amount='20.0'/>" +
                                    "<delete type='seriesStat' series='1' month='200807'" +
                                    "        _amount='10.0' _plannedAmount='100.0'/>" +
                                    "<create type='seriesStat' series='1' amount='0.0' month='200809' plannedAmount='0.0'" +
                                    "         />");
  }

  public void testUpdatingPlannedAmount() throws Exception {
    checker.parse(repository,
                  "<series id='1' amount='100.0' />" +
                  "<month id='200807'/>" +
                  "<transaction id='1' series='1' month='200807' amount='10.0'/>");

    repository.update(KeyBuilder.newKey(Series.TYPE, 1), value(Series.AMOUNT, 150.0));

    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    " <update type='seriesStat' series='1' month='200807'" +
                                    "         plannedAmount='150.0' _plannedAmount='100.0'/>");
  }
}
