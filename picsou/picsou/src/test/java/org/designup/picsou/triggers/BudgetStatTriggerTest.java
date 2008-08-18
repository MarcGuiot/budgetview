package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatchers;

public class BudgetStatTriggerTest extends PicsouTriggerTestCase {
  private Key budget_0;

  public void testOnCreateBudgetStat() throws Exception {
    createSeriesBudget();
    listener.assertLastChangesEqual(BudgetStat.TYPE,
                                    "<update type='budgetStat' amount='-30.0' month='200807' _amount='0.0' " +
                                    "        budgetArea='3'/>" +
                                    "<create type='budgetStat' amount='30.0' month='200807'  budgetArea='1'/>" +
                                    "");
  }

  public void testUpdateStat() throws Exception {
    createSeriesBudget();
    repository.update(budget_0, SeriesBudget.AMOUNT, 6.0);
    listener.assertLastChangesEqual(BudgetStat.TYPE,
                                    "<update type='budgetStat' amount='-6.0' _amount='-30.0'  " +
                                    "        budgetArea='3' month='200807'/>" +
                                    "<update type='budgetStat' amount='6.0' _amount='30.0'  " +
                                    "        budgetArea='1' month='200807'/>" +
                                    "");
  }

  public void testDeactivate() throws Exception {
    createSeriesBudget();
    listener.reset();
    repository.update(Key.create(Series.TYPE, 1), Series.JULY, false);
    listener.assertLastChangesEqual(BudgetStat.TYPE,
                                    "<update type='budgetStat' amount='0.0' _amount='30.0'  " +
                                    "        budgetArea='1' month='200807'/>");
  }

  private void createSeriesBudget() {
    createMonth(200807);
    repository.create(Key.create(Series.TYPE, 1),
                      value(Series.BUDGET_AREA, BudgetArea.RECURRING_EXPENSES.getId()),
                      value(Series.AMOUNT, 30.0));
    budget_0 = repository.getAll(SeriesBudget.TYPE, GlobMatchers.fieldEquals(SeriesBudget.SERIES, 1))
      .get(0).getKey();
  }
}
