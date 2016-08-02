package com.budgetview.desktop.dashboard.widgets;

import com.budgetview.desktop.card.utils.GotoCardAction;
import com.budgetview.desktop.dashboard.DashboardWidget;
import com.budgetview.desktop.description.Formatting;
import com.budgetview.desktop.model.Card;
import com.budgetview.desktop.model.DashboardStat;
import com.budgetview.model.Month;
import com.budgetview.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class RemainderWidget extends DashboardWidget {
  public RemainderWidget(GlobRepository repository, Directory directory) {
    super(repository, directory);
    widgetButton.addActionListener(new GotoCardAction(Card.BUDGET, directory));
  }

  protected void doUpdate(Glob dashboardStat, GlobRepository repository) {
    double remainder = dashboardStat.get(DashboardStat.REMAINDER, 0.00);
    setWidgetStyle(remainder >= 0 ? "textOK" : "textNOK");
    widgetButton.setText(Formatting.toStandardValueString(remainder));

    String postfix = dashboardStat.isTrue(DashboardStat.SINGLE_MAIN_ACCOUNT) ? "single" : "multi";
    String month = Month.getFullMonthLabel(repository.get(DashboardStat.KEY).get(DashboardStat.LAST_FORECAST_MONTH), true);
    legend.setText(Lang.get(remainder >= 0 ? "remainderWidget.title.positive." + postfix : "remainderWidget.title.negative." + postfix, month));
  }
}
