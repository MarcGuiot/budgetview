package com.budgetview.triggers;

import com.budgetview.model.Transaction;
import com.budgetview.model.SeriesBudget;

import static org.globsframework.model.utils.GlobMatchers.isTrue;

public class SeriesUpdateTriggerTest extends PicsouTriggerTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    createMonths(200807, 200808, 200809);
    createFreeSeries();
  }

  public void testCreateSeriesGeneratesSeriesBudgetsAndTransactions() throws Exception {
    Integer[] free = getBudgetId(FREE_SERIES_ID);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "  <create active='true' plannedAmount='-29.9' day='7' id='" + free[2] + "'" +
      "          month='200809' series='100' type='seriesBudget' />" +
      "  <create active='true' plannedAmount='-29.9' day='7' id='" + free[1] + "'" +
      "          month='200808' series='100' type='seriesBudget' />" +
      "  <create active='true' plannedAmount='-29.9' day='7' id='" + free[0] + "'" +
      "          month='200807' series='100' type='seriesBudget' />");

    Integer[] ids =
      repository.getAll(Transaction.TYPE, isTrue(Transaction.PLANNED))
        .sort(Transaction.BUDGET_MONTH)
        .getValues(Transaction.ID);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <create account='100' originalAccount='100' amount='-29.9' bankDay='18' bankMonth='200809' mirror='false'" +
      "          day='18' id='" + ids[1] + "' label='free telecom' month='200809'" +
      "          budgetDay='18' budgetMonth='200809' positionDay='18' positionMonth='200809' " +
      "          planned='true' series='100' transactionType='5' type='transaction' createdBySeries='false'/>" +
      "  <create account='100' originalAccount='100' amount='-29.9' bankDay='18' bankMonth='200808' mirror='false'" +
      "          day='18' id='" + ids[0] + "' label='free telecom' month='200808'" +
      "          budgetDay='18' budgetMonth='200808' positionDay='18' positionMonth='200808' " +
      "          planned='true' series='100' transactionType='5' type='transaction' createdBySeries='false'/>" +
      "");
  }
}
