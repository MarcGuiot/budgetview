package org.designup.picsou.triggers;

import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.util.AmountMap;
import org.globsframework.model.Key;
import org.globsframework.utils.TestUtils;
import org.globsframework.utils.Utils;

import java.util.Arrays;

import static org.globsframework.model.FieldValue.value;

public class PlannedTransactionCreationTriggerTest extends PicsouTriggerTestCase {

  public void testMonthChange() throws Exception {
    repository.startChangeSet();
    createFreeSeries();
    createMonths(200807, 200808, 200809);
    repository.completeChangeSet();
    Integer[] plannedTransaction = getPlannedTransactions();
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "<create account='100' amount='-29.9' bankDay='18' bankMonth='200809' mirror='false'" +
      "        positionDay='18' positionMonth='200809' budgetDay='18' budgetMonth='200809' " +
      "        day='18' id='" + plannedTransaction[1] + "' label='free telecom' month='200809'" +
      "        planned='true' series='100' transactionType='5' type='transaction' createdBySeries='false'/>" +
      "<create account='100' amount='-29.9' bankDay='18' bankMonth='200808' mirror='false'" +
      "        positionDay='18' positionMonth='200808' budgetDay='18' budgetMonth='200808' " +
      "        day='18' id='" + plannedTransaction[0] + "' label='free telecom' month='200808'" +
      "        planned='true' series='100' transactionType='5' type='transaction'  createdBySeries='false'/>" +
      "");
    Integer[] budgetId = getBudgetId(FREE_SERIES_ID);
    Integer[] unknownBudget = getBudgetId(Series.UNCATEGORIZED_SERIES_ID);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<create active='true' plannedAmount='-29.9' day='7' id='" + budgetId[2] + "'" +
      "        month='200809' series='100' type='seriesBudget' />" +
      "<create active='true' plannedAmount='-29.9' day='7' id='" + budgetId[1] + "'" +
      "        month='200808' series='100' type='seriesBudget' />" +
      "<create active='true' plannedAmount='-29.9' day='7' id='" + budgetId[0] + "'" +
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
                      value(Transaction.LABEL, "free"),
                      value(Transaction.ACCOUNT, 3));

    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <create account='3' amount='-40.0' bankMonth='200808' bankDay='1' day='1' id='10' mirror='false'" +
      "          budgetDay='1' budgetMonth='200808'" +
      "          label='free' month='200808' planned='false' series='100' type='transaction' createdBySeries='false'/>" +
      "  <delete _account='100' _amount='-29.9' _bankDay='18' _bankMonth='200808' _mirror='false'" +
      "          _day='18' _label='free telecom' _month='200808' _planned='true'" +
      "          _budgetDay='18' _budgetMonth='200808' _positionDay='18' _positionMonth='200808' " +
      "          _series='100' _transactionType='5' id='" + plannedTransaction[0] + "' type='transaction' _createdBySeries='false'/>" +
      "");
    listener.assertLastChangesEqual(SeriesBudget.TYPE,
                                    "<update _actualAmount='(null)' id='" + budgetId[1] + "'" +
                                    "        actualAmount='-40.0' type='seriesBudget'/>");
    plannedTransaction = getPlannedTransactions();

    repository.update(transactionKey, value(Transaction.BUDGET_MONTH, 200809));
    Integer[] newPlannedTransaction = getPlannedTransactions();
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <delete _account='100' _amount='-29.9' _bankDay='18' _bankMonth='200809' _mirror='false'\n" +
      "          _day='18' _label='free telecom' _month='200809' _planned='true'" +
      "          _budgetDay='18' _budgetMonth='200809' _positionDay='18' _positionMonth='200809' " +
      "          _series='100' _transactionType='5' id='" + plannedTransaction[0] + "' type='transaction' _createdBySeries='false'/>\n" +
      "  <update _budgetMonth='200808' id='10' budgetMonth='200809' type='transaction'/>" +
      "  <create account='100' amount='-29.9' bankDay='18' bankMonth='200808' mirror='false'" +
      "           budgetDay='18' budgetMonth='200808' positionDay='18' positionMonth='200808' " +
      "          day='18' id='" + newPlannedTransaction[0] + "' label='free telecom' month='200808'\n" +
      "          planned='true' series='100' transactionType='5' type='transaction' createdBySeries='false'/>" +
      "");
  }

  public void testEnvelopeSeriesChangeWithIncome() throws Exception {
    repository.startChangeSet();
    createEnveloppeSeries();
    createIncomeSeries();
    createMonths(200807, 200808, 200809);
    repository.completeChangeSet();
    Integer[] incomePlannedTransaction = getPlannedTransactions(INCOME_SERIES_ID);
    Integer[] enveloppePlannedTransaction = getPlannedTransactions(ENVELOPPE_SERIES_ID);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <create account='100' amount='2000.0' bankDay='18' bankMonth='200808' mirror='false'\n" +
      "          positionDay='18' positionMonth='200808' budgetDay='18' budgetMonth='200808'" +
      "          day='18' id='" + incomePlannedTransaction[0] + "' label='salaire' month='200808'\n" +
      "          planned='true' series='102' transactionType='1' type='transaction' createdBySeries='false'/>\n" +
      "  <create account='100' amount='-1000.0' bankDay='18' bankMonth='200808' mirror='false'\n" +
      "          positionDay='18' positionMonth='200808' budgetDay='18' budgetMonth='200808'" +
      "          day='18' id='" + enveloppePlannedTransaction[0] + "' label='courses' month='200808'\n" +
      "          planned='true' series='101' transactionType='5' type='transaction' createdBySeries='false'/>" +
      "  <create account='100' amount='2000.0' bankDay='18' bankMonth='200809' mirror='false'\n" +
      "          positionDay='18' positionMonth='200809' budgetDay='18' budgetMonth='200809'" +
      "          day='18' id='" + incomePlannedTransaction[1] + "' label='salaire' month='200809'\n" +
      "          planned='true' series='102' transactionType='1' type='transaction' createdBySeries='false'/>\n" +
      "  <create account='100' amount='-1000.0' bankDay='18' bankMonth='200809' mirror='false'\n" +
      "          positionDay='18' positionMonth='200809' budgetDay='18' budgetMonth='200809'" +
      "          day='18' id='" + enveloppePlannedTransaction[1] + "' label='courses' month='200809'\n" +
      "          planned='true' series='101' transactionType='5' type='transaction' createdBySeries='false'/>\n" +
      "");
    Integer[] unknownBudgetIds = getBudgetId(Series.UNCATEGORIZED_SERIES_ID);
    Integer[] incomeBudgetIds = getBudgetId(INCOME_SERIES_ID);
    Integer[] enveloppeBudgetIds = getBudgetId(ENVELOPPE_SERIES_ID);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<create active='true' plannedAmount='2000.0' day='4' id='" + incomeBudgetIds[0] + "'" +
      "        month='200807' series='102' type='seriesBudget'/>" +
      "<create active='true' plannedAmount='2000.0' day='4' id='" + incomeBudgetIds[1] + "'" +
      "        month='200808' series='102' type='seriesBudget'/>" +
      "<create active='true' plannedAmount='2000.0' day='4' id='" + incomeBudgetIds[2] + "'" +
      "        month='200809' series='102' type='seriesBudget'/>" +
      "<create active='true' plannedAmount='-1000.0' day='25' id='" + enveloppeBudgetIds[0] + "'" +
      "        month='200807' series='101' type='seriesBudget'/>" +
      "<create active='true' plannedAmount='-1000.0' day='25' id='" + enveloppeBudgetIds[1] + "'" +
      "        month='200808' series='101' type='seriesBudget'/>" +
      "<create active='true' plannedAmount='-1000.0' day='25' id='" + enveloppeBudgetIds[2] + "'" +
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
                      value(Transaction.LABEL, "picsou"),
                      value(Transaction.ACCOUNT, 3));
    repository.create(Transaction.TYPE,
                      value(Transaction.ID, 1),
                      value(Transaction.SERIES, ENVELOPPE_SERIES_ID),
                      value(Transaction.AMOUNT, -300.),
                      value(Transaction.BUDGET_MONTH, 200808),
                      value(Transaction.BUDGET_DAY, 1),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.BANK_DAY, 1),
                      value(Transaction.LABEL, "Auchan"),
                      value(Transaction.ACCOUNT, 3));
    repository.completeChangeSet();
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "<update _amount='2000.0' amount='100.0' id='" + incomePlannedTransaction[0] + "' type='transaction'/>" +
      "<create account='3' amount='-300.0' bankMonth='200808' bankDay='1' id='1' label='Auchan'" +
      "        budgetDay='1' budgetMonth='200808'" +
      "        planned='false' series='101' type='transaction' mirror='false'  createdBySeries='false'/>" +
      "<create account='3' amount='1900.0' bankMonth='200808' bankDay='1' id='0' label='picsou'" +
      "        budgetDay='1' budgetMonth='200808'" +
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
                      value(Transaction.ACCOUNT, 3),
                      value(Transaction.SERIES, Series.OCCASIONAL_SERIES_ID));
    repository.completeChangeSet();
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "<update _amount='-300.0' amount='-200.0' id='1' type='transaction'/>\n" +
      "<update _amount='-700.0' amount='-800.0' id='" + enveloppePlannedTransaction[0] + "' type='transaction'/>\n" +
      "<create amount='-100.0' bankMonth='200808' bankDay='1' budgetDay='1'  id='2' budgetMonth='200808'\n" +
      "        planned='false' series='0' type='transaction' mirror='false' createdBySeries='false'" +
      "        account='3'/>");
  }

  public void testOverrunEnveloppe() throws Exception {
    createEnveloppeSeries();
    createMonths(200807, 200808, 200809);
    repository.create(Transaction.TYPE,
                      value(Transaction.ACCOUNT, 3),
                      value(Transaction.ID, 2),
                      value(Transaction.SERIES, ENVELOPPE_SERIES_ID),
                      value(Transaction.AMOUNT, -900.),
                      value(Transaction.BUDGET_MONTH, 200808),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.DAY, 1),
                      value(Transaction.BANK_DAY, 1),
                      value(Transaction.LABEL, "Auchan"));
    repository.create(Transaction.TYPE,
                      value(Transaction.ACCOUNT, 3),
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
      "<update _actualAmount='-900.0' id='" + budget[1] + "' actualAmount='-1500.0' type='seriesBudget'/>");
    repository.update(Key.create(Transaction.TYPE, 3), Transaction.AMOUNT, -30.);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _actualAmount='-1500.0' id='" + budget[1] + "' actualAmount='-930.0' type='seriesBudget'/>");
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <update _amount='-600.0' amount='-30.0' id='3' type='transaction'/>" +
      "  <create account='100' amount='-70.0' bankDay='18' bankMonth='200808' budgetMonth='200808' budgetDay='18' mirror='false'" +
      "          positionMonth='200808' positionDay='18'" +
      "          day='18' id='102' label='courses' month='200808'" +
      "          planned='true' series='101' transactionType='5' type='transaction' createdBySeries='false'/>");
    repository.update(Key.create(Transaction.TYPE, 3), Transaction.AMOUNT, -200.);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _actualAmount='-930.0' id='" + budget[1] + "' actualAmount='-1100.0' type='seriesBudget'/>");
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <delete _account='100' _amount='-70.0' _bankDay='18' _bankMonth='200808' _mirror='false'\n" +
      "          _day='18' _label='courses' _month='200808' _planned='true'" +
      "          _budgetDay='18' _budgetMonth='200808' _positionDay='18' _positionMonth='200808' " +
      "          _series='101' _transactionType='5' id='102' type='transaction' _createdBySeries='false'/>\n" +
      "  <update _amount='-30.0' amount='-200.0' id='3' type='transaction'/>");
  }

  public void testOverrunIncome() throws Exception {
    createIncomeSeries();
    createMonths(200807, 200808, 200809);
    repository.create(Transaction.TYPE,
                      value(Transaction.ID, 103),
                      value(Transaction.ACCOUNT, 3),
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
      "<update _actualAmount='(null)' id='" + budget[1] + "' actualAmount='2200.0' type='seriesBudget'/>");

    repository.update(Key.create(Transaction.TYPE, 103), Transaction.AMOUNT, 1900.);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _actualAmount='2200.0' id='" + budget[1] + "' actualAmount='1900.0' type='seriesBudget'/>");
    Integer[] transaction = getPlannedTransactions(INCOME_SERIES_ID);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <update _amount='2200.0' amount='1900.0' id='103' type='transaction'/>\n" +
      "  <create account='100' amount='100.0' bankDay='18' bankMonth='200808'\n mirror='false'" +
      "          positionMonth='200808' positionDay='18' budgetMonth='200808' budgetDay='18' " +
      "          day='18' id='" + transaction[0] + "' label='salaire' month='200808'" +
      "          planned='true' series='102' transactionType='1' type='transaction' createdBySeries='false'/>");

    transaction = getPlannedTransactions(INCOME_SERIES_ID);
    repository.update(Key.create(Transaction.TYPE, 103), Transaction.AMOUNT, 2300.);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update actualAmount='2300.0' id='" + budget[1] + "' _actualAmount='1900.0' type='seriesBudget'/>");
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <update _amount='1900.0' amount='2300.0' id='103' type='transaction'/>\n" +
      "  <delete _account='100' _amount='100.0' _bankDay='18' _bankMonth='200808' _mirror='false'\n" +
      "          _positionMonth='200808' _positionDay='18' _budgetMonth='200808' _budgetDay='18' " +
      "          _day='18' _label='salaire' _month='200808' _planned='true'" +
      "          _series='102' _transactionType='1' id='" + transaction[0] + "' type='transaction' _createdBySeries='false'/>");
  }

  public void testLevelPlannedWhenExceeded() throws Exception {

    // -- One account: no levelling needed
    checkLevelPlannedWhenExceed(new int[]{100},
                                new double[]{300},
                                new double[]{100},
                                new double[]{300});

    // -- One account: no levelling possible
    checkLevelPlannedWhenExceed(new int[]{100},
                                new double[]{300},
                                new double[]{400},
                                new double[]{300});

    // -- Two accounts: no levelling needed
    checkLevelPlannedWhenExceed(new int[]{100, 101},
                                new double[]{300, 500},
                                new double[]{100, 200},
                                new double[]{300, 500});

    // -- Two accounts: extra pushed to second account
    checkLevelPlannedWhenExceed(new int[]{100, 101},
                                new double[]{300, 500},
                                new double[]{400, 200},
                                new double[]{400, 400});

    // -- Two accounts: extra pushed to first account
    checkLevelPlannedWhenExceed(new int[]{100, 101},
                                new double[]{300, 500},
                                new double[]{100, 600},
                                new double[]{200, 600});

    // -- Two accounts: part of extra pushed to second account
    checkLevelPlannedWhenExceed(new int[]{100, 101},
                                new double[]{300, 500},
                                new double[]{400, 450},
                                new double[]{400, 450});

    // -- Two accounts: total actual exceeds the total planned
    checkLevelPlannedWhenExceed(new int[]{100, 101},
                                new double[]{300, 500},
                                new double[]{250, 600},
                                new double[]{250, 600});

    // -- Three accounts: extra taken from first account available
    checkLevelPlannedWhenExceed(new int[]{100, 101, 102},
                                new double[]{300, 500, 200},
                                new double[]{200, 600, 100},
                                new double[]{200, 600, 200});

    // -- Three accounts: no planned for first one
    checkLevelPlannedWhenExceed(new int[]{100, 101, 102},
                                new double[]{0, 500, 300},
                                new double[]{0, 600, 100},
                                new double[]{0, 600, 200});
  }

  private static void checkLevelPlannedWhenExceed(int[] accountIds,
                                                  double[] planned,
                                                  double[] actual,
                                                  double[] expectedPlanned) {
    AmountMap amounts = new AmountMap();
    for (int i = 0; i < accountIds.length; i++) {
      amounts.add(accountIds[i], actual[i]);
    }
    PlannedTransactionCreationTrigger.levelPlannedWhenExceeded(Utils.box(accountIds), planned, amounts);
    TestUtils.assertEquals(planned, expectedPlanned);
  }
}

