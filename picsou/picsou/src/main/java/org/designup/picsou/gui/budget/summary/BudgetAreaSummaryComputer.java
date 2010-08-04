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
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.awt.*;
import java.util.Set;

public abstract class BudgetAreaSummaryComputer implements ColorChangeListener {
  protected final GlobRepository repository;

  protected ComputeAmounts totalAmounts;
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
    totalAmounts = new ComputeAmounts(repository) {
      protected boolean isFuture(Glob budgetStat) {
        return isFutureOrPresentMonth(budgetStat, currentMonthId);
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

  public static abstract class ComputeAmounts {
    protected double futureOverrun;
    protected double observed;
    protected double initiallyPlanned;
    protected double adjustedPlanned;

    protected boolean isPartialOverrun;
    protected double gaugeActual;
    protected double gaugeTarget;
    private GlobRepository repository;
    private Double futureRemaining;
    private double pastRemaining;
    private double pastOverrun;
    private double totalRemaining;

    public ComputeAmounts(GlobRepository repository) {
      this.repository = repository;
    }

    public void update(GlobList budgetStats, BudgetArea budgetArea) {
      observed = 0.0;
      initiallyPlanned = 0.0;
      double futurRemaining = 0.0;
      double pastRemaining = 0.0;
      double futurOverrun = 0.0;
      double pastOverrun = 0.0;
      for (Glob budgetStat : budgetStats) {
        observed += getObserved(budgetStat, budgetArea);
        initiallyPlanned += getPlanned(budgetStat, budgetArea);
        if (isFuture(budgetStat)) {
          futurRemaining += getPositiveRemaining(budgetStat, budgetArea);
          futurRemaining += getNegativeRemaining(budgetStat, budgetArea);

          futurOverrun += getPositiveOverrun(budgetStat, budgetArea);
          futurOverrun += getNegativeOverrun(budgetStat, budgetArea);
        }
        else {
          pastRemaining += getPositiveRemaining(budgetStat, budgetArea);
          pastRemaining += getNegativeRemaining(budgetStat, budgetArea);
          pastOverrun += getPositiveOverrun(budgetStat, budgetArea);
          pastOverrun += getNegativeOverrun(budgetStat, budgetArea);
        }
      }
      futureRemaining = Amounts.normalize(futurRemaining);
      futureOverrun = Amounts.normalize(futurOverrun);

      this.pastRemaining = Amounts.normalize(pastRemaining);
      this.pastOverrun = Amounts.normalize(pastOverrun);

      totalRemaining = futureRemaining + this.pastRemaining;
      double totalOverrun = pastOverrun + futureOverrun;
      observed = Amounts.normalize(observed);
      initiallyPlanned = Amounts.normalize(initiallyPlanned);
      adjustedPlanned = Amounts.normalize(observed + totalRemaining);
      isPartialOverrun = !Amounts.isNearZero(totalOverrun);
      gaugeActual = observed;
      gaugeTarget = initiallyPlanned;
    }

    protected abstract boolean isFuture(Glob budgetStat);

    public double getFutureOverrun() {
      return futureOverrun;
    }

    public Double getFutureRemaining() {
      return futureRemaining;
    }

    public double getObserved() {
      return observed;
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

    public boolean isPartialOverrun() {
      return isPartialOverrun;
    }

    public double getGaugeActual() {
      return gaugeActual;
    }

    public double getGaugeTarget() {
      return gaugeTarget;
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

    protected Double getPositiveRemaining(Glob stat, BudgetArea budgetArea) {
      if (stat.getType() == BudgetStat.TYPE) {
        return stat.get(BudgetStat.getPositiveRemaining(budgetArea));
      }
      if (stat.getType() == SavingsBudgetStat.TYPE) {
        return stat.get(SavingsBudgetStat.getRemaining(budgetArea));
      }
      throw new UnexpectedApplicationState(stat.getType().getName());
    }

    protected Double getNegativeRemaining(Glob stat, BudgetArea budgetArea) {
      if (stat.getType() == BudgetStat.TYPE) {
        return stat.get(BudgetStat.getNegativeRemaining(budgetArea));
      }
      if (stat.getType() == SavingsBudgetStat.TYPE) {
        return stat.get(SavingsBudgetStat.getRemaining(budgetArea));
      }
      throw new UnexpectedApplicationState(stat.getType().getName());
    }

    protected Double getPositiveOverrun(Glob stat, BudgetArea budgetArea) {
      if (stat.getType() == BudgetStat.TYPE) {
        return stat.get(BudgetStat.getPositiveOverrun(budgetArea));
      }
      if (stat.getType() == SavingsBudgetStat.TYPE) {
        return stat.get(SavingsBudgetStat.getRemaining(budgetArea));
      }
      throw new UnexpectedApplicationState(stat.getType().getName());
    }

    protected Double getNegativeOverrun(Glob stat, BudgetArea budgetArea) {
      if (stat.getType() == BudgetStat.TYPE) {
        return stat.get(BudgetStat.getNegativeOverrun(budgetArea));
      }
      if (stat.getType() == SavingsBudgetStat.TYPE) {
        return stat.get(SavingsBudgetStat.getRemaining(budgetArea));
      }
      throw new UnexpectedApplicationState(stat.getType().getName());
    }
  }

  static boolean isFutureOrPresentMonth(Glob budgetStat, final Integer currentMonthId) {

    Integer monthId = budgetStat.getType() == BudgetStat.TYPE ?
                      budgetStat.get(BudgetStat.MONTH) :
                      budgetStat.get(SavingsBudgetStat.MONTH);
    return monthId >= currentMonthId;
  }

  public String getObservedLabel(BudgetArea budgetArea) {
    return format(totalAmounts.getObserved(), budgetArea);
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

  public Double getObserved() {
    return totalAmounts.getObserved();
  }

  public Double getGaugeTarget() {
    return totalAmounts.getGaugeTarget();
  }

  public Double getInitialyPlanned() {
    return totalAmounts.getInitiallyPlanned();
  }

  public Double getGaugeActual() {
    return totalAmounts.getGaugeActual();
  }

  private String format(Double value, final BudgetArea budgetArea) {
    return Formatting.toString(value, budgetArea);
  }

  protected abstract void clearComponents();

  protected abstract void updateComponents(BudgetArea budgetArea);
}
