package org.designup.picsou.gui.card;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.budget.BudgetWidget;
import org.designup.picsou.gui.series.evolution.EvolutionWidget;
import org.designup.picsou.gui.savings.SavingsWidget;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.card.widgets.TextNavigationWidget;
import org.designup.picsou.gui.card.widgets.NavigationWidgetPanel;
import org.designup.picsou.gui.help.HelpAction;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class NavigationView extends WidgetView {

  private static Card[] CARDS = {Card.CATEGORIZATION, Card.DATA};

  public NavigationView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/home/navigationView.splits",
                                                      repository, directory);

    add("budget", builder, new BudgetWidget(repository, directory));
    add("evolution", builder, new EvolutionWidget(repository, directory));
    add("savings", builder, new SavingsWidget(repository, directory));

    for (Card card : CARDS) {
      add(card.getName(), builder, new TextNavigationWidget(card, repository, directory));
    }

    parentBuilder.add("navigationView", builder);
  }
}
