package org.designup.picsou.triggers;

import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.model.Transaction;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatchers;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;
import static org.globsframework.model.utils.GlobMatchers.*;

public class SeriesUpdateTriggerTest extends PicsouTriggerTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    createMonth(200807, 200808, 200809);
    createFreeSeries();
  }

  public void testCreateSeriesGeneratesSeriesBudgetsAndTransactions() throws Exception {
    Integer[] occasional = getBudgetId(0);
    Integer[] free = getBudgetId(FREE_SERIES_ID);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "  <create active='true' amount='-29.9' day='7' id='" + free[2] + "'" +
      "          month='200809' series='100' type='seriesBudget' />" +
      "  <create active='true' amount='-29.9' day='7' id='" + free[1] + "'" +
      "          month='200808' series='100' type='seriesBudget' />" +
      "  <create active='true' amount='-29.9' day='7' id='" + free[0] + "'" +
      "          month='200807' series='100' type='seriesBudget' />");

    Integer[] ids =
      repository.getAll(Transaction.TYPE, isTrue(Transaction.PLANNED))
        .sort(Transaction.BUDGET_MONTH)
        .getValues(Transaction.ID);
    listener.assertLastChangesEqual(
      Transaction.TYPE,
      "  <create account='-1' amount='-29.9' bankDay='16' bankMonth='200809' mirror='false'" +
      "          day='16' id='" + ids[1] + "' label='free telecom' month='200809'" +
      "          budgetDay='16' budgetMonth='200809' positionDay='16' positionMonth='200809' " +
      "          planned='true' series='100' transactionType='5' type='transaction' createdBySeries='false'/>" +
      "  <create account='-1' amount='-29.9' bankDay='16' bankMonth='200808' mirror='false'" +
      "          day='16' id='" + ids[0] + "' label='free telecom' month='200808'" +
      "          budgetDay='16' budgetMonth='200808' positionDay='16' positionMonth='200808' " +
      "          planned='true' series='100' transactionType='5' type='transaction' createdBySeries='false'/>" +
      "");
  }
}
