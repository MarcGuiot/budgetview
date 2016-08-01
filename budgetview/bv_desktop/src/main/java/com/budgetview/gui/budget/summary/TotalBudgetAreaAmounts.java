package com.budgetview.gui.budget.summary;

import com.budgetview.gui.model.SavingsBudgetStat;
import com.budgetview.model.BudgetArea;
import com.budgetview.gui.model.BudgetStat;
import com.budgetview.shared.utils.Amounts;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

public abstract class TotalBudgetAreaAmounts {
  private double actual;
  private double initiallyPlanned;
  private double adjustedPlanned;
  private double futureRemaining;
  private double futureOverrun;
  private double pastRemaining;
  private double pastOverrun;

  public void update(GlobList budgetStats, BudgetArea budgetArea) {
    actual = 0.0;
    initiallyPlanned = 0.0;
    futureRemaining = 0.0;
    pastRemaining = 0.0;
    futureOverrun = 0.0;
    pastOverrun = 0.0;
    for (Glob budgetStat : budgetStats) {
      actual += getObserved(budgetStat, budgetArea);
      initiallyPlanned += getPlanned(budgetStat, budgetArea);
      if (isFuture(budgetStat)) {
        futureRemaining += getPositiveRemaining(budgetStat, budgetArea);
        futureRemaining += getNegativeRemaining(budgetStat, budgetArea);

        futureOverrun += getPositiveOverrun(budgetStat, budgetArea);
        futureOverrun += getNegativeOverrun(budgetStat, budgetArea);
      }
      else {
        pastRemaining += getPositiveRemaining(budgetStat, budgetArea);
        pastRemaining += getNegativeRemaining(budgetStat, budgetArea);
        pastOverrun += getPositiveOverrun(budgetStat, budgetArea);
        pastOverrun += getNegativeOverrun(budgetStat, budgetArea);
      }
    }
    actual = Amounts.normalize(actual);
    initiallyPlanned = Amounts.normalize(initiallyPlanned);
    futureRemaining = Amounts.normalize(futureRemaining);
    futureOverrun = Amounts.normalize(futureOverrun);
    pastRemaining = Amounts.normalize(pastRemaining);
    pastOverrun = Amounts.normalize(pastOverrun);
    adjustedPlanned = Amounts.normalize(actual + futureRemaining + pastRemaining);
  }

  protected boolean isFuture(Glob budgetStat) {
    return BudgetAreaSummaryComputer.isFutureOrPresentMonth(budgetStat, getCurrentMonths());
  }

  protected abstract Integer getCurrentMonths();

  public double getFutureOverrun() {
    return futureOverrun;
  }

  public Double getFutureRemaining() {
    return futureRemaining;
  }

  public double getActual() {
    return actual;
  }

  public double getInitiallyPlanned() {
    return initiallyPlanned;
  }

  public double getAdjustedPlanned() {
    return adjustedPlanned;
  }

  public Double getPastRemaining() {
    return pastRemaining;
  }

  public Double getPastOverrun() {
    return pastOverrun;
  }

  private Double getObserved(Glob stat, BudgetArea budgetArea) {
    if (stat.getType() == BudgetStat.TYPE) {
      return stat.get(BudgetStat.getObserved(budgetArea));
    }
    if (stat.getType() == SavingsBudgetStat.TYPE) {
      return stat.get(SavingsBudgetStat.getObserved(budgetArea));
    }
    throw new UnexpectedApplicationState(stat.getType().getName());
  }

  private Double getPlanned(Glob stat, BudgetArea budgetArea) {
    if (stat.getType() == BudgetStat.TYPE) {
      return stat.get(BudgetStat.getPlanned(budgetArea));
    }
    if (stat.getType() == SavingsBudgetStat.TYPE) {
      return stat.get(SavingsBudgetStat.getPlanned(budgetArea));
    }
    throw new UnexpectedApplicationState(stat.getType().getName());
  }

  private Double getPositiveRemaining(Glob stat, BudgetArea budgetArea) {
    if (stat.getType() == BudgetStat.TYPE) {
      return stat.get(BudgetStat.getPositiveRemaining(budgetArea));
    }
    if (stat.getType() == SavingsBudgetStat.TYPE) {
      return stat.get(SavingsBudgetStat.getRemaining(budgetArea));
    }
    throw new UnexpectedApplicationState(stat.getType().getName());
  }

  private Double getNegativeRemaining(Glob stat, BudgetArea budgetArea) {
    if (stat.getType() == BudgetStat.TYPE) {
      return stat.get(BudgetStat.getNegativeRemaining(budgetArea));
    }
    if (stat.getType() == SavingsBudgetStat.TYPE) {
      return stat.get(SavingsBudgetStat.getRemaining(budgetArea));
    }
    throw new UnexpectedApplicationState(stat.getType().getName());
  }

  private Double getPositiveOverrun(Glob stat, BudgetArea budgetArea) {
    if (stat.getType() == BudgetStat.TYPE) {
      return stat.get(BudgetStat.getPositiveOverrun(budgetArea));
    }
    if (stat.getType() == SavingsBudgetStat.TYPE) {
      return stat.get(SavingsBudgetStat.getRemaining(budgetArea));
    }
    throw new UnexpectedApplicationState(stat.getType().getName());
  }

  private Double getNegativeOverrun(Glob stat, BudgetArea budgetArea) {
    if (stat.getType() == BudgetStat.TYPE) {
      return stat.get(BudgetStat.getNegativeOverrun(budgetArea));
    }
    if (stat.getType() == SavingsBudgetStat.TYPE) {
      return stat.get(SavingsBudgetStat.getRemaining(budgetArea));
    }
    throw new UnexpectedApplicationState(stat.getType().getName());
  }
}
