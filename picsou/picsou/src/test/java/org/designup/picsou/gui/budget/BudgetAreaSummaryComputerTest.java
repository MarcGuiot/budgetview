package org.designup.picsou.gui.budget;

import junit.framework.TestCase;
import org.designup.picsou.gui.components.Gauge;
import org.designup.picsou.gui.components.TextDisplay;
import org.designup.picsou.gui.model.BalanceStat;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.color.ColorService;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.impl.DefaultGlobRepository;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class BudgetAreaSummaryComputerTest extends TestCase {

  public void testStandardIncome() throws Exception {
    init(BudgetArea.INCOME, 1000, 1200, 200)
      .checkObserved("1000.00")
      .checkPlanned("1200.00")
      .checkNormalTooltip()
      .checkNoOverrun()
      .checkGauge(1000, 1200);
  }

  public void testIncomeOverrun() throws Exception {
    init(BudgetArea.INCOME, 1000, 1200, 300)
      .checkObserved("1000.00")
      .checkPlanned("1300.00")
      .checkOverrunTooltip("1200.00", "100.00")
      .checkPositiveOverrun()
      .checkGaugeWithPartialOverrun(1000, 1300, 100);
  }

  public void testIncomeWithNegativeObserved() throws Exception {
    init(BudgetArea.INCOME, -200, 1200, 1000)
      .checkObserved("-200.00")
      .checkPlanned("800.00")
      .checkNoLabelOverrun()
      .checkGaugeErrorOverrun()
      .checkNormalTooltip()
      .checkGauge(-200, 800);
  }

  public void testIncomeWithNegativePlanned() throws Exception {
    init(BudgetArea.INCOME, -200, -1200, -1000)
      .checkObserved("-200.00")
      .checkPlanned("-1200.00")
      .checkNoOverrun()
      .checkGauge(-200, -1200);
  }

  public void testEmptyIncome() throws Exception {
    init(BudgetArea.INCOME, 0, 0, 0)
      .checkObserved("0.00")
      .checkPlanned("0.00")
      .checkNoOverrun()
      .checkGauge(0, 0);
  }

  public void testIncomeWithNoPlannedAndPositiveObserved() throws Exception {
    init(BudgetArea.INCOME, 200, 0, 0)
      .checkObserved("200.00")
      .checkPlanned("200.00")
      .checkPositiveOverrun()
      .checkOverrunTooltip("0.00", "200.00")
      .checkGauge(200, 0);
  }

  public void testIncomeWithNoPlannedAndNegativeObserved() throws Exception {
    init(BudgetArea.INCOME, -200, 0, 0)
      .checkObserved("-200.00")
      .checkPlanned("-200.00")
      .checkErrorOverrun()
      .checkOverrunTooltip("0.00", "200.00")
      .checkGauge(-200, 0);
  }

  public void testStandardExpenses() throws Exception {
    init(BudgetArea.ENVELOPES, -1000, -1200, -200)
      .checkObserved("1000.00")
      .checkPlanned("1200.00")
      .checkNormalTooltip()
      .checkNoOverrun()
      .checkGauge(-1000, -1200);
  }

  public void testExpensesWithNoObserved() throws Exception {
    init(BudgetArea.ENVELOPES, 0, -1200, -1200)
      .checkObserved("0.00")
      .checkPlanned("1200.00")
      .checkNoOverrun()
      .checkNormalTooltip()
      .checkGauge(0, -1200);
  }

  public void testExpensesWithNegativeOverrun() throws Exception {
    init(BudgetArea.ENVELOPES, -1000, -1200, -300)
      .checkObserved("1000.00")
      .checkPlanned("1300.00")
      .checkErrorOverrun()
      .checkOverrunTooltip("1200.00", "100.00")
      .checkGaugeWithPartialOverrun(-1000, -1300, -100);
  }

  public void testExpensesWithPositivePlanned() throws Exception {
    init(BudgetArea.SPECIAL, 1000, 1200, 200)
      .checkObserved("+1000.00")
      .checkPlanned("+1200.00")
      .checkNoOverrun()
      .checkNormalTooltip()
      .checkGauge(1000, 1200);
  }

  public void testExpensesWithPositivePlannedAndOverrun() throws Exception {
    init(BudgetArea.SPECIAL, 1000, 1200, 300)
      .checkObserved("+1000.00")
      .checkPlanned("+1300.00")
      .checkPositiveOverrun()
      .checkOverrunTooltip("+1200.00", "100.00")
      .checkGaugeWithPartialOverrun(1000, 1300, 100);
  }

  public void testExpensesWithPositiveObserved() throws Exception {
    init(BudgetArea.SPECIAL, 100, -1200, -1000)
      .checkObserved("+100.00")
      .checkPlanned("900.00")
      .checkNormalTooltip()
      .checkNoLabelOverrun()
      .checkGaugePositiveOverrun()
      .checkGauge(100, -900);
  }

  public void testEmptyExpenses() throws Exception {
    init(BudgetArea.SPECIAL, 0, 0, 0)
      .checkObserved("0.00")
      .checkPlanned("0.00")
      .checkNoOverrun()
      .checkGauge(0, 0);
  }

  public void testExpensesWithNoPlannedAndNegativeObserved() throws Exception {
    init(BudgetArea.SPECIAL, -200, 0, 0)
      .checkObserved("200.00")
      .checkPlanned("200.00")
      .checkErrorOverrun()
      .checkOverrunTooltip("0.00", "200.00")
      .checkGauge(-200, 0);
  }

  public void testExpensesWithNoPlannedAndPositiveObserved() throws Exception {
    init(BudgetArea.SPECIAL, 200, 0, 0)
      .checkObserved("+200.00")
      .checkPlanned("+200.00")
      .checkPositiveOverrun()
      .checkOverrunTooltip("0.00", "200.00")
      .checkGauge(200, 0);
  }

  public void testPastExpensesWithOverrun() throws Exception {
    init(BudgetArea.SPECIAL, 200809, -110, -100, 0)
      .checkObserved("110.00")
      .checkPlanned("110.00")
      .checkErrorOverrun()
      .checkOverrunTooltip("100.00", "10.00")
      .checkGauge(-110, -100);
  }

  public void testPastExpensesWithNoObserved() throws Exception {
    init(BudgetArea.SPECIAL, 200809, 0, -100, 0)
      .checkObserved("0.00")
      .checkPlanned("100.00")
      .checkNoOverrun()
      .checkNormalTooltip()
      .checkGauge(0, -100);
  }

  private Checker init(BudgetArea budgetArea, double observed, double planned, double remaining) {
    return init(budgetArea, 200808, observed, planned, remaining);
  }

  private Checker init(BudgetArea budgetArea, int currentMonth, double observed, double planned, double remaining) {
    GlobRepository repository = new DefaultGlobRepository();

    Glob balanceStat =
      repository.create(BalanceStat.TYPE,
                        value(BalanceStat.MONTH, 200808),
                        value(BalanceStat.getObserved(budgetArea), observed),
                        value(BalanceStat.getPlanned(budgetArea), planned),
                        value(BalanceStat.getRemaining(budgetArea), remaining));

    repository.create(CurrentMonth.KEY, value(CurrentMonth.LAST_TRANSACTION_MONTH, currentMonth));

    return new Checker(budgetArea, balanceStat, repository);
  }

  private class Checker {
    private BudgetAreaSummaryComputer computer;
    private JLabel amountLabel = new JLabel();
    private JLabel plannedLabel = new JLabel();
    private Gauge gauge = new Gauge();

    private Checker(BudgetArea budgetArea, Glob balanceStat, GlobRepository repository) {

      Directory directory = new DefaultDirectory();
      directory.add(new ColorService());

      this.computer =
        new BudgetAreaSummaryComputer(budgetArea,
                                      TextDisplay.create(amountLabel), TextDisplay.create(plannedLabel), gauge,
                                      repository, directory);

      this.computer.update(new GlobList(balanceStat));
    }

    public Checker checkObserved(String label) {
      assertEquals("Observed", label, amountLabel.getText());
      return this;
    }

    public Checker checkPlanned(String label) {
      assertEquals("Planned", label, plannedLabel.getText());
      return this;
    }

    public Checker checkOverrunTooltip(String planned, String overrunPart) {
      assertEquals("Overrun tooltip",
                   Lang.get("monthsummary.planned.tooltip.overrun", planned, overrunPart),
                   plannedLabel.getToolTipText());
      return this;
    }

    public Checker checkNormalTooltip() {
      assertEquals("Normal tooltip",
                   Lang.get("monthsummary.planned.tooltip.normal"),
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
