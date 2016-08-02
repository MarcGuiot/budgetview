package com.budgetview.desktop.dashboard.widgets;

import com.budgetview.desktop.card.utils.GotoCardAction;
import com.budgetview.desktop.dashboard.DashboardWidget;
import com.budgetview.desktop.model.Card;
import com.budgetview.desktop.model.DashboardStat;
import com.budgetview.desktop.model.WeatherType;
import com.budgetview.desktop.utils.Gui;
import com.budgetview.model.Month;
import com.budgetview.utils.Lang;
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
      String month = Month.getFullMonthLabel(dashboardStat.get(DashboardStat.LAST_FORECAST_MONTH), true);
      String postfix = dashboardStat.isTrue(DashboardStat.SINGLE_MAIN_ACCOUNT) ? "single" : "multi";
      switch (weather) {
        case SUNNY:
          widgetButton.setIcon(SUNNY_ICON);
          legend.setText(Lang.get("weatherWidget.legend.sunny." + postfix, month));
          break;
        case CLOUDY:
          widgetButton.setIcon(CLOUDY_ICON);
          legend.setText(Lang.get("weatherWidget.legend.cloudy." + postfix, month));
          break;
        case RAINY:
          widgetButton.setIcon(RAINY_ICON);
          legend.setText(Lang.get("weatherWidget.legend.rainy." + postfix, month));
          break;
      }
    }

  }
}