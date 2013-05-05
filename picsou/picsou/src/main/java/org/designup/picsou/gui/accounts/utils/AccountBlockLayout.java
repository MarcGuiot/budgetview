package org.designup.picsou.gui.accounts.utils;

import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import javax.swing.*;
import java.awt.*;

public class AccountBlockLayout implements LayoutManager {

  public void addLayoutComponent(String name, Component comp) {
  }

  public void removeLayoutComponent(Component comp) {
  }

  public Dimension preferredLayoutSize(Container parent) {
    return new Dimension(Integer.MAX_VALUE, 60);
  }

  public Dimension minimumLayoutSize(Container parent) {
    return new Dimension(50,50);
  }

  public void layoutContainer(Container parent) {
    Metrics metrics = createMetrics(parent);
    for (Component component : parent.getComponents()) {
      if (component.getName().equals("editAccount")) {
        metrics.layoutAccountName(component);
      }
      else if (component.getName().equals("accountPosition")) {
        metrics.layoutPosition(component);
      }
      else if (component.getName().equals("accountUpdateDate")) {
        metrics.layoutPositionDate(component);
      }
      else if (component.getName().equals("selectAccount")) {
        metrics.layoutSelectAccount(component);
      }
      else if (component.getName().equals("accountPositionsChart")) {
        metrics.layoutChart(component);
      }
      else {
        throw new UnexpectedApplicationState("Unexpected component found in layout: " + component);
      }
    }
  }

  private Metrics createMetrics(Container parent) {
    Metrics metrics = new Metrics(parent);
    for (Component component : parent.getComponents()) {
      if (component.getName().equals("editAccount")) {
        metrics.initAccountName(component);
      }
      else if (component.getName().equals("accountPosition")) {
        metrics.initAccountPosition(component);
      }
      else if (component.getName().equals("accountUpdateDate")) {
        metrics.initAccountPositionDate(component);
      }
      else if (component.getName().equals("selectAccount")) {
        metrics.initSelectAccount(component);
      }
    }
    return metrics;
  }

  private class Metrics {

    private static final int CHART_HEIGHT = 35;
    private static final int CHART_LEFT_MARGIN = 10;
    private static final int TITLE_BOTTOM_MARGIN = 5;
    private static final int SELECT_RIGHT_MARGIN = 4;
    private static final int POSITION_BOTTOM_MARGIN = 4;

    private int titleHeight;
    private int titleWidth;
    private int positionWidth;
    private int positionHeight;
    private int maxPositionWidth;
    private int positionDateWidth;
    private int positionDateHeight;
    private int selectWidth;
    private int selectHeight;
    private int top;
    private int bottom;
    private int left;
    private int right;
    private int width;

    public Metrics(Container target) {
      Insets insets = target.getInsets();
      top = insets.top;
      bottom = target.getHeight() - insets.bottom;
      left = insets.left;
      width = target.getWidth();
      right = width - insets.right;
    }

    public void initAccountName(Component component) {
      Dimension size = component.getPreferredSize();
      titleHeight = size.height;
      titleWidth= size.width;
    }

    public void initAccountPosition(Component component) {
      Dimension size = component.getPreferredSize();
      positionWidth =  size.width;
      positionHeight =  size.height;

      JButton button = (JButton)component;
      FontMetrics metrics = button.getFontMetrics(button.getFont());
      maxPositionWidth = Math.max(metrics.stringWidth("000.000.00"), positionWidth);
    }

    public void initAccountPositionDate(Component component) {
      Dimension size = component.getPreferredSize();
      positionDateWidth =  size.width;
      positionDateHeight =  size.height;
    }

    public void initSelectAccount(Component component) {
      Dimension size = component.getPreferredSize();
      selectWidth =  size.width;
      selectHeight =  size.height;
    }

    public void layoutAccountName(Component component) {
      component.setBounds(top, left,
                          titleWidth, titleHeight);
    }

    public void layoutPosition(Component component) {
      component.setBounds(right - selectWidth - SELECT_RIGHT_MARGIN - positionWidth,
                          top + titleHeight + TITLE_BOTTOM_MARGIN,
                          positionWidth, positionHeight);
    }

    public void layoutPositionDate(Component component) {
      component.setBounds(right - selectWidth - SELECT_RIGHT_MARGIN - positionDateWidth,
                          top + titleHeight + TITLE_BOTTOM_MARGIN + positionHeight + POSITION_BOTTOM_MARGIN,
                          positionDateWidth, positionDateHeight);
    }

    public void layoutSelectAccount(Component component) {
      component.setBounds(right - selectWidth,
                          top + titleHeight + TITLE_BOTTOM_MARGIN + positionHeight / 2 - selectHeight / 2,
                          selectWidth, selectHeight);
    }

    public void layoutChart(Component component) {
      component.setBounds(CHART_LEFT_MARGIN,
                          top + titleHeight + TITLE_BOTTOM_MARGIN,
                          width - CHART_LEFT_MARGIN - selectWidth - SELECT_RIGHT_MARGIN - maxPositionWidth - TITLE_BOTTOM_MARGIN, CHART_HEIGHT);
    }
  }
}
