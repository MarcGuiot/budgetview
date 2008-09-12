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
                                    "<create type='seriesStat' series='10' month='200807'" +
                                    "        amount='0.0' plannedAmount='150.0'/>" +
                                    "<create type='seriesStat' series='0' month='200807'" +
                                    "        amount='0.0' plannedAmount='-150.0'/>" +
                                    "<create amount='0.0' month='200807' plannedAmount='0.0' series='1'" +
                                    "        type='seriesStat'/>");
    repository.enterBulkDispatchingMode();
    createTransaction(10, 10, 200807, 10.0);
    createTransaction(11, 1, 200807, -20.0);
    repository.completeBulkDispatchingMode();
    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<update type='seriesStat' series='10' month='200807'" +
                                    "        amount='10.0' _amount='0.0'/>" +
                                    "<update _amount='0.0' amount='-20.0' month='200807' series='1'" +
                                    "        type='seriesStat'/>");
  }

  public void testUnassignedTransactionSeriesAndDeleteSeries() throws Exception {
    checker.parse(repository,
                  "<series id='10' initialAmount='-100.0' budgetAreaName='recurringExpenses' profileTypeName='monthly'/>" +
                  "<series id='20' initialAmount='-100.0' budgetAreaName='recurringExpenses' profileTypeName='monthly'/>" +
                  "<month id='200807'/>" +
                  "<transaction id='1' series='10' month='200807' bankMonth='200807' amount='-10.0'/>" +
                  "<transaction id='2' series='20' month='200807' bankMonth='200807' amount='-10.0'/>" +
                  "<transaction id='3' series='20' month='200807' bankMonth='200807' amount='-10.0'/>" +
                  "<transaction id='4' month='200807' bankMonth='200807' amount='-50.0'/>" +
                  "");

    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<create type='seriesStat' series='10' month='200807'" +
                                    "        amount='-10.0' plannedAmount='-100.0'/>" +
                                    "<create type='seriesStat' series='20' month='200807'" +
                                    "        amount='-20.0' plannedAmount='-100.0'/>" +
                                    "<create type='seriesStat' series='0' month='200807'" +
                                    "        amount='0.0' plannedAmount='200.0'/>" +
                                    "<create type='seriesStat' series='1' month='200807'" +
                                    "        amount='-50.0' plannedAmount='0.0'/>" +
                                    "");

    repository.enterBulkDispatchingMode();
    repository.update(Key.create(Transaction.TYPE, 2), value(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID));
    repository.delete(Key.create(Transaction.TYPE, 3));
    repository.delete(Key.create(Series.TYPE, 20));
    repository.completeBulkDispatchingMode();

    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<delete type='seriesStat' series='20' month='200807'" +
                                    "        _amount='-20.0' _plannedAmount='-100.0'/>" +
                                    "<update type='seriesStat' month='200807' series='0'" +
                                    "        plannedAmount='100.0' _plannedAmount='200.0'/>" +
                                    "<update _amount='-50.0' amount='-60.0' month='200807' series='1'" +
                                    "        type='seriesStat'/>");
  }

  public void testTransactionChangeAmount() throws Exception {
    createSeries(10, 150.0);
    createMonth(200807);
    createTransaction(10, 10, 200807, 10.0);
    updateTransactionAmount(10, 5.0);
    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<update type='seriesStat' series='10' month='200807'" +
                                    "        amount='5.0' _amount='10.0'/>");
  }

  public void testChangeSerie() throws Exception {
    createSeries(10, 150.0);
    createSeries(20, 50.0);
    createMonth(200807);
    createTransaction(10, 10, 200807, 10.0);
    repository.enterBulkDispatchingMode();
    updateTransactionAmount(10, 5.0);
    updateTransactionSeries(10, 20);
    repository.completeBulkDispatchingMode();
    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<update type='seriesStat' series='10' month='200807'" +
                                    "        amount='0.0' _amount='10.0'/>" +
                                    "<update type='seriesStat' series='20' month='200807'" +
                                    "        amount='5.0' _amount='0.0'/>");
  }

  public void testChangingMonths() throws Exception {
    checker.parse(repository,
                  "<series id='10' initialAmount='100.0'  budgetAreaName='recurringExpenses' july='true' profileTypeName='monthly'/>" +
                  "<month id='200807'/>" +
                  "<month id='200808'/>" +
                  "<transaction id='1' series='10' month='200807' bankMonth='200807' amount='10.0'/>" +
                  "<transaction id='2' series='10' month='200808' bankMonth='200808' amount='20.0'/>");

    repository.update(Key.create(Transaction.TYPE, 1), value(Transaction.MONTH, 200808));
    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<update type='seriesStat' series='10' month='200808'" +
                                    "        amount='30.0' _amount='20.0'/>" +
                                    "<update type='seriesStat' series='10' month='200807'" +
                                    "        amount='0.0' _amount='10.0'/>");
  }

  public void testUpdatingPlannedAmount() throws Exception {
    checker.parse(repository,
                  "<series id='10' initialAmount='100.0' budgetAreaName='recurringExpenses' profileTypeName='monthly'/>" +
                  "<month id='200807'/>" +
                  "<transaction id='1' series='10' month='200807' bankMonth='200807' amount='10.0'/>");

    repository.update(Key.create(Series.TYPE, 10), value(Series.INITIAL_AMOUNT, 150.0));

    listener.assertNoChanges(SeriesBudget.TYPE);
  }

  public void testWithIncomeAndReccuring() throws Exception {
    checker.parse(repository,
                  "<series id='10' initialAmount='-100.0' budgetAreaName='recurringExpenses'" +
                  "        profileTypeName='monthly' defaultCategoryName='none'/>" +
                  "<series id='20' initialAmount='1000.0' budgetAreaName='income' profileTypeName='monthly'" +
                  "         defaultCategoryName='none'/>" +
                  "<series id='30' initialAmount='-500.0' budgetAreaName='expensesEnvelope' profileTypeName='monthly'" +
                  "         defaultCategoryName='none'/>" +
                  "<month id='200807'/>" +
                  "<transaction id='1' series='10' month='200807' bankMonth='200807' amount='-90.0' categoryName='none'/>" +
                  "<transaction id='2' series='30' month='200807' bankMonth='200807' amount='200.0' categoryName='none'/>" +
                  "");
    listener.assertLastChangesEqual(
      SeriesStat.TYPE,
      "<create amount='-90.0' month='200807' plannedAmount='-100.0' series='10' type='seriesStat'/>" +
      "<create amount='0.0' month='200807' plannedAmount='1000.0' series='20' type='seriesStat'/>" +
      "<create amount='0.0' month='200807' plannedAmount='-400.0' series='0' type='seriesStat'/>" +
      "<create amount='0.0' month='200807' plannedAmount='0.0' series='1' type='seriesStat'/>" +
      "<create amount='200.0' month='200807' plannedAmount='-500.0' series='30' type='seriesStat'/>");
    createTransaction(10, 20, 200807, 750.);
    listener.assertLastChangesEqual(SeriesStat.TYPE,
                                    "<update _amount='0.0' amount='750.0' month='200807' series='20'" +
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
                      value(Transaction.AMOUNT, amount),
                      value(Transaction.CATEGORY, Category.NONE));
  }
}
