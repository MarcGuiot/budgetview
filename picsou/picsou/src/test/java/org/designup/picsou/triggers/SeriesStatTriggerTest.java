package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.model.Transaction;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Key;

public class SeriesStatTriggerTest extends PicsouTriggerTestCase {

  public void testStandardCreation() throws Exception {
    createSeries(10, 150.0);
    listener.assertNoChanges(SeriesStat.TYPE);

    createMonth(200807);
    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<create type='seriesStat' series='10' month='200807' overrunAmount='0.0'" +
                                    "        plannedAmount='150.0' remainingAmount='150.0' active='true'/>" +
                                    "<create type='seriesStat' series='1' month='200807' overrunAmount='0.0'" +
                                    "        remainingAmount='0.0' active='true'/>");
    repository.startChangeSet();
    createTransaction(10, 10, 200807, 10.0);
    createTransaction(11, 1, 200807, -20.0);
    repository.completeChangeSet();
    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<update type='seriesStat' series='10' month='200807' remainingAmount='140.0'" +
                                    "        amount='10.0' _amount='(null)' _remainingAmount='150.0'/>" +
                                    "<update type='seriesStat' series='1' month='200807' _overrunAmount='0.0'" +
                                    "        amount='-20.0' _amount='(null)' overrunAmount='-20.0'/>");
  }

  public void testUnassignedTransactionSeriesAndDeleteSeries() throws Exception {

    checker.parse(repository,
                  "<series id='10' initialAmount='-100.0' budgetAreaName='recurring' " +
                  "        profileTypeName='custom' isAutomatic='false' name='10'/>" +
                  "<series id='20' initialAmount='-100.0' budgetAreaName='recurring' " +
                  "        profileTypeName='custom' isAutomatic='false'  name='20'/>" +
                  "<month id='200807'/>" +
                  "<transaction id='1' series='10' month='200807' day='1' budgetMonth='200807' budgetDay='1' bankMonth='200807' bankDay='1' amount='-10.0' account='3'/>" +
                  "<transaction id='2' series='20' month='200807' day='1' budgetMonth='200807' budgetDay='1' bankMonth='200807' bankDay='1' amount='-10.0' account='3'/>" +
                  "<transaction id='3' series='20' month='200807' day='1' budgetMonth='200807' budgetDay='1' bankMonth='200807' bankDay='1' amount='-10.0' account='3'/>" +
                  "<transaction id='4' month='200807' bankMonth='200807' bankDay='1' budgetMonth='200807' budgetDay='1' amount='-50.0'/>" +
                  "");

    listener.assertLastChangesEqual(
      SeriesStat.TYPE,
      "<create type='seriesStat' series='10' month='200807'  overrunAmount='0.0'" +
      "        amount='-10.0' plannedAmount='-100.0' remainingAmount='-90.0' active='true'/>" +
      "<create type='seriesStat' series='20' month='200807' overrunAmount='0.0'" +
      "        amount='-20.0' plannedAmount='-100.0' remainingAmount='-80.0' active='true'/>" +
      "<create type='seriesStat' series='1' month='200807' overrunAmount='-50.0'" +
      "        amount='-50.0' remainingAmount='0.0' active='true'/>");

    repository.startChangeSet();
    repository.update(Key.create(Transaction.TYPE, 2), value(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID));
    repository.delete(Key.create(Transaction.TYPE, 3));
    repository.delete(Key.create(Series.TYPE, 20));
    repository.completeChangeSet();

    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<update type='seriesStat' series='1' month='200807' _overrunAmount='-50.0'" +
                                    "        _amount='-50.0' amount='-60.0' overrunAmount='-60.0'/>" +
                                    "<delete type='seriesStat' series='20' month='200807' _overrunAmount='0.0'" +
                                    "        _amount='-20.0' _plannedAmount='-100.0'  _remainingAmount='-80.0'" +
                                    "        _active='true'/>");
  }

  public void testTransactionChangeAmount() throws Exception {
    createSeries(10, 150.0);
    createMonth(200807);
    createTransaction(10, 10, 200807, 10.0);
    updateTransactionAmount(10, 5.0);
    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<update type='seriesStat' series='10' month='200807' remainingAmount='145.0'" +
                                    "        amount='5.0' _amount='10.0' _remainingAmount='140.0'/>");
  }

  public void testChangeSerie() throws Exception {
    createSeries(10, 150.0);
    createSeries(20, 50.0);
    createMonth(200807);
    createTransaction(10, 10, 200807, 10.0);
    repository.startChangeSet();
    updateTransactionAmount(10, 5.0);
    updateTransactionSeries(10, 20);
    repository.completeChangeSet();
    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<update type='seriesStat' series='10' month='200807' _remainingAmount='140.0'" +
                                    "        amount='(null)' _amount='10.0' remainingAmount='150.0'/>" +
                                    "<update type='seriesStat' series='20' month='200807' _remainingAmount='50.0'" +
                                    "        amount='5.0' _amount='(null)' remainingAmount='45.0'/>");
  }

  public void testChangingMonths() throws Exception {
    checker.parse(repository,
                  "<series id='10' initialAmount='100.0'  budgetAreaName='recurring' isAutomatic='false' " +
                  "        july='true' profileTypeName='custom' name='10'/>" +
                  "<month id='200807'/>" +
                  "<month id='200808'/>" +
                  "<transaction id='1' series='10' month='200807' bankMonth='200807' bankDay='1' budgetMonth='200807' budgetDay='1' amount='10.0' account='3'/>" +
                  "<transaction id='2' series='10' month='200808' bankMonth='200808' bankDay='1' budgetMonth='200808' budgetDay='1' amount='20.0' account='3'/>");

    repository.update(Key.create(Transaction.TYPE, 1), value(Transaction.BUDGET_MONTH, 200808));
    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<update type='seriesStat' series='10' month='200808'  remainingAmount='70.0'" +
                                    "        amount='30.0' _amount='20.0'  _remainingAmount='80.0'/>" +
                                    "<update type='seriesStat' series='10' month='200807' _remainingAmount='90.0'" +
                                    "        amount='(null)' _amount='10.0' remainingAmount='100.0'/>");
  }

  public void testUpdatingPlannedAmount() throws Exception {
    checker.parse(repository,
                  "<series id='10' initialAmount='100.0' budgetAreaName='recurring' isAutomatic='false' " +
                  "        profileTypeName='custom' name='10'/>" +
                  "<month id='200807'/>" +
                  "<transaction id='1' series='10' month='200807' bankMonth='200807' bankDay='1' budgetMonth='200807' budgetDay='1'  amount='10.0' account='3'/>");

    repository.update(Key.create(Series.TYPE, 10), value(Series.INITIAL_AMOUNT, 150.0));

    listener.assertNoChanges(SeriesBudget.TYPE);
  }

  public void testWithIncomeAndRecurring() throws Exception {
    checker.parse(repository,
                  "<series id='10' initialAmount='-100.0' budgetAreaName='recurring' name='10'" +
                  "        profileTypeName='custom' isAutomatic='false' />" +
                  "<series id='20' initialAmount='1000.0' profileTypeName='custom'" +
                  "        isAutomatic='false' name='10' budgetAreaName='recurring'/>" +
                  "<series id='30' initialAmount='-500.0' profileTypeName='custom'" +
                  "        isAutomatic='false' name='10' budgetAreaName='recurring'/>" +
                  "<month id='200807'/>" +
                  "<transaction id='1' series='10' month='200807' bankMonth='200807' bankDay='1' budgetMonth='200807' budgetDay='1' amount='-90.0' account='3'/>" +
                  "<transaction id='2' series='30' month='200807' bankMonth='200807' bankDay='1' budgetMonth='200807' budgetDay='1' amount='200.0' account='3'/>" +
                  "");
    listener.assertLastChangesEqual(
      SeriesStat.TYPE,
      "  <create amount='200.0' month='200807' overrunAmount='0.0' plannedAmount='-500.0'\n" +
      "          remainingAmount='-700.0' series='30' type='seriesStat' active='true'/>\n" +
      "  <create amount='-90.0' month='200807' overrunAmount='0.0' plannedAmount='-100.0'\n" +
      "          remainingAmount='-10.0' series='10' type='seriesStat' active='true'/>" +
      "  <create month='200807' overrunAmount='0.0' remainingAmount='0.0'" +
      "          series='1' type='seriesStat' active='true'/>\n" +
      "  <create month='200807' overrunAmount='0.0' plannedAmount='1000.0' remainingAmount='1000.0'" +
      "          series='20' type='seriesStat' active='true'/>");
    createTransaction(10, 20, 200807, 750.);
    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<update _amount='(null)' amount='750.0' _remainingAmount='1000.0' month='200807' series='20'" +
                                    "        type='seriesStat' remainingAmount='250.0'/>");
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
                      value(Transaction.BUDGET_MONTH, monthId),
                      value(Transaction.DAY, 1),
                      value(Transaction.BANK_DAY, 1),
                      value(Transaction.ACCOUNT, 3),
                      value(Transaction.BANK_MONTH, monthId),
                      value(Transaction.AMOUNT, amount));
  }
}
