package org.designup.picsou.gui.budget.summary;

import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.model.SavingsBudgetStat;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.util.Amounts;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.awt.*;

public abstract class BudgetAreaSummaryComputer implements ColorChangeListener {
  protected final GlobRepository repository;

  protected double overrun;
  protected double observed;
  protected double initiallyPlanned;
  protected double adjustedPlanned;

  protected boolean isPartialOverrun;
  protected double gaugeActual;
  protected double gaugeTarget;

  protected Color normalAmountColor;
  protected Color errorOverrunAmountColor;
  protected Color positiveOverrunAmountColor;

  private String normalAmountColorKey = "block.inner.amount";
  private String errorOverrunAmountColorKey = "block.inner.amount.overrun.error";
  private String positiveOverrunAmountColorKey = "block.inner.amount.overrun.positive";
  private ColorService colorService;

  public BudgetAreaSummaryComputer(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.colorService = directory.get(ColorService.class);
    this.colorService.addListener(this);
  }

  public void setColors(String normalAmountColorKey,
                        String errorOverrunAmountColorKey,
                        String positiveOverrunAmountColorKey) {
    this.normalAmountColorKey = normalAmountColorKey;
    this.errorOverrunAmountColorKey = errorOverrunAmountColorKey;
    this.positiveOverrunAmountColorKey = positiveOverrunAmountColorKey;
    colorsChanged(colorService);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    this.normalAmountColor = colorLocator.get(normalAmountColorKey);
    this.errorOverrunAmountColor = colorLocator.get(errorOverrunAmountColorKey);
    this.positiveOverrunAmountColor = colorLocator.get(positiveOverrunAmountColorKey);
  }

  public void update(GlobList budgetStats, BudgetArea budgetArea) {
    if (budgetStats.isEmpty()) {
      clearComponents();
      return;
    }

    observed = 0.0;
    initiallyPlanned = 0.0;
    Double remaining = 0.0;
    for (Glob budgetStat : budgetStats) {
      observed += getObserved(budgetStat, budgetArea);
      initiallyPlanned += getPlanned(budgetStat, budgetArea);
      remaining += getRemaining(budgetStat, budgetArea);
    }

    observed = Amounts.normalize(observed);
    remaining = Amounts.normalize(remaining);
    initiallyPlanned = Amounts.normalize(initiallyPlanned);

    if (Amounts.isNotZero(remaining)) {

      adjustedPlanned = Amounts.normalize(observed + remaining);

      if (Amounts.isNearZero(initiallyPlanned) && Amounts.isNotZero(adjustedPlanned)) {
        isPartialOverrun = true;
        overrun = observed;
        gaugeActual = observed;
        gaugeTarget = initiallyPlanned;
      }
      else if (Amounts.sameSign(adjustedPlanned, initiallyPlanned)) {
        if (Math.abs(adjustedPlanned) > Math.abs(initiallyPlanned)) {
          isPartialOverrun = true;
          overrun = Amounts.normalize(adjustedPlanned - initiallyPlanned);
          gaugeActual = observed;
          gaugeTarget = initiallyPlanned;
        }
        else {
          isPartialOverrun = false;
          overrun = 0;
          gaugeActual = observed;
          gaugeTarget = initiallyPlanned;
        }
      }
      else {
        isPartialOverrun = false;
        overrun = adjustedPlanned;
        gaugeActual = observed;
        gaugeTarget = initiallyPlanned;
      }
    }
    else if (isPastMonths(budgetStats)) {
      isPartialOverrun = false;
      gaugeActual = observed;
      gaugeTarget = initiallyPlanned;
      if (Amounts.isNearZero(initiallyPlanned) && Amounts.isNotZero(observed)) {
        adjustedPlanned = observed;
        overrun = observed;
      }
      else if (Amounts.isNearZero(observed) && Amounts.isNotZero(initiallyPlanned)) {
        adjustedPlanned = initiallyPlanned;
        overrun = 0;
      }
      else if (Amounts.sameSign(observed, initiallyPlanned)) {
        if (Math.abs(observed) > Math.abs(initiallyPlanned)) {
          adjustedPlanned = observed;
          overrun = Amounts.normalize(observed - initiallyPlanned);
        }
        else {
          adjustedPlanned = initiallyPlanned;
          overrun = 0;
        }
      }
      else {
        adjustedPlanned = observed;
        overrun = Amounts.normalize(observed - initiallyPlanned);
      }
    }
    else {

      adjustedPlanned = Amounts.normalize(observed + remaining);

      isPartialOverrun = false;
      gaugeActual = observed;
      gaugeTarget = initiallyPlanned;
      if (Amounts.isNearZero(initiallyPlanned) && (Amounts.isNotZero(observed))) {
        overrun = observed;
      }
      else if (Amounts.sameSign(observed, initiallyPlanned)) {
        if (Math.abs(observed) > Math.abs(initiallyPlanned)) {
          overrun = Amounts.normalize(observed - initiallyPlanned);
        }
        else {
          overrun = 0;
        }
      }
      else {
        overrun = Amounts.normalize(observed - initiallyPlanned);
      }
    }

    updateComponents(budgetArea);
  }

  protected Double getObserved(Glob stat, BudgetArea budgetArea) {
    if (stat.getType() == BudgetStat.TYPE) {
      return stat.get(BudgetStat.getObserved(budgetArea));
    }
    if (stat.getType() == SavingsBudgetStat.TYPE) {
      return stat.get(SavingsBudgetStat.getObserved(budgetArea));
    }
    throw new UnexpectedApplicationState(stat.getType().getName());
  }

  protected Double getPlanned(Glob stat, BudgetArea budgetArea) {
    if (stat.getType() == BudgetStat.TYPE) {
      return stat.get(BudgetStat.getPlanned(budgetArea));
    }
    if (stat.getType() == SavingsBudgetStat.TYPE) {
      return stat.get(SavingsBudgetStat.getPlanned(budgetArea));
    }
    throw new UnexpectedApplicationState(stat.getType().getName());
  }

  protected Double getRemaining(Glob stat, BudgetArea budgetArea) {
    if (stat.getType() == BudgetStat.TYPE) {
      return stat.get(BudgetStat.getRemaining(budgetArea));
    }
    if (stat.getType() == SavingsBudgetStat.TYPE) {
      return stat.get(SavingsBudgetStat.getRemaining(budgetArea));
    }
    throw new UnexpectedApplicationState(stat.getType().getName());
  }

  private boolean isPastMonths(GlobList budgetStats) {
    Integer currentMonthId = CurrentMonth.getLastTransactionMonth(repository);
    if (currentMonthId == null) {
      return false;
    }

    Integer lastMonth = -1;
    for (Glob budgetStat : budgetStats) {
      Integer monthId = budgetStat.getType() == BudgetStat.TYPE ?
                        budgetStat.get(BudgetStat.MONTH) :
                        budgetStat.get(SavingsBudgetStat.MONTH);
      lastMonth = monthId > lastMonth ? monthId : lastMonth;
    }

    return lastMonth < currentMonthId;
  }

  public String getObservedLabel(BudgetArea budgetArea) {
    return format(observed, budgetArea);
  }

  public String getPlannedLabel(BudgetArea budgetArea) {
    return format(initiallyPlanned, budgetArea);
  }

  public String getPlannedTooltip(BudgetArea budgetArea) {
    if (Amounts.isNotZero(overrun)) {
      return Lang.get("budgetSummary.planned.tooltip.overrun",
                      format(adjustedPlanned, budgetArea),
                      Formatting.toString(Math.abs(overrun)));
    }
    else {
      return Lang.get("budgetSummary.planned.tooltip.normal");
    }
  }

  public void dispose() {
    colorService.removeListener(this);
  }

  public boolean isPartialOverrun() {
    return isPartialOverrun;
  }

  public boolean hasPositiveOverrun() {
    return Amounts.isNotZero(overrun) && overrun > 0;
  }

  public boolean hasErrorOverrun() {
    return Amounts.isNotZero(overrun) && overrun < 0;
  }

  private String format(Double value, final BudgetArea budgetArea) {
    return Formatting.toString(value, budgetArea);
  }

  protected abstract void clearComponents();

  protected abstract void updateComponents(BudgetArea budgetArea);
}