package org.designup.picsou.triggers;

import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.model.Transaction;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Key;

public class TransactionPlannedTriggerTest extends PicsouTriggerTestCase {

  public void testMonthChange() throws Exception {
    repository.startChangeSet();
    createFreeSeries();
    createMonth(200807, 200808, 200809);
    repository.completeChangeSet();
    Integer[] plannedTransaction = getPlannedTransaction();
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "<create account='-1' amount='-29.9' bankDay='7' bankMonth='200809' mirror='false'" +
      "        positionDay='7' positionMonth='200809' budgetDay='7' budgetMonth='200809' " +
      "        day='7' id='" + plannedTransaction[1] + "' label='free telecom' month='200809'" +
      "        planned='true' series='100' transactionType='5' type='transaction' createdBySeries='false'/>" +
      "<create account='-1' amount='-29.9' bankDay='7' bankMonth='200808' mirror='false'" +
      "        positionDay='7' positionMonth='200808' budgetDay='7' budgetMonth='200808' " +
      "        day='7' id='" + plannedTransaction[0] + "' label='free telecom' month='200808'" +
      "        planned='true' series='100' transactionType='5' type='transaction'  createdBySeries='false'/>" +
      "");
    Integer[] budgetId = getBudgetId(FREE_SERIES_ID);
    Integer[] unknownBudget = getBudgetId(Series.UNCATEGORIZED_SERIES_ID);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<create active='true' amount='-29.9' day='7' id='" + budgetId[2] + "'" +
      "        month='200809' series='100' type='seriesBudget' />" +
      "<create active='true' amount='-29.9' day='7' id='" + budgetId[1] + "'" +
      "        month='200808' series='100' type='seriesBudget' />" +
      "<create active='true' amount='-29.9' day='7' id='" + budgetId[0] + "'" +
      "        month='200807' series='100' type='seriesBudget' />" +
      "<create active='true' id='" + unknownBudget[2] + "' month='200809'" +
      "        series='1' day='1' type='seriesBudget'/>" +
      "<create active='true' id='" + unknownBudget[1] + "' month='200808'" +
      "        series='1' day='1' type='seriesBudget'/>" +
      "<create active='true' id='" + unknownBudget[0] + "' month='200807'" +
      "        series='1' day='1' type='seriesBudget'/>");
    Key transactionKey = Key.create(Transaction.TYPE, 10);
    repository.create(transactionKey,
                      value(Transaction.SERIES, FREE_SERIES_ID),
                      value(Transaction.MONTH, 200808),
                      value(Transaction.DAY, 1),
                      value(Transaction.BUDGET_MONTH, 200808),
                      value(Transaction.BUDGET_DAY, 1),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.BANK_DAY, 1),
                      value(Transaction.AMOUNT, -40.0),
                      value(Transaction.LABEL, "free"));

    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <delete _account='-1' _amount='-29.9' _bankDay='7' _bankMonth='200808' _mirror='false'" +
      "          _day='7' _label='free telecom' _month='200808' _planned='true'" +
      "          _budgetDay='7' _budgetMonth='200808' _positionDay='7' _positionMonth='200808' " +
      "          _series='100' _transactionType='5' id='" + plannedTransaction[0] + "' type='transaction' _createdBySeries='false'/>" +
      "  <create amount='-40.0' bankMonth='200808' bankDay='1' day='1' id='10' mirror='false'" +
      "          budgetDay='1' budgetMonth='200808' " +
      "          label='free' month='200808' planned='false' series='100' type='transaction' createdBySeries='false'/>" +
      "");
    listener.assertLastChangesEqual(SeriesBudget.TYPE,
                                    "<update _observedAmount='(null)' id='" + budgetId[1] + "'" +
                                    "        observedAmount='-40.0' type='seriesBudget'/>");
    plannedTransaction = getPlannedTransaction();

    repository.update(transactionKey, value(Transaction.BUDGET_MONTH, 200809));
    Integer[] newPlannedTransaction = getPlannedTransaction();
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <delete _account='-1' _amount='-29.9' _bankDay='7' _bankMonth='200809' _mirror='false'\n" +
      "          _day='7' _label='free telecom' _month='200809' _planned='true'\n" +
      "          _budgetDay='7' _budgetMonth='200809' _positionDay='7' _positionMonth='200809' " +
      "          _series='100' _transactionType='5' id='" + plannedTransaction[0] + "' type='transaction' _createdBySeries='false'/>\n" +
      "  <update _budgetMonth='200808' id='10' budgetMonth='200809' type='transaction'/>" +
      "  <create account='-1' amount='-29.9' bankDay='7' bankMonth='200808' mirror='false'\n" +
      "           budgetDay='7' budgetMonth='200808' positionDay='7' positionMonth='200808' " +
      "          day='7' id='" + newPlannedTransaction[0] + "' label='free telecom' month='200808'\n" +
      "          planned='true' series='100' transactionType='5' type='transaction' createdBySeries='false'/>" +
      "");
  }

  public void testEnvelopeSeriesChangeWithIncome() throws Exception {
    repository.startChangeSet();
    createEnveloppeSeries();
    createIncomeSeries();
    createMonth(200807, 200808, 200809);
    repository.completeChangeSet();
    Integer[] incomePlannedTransaction = getPlannedTransaction(INCOME_SERIES_ID);
    Integer[] enveloppePlannedTransaction = getPlannedTransaction(ENVELOPPE_SERIES_ID);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <create account='-1' amount='2000.0' bankDay='4' bankMonth='200808' mirror='false'\n" +
      "          positionDay='4' positionMonth='200808' budgetDay='4' budgetMonth='200808' " +
      "          day='4' id='" + incomePlannedTransaction[0] + "' label='salaire' month='200808'\n" +
      "          planned='true' series='102' transactionType='1' type='transaction' createdBySeries='false'/>\n" +
      "  <create account='-1' amount='2000.0' bankDay='4' bankMonth='200809' mirror='false'\n" +
      "          positionDay='4' positionMonth='200809' budgetDay='4' budgetMonth='200809' " +
      "          day='4' id='" + incomePlannedTransaction[1] + "' label='salaire' month='200809'\n" +
      "          planned='true' series='102' transactionType='1' type='transaction' createdBySeries='false'/>\n" +
      "  <create account='-1' amount='-1000.0' bankDay='25' bankMonth='200808' mirror='false'\n" +
      "          positionDay='25' positionMonth='200808' budgetDay='25' budgetMonth='200808' " +
      "          day='25' id='" + enveloppePlannedTransaction[0] + "' label='courses' month='200808'\n" +
      "          planned='true' series='101' transactionType='5' type='transaction' createdBySeries='false'/>" +
      "  <create account='-1' amount='-1000.0' bankDay='25' bankMonth='200809' mirror='false'\n" +
      "          positionDay='25' positionMonth='200809' budgetDay='25' budgetMonth='200809' " +
      "          day='25' id='" + enveloppePlannedTransaction[1] + "' label='courses' month='200809'\n" +
      "          planned='true' series='101' transactionType='5' type='transaction' createdBySeries='false'/>\n" +
      "");
    Integer[] unknownBudgetIds = getBudgetId(Series.UNCATEGORIZED_SERIES_ID);
    Integer[] incomeBudgetIds = getBudgetId(INCOME_SERIES_ID);
    Integer[] enveloppeBudgetIds = getBudgetId(ENVELOPPE_SERIES_ID);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<create active='true' amount='2000.0' day='4' id='" + incomeBudgetIds[0] + "'" +
      "        month='200807' series='102' type='seriesBudget'/>" +
      "<create active='true' amount='2000.0' day='4' id='" + incomeBudgetIds[1] + "'" +
      "        month='200808' series='102' type='seriesBudget'/>" +
      "<create active='true' amount='2000.0' day='4' id='" + incomeBudgetIds[2] + "'" +
      "        month='200809' series='102' type='seriesBudget'/>" +
      "<create active='true' amount='-1000.0' day='25' id='" + enveloppeBudgetIds[0] + "'" +
      "        month='200807' series='101' type='seriesBudget'/>" +
      "<create active='true' amount='-1000.0' day='25' id='" + enveloppeBudgetIds[1] + "'" +
      "        month='200808' series='101' type='seriesBudget'/>" +
      "<create active='true' amount='-1000.0' day='25' id='" + enveloppeBudgetIds[2] + "'" +
      "        month='200809' series='101' type='seriesBudget'/>" +
      "<create active='true' id='" + unknownBudgetIds[2] + "' day='1' month='200809'" +
      "        series='1' type='seriesBudget'/>" +
      "<create active='true' id='" + unknownBudgetIds[1] + "' day='1' month='200808'" +
      "        series='1' type='seriesBudget'/>" +
      "<create active='true' id='" + unknownBudgetIds[0] + "' day='1' month='200807'" +
      "        series='1' type='seriesBudget'/>");
    repository.startChangeSet();
    repository.create(Transaction.TYPE,
                      value(Transaction.ID, 0),
                      value(Transaction.SERIES, INCOME_SERIES_ID),
                      value(Transaction.AMOUNT, 1900.),
                      value(Transaction.BUDGET_MONTH, 200808),
                      value(Transaction.BUDGET_DAY, 1),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.BANK_DAY, 1),
                      value(Transaction.LABEL, "picsou"));
    repository.create(Transaction.TYPE,
                      value(Transaction.ID, 1),
                      value(Transaction.SERIES, ENVELOPPE_SERIES_ID),
                      value(Transaction.AMOUNT, -300.),
                      value(Transaction.BUDGET_MONTH, 200808),
                      value(Transaction.BUDGET_DAY, 1),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.BANK_DAY, 1),
                      value(Transaction.LABEL, "Auchan"));
    repository.completeChangeSet();
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "<update _amount='2000.0' amount='100.0' id='" + incomePlannedTransaction[0] + "' type='transaction'/>" +
      "<create amount='-300.0' bankMonth='200808' bankDay='1' id='1' label='Auchan'" +
      "        budgetDay='1' budgetMonth='200808' " +
      "        planned='false' series='101' type='transaction' mirror='false'  createdBySeries='false'/>" +
      "<create amount='1900.0' bankMonth='200808' bankDay='1' id='0' label='picsou'" +
      "        budgetDay='1' budgetMonth='200808' " +
      "        planned='false' series='102' type='transaction' mirror='false'  createdBySeries='false'/>" +
      "<update _amount='-1000.0' amount='-700.0' id='" + enveloppePlannedTransaction[0] + "' type='transaction'/>");
    repository.startChangeSet();
    repository.update(Key.create(Transaction.TYPE, 1),
                      value(Transaction.SERIES, ENVELOPPE_SERIES_ID),
                      value(Transaction.AMOUNT, -200.),
                      value(Transaction.BUDGET_MONTH, 200808),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.BANK_DAY, 1),
                      value(Transaction.LABEL, "Auchan"));
    repository.create(Key.create(Transaction.TYPE, 2),
                      value(Transaction.AMOUNT, -100.),
                      value(Transaction.BUDGET_MONTH, 200808),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.BANK_DAY, 1),
                      value(Transaction.BUDGET_DAY, 1),
                      value(Transaction.SERIES, Series.OCCASIONAL_SERIES_ID));
    repository.completeChangeSet();
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "<update _amount='-300.0' amount='-200.0' id='1' type='transaction'/>\n" +
      "<update _amount='-700.0' amount='-800.0' id='" + enveloppePlannedTransaction[0] + "' type='transaction'/>\n" +
      "<create amount='-100.0' bankMonth='200808' bankDay='1' budgetDay='1'  id='2' budgetMonth='200808'\n" +
      "        planned='false' series='0' type='transaction' mirror='false' createdBySeries='false'/>");
  }

  public void testOverrunEnveloppe() throws Exception {
    createEnveloppeSeries();
    createMonth(200807, 200808, 200809);
    repository.create(Transaction.TYPE,
                      value(Transaction.ID, 2),
                      value(Transaction.SERIES, ENVELOPPE_SERIES_ID),
                      value(Transaction.AMOUNT, -900.),
                      value(Transaction.BUDGET_MONTH, 200808),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.DAY, 1),
                      value(Transaction.BANK_DAY, 1),
                      value(Transaction.LABEL, "Auchan"));
    repository.create(Transaction.TYPE,
                      value(Transaction.ID, 3),
                      value(Transaction.SERIES, ENVELOPPE_SERIES_ID),
                      value(Transaction.AMOUNT, -600.),
                      value(Transaction.BUDGET_MONTH, 200808),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.DAY, 1),
                      value(Transaction.BANK_DAY, 1),
                      value(Transaction.BUDGET_DAY, 1),
                      value(Transaction.LABEL, "Auchan"));
    Integer[] budget = getBudgetId(ENVELOPPE_SERIES_ID);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _observedAmount='-900.0' id='" + budget[1] + "' observedAmount='-1500.0' type='seriesBudget'/>");
    repository.update(Key.create(Transaction.TYPE, 3), Transaction.AMOUNT, -30.);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _observedAmount='-1500.0' id='" + budget[1] + "' observedAmount='-930.0' type='seriesBudget'/>");
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <update _amount='-600.0' amount='-30.0' id='3' type='transaction'/>" +
      "  <create account='-1' amount='-70.0' bankDay='25' bankMonth='200808' budgetMonth='200808' budgetDay='25' mirror='false'" +
      "          positionMonth='200808' positionDay='25'" +
      "          day='25' id='102' label='courses' month='200808'" +
      "          planned='true' series='101' transactionType='5' type='transaction' createdBySeries='false'/>");
    repository.update(Key.create(Transaction.TYPE, 3), Transaction.AMOUNT, -200.);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _observedAmount='-930.0' id='" + budget[1] + "' observedAmount='-1100.0' type='seriesBudget'/>");
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <delete _account='-1' _amount='-70.0' _bankDay='25' _bankMonth='200808' _mirror='false'\n" +
      "          _day='25' _label='courses' _month='200808' _planned='true'\n" +
      "          _budgetDay='25' _budgetMonth='200808' _positionDay='25' _positionMonth='200808' " +
      "          _series='101' _transactionType='5' id='102' type='transaction' _createdBySeries='false'/>\n" +
      "  <update _amount='-30.0' amount='-200.0' id='3' type='transaction'/>");
  }

  public void testOverrunIncome() throws Exception {
    createIncomeSeries();
    createMonth(200807, 200808, 200809);
    repository.create(Transaction.TYPE,
                      value(Transaction.ID, 103),
                      value(Transaction.SERIES, INCOME_SERIES_ID),
                      value(Transaction.AMOUNT, 2200.),
                      value(Transaction.BUDGET_MONTH, 200808),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.DAY, 1),
                      value(Transaction.BANK_DAY, 1),
                      value(Transaction.LABEL, "Auchan"));
    Integer[] budget = getBudgetId(INCOME_SERIES_ID);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _observedAmount='(null)' id='" + budget[1] + "' observedAmount='2200.0' type='seriesBudget'/>");

    repository.update(Key.create(Transaction.TYPE, 103), Transaction.AMOUNT, 1900.);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _observedAmount='2200.0' id='" + budget[1] + "' observedAmount='1900.0' type='seriesBudget'/>");
    Integer[] transaction = getPlannedTransaction(INCOME_SERIES_ID);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <update _amount='2200.0' amount='1900.0' id='103' type='transaction'/>\n" +
      "  <create account='-1' amount='100.0' bankDay='4' bankMonth='200808'\n mirror='false'" +
      "          positionMonth='200808' positionDay='4' budgetMonth='200808' budgetDay='4' " +
      "          day='4' id='" + transaction[0] + "' label='salaire' month='200808'\n" +
      "          planned='true' series='102' transactionType='1' type='transaction' createdBySeries='false'/>");

    transaction = getPlannedTransaction(INCOME_SERIES_ID);
    repository.update(Key.create(Transaction.TYPE, 103), Transaction.AMOUNT, 2300.);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update observedAmount='2300.0' id='" + budget[1] + "' _observedAmount='1900.0' type='seriesBudget'/>");
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <update _amount='1900.0' amount='2300.0' id='103' type='transaction'/>\n" +
      "  <delete _account='-1' _amount='100.0' _bankDay='4' _bankMonth='200808' _mirror='false'\n" +
      "          _positionMonth='200808' _positionDay='4' _budgetMonth='200808' _budgetDay='4' " +
      "          _day='4' _label='salaire' _month='200808' _planned='true'\n" +
      "          _series='102' _transactionType='1' id='" + transaction[0] + "' type='transaction' _createdBySeries='false'/>");

  }

  public void testOnOccasional() throws Exception {
    createMonth(200807, 200808, 200809);
    repository.create(Transaction.TYPE,
                      value(Transaction.ID, 103),
                      value(Transaction.SERIES, Series.OCCASIONAL_SERIES_ID),
                      value(Transaction.AMOUNT, 2200.),
                      value(Transaction.BUDGET_MONTH, 200808),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.DAY, 1),
                      value(Transaction.BANK_DAY, 1),
                      value(Transaction.LABEL, "Auchan"));
    repository.update(Key.create(Transaction.TYPE, 103), Transaction.AMOUNT, 1900.);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <update _amount='2200.0' amount='1900.0' id='103' type='transaction'/>");
  }
}

