package org.designup.picsou.gui.utils;

import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.awt.*;

public class HeaderPanelLayout implements LayoutManager {

  private static final int PADDING_TOP = 5;
  private static final int PADDING_BOTTOM = 10;
  private static final int PADDING_LEFT = 10;
  private static final int PADDING_RIGHT = 15;

  private boolean initialized = false;
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

  private int getMinHeight() {
    return PADDING_TOP + PADDING_BOTTOM +
           Math.max(h(timeView),
                    h(prevPeriod) + h(firstPeriod) + VERTICAL_MARGIN);
  }

  private int getMinWidth() {
    return PADDING_LEFT +
           w(periodTitle) +
           w(firstPeriod) +
           w(timeView) +
           w(lastPeriod) +
           w(importFile) +
           PADDING_RIGHT;
  }

  private void init(Container parent) {
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
    initialized = true;
  }

  public void layoutContainer(Container parent) {
    if (!initialized) {
      init(parent);
    }
    
    int left = PADDING_LEFT;
    int right = parent.getSize().width - PADDING_RIGHT;
    int bottom = parent.getSize().height - PADDING_BOTTOM;

    int periodTitleTop = bottom - h(periodTitle);
    int periodTitleLeft = left;
    int periodTitleRight = periodTitleLeft + Math.max(210, w(periodTitle));
    periodTitle.setBounds(periodTitleLeft, periodTitleTop, periodTitleRight - periodTitleLeft, h(periodTitle));

    int prevButtonsWidth = Math.max(w(prevPeriod), w(nextPeriod));

    int prevPeriodTop = bottom - h(firstPeriod) - VERTICAL_MARGIN - h(prevPeriod);
    int prevPeriodLeft = periodTitleRight + LARGE_HORIZONTAL_MARGIN + prevButtonsWidth / 2 - w(prevPeriod) / 2;
    prevPeriod.setBounds(prevPeriodLeft, prevPeriodTop, w(prevPeriod), h(prevPeriod));

    int firstPeriodTop = bottom - h(firstPeriod);
    int firstPeriodLeft = periodTitleRight + LARGE_HORIZONTAL_MARGIN + prevButtonsWidth / 2 - w(firstPeriod) / 2;
    firstPeriod.setBounds(firstPeriodLeft, firstPeriodTop, w(firstPeriod), h(firstPeriod));

    int importFileTop = bottom - h(importFile);
    int importFileLeft = right - w(importFile);
    importFile.setBounds(importFileLeft, importFileTop, w(importFile), h(importFile));

    int nextButtonsWidth = Math.max(w(nextPeriod), w(lastPeriod));

    int nextPeriodTop = bottom - h(lastPeriod) - VERTICAL_MARGIN - h(nextPeriod);
    int nextPeriodLeft = importFileLeft - LARGE_HORIZONTAL_MARGIN - nextButtonsWidth / 2 - w(nextPeriod) / 2;
    nextPeriod.setBounds(nextPeriodLeft, nextPeriodTop, w(nextPeriod), h(nextPeriod));

    int lastPeriodTop = bottom - h(lastPeriod);
    int lastPeriodLeft = importFileLeft - LARGE_HORIZONTAL_MARGIN - prevButtonsWidth / 2 - w(lastPeriod) / 2;
    lastPeriod.setBounds(lastPeriodLeft, lastPeriodTop, w(lastPeriod), h(lastPeriod));

    int timeViewTop = bottom -h(timeView);
    int timeViewLeft = periodTitleRight + LARGE_HORIZONTAL_MARGIN + prevButtonsWidth + SMALL_HORIZONTAL_MARGIN;
    int timeViewRight = importFileLeft - LARGE_HORIZONTAL_MARGIN - nextButtonsWidth - SMALL_HORIZONTAL_MARGIN;
    timeView.setBounds(timeViewLeft, timeViewTop, timeViewRight - timeViewLeft, h(timeView));
  }

  public int w(Component component) {
    return component.getPreferredSize().width;
  }

  public int h(Component component) {
    return component.getPreferredSize().height;
  }
}
