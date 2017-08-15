package com.budgetview.desktop.budget.summary;

import com.budgetview.desktop.description.Formatting;
import com.budgetview.desktop.model.BudgetStat;
import com.budgetview.desktop.model.SavingsBudgetStat;
import com.budgetview.model.CurrentMonth;
import com.budgetview.shared.model.BudgetArea;
import com.budgetview.shared.utils.Amounts;
import com.budgetview.utils.Lang;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import java.awt.*;
import java.util.Set;

public abstract class BudgetAreaSummaryComputer implements ColorChangeListener {
  protected final GlobRepository repository;

  protected TotalBudgetAreaAmounts totalAmounts;
  protected Color normalAmountColor;
  protected Color errorOverrunAmountColor;
  protected Color positiveOverrunAmountColor;
  private String normalAmountColorKey = "block.inner.amount";
  private String errorOverrunAmountColorKey = "block.inner.amount.overrun.error";
  private String positiveOverrunAmountColorKey = "block.inner.amount.overrun.positive";
  private ColorService colorService;
  private Integer currentMonthId;


  public BudgetAreaSummaryComputer(final GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.colorService = directory.get(ColorService.class);
    this.colorService.addListener(this);
    if (repository.contains(CurrentMonth.KEY)) {
      currentMonthId = CurrentMonth.getCurrentMonth(repository);
    }
    totalAmounts = new TotalBudgetAreaAmounts() {
      protected Integer getCurrentMonths() {
        return currentMonthId;
      }
    };
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(CurrentMonth.KEY)) {
          currentMonthId = CurrentMonth.getCurrentMonth(repository);
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (repository.contains(CurrentMonth.KEY) || repository.contains(CurrentMonth.KEY)) {
          currentMonthId = CurrentMonth.getCurrentMonth(repository);
        }
      }
    }
    );
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
    totalAmounts.update(budgetStats, budgetArea);
    updateComponents(budgetArea);
  }

  static boolean isFutureOrPresentMonth(Glob budgetStat, final Integer currentMonthId) {
    Integer monthId = budgetStat.getType() == BudgetStat.TYPE ?
                      budgetStat.get(BudgetStat.MONTH) :
                      budgetStat.get(SavingsBudgetStat.MONTH);
    return monthId >= currentMonthId;
  }

  public String getObservedLabel(BudgetArea budgetArea) {
    return format(totalAmounts.getActual(), budgetArea);
  }

  public String getPlannedLabel(BudgetArea budgetArea) {
    return format(totalAmounts.getInitiallyPlanned(), budgetArea);
  }

  public String getPlannedTooltip(BudgetArea budgetArea) {
    if (Amounts.isNotZero(getOverrun())) {
      return Lang.get("budgetSummary.planned.tooltip.overrun",
                      format(totalAmounts.getAdjustedPlanned(), budgetArea),
                      Formatting.toString(Math.abs(getOverrun())));
    }
    else {
      return Lang.get("budgetSummary.planned.tooltip.normal");
    }
  }

  public void dispose() {
    colorService.removeListener(this);
  }

  public boolean hasPositiveOverrun() {
    Double overrun = getOverrun();
    return Amounts.isNotZero(overrun) && overrun > 0;
  }

  public boolean hasErrorOverrun() {
    Double overrun = getOverrun();
    return Amounts.isNotZero(overrun) && overrun < 0;
  }

  public Double getOverrun() {
    return totalAmounts.getFutureOverrun() + totalAmounts.getPastOverrun();
  }

  private String format(Double value, final BudgetArea budgetArea) {
    return Formatting.toString(value, budgetArea);
  }

  protected abstract void clearComponents();

  protected abstract void updateComponents(BudgetArea budgetArea);
}
