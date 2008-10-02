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
      "<update _amount='-1000.0' amount='-1200.0' id='0' type='transaction'/>");
  }

  public void testChangeSeriesBudgetAmountCreatePlannedTransaction() throws Exception {
    createEnveloppeSeries();
    createMonth(200807, 200808, 200809);
    Integer[] budgetId = getBudgetId(ENVELOPPE_SERIES_ID);
    repository.create(Transaction.TYPE,
                      value(Transaction.ID, 100),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.BANK_DAY, 1),
                      value(Transaction.MONTH, 200808),
                      value(Transaction.AMOUNT, -1000.0),
                      value(Transaction.SERIES, ENVELOPPE_SERIES_ID));
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "<delete _account='-1' _amount='-1000.0' _bankDay='25' _bankMonth='200808'\n" +
      "        _category='2' _day='25' _label='Planned: course' _month='200808' _planned='true'\n" +
      "        _series='101' _transactionType='11' id='0' type='transaction'/>\n" +
      "<create amount='-1000.0' bankMonth='200808' id='100' month='200808' bankDay='1' \n" +
      "        planned='false' series='101' type='transaction' category='0'/>");
    repository.update(Key.create(SeriesBudget.TYPE, budgetId[1]), SeriesBudget.AMOUNT, -1200.);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <create account='-1' amount='-200.0' bankDay='25' bankMonth='200808'\n" +
      "          category='2' day='25' id='2' label='Planned: course' month='200808'\n" +
      "          planned='true' series='101' transactionType='11' type='transaction'/>");
  }

  public void testChangeSeriesWithOverrunCanCreateOrUpdatePlannedTransaction() throws Exception {
    createEnveloppeSeries();
    createMonth(200807, 200808, 200809);
    Integer[] budgetId = getBudgetId(ENVELOPPE_SERIES_ID);
    repository.create(Transaction.TYPE,
                      value(Transaction.ID, 100),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.BANK_DAY, 1),
                      value(Transaction.MONTH, 200808),
                      value(Transaction.AMOUNT, -1000.0),
                      value(Transaction.SERIES, ENVELOPPE_SERIES_ID));
    repository.update(Key.create(SeriesBudget.TYPE, budgetId[1]), SeriesBudget.AMOUNT, -900.);
    Integer[] occasionnalBudgetIds = getBudgetId(0);
    Integer[] enveloppeBudgetIds = getBudgetId(ENVELOPPE_SERIES_ID);
    listener.assertLastChangesEqual(Transaction.TYPE, "");
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _amount='1000.0' amount='900.0' id='" + occasionnalBudgetIds[1] + "' type='seriesBudget'/>" +
      "<update _amount='-1000.0' _overrunAmount='0.0' overrunAmount='-100.0'  amount='-900.0' id='" + enveloppeBudgetIds[1] + "' type='seriesBudget'/>");
    repository.update(Key.create(SeriesBudget.TYPE, budgetId[1]), SeriesBudget.AMOUNT, -1000.);
    listener.assertLastChangesEqual(Transaction.TYPE, "");
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "  <update _amount='900.0' amount='1000.0' id='" + occasionnalBudgetIds[1] + "' type='seriesBudget'/>" +
      "  <update _amount='-900.0' _overrunAmount='-100.0' amount='-1000.0' id='" + enveloppeBudgetIds[1] + "'" +
      "          overrunAmount='0.0' type='seriesBudget'/>");
    repository.update(Key.create(SeriesBudget.TYPE, budgetId[1]), SeriesBudget.AMOUNT, -1100.);
    Integer[] enveloppeTransactions = getPlannedTransaction(ENVELOPPE_SERIES_ID);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "<create account='-1' amount='-100.0' bankDay='25' bankMonth='200808'\n" +
      "        category='2' day='25' id='" + enveloppeTransactions[0] + "' label='Planned: course' month='200808'\n" +
      "        planned='true' series='101' transactionType='11' type='transaction'/>");
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _amount='1000.0' amount='1100.0' id='" + occasionnalBudgetIds[1] + "' type='seriesBudget'/>" +
      "<update _amount='-1000.0' amount='-1100.0' id='" + budgetId[1] + "' type='seriesBudget'/>");
  }

  public void testChangeAmountWithIncom() throws Exception {
    createIncomeSeries();
    createMonth(200807, 200808, 200809);
    Integer[] budgetId = getBudgetId(INCOME_SERIES_ID);
    repository.create(Transaction.TYPE,
                      value(Transaction.ID, 100),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.BANK_DAY, 1),
                      value(Transaction.MONTH, 200808),
                      value(Transaction.AMOUNT, 1500.0),
                      value(Transaction.SERIES, INCOME_SERIES_ID));
    Key budget = Key.create(SeriesBudget.TYPE, budgetId[1]);
    Integer[] transaction = getPlannedTransaction(INCOME_SERIES_ID);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "<update _amount='2000.0' amount='500.0' id='" + transaction[0] + "' type='transaction'/>\n" +
      "<create amount='1500.0' bankMonth='200808' bankDay='1' id='100' month='200808'\n" +
      "        planned='false' series='102' type='transaction' category='0'/>");
    repository.update(budget, SeriesBudget.AMOUNT, 1800.);
    Integer[] occasionalBudgetIds = getBudgetId(Series.OCCASIONAL_SERIES_ID);
    Integer[] incomeBudget = getBudgetId(INCOME_SERIES_ID);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "<update _amount='500.0' amount='300.0' id='0' type='transaction'/>");
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "  <update _amount='-2000.0' amount='-1800.0' id='" + occasionalBudgetIds[1] + "' type='seriesBudget'/>" +
      "  <update _amount='2000.0' amount='1800.0' id='" + incomeBudget[1] + "'" +
      "          type='seriesBudget'/>");
    repository.update(budget, SeriesBudget.AMOUNT, 1500.);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "<delete _account='-1' _amount='300.0' _bankDay='4' _bankMonth='200808'\n" +
      "      _category='19' _day='4' _label='Planned: salaire' _month='200808' _planned='true'\n" +
      "      _series='102' _transactionType='11' id='0' type='transaction'/>" +
      "");
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _amount='-1800.0' amount='-1500.0' id='" + occasionalBudgetIds[1] + "' type='seriesBudget'/>" +
      "<update _amount='1800.0' amount='1500.0' id='" + incomeBudget[1] + "'" +
      "        type='seriesBudget'/>");
    repository.update(budget, SeriesBudget.AMOUNT, 1400.);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "");
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _amount='-1500.0' amount='-1400.0' id='" + occasionalBudgetIds[1] + "' type='seriesBudget'/>" +
      "<update _amount='1500.0' amount='1400.0' id='" + incomeBudget[1] + "' _overrunAmount='0.0'" +
      "        overrunAmount='100.0' type='seriesBudget'/>");
  }
}
