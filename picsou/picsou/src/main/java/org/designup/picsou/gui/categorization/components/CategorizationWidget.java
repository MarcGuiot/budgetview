package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.card.widgets.AbstractNavigationWidget;
import org.designup.picsou.gui.components.charts.Gauge;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.util.Amounts;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Updatable;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CategorizationWidget extends AbstractNavigationWidget implements Updatable {
  private Gauge gauge;
  private JLabel label;
  private JPanel panel;
  private CategorizationLevel level;

  public CategorizationWidget(GlobRepository repository, Directory directory) {
    super(Card.CATEGORIZATION, repository, directory);
    level = new CategorizationLevel(repository, directory);
    level.setFilterOnCurrentMonth();
    level.addListener(this);
    createPanel();
  }

  public boolean isNavigation() {
    return true;
  }

  public Action getAction() {
    final NavigationService navigationService = directory.get(NavigationService.class);
    return new AbstractAction(Card.CATEGORIZATION.getLabel()) {
      public void actionPerformed(ActionEvent e) {
        navigationService.gotoCategorizationForCurrentSelection();
      }
    };
  }

  private void createPanel() {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/categorization/categorizationWidget.splits",
                            repository, directory);

    gauge = builder.add("gauge", new Gauge()).getComponent();
    label = builder.add("label", new JLabel()).getComponent();
    panel = builder.load();
  }

  public JPanel getComponent() {
    return panel;
  }

  public void update() {
    if (!repository.contains(Transaction.TYPE)) {
      label.setText(Lang.get("categorizationWidget.noTransactions"));
      gauge.setVisible(false);
      return;
    }

    if (Amounts.isNearZero(level.getPercentage())) {
      label.setText(Lang.get("categorizationWidget.allCategorized"));
      gauge.setVisible(false);
      return;
    }

    label.setText(Lang.get("categorizationWidget.toCategorize"));
    gauge.setValues(level.getPercentage(), 1.0);
    gauge.setVisible(true);
  }
}
