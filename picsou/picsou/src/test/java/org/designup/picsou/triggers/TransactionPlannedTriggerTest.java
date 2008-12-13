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
      "        category='8' day='7' id='" + plannedTransaction[1] + "' label='Planned: free telecom' month='200809'" +
      "        planned='true' series='100' transactionType='5' type='transaction' createdBySeries='false'/>" +
      "<create account='-1' amount='-29.9' bankDay='7' bankMonth='200808' mirror='false'" +
      "        category='8' day='7' id='" + plannedTransaction[0] + "' label='Planned: free telecom' month='200808'" +
      "        planned='true' series='100' transactionType='5' type='transaction'  createdBySeries='false'/>" +
      "");
    Integer[] budgetId = getBudgetId(FREE_SERIES_ID);
    Integer[] occasionalBudget = getBudgetId(Series.OCCASIONAL_SERIES_ID);
    Integer[] unknownBudget = getBudgetId(Series.UNCATEGORIZED_SERIES_ID);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<create active='true' amount='-29.9' day='7' id='" + budgetId[2] + "'" +
      "        month='200809' series='100' type='seriesBudget' overrunAmount='0.0'/>" +
      "<create active='true' amount='-29.9' day='7' id='" + budgetId[1] + "'" +
      "        month='200808' series='100' type='seriesBudget' overrunAmount='0.0'/>" +
      "<create active='true' amount='-29.9' day='7' id='" + budgetId[0] + "'" +
      "        month='200807' series='100' type='seriesBudget' overrunAmount='0.0'/>" +
      "<create active='true' amount='0.0' id='" + occasionalBudget[2] + "' month='200809'" +
      "        series='0' day='1' type='seriesBudget' overrunAmount='0.0'/>" +
      "<create active='true' amount='0.0' id='" + occasionalBudget[1] + "' month='200808'" +
      "        series='0' day='1' type='seriesBudget' overrunAmount='0.0'/>" +
      "<create active='true' amount='0.0' id='" + occasionalBudget[0] + "' month='200807'" +
      "        series='0' day='1' type='seriesBudget' overrunAmount='0.0'/>" +
      "<create active='true' amount='0.0' id='" + unknownBudget[2] + "' month='200809'" +
      "        overrunAmount='0.0' series='1' day='1' type='seriesBudget'/>" +
      "<create active='true' amount='0.0' id='" + unknownBudget[1] + "' month='200808'" +
      "        overrunAmount='0.0' series='1' day='1' type='seriesBudget'/>" +
      "<create active='true' amount='0.0' id='" + unknownBudget[0] + "' month='200807'" +
      "        overrunAmount='0.0' series='1' day='1' type='seriesBudget'/>");
    Key transactionKey = Key.create(Transaction.TYPE, 10);
    repository.create(transactionKey,
                      value(Transaction.SERIES, FREE_SERIES_ID),
                      value(Transaction.MONTH, 200808),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.DAY, 1),
                      value(Transaction.BANK_DAY, 1),
                      value(Transaction.AMOUNT, -40.0),
                      value(Transaction.LABEL, "free"));

    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <delete _account='-1' _amount='-29.9' _bankDay='7' _bankMonth='200808' _mirror='false'" +
      "          _category='8' _day='7' _label='Planned: free telecom' _month='200808' _planned='true'" +
      "          _series='100' _transactionType='5' id='" + plannedTransaction[0] + "' type='transaction' _createdBySeries='false'/>" +
      "  <create amount='-40.0' bankMonth='200808' bankDay='1' day='1' id='10' category='0' mirror='false'" +
      "          label='free' month='200808' planned='false' series='100' type='transaction' createdBySeries='false'/>" +
      "");
    listener.assertLastChangesEqual(SeriesBudget.TYPE,
                                    "<update _overrunAmount='0.0' id='" + budgetId[1] + "'" +
                                    "        overrunAmount='-10.1' type='seriesBudget'/>");
    plannedTransaction = getPlannedTransaction();

    repository.update(transactionKey, value(Transaction.MONTH, 200809));
    Integer[] newPlannedTransaction = getPlannedTransaction();
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <delete _account='-1' _amount='-29.9' _bankDay='7' _bankMonth='200809' _mirror='false'\n" +
      "          _category='8' _day='7' _label='Planned: free telecom' _month='200809' _planned='true'\n" +
      "          _series='100' _transactionType='5' id='" + plannedTransaction[0] + "' type='transaction' _createdBySeries='false'/>\n" +
      "  <update _month='200808' id='10' month='200809' type='transaction'/>" +
      "  <create account='-1' amount='-29.9' bankDay='7' bankMonth='200808' mirror='false'\n" +
      "          category='8' day='7' id='" + newPlannedTransaction[0] + "' label='Planned: free telecom' month='200808'\n" +
      "          planned='true' series='100' transactionType='5' type='transaction' createdBySeries='false'/>" +
      "");
  }

  public void testEnveloppeSeriesChangeWithIncome() throws Exception {
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
      "          category='19' day='4' id='" + incomePlannedTransaction[0] + "' label='Planned: salaire' month='200808'\n" +
      "          planned='true' series='102' transactionType='1' type='transaction' createdBySeries='false'/>\n" +
      "  <create account='-1' amount='2000.0' bankDay='4' bankMonth='200809' mirror='false'\n" +
      "          category='19' day='4' id='" + incomePlannedTransaction[1] + "' label='Planned: salaire' month='200809'\n" +
      "          planned='true' series='102' transactionType='1' type='transaction' createdBySeries='false'/>\n" +
      "  <create account='-1' amount='-1000.0' bankDay='25' bankMonth='200808' mirror='false'\n" +
      "          category='2' day='25' id='" + enveloppePlannedTransaction[0] + "' label='Planned: course' month='200808'\n" +
      "          planned='true' series='101' transactionType='5' type='transaction' createdBySeries='false'/>" +
      "  <create account='-1' amount='-1000.0' bankDay='25' bankMonth='200809' mirror='false'\n" +
      "          category='2' day='25' id='" + enveloppePlannedTransaction[1] + "' label='Planned: course' month='200809'\n" +
      "          planned='true' series='101' transactionType='5' type='transaction' createdBySeries='false'/>\n" +
      "");
    Integer[] occasionalBudgetIds = getBudgetId(Series.OCCASIONAL_SERIES_ID);
    Integer[] unknownBudgetIds = getBudgetId(Series.UNCATEGORIZED_SERIES_ID);
    Integer[] incomBudgetIds = getBudgetId(INCOME_SERIES_ID);
    Integer[] enveloppeBudgetIds = getBudgetId(ENVELOPPE_SERIES_ID);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<create active='true' amount='2000.0' day='4' id='" + incomBudgetIds[0] + "'" +
      "        month='200807' series='102' type='seriesBudget' overrunAmount='0.0'/>" +
      "<create active='true' amount='2000.0' day='4' id='" + incomBudgetIds[1] + "'" +
      "        month='200808' series='102' type='seriesBudget' overrunAmount='0.0'/>" +
      "<create active='true' amount='2000.0' day='4' id='" + incomBudgetIds[2] + "'" +
      "        month='200809' series='102' type='seriesBudget' overrunAmount='0.0'/>" +
      "<create active='true' amount='0.0' id='" + occasionalBudgetIds[0] + "' day='1' month='200807'" +
      "        series='0' type='seriesBudget' overrunAmount='0.0'/>" +
      "<create active='true' amount='0.0' id='" + occasionalBudgetIds[1] + "' day='1' month='200808'" +
      "        series='0' type='seriesBudget' overrunAmount='0.0'/>" +
      "<create active='true' amount='0.0' id='" + occasionalBudgetIds[2] + "' day='1' month='200809'" +
      "        series='0' type='seriesBudget' overrunAmount='0.0'/>" +
      "<create active='true' amount='-1000.0' day='25' id='" + enveloppeBudgetIds[0] + "'" +
      "        month='200807' series='101' type='seriesBudget' overrunAmount='0.0'/>" +
      "<create active='true' amount='-1000.0' day='25' id='" + enveloppeBudgetIds[1] + "'" +
      "        month='200808' series='101' type='seriesBudget' overrunAmount='0.0'/>" +
      "<create active='true' amount='-1000.0' day='25' id='" + enveloppeBudgetIds[2] + "'" +
      "        month='200809' series='101' type='seriesBudget' overrunAmount='0.0'/>" +
      "<create active='true' amount='0.0' id='" + unknownBudgetIds[2] + "' day='1' month='200809'" +
      "        overrunAmount='0.0' series='1' type='seriesBudget'/>" +
      "<create active='true' amount='0.0' id='" + unknownBudgetIds[1] + "' day='1' month='200808'" +
      "        overrunAmount='0.0' series='1' type='seriesBudget'/>" +
      "<create active='true' amount='0.0' id='" + unknownBudgetIds[0] + "' day='1' month='200807'" +
      "        overrunAmount='0.0' series='1' type='seriesBudget'/>");
    repository.startChangeSet();
    repository.create(Transaction.TYPE,
                      value(Transaction.ID, 100),
                      value(Transaction.SERIES, INCOME_SERIES_ID),
                      value(Transaction.AMOUNT, 1900.),
                      value(Transaction.MONTH, 200808),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.DAY, 1),
                      value(Transaction.BANK_DAY, 1),
                      value(Transaction.LABEL, "picsou"));
    repository.create(Transaction.TYPE,
                      value(Transaction.ID, 101),
                      value(Transaction.SERIES, ENVELOPPE_SERIES_ID),
                      value(Transaction.AMOUNT, -300.),
                      value(Transaction.MONTH, 200808),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.DAY, 1),
                      value(Transaction.BANK_DAY, 1),
                      value(Transaction.LABEL, "Auchan"));
    repository.completeChangeSet();
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "<update _amount='2000.0' amount='100.0' id='" + incomePlannedTransaction[0] + "' type='transaction'/>" +
      "<create amount='-300.0' bankMonth='200808' bankDay='1' day='1' id='101' label='Auchan' category='0'" +
      "        month='200808' planned='false' series='101' type='transaction' mirror='false'  createdBySeries='false'/>" +
      "<create amount='1900.0' bankMonth='200808' bankDay='1' day='1' id='100' label='picsou' category='0'" +
      "        month='200808' planned='false' series='102' type='transaction' mirror='false'  createdBySeries='false'/>" +
      "<update _amount='-1000.0' amount='-700.0' id='" + enveloppePlannedTransaction[0] + "' type='transaction'/>");
    repository.startChangeSet();
    repository.update(Key.create(Transaction.TYPE, 101),
                      value(Transaction.SERIES, ENVELOPPE_SERIES_ID),
                      value(Transaction.AMOUNT, -200.),
                      value(Transaction.MONTH, 200808),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.DAY, 1),
                      value(Transaction.BANK_DAY, 1),
                      value(Transaction.LABEL, "Auchan"));
    repository.create(Key.create(Transaction.TYPE, 102),
                      value(Transaction.AMOUNT, -100.),
                      value(Transaction.MONTH, 200808),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.DAY, 1),
                      value(Transaction.BANK_DAY, 1),
                      value(Transaction.SERIES, Series.OCCASIONAL_SERIES_ID));
    repository.completeChangeSet();
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "<update _amount='-300.0' amount='-200.0' id='101' type='transaction'/>\n" +
      "<update _amount='-700.0' amount='-800.0' id='" + enveloppePlannedTransaction[0] + "' type='transaction'/>\n" +
      "<create amount='-100.0' bankMonth='200808' bankDay='1' day='1'  id='102' month='200808'\n" +
      "        planned='false' series='0' type='transaction' category='0' mirror='false' createdBySeries='false'/>");
  }

  public void testOverrunEnveloppe() throws Exception {
    createEnveloppeSeries();
    createMonth(200807, 200808, 200809);
    repository.create(Transaction.TYPE,
                      value(Transaction.ID, 102),
                      value(Transaction.SERIES, ENVELOPPE_SERIES_ID),
                      value(Transaction.AMOUNT, -900.),
                      value(Transaction.MONTH, 200808),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.DAY, 1),
                      value(Transaction.BANK_DAY, 1),
                      value(Transaction.LABEL, "Auchan"));
    repository.create(Transaction.TYPE,
                      value(Transaction.ID, 103),
                      value(Transaction.SERIES, ENVELOPPE_SERIES_ID),
                      value(Transaction.AMOUNT, -600.),
                      value(Transaction.MONTH, 200808),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.DAY, 1),
                      value(Transaction.BANK_DAY, 1),
                      value(Transaction.LABEL, "Auchan"));
    Integer[] budget = getBudgetId(ENVELOPPE_SERIES_ID);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _overrunAmount='0.0' id='" + budget[1] + "' overrunAmount='-500.0' type='seriesBudget'/>");
    repository.update(Key.create(Transaction.TYPE, 103), Transaction.AMOUNT, -30.);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _overrunAmount='-500.0' id='" + budget[1] + "' overrunAmount='0.0' type='seriesBudget'/>");
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <update _amount='-600.0' amount='-30.0' id='103' type='transaction'/>" +
      "  <create account='-1' amount='-70.0' bankDay='25' bankMonth='200808' mirror='false'" +
      "          category='2' day='25' id='2' label='Planned: course' month='200808'" +
      "          planned='true' series='101' transactionType='5' type='transaction' createdBySeries='false'/>");
    repository.update(Key.create(Transaction.TYPE, 103), Transaction.AMOUNT, -200.);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _overrunAmount='0.0' id='" + budget[1] + "' overrunAmount='-100.0' type='seriesBudget'/>");
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <delete _account='-1' _amount='-70.0' _bankDay='25' _bankMonth='200808' _mirror='false'\n" +
      "          _category='2' _day='25' _label='Planned: course' _month='200808' _planned='true'\n" +
      "          _series='101' _transactionType='5' id='2' type='transaction' _createdBySeries='false'/>\n" +
      "  <update _amount='-30.0' amount='-200.0' id='103' type='transaction'/>");
  }

  public void testOverrunIncom() throws Exception {
    createIncomeSeries();
    createMonth(200807, 200808, 200809);
    repository.create(Transaction.TYPE,
                      value(Transaction.ID, 103),
                      value(Transaction.SERIES, INCOME_SERIES_ID),
                      value(Transaction.AMOUNT, 2200.),
                      value(Transaction.MONTH, 200808),
                      value(Transaction.BANK_MONTH, 200808),
                      value(Transaction.DAY, 1),
                      value(Transaction.BANK_DAY, 1),
                      value(Transaction.LABEL, "Auchan"));
    Integer[] budget = getBudgetId(INCOME_SERIES_ID);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _overrunAmount='0.0' id='" + budget[1] + "' overrunAmount='200.0' type='seriesBudget'/>");

    repository.update(Key.create(Transaction.TYPE, 103), Transaction.AMOUNT, 1900.);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _overrunAmount='200.0' id='" + budget[1] + "' overrunAmount='0.0' type='seriesBudget'/>");
    Integer[] transaction = getPlannedTransaction(INCOME_SERIES_ID);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <update _amount='2200.0' amount='1900.0' id='103' type='transaction'/>\n" +
      "  <create account='-1' amount='100.0' bankDay='4' bankMonth='200808'\n mirror='false'" +
      "          category='19' day='4' id='" + transaction[0] + "' label='Planned: salaire' month='200808'\n" +
      "          planned='true' series='102' transactionType='1' type='transaction' createdBySeries='false'/>");

    transaction = getPlannedTransaction(INCOME_SERIES_ID);
    repository.update(Key.create(Transaction.TYPE, 103), Transaction.AMOUNT, 2300.);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update overrunAmount='300.0' id='" + budget[1] + "' _overrunAmount='0.0' type='seriesBudget'/>");
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <update _amount='1900.0' amount='2300.0' id='103' type='transaction'/>\n" +
      "  <delete _account='-1' _amount='100.0' _bankDay='4' _bankMonth='200808' _mirror='false'\n" +
      "          _category='19' _day='4' _label='Planned: salaire' _month='200808' _planned='true'\n" +
      "          _series='102' _transactionType='1' id='" + transaction[0] + "' type='transaction' _createdBySeries='false'/>");

  }

  public void testOnOccasional() throws Exception {
    createMonth(200807, 200808, 200809);
    repository.create(Transaction.TYPE,
                      value(Transaction.ID, 103),
                      value(Transaction.SERIES, Series.OCCASIONAL_SERIES_ID),
                      value(Transaction.AMOUNT, 2200.),
                      value(Transaction.MONTH, 200808),
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

