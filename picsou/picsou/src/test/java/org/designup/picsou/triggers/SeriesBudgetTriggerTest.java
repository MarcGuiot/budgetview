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
    createSeries(1, BudgetArea.INCOME, 100.);
    createSeries(2, BudgetArea.EXPENSES_ENVELOPE, 50.);
    repository.completeBulkDispatchingMode();
    listener.assertLastChangesEqual(SeriesBudget.TYPE,
                                    "<update _amount='0.0' amount='50.0' id='0' type='seriesBudget'/>" +
                                    "<create active='true' amount='50.0' day='30' id='2'" +
                                    "        month='200809' series='2' type='seriesBudget'/>" +
                                    "<create active='true' amount='100.0' day='30' id='1' type='seriesBudget'" +
                                    "        month='200809' series='1'/>");
  }

  public void testUpdateBudgetChangeOccasionalBudget() throws Exception {
    createSeries(1, BudgetArea.INCOME, 100.);
    listener.reset();
    createSeries(2, BudgetArea.RECURRING_EXPENSES, 100.);
    listener.assertLastChangesEqual(SeriesBudget.TYPE,
                                    "<update _amount='100.0' amount='0.0' id='0' type='seriesBudget'/>" +
                                    "<create active='true' amount='100.0' day='30' id='2'" +
                                    "        month='200809' series='2' type='seriesBudget'/>");
  }

  public void testDeleteChangeOccasionalBudget() throws Exception {
    createSeries(1, BudgetArea.INCOME, 100.);
    listener.reset();
    createSeries(2, BudgetArea.RECURRING_EXPENSES, 100.);
    listener.reset();
    repository.delete(Key.create(SeriesBudget.TYPE, 2));
    listener.assertLastChangesEqual(SeriesBudget.TYPE,
                                    "<update _amount='0.0' amount='100.0' id='0' type='seriesBudget'/>" +
                                    "<delete _amount='100.0' _month='200809' _series='2' id='2'" +
                                    "        type='seriesBudget' _active='true' _day='30'/>");
  }

  private void createSeries(int seriesId, BudgetArea budgetArea, double amount) {
    repository.create(Key.create(Series.TYPE, seriesId),
                      value(Series.BUDGET_AREA, budgetArea.getId()),
                      value(Series.AMOUNT, amount));
  }
}
