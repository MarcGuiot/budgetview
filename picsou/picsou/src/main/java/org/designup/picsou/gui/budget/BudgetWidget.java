package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.card.widgets.AbstractNavigationWidget;
import org.designup.picsou.gui.components.TextDisplay;
import org.designup.picsou.gui.components.charts.BudgetAreaGaugeFactory;
import org.designup.picsou.gui.components.charts.Gauge;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.model.BudgetArea;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class BudgetWidget extends AbstractNavigationWidget {

  private BudgetArea[] BUDGET_AREAS = {BudgetArea.INCOME, BudgetArea.SAVINGS,
                                       BudgetArea.RECURRING, BudgetArea.VARIABLE, BudgetArea.EXTRAS};

  public BudgetWidget(GlobRepository repository, Directory directory) {
    super(Card.BUDGET, repository, directory);
  }

  public JComponent getComponent() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/budget/budgetWidget.splits",
                                                      repository, directory);

    for (BudgetArea budgetArea : BUDGET_AREAS) {

      Gauge gauge = BudgetAreaGaugeFactory.createGauge(budgetArea);
      BudgetAreaHeaderUpdater updater = new BudgetAreaHeaderUpdater(TextDisplay.NULL, TextDisplay.NULL,
                                                                    gauge, repository, directory);
      BudgetAreaHeader header = new BudgetAreaHeader(budgetArea, updater, repository, directory);

      builder.add(budgetArea.name().toLowerCase(), gauge);
    }

    return builder.load();
  }
}
