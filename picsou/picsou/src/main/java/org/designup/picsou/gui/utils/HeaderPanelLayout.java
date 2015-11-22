package org.designup.picsou.gui.utils;

import org.designup.picsou.gui.components.layout.CustomLayout;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.awt.*;

public class HeaderPanelLayout extends CustomLayout {

  private static final int PADDING_TOP = 15;
  private static final int PADDING_BOTTOM = 10;
  private static final int PADDING_LEFT = 10;
  private static final int PADDING_RIGHT = 15;

  private Component periodTitle;
  private Component prevPeriod;
  private Component firstPeriod;
  private Component timeView;
  private Component nextPeriod;
  private Component lastPeriod;
  private Component importFile;

  private static final int SMALL_HORIZONTAL_MARGIN = 5;
  private static final int LARGE_HORIZONTAL_MARGIN = 20;
  private static final int VERTICAL_MARGIN = 2;

  public int getPreferredWidth() {
    return Integer.MAX_VALUE;
  }

  public int getPreferredHeight() {
    return getMinHeight();
  }

  public int getMinHeight() {
    return PADDING_TOP + PADDING_BOTTOM +
           Math.max(height(timeView),
                    height(prevPeriod) + height(firstPeriod) + VERTICAL_MARGIN);
  }

  public int getMinWidth() {
    return PADDING_LEFT +
           width(periodTitle) +
           width(firstPeriod) +
           width(timeView) +
           width(lastPeriod) +
           width(importFile) +
           PADDING_RIGHT;
  }

  public void init(Container parent) {
    for (Component component : parent.getComponents()) {
      if (component.getName().equals("periodTitle")) {
        periodTitle = component;
      }
      else if (component.getName().equals("prevPeriod")) {
        prevPeriod = component;
      }
      else if (component.getName().equals("firstPeriod")) {
        firstPeriod = component;
      }
      else if (component.getName().equals("timeView")) {
        timeView = component;
      }
      else if (component.getName().equals("nextPeriod")) {
        nextPeriod = component;
      }
      else if (component.getName().equals("lastPeriod")) {
        lastPeriod = component;
      }
      else if (component.getName().equals("importFile")) {
        importFile = component;
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

    int prevButtonsWidth = Math.max(width(prevPeriod), width(nextPeriod));

    int prevPeriodTop = bottom - height(firstPeriod) - VERTICAL_MARGIN - height(prevPeriod);
    int prevPeriodLeft = periodTitleRight + LARGE_HORIZONTAL_MARGIN + prevButtonsWidth / 2 - width(prevPeriod) / 2;
    layout(prevPeriod, prevPeriodLeft, prevPeriodTop);

    int firstPeriodTop = bottom - height(firstPeriod);
    int firstPeriodLeft = periodTitleRight + LARGE_HORIZONTAL_MARGIN + prevButtonsWidth / 2 - width(firstPeriod) / 2;
    layout(firstPeriod, firstPeriodLeft, firstPeriodTop);

    int importFileTop = bottom - height(importFile);
    int importFileLeft = right - width(importFile);
    layout(importFile, importFileLeft, importFileTop);

    int nextButtonsWidth = Math.max(width(nextPeriod), width(lastPeriod));

    int nextPeriodTop = bottom - height(lastPeriod) - VERTICAL_MARGIN - height(nextPeriod);
    int nextPeriodLeft = importFileLeft - LARGE_HORIZONTAL_MARGIN - nextButtonsWidth / 2 - width(nextPeriod) / 2;
    layout(nextPeriod, nextPeriodLeft, nextPeriodTop);

    int lastPeriodTop = bottom - height(lastPeriod);
    int lastPeriodLeft = importFileLeft - LARGE_HORIZONTAL_MARGIN - prevButtonsWidth / 2 - width(lastPeriod) / 2;
    layout(lastPeriod, lastPeriodLeft, lastPeriodTop);

    int timeViewTop = bottom - height(timeView);
    int timeViewLeft = periodTitleRight + LARGE_HORIZONTAL_MARGIN + prevButtonsWidth + SMALL_HORIZONTAL_MARGIN;
    int timeViewRight = importFileLeft - LARGE_HORIZONTAL_MARGIN - nextButtonsWidth - SMALL_HORIZONTAL_MARGIN;
    layout(timeView, timeViewLeft, timeViewTop, timeViewRight - timeViewLeft, height(timeView));
  }
}
