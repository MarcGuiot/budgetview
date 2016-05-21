package com.budgetview.gui.budget;

import com.budgetview.gui.model.PeriodBudgetAreaStat;
import com.budgetview.shared.utils.Amounts;
import com.budgetview.gui.description.Formatting;
import com.budgetview.model.BudgetArea;
import com.budgetview.utils.Lang;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.KeyChangeListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public class BudgetAreaHeaderComponentsUpdater extends KeyChangeListener implements ColorChangeListener {

  private BudgetArea budgetArea;
  private final JLabel amountLabel;
  private final JLabel plannedAmountLabel;
  private final GlobRepository repository;

  protected Color normalAmountColor;
  protected Color errorOverrunAmountColor;
  protected Color positiveOverrunAmountColor;

  public static void init(BudgetArea budgetArea, Key statKey, JLabel amountLabel, JLabel plannedAmountLabel,
                          GlobRepository repository, Directory directory) {
    repository.addChangeListener(new BudgetAreaHeaderComponentsUpdater(budgetArea, statKey,
                                                                       amountLabel, plannedAmountLabel,
                                                                       repository, directory));
  }

  private BudgetAreaHeaderComponentsUpdater(BudgetArea budgetArea, Key statKey,
                                            JLabel amountLabel, JLabel plannedAmountLabel,
                                            GlobRepository repository, Directory directory) {
    super(statKey);
    this.budgetArea = budgetArea;
    this.amountLabel = amountLabel;
    this.plannedAmountLabel = plannedAmountLabel;
    this.repository = repository;
    directory.get(ColorService.class).addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    normalAmountColor = colorLocator.get("block.total");
    errorOverrunAmountColor = colorLocator.get("block.total.overrun.error");
    positiveOverrunAmountColor = colorLocator.get("block.total.overrun.positive");
  }

  public void update() {

    Glob stat = repository.find(key);
    if (stat == null) {
      amountLabel.setText("");
      plannedAmountLabel.setText("");
      return;
    }

    Double amount = stat.get(PeriodBudgetAreaStat.AMOUNT, 0.00);
    Double plannedAmount = stat.get(PeriodBudgetAreaStat.PLANNED_AMOUNT, 0.00);
    Double futureRemaining = stat.get(PeriodBudgetAreaStat.FUTURE_REMAINING, 0.00);
    Double pastRemaining = stat.get(PeriodBudgetAreaStat.PAST_REMAINING, 0.00);
    Double futureOverrun = stat.get(PeriodBudgetAreaStat.FUTURE_OVERRUN, 0.00);
    Double pastOverrun = stat.get(PeriodBudgetAreaStat.PAST_OVERRUN, 0.00);
    Double overrun = futureOverrun + pastOverrun;
    double adjustedPlanned = Amounts.normalize(amount + futureRemaining + pastRemaining);

    amountLabel.setText(format(amount));
    plannedAmountLabel.setText(format(plannedAmount));
    plannedAmountLabel.setToolTipText(getPlannedTooltip(overrun, adjustedPlanned));
    if (hasErrorOverrun(overrun)) {
      plannedAmountLabel.setForeground(errorOverrunAmountColor);
    }
    else if (hasPositiveOverrun(overrun)) {
      plannedAmountLabel.setForeground(positiveOverrunAmountColor);
    }
    else {
      plannedAmountLabel.setForeground(normalAmountColor);
    }
  }

  public String getPlannedTooltip(double overrun, double adjustedPlanned) {
    if (Amounts.isNotZero(overrun)) {
      return Lang.get("budgetSummary.planned.tooltip.overrun",
                      format(adjustedPlanned),
                      Formatting.toString(Math.abs(overrun)));
    }
    else {
      return Lang.get("budgetSummary.planned.tooltip.normal");
    }
  }

  private String format(Double value) {
    return Formatting.toString(value, budgetArea);
  }

  public boolean hasPositiveOverrun(Double overrun) {
    return Amounts.isNotZero(overrun) && overrun > 0;
  }

  public boolean hasErrorOverrun(Double overrun) {
    return Amounts.isNotZero(overrun) && overrun < 0;
  }
}
