package org.designup.picsou.triggers;

import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.model.Transaction;
import org.globsframework.model.Key;

import static org.globsframework.model.FieldValue.value;

public class SeriesBudgetUpdateTransactionTriggerTest extends PicsouTriggerTestCase {

  public void testChangeSeriesBudgetAmountUpdatePlannedTransaction() throws Exception {
    createEnveloppeSeries();
    createMonth(200807, 200808, 200809);
    Integer[] budgetId = getBudgetId(ENVELOPPE_SERIES_ID);
    repository.update(Key.create(SeriesBudget.TYPE, budgetId[1]), SeriesBudget.PLANNED_AMOUNT, -1200.);
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
                      value(Transaction.SERIES, ENVELOPPE_SERIES_ID),
                      value(Transaction.ACCOUNT, 3));
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "<delete _account='-1' _amount='-1000.0' _bankDay='18' _bankMonth='200808' " +
      "        _budgetDay='18' _budgetMonth='200808' _positionDay='18' _positionMonth='200808'" +
      "        _day='18' _label='courses' _month='200808' _planned='true' _mirror='false' _reconciliationAnnotationSet='false'" +
      "        _series='101' _transactionType='5' id='100' type='transaction' _createdBySeries='false'/>\n" +
      "<create amount='-1000.0' bankMonth='200808' id='0' month='200808' bankDay='1' budgetMonth='200808' " +
      "        planned='false' series='101' type='transaction' mirror='false' createdBySeries='false'" +
      "        reconciliationAnnotationSet='false' account='3'/>");
    repository.update(Key.create(SeriesBudget.TYPE, budgetId[1]), SeriesBudget.PLANNED_AMOUNT, -1200.);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <create id='102' account='-1' amount='-200.0' bankDay='18' bankMonth='200808' mirror='false'\n" +
      "          day='18' month='200808' label='courses'" +
      "          budgetDay='18' budgetMonth='200808' positionDay='18' positionMonth='200808' reconciliationAnnotationSet='false'" +
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
                      value(Transaction.BUDGET_MONTH, 200808),
                      value(Transaction.AMOUNT, -1000.0),
                      value(Transaction.SERIES, ENVELOPPE_SERIES_ID),
                      value(Transaction.ACCOUNT, 3));
    repository.update(Key.create(SeriesBudget.TYPE, budgetId[1]), SeriesBudget.PLANNED_AMOUNT, -900.);
    Integer[] enveloppeBudgetIds = getBudgetId(ENVELOPPE_SERIES_ID);
    listener.assertLastChangesEqual(Transaction.TYPE, "");
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _plannedAmount='-1000.0'   plannedAmount='-900.0' id='" + enveloppeBudgetIds[1] + "' type='seriesBudget'/>");
    repository.update(Key.create(SeriesBudget.TYPE, budgetId[1]), SeriesBudget.PLANNED_AMOUNT, -1000.);
    listener.assertLastChangesEqual(Transaction.TYPE, "");
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "  <update _plannedAmount='-900.0'  plannedAmount='-1000.0' id='" + enveloppeBudgetIds[1] + "'" +
      "          type='seriesBudget'/>");
    repository.update(Key.create(SeriesBudget.TYPE, budgetId[1]), SeriesBudget.PLANNED_AMOUNT, -1100.);
    Integer[] enveloppeTransactions = getPlannedTransaction(ENVELOPPE_SERIES_ID);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "<create account='-1' amount='-100.0' bankDay='18' budgetMonth='200808' budgetDay='18' bankMonth='200808'" +
      "        positionMonth='200808' positionDay='18' reconciliationAnnotationSet='false'" +
      "        day='18' id='" + enveloppeTransactions[0] + "' label='courses' month='200808'\n" +
      "        planned='true' series='101' transactionType='5' type='transaction' mirror='false' createdBySeries='false'/>");
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _plannedAmount='-1000.0' plannedAmount='-1100.0' id='" + budgetId[1] + "' type='seriesBudget'/>");
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
                      value(Transaction.SERIES, INCOME_SERIES_ID),
                      value(Transaction.ACCOUNT, 3));
    Key budget = Key.create(SeriesBudget.TYPE, budgetId[1]);
    Integer[] transaction = getPlannedTransaction(INCOME_SERIES_ID);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "<update _amount='2000.0' amount='500.0' id='" + transaction[0] + "' type='transaction'/>\n" +
      "<create amount='1500.0' budgetDay='1' budgetMonth='200808' id='0'" +
      "        bankDay='1' bankMonth='200808' reconciliationAnnotationSet='false'" +
      "        planned='false' series='102' type='transaction' mirror='false' createdBySeries='false' account='3'/>");
    repository.update(budget, SeriesBudget.PLANNED_AMOUNT, 1800.);
    Integer[] incomeBudget = getBudgetId(INCOME_SERIES_ID);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "<update _amount='500.0' amount='300.0' id='100' type='transaction'/>");
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "  <update _plannedAmount='2000.0' plannedAmount='1800.0' id='" + incomeBudget[1] + "'" +
      "          type='seriesBudget'/>");
    repository.update(budget, SeriesBudget.PLANNED_AMOUNT, 1500.);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "<delete _account='-1' _amount='300.0' _bankDay='18' _bankMonth='200808'" +
      "         _positionDay='18' _positionMonth='200808'  _budgetDay='18' _budgetMonth='200808'" +
      "        _day='18' _label='salaire' _month='200808' _planned='true' _reconciliationAnnotationSet='false'" +
      "        _series='102' _transactionType='1' id='100' type='transaction' _mirror='false' _createdBySeries='false'/>" +
      "");
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _plannedAmount='1800.0' plannedAmount='1500.0' id='" + incomeBudget[1] + "'" +
      "        type='seriesBudget'/>");
    repository.update(budget, SeriesBudget.PLANNED_AMOUNT, 1400.);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "");
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _plannedAmount='1500.0' plannedAmount='1400.0' id='" + incomeBudget[1] + "' " +
      "        type='seriesBudget'/>");
  }
}
