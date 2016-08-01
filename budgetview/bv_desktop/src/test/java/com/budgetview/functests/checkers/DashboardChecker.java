package com.budgetview.functests.checkers;

import com.budgetview.gui.dashboard.widgets.WeatherWidget;
import junit.framework.Assert;
import org.globsframework.gui.splits.utils.HtmlUtils;
import org.globsframework.utils.TablePrinter;
import org.uispec4j.Panel;
import org.uispec4j.Window;

import javax.swing.*;

public class DashboardChecker extends FilteredViewChecker<DashboardChecker> {

  private Panel panel;

  public DashboardChecker(Window mainWindow) {
    super(mainWindow, "dashboardView", "accountFilterMessage");
  }

  public void checkContent(String expected) {
    Assert.assertEquals(expected.trim(), getContent().trim());
  }

  private String getContent() {
    TablePrinter printer = new TablePrinter();
    printer.addRow(getWidget("importWidget"), getLegend("importLegend"));
    printer.addRow(getWidget("uncategorizedWidget"), getLegend("uncategorizedLegend"));
    printer.addRow(getWeather("weatherWidget"), getLegend("weatherLegend"));
    printer.addRow(getWidget("remainderWidget"), getLegend("remainderLegend"));
    printer.addRow(getWidget("mainAccountsWidget"), getLegend("mainAccountsLegend"));
    if (containsAllAccountWidget()) {
      printer.addRow(getWidget("allAccountsWidget"), getLegend("allAccountsLegend"));
    }
    return printer.toString();
  }

  private String getWidget(String name) {
    return getPanel().getButton(name).getLabel();
  }

  private String getWeather(String weatherWidget) {
    Icon icon = getPanel().getButton(weatherWidget).getAwtComponent().getIcon();
    if (icon == WeatherWidget.SUNNY_ICON) {
      return "sunny";
    }
    else if (icon == WeatherWidget.CLOUDY_ICON) {
      return "cloudy";
    }
    else if (icon == WeatherWidget.RAINY_ICON) {
      return "rainy";
    }
    return "???";
  }

  private boolean containsAllAccountWidget() {
    return getPanel().getButton("allAccountsWidget").isVisible().isTrue();
  }

  private String getLegend(String name) {
    return HtmlUtils.cleanup(getPanel().getTextBox(name).getText());
  }

  public Panel getPanel() {
    if (panel == null) {
      views.selectHome();
      panel = mainWindow.getPanel("dashboardView");
    }
    return panel;
  }
}
