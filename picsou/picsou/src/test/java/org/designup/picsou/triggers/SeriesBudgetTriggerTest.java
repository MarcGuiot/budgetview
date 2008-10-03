package org.designup.picsou.triggers;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Key;

public class SeriesBudgetTriggerTest extends PicsouTriggerTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    createMonth(200809);
  }

  public void testCreateIncomeCreateOccasionalBudget() throws Exception {
    repository.enterBulkDispatchingMode();
    createSeries(10, BudgetArea.INCOME, 100.);
    createSeries(20, BudgetArea.ENVELOPES, -50.);
    repository.completeBulkDispatchingMode();
    Integer[] occasional = getBudgetId(Series.OCCASIONAL_SERIES_ID);
    Integer[] id10 = getBudgetId(10);
    Integer[] id20 = getBudgetId(20);
    listener.assertLastChangesEqual(
      SeriesBudget.TYPE,
      "<update _amount='0.0' amount='-50.0' id='" + occasional[0] + "' type='seriesBudget'/>" +
      "<create active='true' amount='-50.0' day='30' id='" + id20[0] + "'" +
      "        month='200809' series='20' type='seriesBudget' overrunAmount='0.0'/>" +
      "<create active='true' amount='100.0' day='30' id='" + id10[0] + "' type='seriesBudget' overrunAmount='0.0'" +
      "        month='200809' series='10'/>");
  }

  public void testCreateRecurringBudgetChangeOccasionalBudget() throws Exception {
    createSeries(10, BudgetArea.INCOME, 100.);
    createSeries(20, BudgetArea.RECURRING, -100.);
    Integer[] occasional = getBudgetId(Series.OCCASIONAL_SERIES_ID);
    Integer[] id20 = getBudgetId(20);
    listener.assertLastChangesEqual(SeriesBudget.TYPE,
                                    "<update _amount='-100.0' amount='0.0' id='" + occasional[0] + "' type='seriesBudget'/>" +
                                    "<create active='true' amount='-100.0' day='30' id='" + id20[0] + "'" +
                                    "        month='200809' series='20' type='seriesBudget' overrunAmount='0.0'/>");
  }

  public void testDeleteChangeOccasionalBudget() throws Exception {
    createSeries(10, BudgetArea.INCOME, 100.);
    createSeries(20, BudgetArea.RECURRING, -100.);
    Integer[] id0 = getBudgetId(Series.OCCASIONAL_SERIES_ID);
    Integer[] id20 = getBudgetId(20);
    repository.delete(Key.create(Series.TYPE, 20));
    listener.assertLastChangesEqual(SeriesBudget.TYPE,
                                    "<update _amount='0.0' amount='-100.0' id='" + id0[0] + "' type='seriesBudget'/>" +
                                    "<delete _amount='-100.0' _month='200809' _series='20' id='" + id20[0] + "'" +
                                    "        type='seriesBudget' _active='true' _day='30' _overrunAmount='0.0'/>");
  }

  private void createSeries(int seriesId, BudgetArea budgetArea, double amount) {
    repository.create(Key.create(Series.TYPE, seriesId),
                      value(Series.BUDGET_AREA, budgetArea.getId()),
                      value(Series.IS_AUTOMATIC, false),
                      value(Series.INITIAL_AMOUNT, amount));
  }
}
