package com.budgetview.desktop.budget.utils;

import com.budgetview.desktop.components.layout.CustomLayout;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.awt.*;

@SuppressWarnings("UnusedDeclaration")
public class BudgetAreaHeaderLayout extends CustomLayout {

  private Component budgetAreaTitle;
  private Component totalGauge;
  private Component totalActualAmount;
  private Component totalSlash;
  private Component totalPlannedAmount;

  private static final int HORIZONTAL_MARGIN = 5;
  private static final int VERTICAL_MARGIN = 2;
  private static final int CHART_HEIGHT = 80;
  private static final int CHART_PADDING = 4;
  private static final int CHART_LEFT_PADDING = 4;

  public int getPreferredWidth() {
    return Integer.MAX_VALUE;
  }

  public int getPreferredHeight() {
    return getMinHeight();
  }

  public int getMinHeight() {
    return budgetAreaTitle.getPreferredSize().height + 2 * VERTICAL_MARGIN;
  }

  public int getMinWidth() {
    return budgetAreaTitle.getPreferredSize().width +
           totalGauge.getPreferredSize().width +
           totalActualAmount.getPreferredSize().width +
           totalSlash.getPreferredSize().width +
           totalPlannedAmount.getPreferredSize().width +
           5 * HORIZONTAL_MARGIN;
  }

  public void init(Container parent) {
    for (Component component : parent.getComponents()) {
      if (component.getName().equals("budgetAreaTitle")) {
        budgetAreaTitle = component;
      }
      else if (component.getName().equals("totalGauge")) {
        totalGauge = component;
      }
      else if (component.getName().equals("totalActualAmount")) {
        totalActualAmount = component;
      }
      else if (component.getName().equals("totalSlash")) {
        totalSlash = component;
      }
      else if (component.getName().equals("totalPlannedAmount")) {
        totalPlannedAmount = component;
      }
      else {
        throw new UnexpectedApplicationState("Unexpected component found in layout: " + component);
      }
    }
  }

  public void layoutComponents(int top, int bottom, int left, int right, int totalWidth, int totalHeight) {

    layout(budgetAreaTitle,
           left,
           centeredTop(top, budgetAreaTitle, bottom));

    layout(totalGauge,
           centeredLeft(left, totalGauge, right),
           centeredTop(top, totalGauge, bottom));

    int plannedLeft = right - width(totalPlannedAmount);
    int slashLeft = plannedLeft - HORIZONTAL_MARGIN - width(totalSlash);
    int actualLeft = slashLeft - HORIZONTAL_MARGIN - width(totalActualAmount);

    layout(totalActualAmount,
           actualLeft,
           centeredTop(top, totalActualAmount, bottom));
    layout(totalSlash,
           slashLeft,
           centeredTop(top, totalSlash, bottom));
    layout(totalPlannedAmount,
           plannedLeft,
           centeredTop(top, totalPlannedAmount, bottom));
  }
}
