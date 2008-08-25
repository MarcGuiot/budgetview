package org.designup.picsou.triggers;

import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.model.Transaction;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Key;

public class TransactionPlannedTriggerTest extends PicsouTriggerTestCase {

  public void testMonthChange() throws Exception {
    repository.enterBulkDispatchingMode();
    createFreeSeries();
    createMonth(200807, 200808, 200809);
    repository.completeBulkDispatchingMode();
    Integer[] plannedTransaction = getPlannedTransaction();
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "<create account='-1' amount='-29.9' bankDay='7' bankMonth='200809'" +
      "        category='8' day='7' id='" + plannedTransaction[1] + "' label='free telecom' month='200809'" +
      "        planned='true' series='100' transactionType='11' type='transaction'/>" +
      "<create account='-1' amount='-29.9' bankDay='7' bankMonth='200808'" +
      "        category='8' day='7' id='" + plannedTransaction[0] + "' label='free telecom' month='200808'" +
      "        planned='true' series='100' transactionType='11' type='transaction'/>" +
      "");
    Integer[] budgetId = getBudgetId(FREE_SERIES_ID);
    Integer[] occasionalBudget = getBudgetId(Series.OCCASIONAL_SERIES_ID);
    Integer[] unknownBudget = getBudgetId(Series.UNKNOWN_SERIES_ID);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<create active='true' amount='-29.9' day='7' id='" + budgetId[2] + "'" +
      "        month='200809' series='100' type='seriesBudget' overBurnAmount='0.0'/>" +
      "<create active='true' amount='-29.9' day='7' id='" + budgetId[1] + "'" +
      "        month='200808' series='100' type='seriesBudget' overBurnAmount='0.0'/>" +
      "<create active='true' amount='-29.9' day='7' id='" + budgetId[0] + "'" +
      "        month='200807' series='100' type='seriesBudget' overBurnAmount='0.0'/>" +
      "<create active='true' amount='29.9' id='" + occasionalBudget[2] + "' month='200809'" +
      "        series='0' type='seriesBudget' overBurnAmount='0.0'/>" +
      "<create active='true' amount='29.9' id='" + occasionalBudget[1] + "' month='200808'" +
      "        series='0' type='seriesBudget' overBurnAmount='0.0'/>" +
      "<create active='true' amount='29.9' id='" + occasionalBudget[0] + "' month='200807'" +
      "        series='0' type='seriesBudget' overBurnAmount='0.0'/>" +
      "<create active='true' amount='0.0' id='" + unknownBudget[2] + "' month='200809'" +
      "        overBurnAmount='0.0' series='1' type='seriesBudget'/>" +
      "<create active='true' amount='0.0' id='" + unknownBudget[1] + "' month='200808'" +
      "        overBurnAmount='0.0' series='1' type='seriesBudget'/>" +
      "<create active='true' amount='0.0' id='" + unknownBudget[0] + "' month='200807'" +
      "        overBurnAmount='0.0' series='1' type='seriesBudget'/>");
    Key transactionKey = Key.create(Transaction.TYPE, 10);
    repository.create(transactionKey,
                      value(Transaction.SERIES, FREE_SERIES_ID),
                      value(Transaction.MONTH, 200808),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.DAY, 1),
                      value(Transaction.AMOUNT, -40.0),
                      value(Transaction.LABEL, "free"));

    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <delete _account='-1' _amount='-29.9' _bankDay='7' _bankMonth='200808'" +
      "          _category='8' _day='7' _label='free telecom' _month='200808' _planned='true'" +
      "          _series='100' _transactionType='11' id='" + plannedTransaction[0] + "' type='transaction'/>" +
      "  <create amount='-40.0' bankMonth='200808' day='1' id='10' category='0'" +
      "          label='free' month='200808' planned='false' series='100' type='transaction'/>" +
      "");
    listener.assertLastChangesEqual(SeriesBudget.TYPE,
                                    "<update _overBurnAmount='0.0' id='" + budgetId[1] + "'" +
                                    "        overBurnAmount='-10.1' type='seriesBudget'/>");
    plannedTransaction = getPlannedTransaction();

    repository.update(transactionKey, value(Transaction.MONTH, 200809));
    Integer[] newPlannedTransaction = getPlannedTransaction();
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <delete _account='-1' _amount='-29.9' _bankDay='7' _bankMonth='200809'\n" +
      "          _category='8' _day='7' _label='free telecom' _month='200809' _planned='true'\n" +
      "          _series='100' _transactionType='11' id='" + plannedTransaction[0] + "' type='transaction'/>\n" +
      "  <update _month='200808' id='10' month='200809' type='transaction'/>" +
      "  <create account='-1' amount='-29.9' bankDay='7' bankMonth='200808'\n" +
      "          category='8' day='7' id='" + newPlannedTransaction[0] + "' label='free telecom' month='200808'\n" +
      "          planned='true' series='100' transactionType='11' type='transaction'/>" +
      "");
  }

  public void testEnveloppeSeriesChangeWithIncome() throws Exception {
    repository.enterBulkDispatchingMode();
    createEnveloppeSeries();
    createIncomeSeries();
    createMonth(200807, 200808, 200809);
    repository.completeBulkDispatchingMode();
    Integer[] incomePlannedTransaction = getPlannedTransaction(INCOME_SERIES_ID);
    Integer[] enveloppePlannedTransaction = getPlannedTransaction(ENVELOPPE_SERIES_ID);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <create account='-1' amount='2000.0' bankDay='4' bankMonth='200808'\n" +
      "          category='19' day='4' id='" + incomePlannedTransaction[0] + "' label='salaire' month='200808'\n" +
      "          planned='true' series='102' transactionType='11' type='transaction'/>\n" +
      "  <create account='-1' amount='2000.0' bankDay='4' bankMonth='200809'\n" +
      "          category='19' day='4' id='" + incomePlannedTransaction[1] + "' label='salaire' month='200809'\n" +
      "          planned='true' series='102' transactionType='11' type='transaction'/>\n" +
      "  <create account='-1' amount='-1000.0' bankDay='25' bankMonth='200808'\n" +
      "          category='2' day='25' id='" + enveloppePlannedTransaction[0] + "' label='course' month='200808'\n" +
      "          planned='true' series='101' transactionType='11' type='transaction'/>" +
      "  <create account='-1' amount='-1000.0' bankDay='25' bankMonth='200809'\n" +
      "          category='2' day='25' id='" + enveloppePlannedTransaction[1] + "' label='course' month='200809'\n" +
      "          planned='true' series='101' transactionType='11' type='transaction'/>\n" +
      "");
    Integer[] occasionalBudgetIds = getBudgetId(Series.OCCASIONAL_SERIES_ID);
    Integer[] unknownBudgetIds = getBudgetId(Series.UNKNOWN_SERIES_ID);
    Integer[] incomBudgetIds = getBudgetId(INCOME_SERIES_ID);
    Integer[] enveloppeBudgetIds = getBudgetId(ENVELOPPE_SERIES_ID);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<create active='true' amount='2000.0' day='4' id='" + incomBudgetIds[0] + "'" +
      "        month='200807' series='102' type='seriesBudget' overBurnAmount='0.0'/>" +
      "<create active='true' amount='2000.0' day='4' id='" + incomBudgetIds[1] + "'" +
      "        month='200808' series='102' type='seriesBudget' overBurnAmount='0.0'/>" +
      "<create active='true' amount='2000.0' day='4' id='" + incomBudgetIds[2] + "'" +
      "        month='200809' series='102' type='seriesBudget' overBurnAmount='0.0'/>" +
      "<create active='true' amount='-1000.0' id='" + occasionalBudgetIds[0] + "' month='200807'" +
      "        series='0' type='seriesBudget' overBurnAmount='0.0'/>" +
      "<create active='true' amount='-1000.0' id='" + occasionalBudgetIds[1] + "' month='200808'" +
      "        series='0' type='seriesBudget' overBurnAmount='0.0'/>" +
      "<create active='true' amount='-1000.0' id='" + occasionalBudgetIds[2] + "' month='200809'" +
      "        series='0' type='seriesBudget' overBurnAmount='0.0'/>" +
      "<create active='true' amount='-1000.0' day='25' id='" + enveloppeBudgetIds[0] + "'" +
      "        month='200807' series='101' type='seriesBudget' overBurnAmount='0.0'/>" +
      "<create active='true' amount='-1000.0' day='25' id='" + enveloppeBudgetIds[1] + "'" +
      "        month='200808' series='101' type='seriesBudget' overBurnAmount='0.0'/>" +
      "<create active='true' amount='-1000.0' day='25' id='" + enveloppeBudgetIds[2] + "'" +
      "        month='200809' series='101' type='seriesBudget' overBurnAmount='0.0'/>" +
      "<create active='true' amount='0.0' id='" + unknownBudgetIds[2] + "' month='200809'" +
      "        overBurnAmount='0.0' series='1' type='seriesBudget'/>" +
      "<create active='true' amount='0.0' id='" + unknownBudgetIds[1] + "' month='200808'" +
      "        overBurnAmount='0.0' series='1' type='seriesBudget'/>" +
      "<create active='true' amount='0.0' id='" + unknownBudgetIds[0] + "' month='200807'" +
      "        overBurnAmount='0.0' series='1' type='seriesBudget'/>");
    repository.enterBulkDispatchingMode();
    repository.create(Transaction.TYPE,
                      value(Transaction.ID, 100),
                      value(Transaction.SERIES, INCOME_SERIES_ID),
                      value(Transaction.AMOUNT, 1900.),
                      value(Transaction.MONTH, 200808),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.LABEL, "picsou"));
    repository.create(Transaction.TYPE,
                      value(Transaction.ID, 101),
                      value(Transaction.SERIES, ENVELOPPE_SERIES_ID),
                      value(Transaction.AMOUNT, -300.),
                      value(Transaction.MONTH, 200808),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.LABEL, "Auchan"));
    repository.completeBulkDispatchingMode();
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "<update _amount='2000.0' amount='100.0' id='" + incomePlannedTransaction[0] + "' type='transaction'/>" +
      "<create amount='-300.0' bankMonth='200808' id='101' label='Auchan' category='0'" +
      "        month='200808' planned='false' series='101' type='transaction'/>" +
      "<create amount='1900.0' bankMonth='200808' id='100' label='picsou' category='0'" +
      "        month='200808' planned='false' series='102' type='transaction'/>" +
      "<update _amount='-1000.0' amount='-700.0' id='" + enveloppePlannedTransaction[0] + "' type='transaction'/>");
    repository.enterBulkDispatchingMode();
    repository.update(Key.create(Transaction.TYPE, 101),
                      value(Transaction.SERIES, ENVELOPPE_SERIES_ID),
                      value(Transaction.AMOUNT, -200.),
                      value(Transaction.MONTH, 200808),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.LABEL, "Auchan"));
    repository.create(Key.create(Transaction.TYPE, 102),
                      value(Transaction.AMOUNT, -100.),
                      value(Transaction.MONTH, 200808),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.SERIES, Series.OCCASIONAL_SERIES_ID));
    repository.completeBulkDispatchingMode();
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "<update _amount='-300.0' amount='-200.0' id='101' type='transaction'/>\n" +
      "<update _amount='-700.0' amount='-800.0' id='" + enveloppePlannedTransaction[0] + "' type='transaction'/>\n" +
      "<create amount='-100.0' bankMonth='200808' id='102' month='200808'\n" +
      "        planned='false' series='0' type='transaction' category='0'/>");
  }

  public void testOverBurnEnveloppe() throws Exception {
    createEnveloppeSeries();
    createMonth(200807, 200808, 200809);
    repository.create(Transaction.TYPE,
                      value(Transaction.ID, 102),
                      value(Transaction.SERIES, ENVELOPPE_SERIES_ID),
                      value(Transaction.AMOUNT, -900.),
                      value(Transaction.MONTH, 200808),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.LABEL, "Auchan"));
    repository.create(Transaction.TYPE,
                      value(Transaction.ID, 103),
                      value(Transaction.SERIES, ENVELOPPE_SERIES_ID),
                      value(Transaction.AMOUNT, -600.),
                      value(Transaction.MONTH, 200808),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.LABEL, "Auchan"));
    Integer[] budget = getBudgetId(ENVELOPPE_SERIES_ID);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _overBurnAmount='0.0' id='" + budget[1] + "' overBurnAmount='-500.0' type='seriesBudget'/>");
    repository.update(Key.create(Transaction.TYPE, 103), Transaction.AMOUNT, -30.);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _overBurnAmount='-500.0' id='" + budget[1] + "' overBurnAmount='0.0' type='seriesBudget'/>");
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <update _amount='-600.0' amount='-30.0' id='103' type='transaction'/>\n" +
      "  <create account='-1' amount='-70.0' bankDay='25' bankMonth='200808'\n" +
      "          category='2' day='25' id='2' label='course' month='200808'\n" +
      "          planned='true' series='101' transactionType='11' type='transaction'/>");
    repository.update(Key.create(Transaction.TYPE, 103), Transaction.AMOUNT, -200.);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _overBurnAmount='0.0' id='" + budget[1] + "' overBurnAmount='-100.0' type='seriesBudget'/>");
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <delete _account='-1' _amount='-70.0' _bankDay='25' _bankMonth='200808'\n" +
      "          _category='2' _day='25' _label='course' _month='200808' _planned='true'\n" +
      "          _series='101' _transactionType='11' id='2' type='transaction'/>\n" +
      "  <update _amount='-30.0' amount='-200.0' id='103' type='transaction'/>");
  }

  public void testOverBurnIncom() throws Exception {
    createIncomeSeries();
    createMonth(200807, 200808, 200809);
    repository.create(Transaction.TYPE,
                      value(Transaction.ID, 103),
                      value(Transaction.SERIES, INCOME_SERIES_ID),
                      value(Transaction.AMOUNT, 2200.),
                      value(Transaction.MONTH, 200808),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.LABEL, "Auchan"));
    Integer[] budget = getBudgetId(INCOME_SERIES_ID);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _overBurnAmount='0.0' id='" + budget[1] + "' overBurnAmount='200.0' type='seriesBudget'/>");

    repository.update(Key.create(Transaction.TYPE, 103), Transaction.AMOUNT, 1900.);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _overBurnAmount='200.0' id='" + budget[1] + "' overBurnAmount='0.0' type='seriesBudget'/>");
    Integer[] transaction = getPlannedTransaction(INCOME_SERIES_ID);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <update _amount='2200.0' amount='1900.0' id='103' type='transaction'/>\n" +
      "  <create account='-1' amount='100.0' bankDay='4' bankMonth='200808'\n" +
      "          category='19' day='4' id='" + transaction[0] + "' label='salaire' month='200808'\n" +
      "          planned='true' series='102' transactionType='11' type='transaction'/>");

    transaction = getPlannedTransaction(INCOME_SERIES_ID);
    repository.update(Key.create(Transaction.TYPE, 103), Transaction.AMOUNT, 2300.);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update overBurnAmount='300.0' id='" + budget[1] + "' _overBurnAmount='0.0' type='seriesBudget'/>");
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <update _amount='1900.0' amount='2300.0' id='103' type='transaction'/>\n" +
      "  <delete _account='-1' _amount='100.0' _bankDay='4' _bankMonth='200808'\n" +
      "          _category='19' _day='4' _label='salaire' _month='200808' _planned='true'\n" +
      "          _series='102' _transactionType='11' id='" + transaction[0] + "' type='transaction'/>");

  }

  public void testOnOccasional() throws Exception {
    createMonth(200807, 200808, 200809);
    repository.create(Transaction.TYPE,
                      value(Transaction.ID, 103),
                      value(Transaction.SERIES, Series.OCCASIONAL_SERIES_ID),
                      value(Transaction.AMOUNT, 2200.),
                      value(Transaction.MONTH, 200808),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.LABEL, "Auchan"));
    repository.update(Key.create(Transaction.TYPE, 103), Transaction.AMOUNT, 1900.);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <update _amount='2200.0' amount='1900.0' id='103' type='transaction'/>");
  }
}

