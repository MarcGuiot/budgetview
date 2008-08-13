package org.designup.picsou.triggers;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.utils.PicsouTestCase;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Key;
import org.globsframework.utils.Dates;

public class BudgetStatTriggerTest extends PicsouTestCase {
  private Key budget_0;

  protected void setUp() throws Exception {
    super.setUp();
    directory.add(new TimeService(Dates.parse("2008/07/09")));
  }

  public void testOnCreateBudgetStat() throws Exception {
    createSeriesBudget();

    listener.assertLastChangesEqual(BudgetStat.TYPE,
                                    "<create type='budgetStat' amount='10.0' budgetArea='1' month='200808'/>");
  }

  private void createSeriesBudget() {
    repository.addTrigger(new BudgetStatTrigger());
    repository.create(Key.create(Series.TYPE, 0),
                      value(Series.BUDGET_AREA, BudgetArea.RECURRING_EXPENSES.getId()),
                      value(Series.AMOUNT, 30.0));
    budget_0 = Key.create(SeriesBudget.TYPE, 0);
    repository.create(budget_0,
                      value(SeriesBudget.AMOUNT, 10.0),
                      value(SeriesBudget.SERIES, 0),
                      value(SeriesBudget.MONTH, 200808));
  }

  public void testUpdateStat() throws Exception {
    createSeriesBudget();
    listener.reset();
    repository.update(budget_0, SeriesBudget.AMOUNT, 6.0);
    listener.assertLastChangesEqual(BudgetStat.TYPE,
                                    "<update type='budgetStat' amount='6.0' _amount='10.0'  " +
                                    "        budgetArea='1' month='200808'/>");
  }

  public void testDelete() throws Exception {
    createSeriesBudget();
    listener.reset();
    repository.delete(budget_0);
    listener.assertLastChangesEqual(BudgetStat.TYPE,
                                    "<update type='budgetStat' amount='0.0' _amount='10.0'  " +
                                    "        budgetArea='1' month='200808'/>");
  }
}
