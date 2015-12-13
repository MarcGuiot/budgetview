package org.designup.picsou.gui.accounts.utils;

import org.designup.picsou.gui.components.layout.CustomLayout;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import javax.swing.*;
import java.awt.*;

public class AccountBlockLayout extends CustomLayout {

  private Component editAccount;
  private Component accountPosition;
  private Component accountUpdateDate;
  private AbstractButton selectAccount;
  private AbstractButton accountWeather;
  private Component positionsChart;
  private AbstractButton toggleGraph;

  private static final int HORIZONTAL_MARGIN = 5;
  private static final int VERTICAL_MARGIN = 3;
  private static final int CHART_HEIGHT = 80;
  private static final int CHART_PADDING = 4;
  private static final int CHART_LEFT_PADDING = 4;
  private static final int POSITION_LEFT_MARGIN = 14;

  public int getPreferredWidth() {
    return Integer.MAX_VALUE;
  }

  public int getPreferredHeight() {
    return getMinHeight();
  }

  protected int getMinHeight() {
    int chartHeight = positionsChart.isVisible() ? VERTICAL_MARGIN + CHART_HEIGHT : 0;
    return getFirstRowHeight() + 3 * VERTICAL_MARGIN + accountUpdateDate.getPreferredSize().height + chartHeight;
  }

  protected int getMinWidth() {
    return editAccount.getPreferredSize().width +
           iconWidth(accountWeather) +
           accountPosition.getPreferredSize().width +
           iconWidth(selectAccount) +
           iconWidth(toggleGraph) +
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
        selectAccount = (AbstractButton)component;
      }
      else if (component.getName().equals("accountWeather")) {
        accountWeather = (AbstractButton)component;
      }
      else if (component.getName().equals("accountPositionsChart")) {
        positionsChart = component;
      }
      else if (component.getName().equals("toggleGraph")) {
        toggleGraph = (AbstractButton)component;
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

    int positionTop = top + firstRowHeight + VERTICAL_MARGIN + secondRowHeight - accountPosition.getPreferredSize().height;
    int positionLeft = left + POSITION_LEFT_MARGIN;
    int positionRight = positionLeft + accountPosition.getPreferredSize().width;
    layout(accountPosition, positionLeft, positionTop);

    int accountUpdateDateTop = top + firstRowHeight + VERTICAL_MARGIN + secondRowHeight - accountUpdateDate.getPreferredSize().height;
    int accountUpdateDateLeft = positionRight + HORIZONTAL_MARGIN;
    layout(accountUpdateDate,
           accountUpdateDateLeft, accountUpdateDateTop,
           accountUpdateDate.getPreferredSize().width, secondRowHeight);

    int accountWeatherTop = top + textRowsHeight / 2 - iconHeight(accountWeather) / 2;
    int accountWeatherWidth = iconWidth(accountWeather);
    int accountWeatherLeft = right - accountWeatherWidth;
    int accountWeatherImageLeft = right - accountWeatherWidth / 2 - accountWeatherWidth / 2;
    layoutIcon(accountWeather, accountWeatherImageLeft, accountWeatherTop);

    int toggleGraphTop = top + textRowsHeight / 2 - iconHeight(toggleGraph) / 2;
    int toggleGraphRight = accountWeatherLeft - HORIZONTAL_MARGIN;
    int toggleGraphLeft = toggleGraphRight - iconWidth(toggleGraph);
    layoutIcon(toggleGraph, toggleGraphLeft, toggleGraphTop);

    int selectAccountTop = top + textRowsHeight / 2 - iconHeight(selectAccount) / 2;
    int selectAccountRight = toggleGraphLeft - HORIZONTAL_MARGIN;
    int selectAccountLeft = selectAccountRight - iconWidth(selectAccount);
    layoutIcon(selectAccount, selectAccountLeft, selectAccountTop);

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
