package org.designup.picsou.gui.dashboard.widgets;

import org.designup.picsou.gui.card.utils.GotoCardAction;
import org.designup.picsou.gui.dashboard.DashboardWidget;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.gui.model.DashboardStat;
import org.designup.picsou.gui.model.WeatherType;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class WeatherWidget extends DashboardWidget {
  public static final Icon SUNNY_ICON;
  public static final Icon CLOUDY_ICON;
  public static final Icon RAINY_ICON;

  static {
    SUNNY_ICON = Gui.IMAGE_LOCATOR.get("dashboard/sunny.png");
    CLOUDY_ICON = Gui.IMAGE_LOCATOR.get("dashboard/cloudy.png");
    RAINY_ICON = Gui.IMAGE_LOCATOR.get("dashboard/rainy.png");
  }

  public WeatherWidget(GlobRepository repository, Directory directory) {
    super(repository, directory);
    widgetButton.addActionListener(new GotoCardAction(Card.BUDGET, directory));
  }

  protected void doUpdate(Glob dashboardStat, GlobRepository repository) {
    WeatherType weather = DashboardStat.getWeather(repository);
    if (weather == null) {
      widgetButton.setIcon(null);
      legend.setText("");
    }
    else {
      String month = Month.getFullMonthLabel(repository.get(DashboardStat.KEY).get(DashboardStat.LAST_FORECAST_MONTH), true);
      switch (weather) {
        case SUNNY:
          widgetButton.setIcon(SUNNY_ICON);
          legend.setText(Lang.get("weatherWidget.legend.sunny", month));
          break;
        case CLOUDY:
          widgetButton.setIcon(CLOUDY_ICON);
          legend.setText(Lang.get("weatherWidget.legend.cloudy", month));
          break;
        case RAINY:
          widgetButton.setIcon(RAINY_ICON);
          legend.setText(Lang.get("weatherWidget.legend.rainy", month));
          break;
      }
    }

  }
}