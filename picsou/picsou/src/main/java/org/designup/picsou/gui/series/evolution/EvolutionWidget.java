package org.designup.picsou.gui.series.evolution;

import org.designup.picsou.gui.series.evolution.histobuilders.AccountHistoChartUpdater;
import org.designup.picsou.gui.series.evolution.histobuilders.HistoChartBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.gui.card.widgets.AbstractNavigationWidget;

import javax.swing.*;

public class EvolutionWidget extends AbstractNavigationWidget {
  public EvolutionWidget(GlobRepository repository, Directory directory) {
    super(Card.EVOLUTION, repository, directory);
  }

  public boolean isNavigation() {
    return true;
  }

  public JComponent getComponent() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/evolution/evolutionWidget.splits",
                                                      repository, directory);

    HistoChartBuilder histoChartBuilder = new HistoChartBuilder(false, false, repository, directory, directory.get(SelectionService.class), 12, 12);
    new AccountHistoChartUpdater(histoChartBuilder, repository, directory) {
      protected void update(HistoChartBuilder histoChartBuilder, Integer currentMonthId) {
        histoChartBuilder.showMainAccountsHisto(currentMonthId);
      }
    };

    builder.add("histoChart", histoChartBuilder.getChart());

    return builder.load();
  }
}
