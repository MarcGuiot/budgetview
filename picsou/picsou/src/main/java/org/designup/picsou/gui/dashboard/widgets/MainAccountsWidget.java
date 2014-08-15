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

public class MainAccountsWidget extends DashboardWidget {
  public MainAccountsWidget(GlobRepository repository, Directory directory) {
    super(repository, directory);
    widgetButton.addActionListener(new GotoCardAction(Card.BUDGET, directory));
  }

  protected void doUpdate(Glob dashboardStat, GlobRepository repository) {
    Double amount = dashboardStat.get(DashboardStat.TOTAL_MAIN_ACCOUNTS, 0.00);
    setWidgetStyle(amount >= 0 ? "textOK" : "textNOK");
    widgetButton.setText(Formatting.toStandardValueString(amount));
    legend.setText(Lang.get("mainAccountsWidget.legend",
                            Formatting.toString(dashboardStat.get(DashboardStat.TOTAL_MAIN_ACCOUNTS_DATE))));
  }
}
