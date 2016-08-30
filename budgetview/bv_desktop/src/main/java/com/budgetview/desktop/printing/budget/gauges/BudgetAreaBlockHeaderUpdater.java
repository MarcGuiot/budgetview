package com.budgetview.desktop.printing.budget.gauges;

import com.budgetview.desktop.budget.summary.BudgetAreaSummaryComputer;
import com.budgetview.desktop.components.TextDisplay;
import com.budgetview.desktop.components.charts.Gauge;
import com.budgetview.desktop.components.charts.GaugeUpdater;
import com.budgetview.shared.model.BudgetArea;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class BudgetAreaBlockHeaderUpdater extends BudgetAreaSummaryComputer {
  private TextDisplay amountLabel;
  private TextDisplay plannedLabel;
  protected Gauge gauge;

  public BudgetAreaBlockHeaderUpdater(TextDisplay amountLabel, TextDisplay plannedLabel, Gauge gauge,
                                      GlobRepository repository, Directory directory) {
    super(repository, directory);

    this.amountLabel = amountLabel;
    this.plannedLabel = plannedLabel;
    this.gauge = gauge;
  }

  protected void clearComponents() {
    amountLabel.setText(null);
    plannedLabel.setText(null);
    gauge.getModel().setValues((double)0, (double)0);
  }

  protected void updateComponents(BudgetArea budgetArea) {
    gauge.setVisible(true);
    amountLabel.setText(getObservedLabel(budgetArea));
    amountLabel.setVisible(true);

    plannedLabel.setText(getPlannedLabel(budgetArea));
    plannedLabel.setToolTipText(getPlannedTooltip(budgetArea));
    if (hasErrorOverrun()) {
      plannedLabel.setForeground(errorOverrunAmountColor);
    }
    else if (hasPositiveOverrun()) {
      plannedLabel.setForeground(positiveOverrunAmountColor);
    }
    else {
      plannedLabel.setForeground(normalAmountColor);
    }

    GaugeUpdater.updateGauge(totalAmounts.getFutureRemaining(),
                             totalAmounts.getFutureOverrun(),
                             totalAmounts.getPastRemaining(),
                             totalAmounts.getPastOverrun(),
                             totalAmounts.getInitiallyPlanned(),
                             totalAmounts.getActual(),
                             true,
                             gauge, budgetArea, false);

  }
}

