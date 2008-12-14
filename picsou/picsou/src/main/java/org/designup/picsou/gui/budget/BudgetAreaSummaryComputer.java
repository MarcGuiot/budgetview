package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.components.Gauge;
import org.designup.picsou.gui.components.TextDisplay;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.BalanceStat;
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

import java.awt.*;

public class BudgetAreaSummaryComputer implements ColorChangeListener {
  private BudgetArea budgetArea;
  private TextDisplay amountLabel;
  private TextDisplay plannedLabel;
  private Gauge gauge;
  private GlobRepository repository;

  private double overrun;
  private double observed;
  private double initiallyPlanned;
  private double adjustedPlanned;

  private boolean isPartialOverrun;
  private double gaugeActual;
  private double gaugeTarget;

  private Color normalAmountColor;
  private Color errorOverrunAmountColor;
  private Color positiveOverrunAmountColor;
  private String normalAmountColorKey = "block.inner.amount";
  private String errorOverrunAmountColorKey = "block.inner.amount.overrun.error";
  private String positiveOverrunAmountColorKey = "block.inner.amount.overrun.positive";
  private ColorService colorService;

  public BudgetAreaSummaryComputer(BudgetArea budgetArea,
                                   TextDisplay amountLabel, TextDisplay plannedLabel, Gauge gauge,
                                   GlobRepository repository, Directory directory) {
    this.budgetArea = budgetArea;
    this.amountLabel = amountLabel;
    this.plannedLabel = plannedLabel;
    this.gauge = gauge;
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

  public void update(GlobList balanceStats) {
    if (balanceStats.isEmpty()) {
      clearComponents();
      return;
    }

    observed = 0.0;
    initiallyPlanned = 0.0;
    Double remaining = 0.0;
    for (Glob balanceStat : balanceStats) {
      observed += balanceStat.get(BalanceStat.getObserved(budgetArea));
      initiallyPlanned += balanceStat.get(BalanceStat.getPlanned(budgetArea));
      remaining += balanceStat.get(BalanceStat.getRemaining(budgetArea));
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
        gaugeTarget = adjustedPlanned;
      }
      else if (Amounts.sameSign(adjustedPlanned, initiallyPlanned)) {
        if (Math.abs(adjustedPlanned) > Math.abs(initiallyPlanned)) {
          isPartialOverrun = true;
          overrun = Amounts.normalize(adjustedPlanned - initiallyPlanned);
          gaugeActual = observed;
          gaugeTarget = adjustedPlanned;
        }
        else {
          isPartialOverrun = false;
          overrun = 0;
          gaugeActual = observed;
          gaugeTarget = adjustedPlanned;
        }
      }
      else {
        isPartialOverrun = false;
        overrun = adjustedPlanned;
        gaugeActual = observed;
        gaugeTarget = adjustedPlanned;
      }
    }

    else if (isPastMonths(balanceStats)) {
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

    updateComponents();
  }

  private boolean isPastMonths(GlobList balanceStats) {
    Integer currentMonthId = CurrentMonth.get(repository);
    if (currentMonthId == null) {
      return false;
    }

    final Integer lastStat = balanceStats.getSortedSet(BalanceStat.MONTH).last();

    return lastStat < currentMonthId;
  }

  public String getObservedLabel() {
    return format(observed);
  }

  public String getPlannedLabel() {
    return format(adjustedPlanned);
  }

  public String getPlannedTooltip() {
    if (Amounts.isNotZero(overrun)) {
      return Lang.get("monthsummary.planned.tooltip.overrun",
                      format(initiallyPlanned),
                      Formatting.toString(Math.abs(overrun)));
    }
    else {
      return Lang.get("monthsummary.planned.tooltip.normal");
    }
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

  private String format(Double value) {
    return Formatting.toString(value, budgetArea);
  }

  private void clearComponents() {
    amountLabel.setText(null);
    plannedLabel.setText(null);
    gauge.setValues(0, 0);
  }

  private void updateComponents() {
    amountLabel.setText(getObservedLabel());
    amountLabel.setVisible(true);

    plannedLabel.setText(getPlannedLabel());
    plannedLabel.setToolTipText(getPlannedTooltip());
    if (hasErrorOverrun()) {
      plannedLabel.setForeground(errorOverrunAmountColor);
    }
    else if (hasPositiveOverrun()) {
      plannedLabel.setForeground(positiveOverrunAmountColor);
    }
    else {
      plannedLabel.setForeground(normalAmountColor);
    }

    gauge.setOverrunIsAnError(observed < 0);
    if (isPartialOverrun) {
      gauge.setValues(gaugeActual, gaugeTarget, overrun);
    }
    else {
      gauge.setValues(gaugeActual, gaugeTarget);
    }

  }
}
