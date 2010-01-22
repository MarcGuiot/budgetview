package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.budget.summary.BudgetAreaSummaryComputer;
import org.designup.picsou.gui.components.TextDisplay;
import org.designup.picsou.gui.components.charts.Gauge;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.util.Amounts;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class BudgetAreaHeaderUpdater extends BudgetAreaSummaryComputer {
  private TextDisplay amountLabel;
  private TextDisplay plannedLabel;
  protected Gauge gauge;

  public BudgetAreaHeaderUpdater(TextDisplay amountLabel, TextDisplay plannedLabel, Gauge gauge,
                                 GlobRepository repository, Directory directory) {
    super(repository, directory);

    this.amountLabel = amountLabel;
    this.plannedLabel = plannedLabel;
    this.gauge = gauge;
  }

  protected void clearComponents() {
    amountLabel.setText(null);
    plannedLabel.setText(null);
    gauge.setValues(0, 0);
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

    Amounts.updateGauge(totalAmounts.getFuturePositiveRemaining(), totalAmounts.getFuturePositiveOverrun(), 
                        totalAmounts.getFutureNegativeRemaining(), totalAmounts.getFutureNegativeOverrun(),
                        totalAmounts.getPastRemaining(),
                        totalAmounts.getPastOverrun(), totalAmounts.getGaugeTarget(), totalAmounts.getGaugeActual(),
                        gauge, budgetArea);

  }
}

