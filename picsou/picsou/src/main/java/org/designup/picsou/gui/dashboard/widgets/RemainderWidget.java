package org.designup.picsou.gui.dashboard.widgets;

import org.designup.picsou.gui.card.utils.GotoCardAction;
import org.designup.picsou.gui.dashboard.DashboardWidget;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.gui.model.DashboardStat;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
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

    String month = Month.getFullMonthLabel(repository.get(DashboardStat.KEY).get(DashboardStat.LAST_FORECAST_MONTH));
    legend.setText(Lang.get(remainder >= 0 ? "remainderWidget.title.positive" : "remainderWidget.title.negative", month));
  }
}
