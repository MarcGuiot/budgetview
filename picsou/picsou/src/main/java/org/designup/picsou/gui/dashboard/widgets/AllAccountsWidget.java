package org.designup.picsou.gui.dashboard.widgets;

import org.designup.picsou.gui.card.utils.GotoCardAction;
import org.designup.picsou.gui.dashboard.DashboardWidget;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.gui.model.DashboardStat;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class AllAccountsWidget extends DashboardWidget {
  public AllAccountsWidget(GlobRepository repository, Directory directory) {
    super(repository, directory);
    widgetButton.addActionListener(new GotoCardAction(Card.BUDGET, directory));
  }

  protected void doUpdate(Glob dashboardStat, GlobRepository repository) {
    if (dashboardStat.isTrue(DashboardStat.HAS_SAVINGS_ACCOUNTS)) {
      widgetButton.setVisible(true);
      legend.setVisible(true);
      Double amount = dashboardStat.get(DashboardStat.TOTAL_ALL_ACCOUNTS, 0.00);
      setWidgetStyle(amount >= 0 ? "textOK" : "textNOK");
      widgetButton.setText(Formatting.toStandardValueString(amount));
      legend.setText(Lang.get("allAccountsWidget.legend",
                              Formatting.toString(dashboardStat.get(DashboardStat.TOTAL_ALL_ACCOUNTS_DATE))));
    }
    else {
      widgetButton.setVisible(false);
      legend.setVisible(false);
    }
  }
}
