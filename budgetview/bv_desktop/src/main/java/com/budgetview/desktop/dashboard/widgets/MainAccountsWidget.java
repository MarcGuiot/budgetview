package com.budgetview.desktop.dashboard.widgets;

import com.budgetview.desktop.card.utils.GotoCardAction;
import com.budgetview.desktop.dashboard.DashboardWidget;
import com.budgetview.desktop.description.Formatting;
import com.budgetview.desktop.model.Card;
import com.budgetview.desktop.model.DashboardStat;
import com.budgetview.utils.Lang;
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
    String key =
      dashboardStat.isTrue(DashboardStat.SINGLE_MAIN_ACCOUNT) ? "mainAccountsWidget.legend.single" : "mainAccountsWidget.legend.multi";
    legend.setText(Lang.get(key, Formatting.toString(dashboardStat.get(DashboardStat.TOTAL_MAIN_ACCOUNTS_DATE))));
  }
}
