package org.designup.picsou.gui.savings;

import org.designup.picsou.gui.card.widgets.AbstractNavigationWidget;
import org.designup.picsou.gui.model.Card;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class SavingsWidget extends AbstractNavigationWidget {
  public SavingsWidget(GlobRepository repository, Directory directory) {
    super(Card.SAVINGS, repository, directory);
  }

  public boolean isNavigation() {
    return true;
  }

  public JComponent getComponent() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/savings/savingsWidget.splits",
                                                      repository, directory);
    
    builder.add("histoChart", SavingsChartView.createChartBuilder(false, false, repository, directory).getChart());

    return builder.load();
  }
}
