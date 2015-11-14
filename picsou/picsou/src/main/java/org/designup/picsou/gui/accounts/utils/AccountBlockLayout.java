package org.designup.picsou.gui.accounts.utils;

import org.designup.picsou.gui.components.layout.CustomLayout;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.awt.*;

public class AccountBlockLayout extends CustomLayout {

  private Component editAccount;
  private Component accountPosition;
  private Component accountUpdateDate;
  private Component selectAccount;
  private Component accountWeather;
  private Component positionsChart;
  private Component toggleGraph;

  private static final int HORIZONTAL_MARGIN = 5;
  private static final int VERTICAL_MARGIN = 2;
  private static final int CHART_HEIGHT = 80;
  private static final int CHART_PADDING = 4;
  private static final int CHART_LEFT_PADDING = 4;
  private static final int POSITION_LEFT_MARGIN = 12;

  private static final int WEATHER_ICON_WODTH = 52;

  public int getPreferredWidth() {
    return Integer.MAX_VALUE;
  }

  public int getPreferredHeight() {
    return getMinHeight();
  }

  protected int getMinHeight() {
    int chartHeight = positionsChart.isVisible() ? VERTICAL_MARGIN + CHART_HEIGHT : 0;
    return getFirstRowHeight() + 2 * VERTICAL_MARGIN + accountUpdateDate.getPreferredSize().height + chartHeight;
  }

  protected int getMinWidth() {
    return editAccount.getPreferredSize().width +
           accountWeather.getPreferredSize().width +
           accountPosition.getPreferredSize().width +
           selectAccount.getPreferredSize().width +
           toggleGraph.getPreferredSize().width +
           5 * HORIZONTAL_MARGIN;
  }

  protected void init(Container parent) {
    for (Component component : parent.getComponents()) {
      if (component.getName().equals("editAccount")) {
        editAccount = component;
      }
      else if (component.getName().equals("accountPosition")) {
        accountPosition = component;
      }
      else if (component.getName().equals("accountUpdateDate")) {
        accountUpdateDate = component;
      }
      else if (component.getName().equals("selectAccount")) {
        selectAccount = component;
      }
      else if (component.getName().equals("accountWeather")) {
        accountWeather = component;
      }
      else if (component.getName().equals("accountPositionsChart")) {
        positionsChart = component;
      }
      else if (component.getName().equals("toggleGraph")) {
        toggleGraph = component;
      }
      else {
        throw new UnexpectedApplicationState("Unexpected component found in layout: " + component);
      }
    }
  }

  public void layoutComponents(int top, int bottom, int left, int right, int totalWidth, int totalHeight) {
    int firstRowHeight = getFirstRowHeight();
    int secondRowHeight = accountUpdateDate.getPreferredSize().height;
    int textRowsHeight = firstRowHeight + VERTICAL_MARGIN + secondRowHeight;

    int positionTop = top + firstRowHeight + secondRowHeight - accountPosition.getPreferredSize().height;
    int positionLeft = left + POSITION_LEFT_MARGIN;
    int positionRight = positionLeft + accountPosition.getPreferredSize().width;
    layout(accountPosition, positionLeft, positionTop);

    int accountUpdateDateTop = top + firstRowHeight + secondRowHeight - accountUpdateDate.getPreferredSize().height;
    int accountUpdateDateLeft = positionRight + HORIZONTAL_MARGIN;
    layout(accountUpdateDate,
           accountUpdateDateLeft, accountUpdateDateTop,
           accountUpdateDate.getPreferredSize().width, secondRowHeight);

    int accountWeatherTop = top + textRowsHeight / 2 - accountWeather.getPreferredSize().height / 2;
    int accountWeatherWidth = Math.max(WEATHER_ICON_WODTH, accountWeather.getPreferredSize().width);
    int accountWeatherLeft = right - accountWeatherWidth;
    layout(accountWeather, accountWeatherLeft, accountWeatherTop);

    int toggleGraphTop = top + textRowsHeight / 2 - toggleGraph.getPreferredSize().height / 2;
    int toggleGraphRight = accountWeatherLeft - HORIZONTAL_MARGIN;
    int toggleGraphLeft = toggleGraphRight - toggleGraph.getPreferredSize().height;
    layout(toggleGraph, toggleGraphLeft, toggleGraphTop);

    int selectAccountTop = top + textRowsHeight / 2 - selectAccount.getPreferredSize().height / 2;
    int selectAccountRight = toggleGraphLeft - HORIZONTAL_MARGIN;
    int selectAccountLeft = selectAccountRight - selectAccount.getPreferredSize().width;
    layout(selectAccount, selectAccountLeft, selectAccountTop);

    int maxEditWidth = selectAccountLeft - 2 * HORIZONTAL_MARGIN - left;
    int editAccountLeft = left + HORIZONTAL_MARGIN;
    int editAccountTop = top + firstRowHeight / 2 - editAccount.getPreferredSize().height / 2;
    int editAccountWidth = editAccount.getPreferredSize().width > maxEditWidth ? maxEditWidth : editAccount.getPreferredSize().width;
    layout(editAccount, editAccountLeft, editAccountTop, editAccountWidth, editAccount.getPreferredSize().height);

    if (positionsChart.isVisible()) {
      int chartTop = accountUpdateDateTop + secondRowHeight + VERTICAL_MARGIN;
      layout(positionsChart,
             left + CHART_LEFT_PADDING, chartTop,
             totalWidth - CHART_PADDING - CHART_LEFT_PADDING, CHART_HEIGHT - CHART_PADDING);
    }
  }

  private int getFirstRowHeight() {
    return editAccount.getSize().height;
  }
}
