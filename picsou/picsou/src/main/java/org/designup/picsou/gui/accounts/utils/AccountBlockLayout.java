package org.designup.picsou.gui.accounts.utils;

import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.awt.*;

public class AccountBlockLayout implements LayoutManager {

  private boolean initialized = false;
  private Component editAccount;
  private Component accountPosition;
  private Component accountUpdateDate;
  private Component selectAccount;
  private Component accountWeather;
  private Component positionsChart;

  private static final int HORIZONTAL_MARGIN = 5;
  private static final int VERTICAL_MARGIN = 2;
  private static final int CHART_HEIGHT = 80;
  private static final int CHART_PADDING = 4;
  private static final int CHART_LEFT_PADDING = 15;

  public void addLayoutComponent(String name, Component comp) {
  }

  public void removeLayoutComponent(Component comp) {
  }

  public Dimension preferredLayoutSize(Container parent) {
    if (!initialized) {
      init(parent);
    }
    return new Dimension(Integer.MAX_VALUE, getMinHeight());
  }

  public Dimension minimumLayoutSize(Container parent) {
    if (!initialized) {
      init(parent);
    }
    return new Dimension(getMinWidth(), getMinHeight());
  }

  public int getMinHeight() {
    int chartHeight = positionsChart.isVisible() ? VERTICAL_MARGIN + CHART_HEIGHT : 0;
    return getFirstRowHeight() + VERTICAL_MARGIN + accountUpdateDate.getPreferredSize().height + chartHeight;
  }

  public int getMinWidth() {
    return editAccount.getPreferredSize().width +
           accountWeather.getPreferredSize().width +
           accountPosition.getPreferredSize().width +
           selectAccount.getPreferredSize().width +
           4 * HORIZONTAL_MARGIN;
  }

  private void init(Container parent) {
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
      else {
        throw new UnexpectedApplicationState("Unexpected component found in layout: " + component);
      }
    }
    initialized = true;
  }

  public void layoutContainer(Container parent) {
    if (!initialized) {
      init(parent);
    }

    Insets insets = parent.getInsets();
    int top = insets.top;
    int left = insets.left;
    int width = parent.getSize().width;
    int right = width - insets.right;

    int firstRowHeight = getFirstRowHeight();

    int positionRight = right - selectAccount.getPreferredSize().width - HORIZONTAL_MARGIN;
    int positionTop = top + firstRowHeight / 2 - accountPosition.getPreferredSize().height / 2;
    int positionLeft = positionRight - accountPosition.getPreferredSize().width;
    accountPosition.setBounds(positionLeft, positionTop,
                              accountPosition.getPreferredSize().width, accountPosition.getPreferredSize().height);

    int accountUpdateDateTop = top + firstRowHeight + VERTICAL_MARGIN;
    int accountUpdateDateLeft = positionRight - accountUpdateDate.getPreferredSize().width;
    int secondRowHeight = accountUpdateDate.getPreferredSize().height;
    accountUpdateDate.setBounds(accountUpdateDateLeft, accountUpdateDateTop,
                                accountUpdateDate.getPreferredSize().width, secondRowHeight);

    int accountWeatherTop = top + (firstRowHeight + VERTICAL_MARGIN + secondRowHeight) / 2 - accountWeather.getPreferredSize().height / 2;
    int accountWeatherLeft =
      positionRight
      - Math.max(accountPosition.getPreferredSize().width, accountUpdateDate.getPreferredSize().width)
      - accountWeather.getPreferredSize().width
      - HORIZONTAL_MARGIN;
    accountWeather.setBounds(accountWeatherLeft, accountWeatherTop,
                            accountWeather.getPreferredSize().width, accountWeather.getPreferredSize().height);

    int selectAccountLeft = right - selectAccount.getPreferredSize().width;
    int selectAccountTop = top + (firstRowHeight + VERTICAL_MARGIN + secondRowHeight) / 2 - selectAccount.getPreferredSize().height / 2;
    selectAccount.setBounds(selectAccountLeft, selectAccountTop,
                            selectAccount.getPreferredSize().width, selectAccount.getPreferredSize().height);

    int editAccountTop = top + firstRowHeight / 2 - editAccount.getPreferredSize().height / 2;
    int editAccountWidth = editAccount.getPreferredSize().width;
    editAccount.setBounds(left, editAccountTop, editAccountWidth, editAccount.getPreferredSize().height);

    if (positionsChart.isVisible()) {
      int chartTop = accountUpdateDateTop + secondRowHeight + VERTICAL_MARGIN;
      positionsChart.setBounds(left + CHART_LEFT_PADDING, chartTop,
                               width - CHART_PADDING - CHART_LEFT_PADDING, CHART_HEIGHT - CHART_PADDING);
    }
  }

  public int getFirstRowHeight() {
    return editAccount.getSize().height;
  }
}
