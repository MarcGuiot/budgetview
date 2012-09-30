package org.designup.picsou.gui.budget.summary;

import junit.framework.TestCase;
import org.designup.picsou.gui.budget.BudgetAreaHeaderUpdater;
import org.designup.picsou.gui.components.ComponentTextDisplay;
import org.designup.picsou.gui.components.charts.Gauge;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.CurrentMonth;
import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.DefaultGlobRepository;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

import static org.globsframework.model.FieldValue.value;

public class BudgetAreaSummaryComputerTest extends TestCase {

  public void testStandardIncome() throws Exception {
    init(BudgetArea.INCOME, 1000, 1200, 200, 0)
      .checkObserved("1000.00")
      .checkPlanned("1200.00")
      .checkNormalTooltip()
      .checkNoOverrun()
      .checkGauge(1000, 1200);
  }

  public void testIncomeOverrun() throws Exception {
    init(BudgetArea.INCOME, 1000, 1200, 300, 100)
      .checkObserved("1000.00")
      .checkPlanned("1200.00")
      .checkOverrunTooltip("1300.00", "100.00")
      .checkPositiveOverrun()
      .checkGaugeWithPartialOverrun(1000, 1200, 100);
  }

  public void testIncomeWithNegativeObserved() throws Exception {
    init(BudgetArea.INCOME, -200, 1200, 1000, -200)
      .checkObserved("-200.00")
      .checkPlanned("1200.00")
      .checkOverrunTooltip("800.00", "200.00")
      .checkGauge(-200, 1200);
  }

  public void testIncomeWithNegativePlanned() throws Exception {
    init(BudgetArea.INCOME, -200, -1200, -1000, 0)
      .checkObserved("-200.00")
      .checkPlanned("-1200.00")
      .checkNoOverrun()
      .checkGauge(-200, -1200);
  }

  public void testEmptyIncome() throws Exception {
    init(BudgetArea.INCOME, 0, 0, 0, 0)
      .checkObserved("0.00")
      .checkPlanned("0.00")
      .checkNoOverrun()
      .checkGauge(0, 0);
  }

  public void testIncomeWithNoPlannedAndPositiveObserved() throws Exception {
    init(BudgetArea.INCOME, 200, 0, 0, 200)
      .checkObserved("200.00")
      .checkPlanned("0.00")
      .checkPositiveOverrun()
      .checkOverrunTooltip("200.00", "200.00")
      .checkGauge(200, 0);
  }

  public void testIncomeWithNoPlannedAndNegativeObserved() throws Exception {
    init(BudgetArea.INCOME, -200, 0, 0, -200)
      .checkObserved("-200.00")
      .checkPlanned("0.00")
      .checkErrorOverrun()
      .checkOverrunTooltip("-200.00", "200.00")
      .checkGauge(-200, 0);
  }

  public void testStandardExpenses() throws Exception {
    init(BudgetArea.VARIABLE, -1000, -1200, -200, 0)
      .checkObserved("1000.00")
      .checkPlanned("1200.00")
      .checkNormalTooltip()
      .checkNoOverrun()
      .checkGauge(-1000, -1200);
  }

  public void testExpensesWithNoObserved() throws Exception {
    init(BudgetArea.VARIABLE, 0, -1200, -1200, 0)
      .checkObserved("0.00")
      .checkPlanned("1200.00")
      .checkNoOverrun()
      .checkNormalTooltip()
      .checkGauge(0, -1200);
  }

  public void testExpensesWithNegativeOverrun() throws Exception {
    init(BudgetArea.VARIABLE, -1000, -1200, -300, -100)
      .checkObserved("1000.00")
      .checkPlanned("1200.00")
      .checkErrorOverrun()
      .checkOverrunTooltip("1300.00", "100.00")
      .checkGaugeWithPartialOverrun(-1000, -1200, -100);
  }

  public void testExpensesWithPositivePlanned() throws Exception {
    init(BudgetArea.EXTRAS, 1000, 1200, 200, 0)
      .checkObserved("+1000.00")
      .checkPlanned("+1200.00")
      .checkNoOverrun()
      .checkNormalTooltip()
      .checkGauge(1000, 1200);
  }

  public void testExpensesWithPositivePlannedAndOverrun() throws Exception {
    init(BudgetArea.EXTRAS, 1000, 1200, 300, 100)
      .checkObserved("+1000.00")
      .checkPlanned("+1200.00")
      .checkPositiveOverrun()
      .checkOverrunTooltip("+1300.00", "100.00")
      .checkGaugeWithPartialOverrun(1000, 1200, 100);
  }

  public void testExpensesWithPositiveObserved() throws Exception {
    init(BudgetArea.EXTRAS, 100, -1200, -1000, 100)
      .checkObserved("+100.00")
      .checkPlanned("1200.00")
      .checkOverrunTooltip("900.00", "100.00")
//      .checkNoLabelOverrun()
//      .checkGaugePositiveOverrun()
      .checkGauge(100, -1200);
  }

  public void testEmptyExpenses() throws Exception {
    init(BudgetArea.EXTRAS, 0, 0, 0, 0)
      .checkObserved("0.00")
      .checkPlanned("0.00")
      .checkNoOverrun()
      .checkGauge(0, 0);
  }

  public void testExpensesWithNoPlannedAndNegativeObserved() throws Exception {
    init(BudgetArea.EXTRAS, -200, 0, 0, -200)
      .checkObserved("200.00")
      .checkPlanned("0.00")
      .checkErrorOverrun()
      .checkOverrunTooltip("200.00", "200.00")
      .checkGauge(-200, 0);
  }

  public void testExpensesWithNoPlannedAndPositiveObserved() throws Exception {
    init(BudgetArea.EXTRAS, 200, 0, 0, 200)
      .checkObserved("+200.00")
      .checkPlanned("0.00")
      .checkPositiveOverrun()
      .checkOverrunTooltip("+200.00", "200.00")
      .checkGauge(200, 0);
  }

  public void testPastExpensesWithOverrun() throws Exception {
    initMonth(BudgetArea.EXTRAS, 200809, -110, -100, 0, -10)
      .checkObserved("110.00")
      .checkPlanned("100.00")
      .checkErrorOverrun()
      .checkOverrunTooltip("110.00", "10.00")
      .checkGauge(-110, -100);
  }

  public void testPastExpensesWithNoObserved() throws Exception {
    initMonth(BudgetArea.EXTRAS, 200809, 0, -100, 0, 0)
      .checkObserved("0.00")
      .checkPlanned("100.00")
      .checkNoOverrun()
      .checkNormalTooltip()
      .checkGauge(0, -100);
  }

  private Checker init(BudgetArea budgetArea, double observed, double planned, double remaining, final int overrun) {
    return initMonth(budgetArea, 200808, observed, planned, remaining, overrun);
  }

  private Checker initMonth(BudgetArea budgetArea, int currentMonth, double observed, double planned,
                            double remaining, final double overrun) {
    GlobRepository repository = new DefaultGlobRepository();

    Glob budgetStat =
      repository.create(BudgetStat.TYPE,
                        value(BudgetStat.MONTH, 200808),
                        value(BudgetStat.getObserved(budgetArea), observed),
                        value(BudgetStat.getPlanned(budgetArea), planned),
                        value(overrun < 0 ? BudgetStat.getNegativeOverrun(budgetArea) : BudgetStat.getPositiveOverrun(budgetArea),
                              overrun),
                        value(remaining < 0 ? BudgetStat.getNegativeRemaining(budgetArea) : BudgetStat.getPositiveRemaining(budgetArea),
                              remaining));

    repository.create(CurrentMonth.KEY, value(CurrentMonth.CURRENT_MONTH, currentMonth));

    return new Checker(budgetArea, budgetStat, repository);
  }

  private class Checker {
    private BudgetAreaSummaryComputer computer;
    private JLabel amountLabel = new JLabel();
    private JLabel plannedLabel = new JLabel();
    private Gauge gauge = new Gauge();

    private Checker(BudgetArea budgetArea, Glob budgetStat, GlobRepository repository) {

      Directory directory = new DefaultDirectory();
      directory.add(new ColorService());

      this.computer =
        new BudgetAreaHeaderUpdater(
          ComponentTextDisplay.create(amountLabel), ComponentTextDisplay.create(plannedLabel), gauge,
          repository, directory);

      this.computer.update(new GlobList(budgetStat), budgetArea);
    }

    public Checker checkObserved(String label) {
      assertEquals("Observed", label, amountLabel.getText());
      return this;
    }

    public Checker checkPlanned(String label) {
      assertEquals("Planned", label, plannedLabel.getText());
      return this;
    }

    public Checker checkOverrunTooltip(String total, String overrunPart) {
      assertEquals("Overrun tooltip",
                   Lang.get("budgetSummary.planned.tooltip.overrun", total, overrunPart),
                   plannedLabel.getToolTipText());
      return this;
    }

    public Checker checkNormalTooltip() {
      assertEquals("Normal tooltip",
                   Lang.get("budgetSummary.planned.tooltip.normal"),
                   plannedLabel.getToolTipText());
      return this;
    }

    public Checker checkNoOverrun() {
      checkNoLabelOverrun();
      assertFalse(gauge.isErrorOverrunShown());
      assertFalse(gauge.isPositiveOverrunShown());
      return this;
    }

    public Checker checkNoLabelOverrun() {
      assertFalse(computer.hasPositiveOverrun());
      assertFalse(computer.hasErrorOverrun());
      return this;
    }

    public Checker checkGaugePositiveOverrun() {
      assertTrue(gauge.isPositiveOverrunShown());
      assertFalse(gauge.isErrorOverrunShown());
      return this;
    }

    public Checker checkGaugeErrorOverrun() {
      assertTrue(gauge.isErrorOverrunShown());
      assertFalse(gauge.isPositiveOverrunShown());
      return this;
    }

    public Checker checkPositiveOverrun() {
      assertTrue(computer.hasPositiveOverrun());
      assertFalse(computer.hasErrorOverrun());
      assertTrue(gauge.isPositiveOverrunShown());
      assertFalse(gauge.isErrorOverrunShown());
      return this;
    }

    public Checker checkErrorOverrun() {
      assertTrue(computer.hasErrorOverrun());
      assertFalse(computer.hasPositiveOverrun());
      assertTrue(gauge.isErrorOverrunShown());
      assertFalse(gauge.isPositiveOverrunShown());
      return this;
    }

    public Checker checkBegin() {
      assertTrue(Amounts.isNotZero(gauge.getBeginPercent()));
      return this;
    }

    public Checker checkGauge(double actualValue, double targetValue) {
      assertEquals(actualValue, gauge.getActualValue());
      assertEquals(targetValue, gauge.getTargetValue());
      return this;
    }

    public Checker checkGaugeWithPartialOverrun(double actualValue, double targetValue, double overrunValue) {
      assertEquals(actualValue, gauge.getActualValue());
      assertEquals(targetValue, gauge.getTargetValue());
      assertEquals(overrunValue, gauge.getOverrunPart());
      return this;
    }
  }
}
