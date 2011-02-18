package org.designup.picsou.triggers;

import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.model.Transaction;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Key;

public class SeriesBudgetUpdateTransactionTriggerTest extends PicsouTriggerTestCase {

  public void testChangeSeriesBudgetAmountUpdatePlannedTransaction() throws Exception {
    createEnveloppeSeries();
    createMonth(200807, 200808, 200809);
    Integer[] budgetId = getBudgetId(ENVELOPPE_SERIES_ID);
    repository.update(Key.create(SeriesBudget.TYPE, budgetId[1]), SeriesBudget.AMOUNT, -1200.);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "<update _amount='-1000.0' amount='-1200.0' id='100' type='transaction'/>");
  }

  public void testChangeSeriesBudgetAmountCreatePlannedTransaction() throws Exception {
    createEnveloppeSeries();
    createMonth(200807, 200808, 200809);
    Integer[] budgetId = getBudgetId(ENVELOPPE_SERIES_ID);
    repository.create(Transaction.TYPE,
                      value(Transaction.ID, 0),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.BANK_DAY, 1),
                      value(Transaction.MONTH, 200808),
                      value(Transaction.BUDGET_MONTH, 200808),
                      value(Transaction.AMOUNT, -1000.0),
                      value(Transaction.SERIES, ENVELOPPE_SERIES_ID));
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "<delete _account='-1' _amount='-1000.0' _bankDay='16' _bankMonth='200808' " +
      "        _budgetDay='16' _budgetMonth='200808' _positionDay='16' _positionMonth='200808'" +
      "        _day='16' _label='courses' _month='200808' _planned='true' _mirror='false'" +
      "        _series='101' _transactionType='5' id='100' type='transaction' _createdBySeries='false'/>\n" +
      "<create amount='-1000.0' bankMonth='200808' id='0' month='200808' bankDay='1' budgetMonth='200808' " +
      "        planned='false' series='101' type='transaction' mirror='false' createdBySeries='false'/>");
    repository.update(Key.create(SeriesBudget.TYPE, budgetId[1]), SeriesBudget.AMOUNT, -1200.);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <create id='102' account='-1' amount='-200.0' bankDay='16' bankMonth='200808' mirror='false'\n" +
      "          day='16' month='200808' label='courses'" +
      "          budgetDay='16' budgetMonth='200808' positionDay='16' positionMonth='200808' " +
      "          planned='true' series='101' transactionType='5' type='transaction' createdBySeries='false'/>");
  }

  public void testChangeSeriesWithOverrunCanCreateOrUpdatePlannedTransaction() throws Exception {
    createEnveloppeSeries();
    createMonth(200807, 200808, 200809);
    Integer[] budgetId = getBudgetId(ENVELOPPE_SERIES_ID);
    repository.create(Transaction.TYPE,
                      value(Transaction.ID, 0),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.BANK_DAY, 1),
//                      value(Transaction.MONTH, 200808),
                      value(Transaction.BUDGET_MONTH, 200808),
                      value(Transaction.AMOUNT, -1000.0),
                      value(Transaction.SERIES, ENVELOPPE_SERIES_ID));
    repository.update(Key.create(SeriesBudget.TYPE, budgetId[1]), SeriesBudget.AMOUNT, -900.);
    Integer[] occasionnalBudgetIds = getBudgetId(0);
    Integer[] enveloppeBudgetIds = getBudgetId(ENVELOPPE_SERIES_ID);
    listener.assertLastChangesEqual(Transaction.TYPE, "");
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _amount='-1000.0'   amount='-900.0' id='" + enveloppeBudgetIds[1] + "' type='seriesBudget'/>");
    repository.update(Key.create(SeriesBudget.TYPE, budgetId[1]), SeriesBudget.AMOUNT, -1000.);
    listener.assertLastChangesEqual(Transaction.TYPE, "");
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "  <update _amount='-900.0'  amount='-1000.0' id='" + enveloppeBudgetIds[1] + "'" +
      "          type='seriesBudget'/>");
    repository.update(Key.create(SeriesBudget.TYPE, budgetId[1]), SeriesBudget.AMOUNT, -1100.);
    Integer[] enveloppeTransactions = getPlannedTransaction(ENVELOPPE_SERIES_ID);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "<create account='-1' amount='-100.0' bankDay='16' budgetMonth='200808' budgetDay='16' bankMonth='200808'" +
      "        positionMonth='200808' positionDay='16' " +
      "        day='16' id='" + enveloppeTransactions[0] + "' label='courses' month='200808'\n" +
      "        planned='true' series='101' transactionType='5' type='transaction' mirror='false' createdBySeries='false'/>");
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _amount='-1000.0' amount='-1100.0' id='" + budgetId[1] + "' type='seriesBudget'/>");
  }

  public void testChangeAmountWithIncome() throws Exception {
    createIncomeSeries();
    createMonth(200807, 200808, 200809);
    Integer[] budgetId = getBudgetId(INCOME_SERIES_ID);
    repository.create(Transaction.TYPE,
                      value(Transaction.ID, 0),
                      value(Transaction.BUDGET_DAY, 1),
                      value(Transaction.BUDGET_MONTH, 200808),
                      value(Transaction.BANK_DAY, 1),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.AMOUNT, 1500.0),
                      value(Transaction.SERIES, INCOME_SERIES_ID));
    Key budget = Key.create(SeriesBudget.TYPE, budgetId[1]);
    Integer[] transaction = getPlannedTransaction(INCOME_SERIES_ID);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "<update _amount='2000.0' amount='500.0' id='" + transaction[0] + "' type='transaction'/>\n" +
      "<create amount='1500.0' budgetDay='1' budgetMonth='200808' id='0'" +
      "        bankDay='1' bankMonth='200808' " +
      "        planned='false' series='102' type='transaction' mirror='false' createdBySeries='false'/>");
    repository.update(budget, SeriesBudget.AMOUNT, 1800.);
    Integer[] occasionalBudgetIds = getBudgetId(Series.OCCASIONAL_SERIES_ID);
    Integer[] incomeBudget = getBudgetId(INCOME_SERIES_ID);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "<update _amount='500.0' amount='300.0' id='100' type='transaction'/>");
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "  <update _amount='2000.0' amount='1800.0' id='" + incomeBudget[1] + "'" +
      "          type='seriesBudget'/>");
    repository.update(budget, SeriesBudget.AMOUNT, 1500.);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "<delete _account='-1' _amount='300.0' _bankDay='20' _bankMonth='200808'" +
      "         _positionDay='20' _positionMonth='200808'  _budgetDay='20' _budgetMonth='200808'" +
      "        _day='20' _label='salaire' _month='200808' _planned='true'\n" +
      "        _series='102' _transactionType='1' id='100' type='transaction' _mirror='false' _createdBySeries='false'/>" +
      "");
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _amount='1800.0' amount='1500.0' id='" + incomeBudget[1] + "'" +
      "        type='seriesBudget'/>");
    repository.update(budget, SeriesBudget.AMOUNT, 1400.);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "");
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _amount='1500.0' amount='1400.0' id='" + incomeBudget[1] + "' " +
      "        type='seriesBudget'/>");
  }
}
