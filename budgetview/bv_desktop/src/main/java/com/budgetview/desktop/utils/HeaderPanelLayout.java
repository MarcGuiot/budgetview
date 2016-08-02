package com.budgetview.desktop.utils;

import com.budgetview.desktop.components.layout.CustomLayout;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import javax.swing.*;
import java.awt.*;

public class HeaderPanelLayout extends CustomLayout {

  private static final int PADDING_TOP = 12;
  private static final int PADDING_BOTTOM = 10;
  private static final int PADDING_LEFT = 20;
  private static final int PADDING_RIGHT = 15;

  private Component periodTitle;
  private Component timeView;
  private AbstractButton importFile;

  private static final int HORIZONTAL_MARGIN = 20;

  public int getPreferredWidth() {
    return Integer.MAX_VALUE;
  }

  public int getPreferredHeight() {
    return getMinHeight();
  }

  public int getMinHeight() {
    return PADDING_TOP + PADDING_BOTTOM + height(timeView);
  }

  public int getMinWidth() {
    return PADDING_LEFT +
           width(periodTitle) +
           width(timeView) +
           width(importFile) +
           PADDING_RIGHT;
  }

  public void init(Container parent) {
    for (Component component : parent.getComponents()) {
      if (component.getName().equals("periodTitle")) {
        periodTitle = component;
      }
      else if (component.getName().equals("timeView")) {
        timeView = component;
      }
      else if (component.getName().equals("importFile")) {
        importFile = (AbstractButton)component;
      }
      else {
        throw new UnexpectedApplicationState("Unexpected component found in layout: " + component);
      }
    }
  }

  public void layoutComponents(int top, int totalBottom, int totalLeft, int totalRight, int totalWidth, int totalHeight) {

    int left = PADDING_LEFT;
    int right = totalWidth - PADDING_RIGHT;
    int bottom = totalHeight - PADDING_BOTTOM;

    int periodTitleTop = bottom - height(periodTitle);
    int periodTitleLeft = left;
    int periodTitleRight = periodTitleLeft + Math.max(210, width(periodTitle));
    layout(periodTitle, periodTitleLeft, periodTitleTop, periodTitleRight - periodTitleLeft, height(periodTitle));

    int importFileTop = bottom - iconHeight(importFile);
    int importFileLeft = right - iconWidth(importFile);
    layoutIcon(importFile, importFileLeft, importFileTop);

    int timeViewTop = bottom - height(timeView);
    int timeViewLeft = periodTitleRight + HORIZONTAL_MARGIN;
    int timeViewRight = importFileLeft - HORIZONTAL_MARGIN;
    layout(timeView, timeViewLeft, timeViewTop, timeViewRight - timeViewLeft, height(timeView));
  }
}
