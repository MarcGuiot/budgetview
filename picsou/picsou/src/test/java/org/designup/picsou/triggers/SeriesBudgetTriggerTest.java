package org.designup.picsou.triggers;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.utils.PicsouTestCase;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Key;

public class SeriesBudgetTriggerTest extends PicsouTestCase {
  protected void setUp() throws Exception {
    super.setUp();
    repository.addTrigger(new SeriesBudgetTrigger());
  }

  public void testCreateIncomeCreateOccasionalBudget() throws Exception {
    createSeries(1, BudgetArea.INCOME);
    createSeriesBudget(0, 1, 100.);
    listener.assertLastChangesEqual(SeriesBudget.TYPE,
                                    "  <create amount='100.0' id='1' month='200809' series='0'" +
                                    "          type='seriesBudget'/>\n" +
                                    "  <create amount='100.0' id='0' month='200809' series='1'" +
                                    "          type='seriesBudget'/>");
  }

  public void testUpdateBudgetChangeOccasionalBudget() throws Exception {
    createSeries(1, BudgetArea.INCOME);
    createSeriesBudget(0, 1, 100.);
    listener.reset();
    createSeries(2, BudgetArea.RECURRING_EXPENSES);
    createSeriesBudget(3, 2, 100.);
    listener.assertLastChangesEqual(SeriesBudget.TYPE,
                                    "  <update _amount='100.0' amount='0.0' id='1' type='seriesBudget'/>\n" +
                                    "  <create amount='100.0' id='3' month='200809' series='2'\n" +
                                    "          type='seriesBudget'/>");
  }

  public void testDeleteChangeOccasionalBudget() throws Exception {
    createSeries(1, BudgetArea.INCOME);
    createSeriesBudget(0, 1, 100.);
    listener.reset();
    createSeries(2, BudgetArea.RECURRING_EXPENSES);
    createSeriesBudget(3, 2, 100.);
    listener.reset();
    repository.delete(Key.create(SeriesBudget.TYPE, 3));
    listener.assertLastChangesEqual(SeriesBudget.TYPE,
                                    "<update _amount='0.0' amount='100.0' id='1' type='seriesBudget'/>\n" +
                                    "<delete _amount='100.0' _month='200809' _series='2' id='3'\n" +
                                    "        type='seriesBudget'/>");
  }

  private void createSeriesBudget(int seriesBudgetId, int seriesId, double amount) {
    repository.create(Key.create(SeriesBudget.TYPE, seriesBudgetId),
                      value(SeriesBudget.AMOUNT, amount),
                      value(SeriesBudget.MONTH, 200809),
                      value(SeriesBudget.SERIES, seriesId));
  }

  private void createSeries(int seriesId, BudgetArea budgetArea) {
    repository.create(Key.create(Series.TYPE, seriesId), value(Series.BUDGET_AREA, budgetArea.getId()));
  }
}
