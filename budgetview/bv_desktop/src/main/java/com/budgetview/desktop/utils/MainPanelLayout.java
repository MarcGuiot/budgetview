  package com.budgetview.desktop.utils;

import com.budgetview.desktop.model.Card;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.awt.*;

public class MainPanelLayout implements LayoutManager {

  private boolean initialized;

  private Component actionsBar;
  private Component timeviewHeader;
  private Component[] sidebars = new Component[4];
  private Component[] contentPanels = new Component[6];
  private Component footer;

  private int currentSidebar = 0;
  private int currentContentPanel = 0;

  private static final int ACCOUNT_VIEW = 0;
  private static final int CATEGORIZATION_SELECTOR = 1;
  private static final int ANALYSIS_SELECTOR = 2;
  private static final int PROJECTS_SELECTOR = 3;
//  private static final int ADDONS_SELECTOR = 4;

  private static final int HOME = 0;
  private static final int BUDGET = 1;
  private static final int DATA = 2;
  private static final int CATEGORIZATION = 3;
  private static final int ANALYSIS = 4;
  private static final int PROJECTS = 5;
//  private static final int ADDONS = 6;

  private StretchMode stretchMode = StretchMode.FIXED_SIDEBAR;
  private boolean initialGuidanceCompleted;

  private enum StretchMode {
    NO_SIDEBAR,
    FIXED_SIDEBAR,
    FIXED_CONTENT
  }

  public void addLayoutComponent(String name, Component comp) {
  }

  public void removeLayoutComponent(Component comp) {
  }

  public Dimension preferredLayoutSize(Container parent) {
    return new Dimension(Integer.MAX_VALUE, 60);
  }

  public Dimension minimumLayoutSize(Container parent) {
    return new Dimension(50, 50);
  }

  public void setCard(Card card, boolean initialGuidanceCompleted) {
    this.initialGuidanceCompleted = initialGuidanceCompleted;
    switch (card) {
      case DASHBOARD:
        currentSidebar = ACCOUNT_VIEW;
        currentContentPanel = HOME;
        stretchMode = this.initialGuidanceCompleted ? StretchMode.FIXED_SIDEBAR : StretchMode.NO_SIDEBAR;
        break;
      case BUDGET:
        currentSidebar = ACCOUNT_VIEW;
        currentContentPanel = BUDGET;
        stretchMode = StretchMode.FIXED_SIDEBAR;
        break;
      case DATA:
        currentSidebar = ACCOUNT_VIEW;
        currentContentPanel = DATA;
        stretchMode = StretchMode.FIXED_SIDEBAR;
        break;
      case CATEGORIZATION:
        currentSidebar = CATEGORIZATION_SELECTOR;
        currentContentPanel = CATEGORIZATION;
        stretchMode = StretchMode.FIXED_CONTENT;
        break;
      case ANALYSIS:
        currentSidebar = ANALYSIS_SELECTOR;
        currentContentPanel = ANALYSIS;
        stretchMode = StretchMode.FIXED_SIDEBAR;
        break;
      case PROJECTS:
        currentSidebar = PROJECTS_SELECTOR;
        currentContentPanel = PROJECTS;
        stretchMode = StretchMode.FIXED_CONTENT;
        break;
//      case ADDONS:
//        currentSidebar = ADDONS_SELECTOR;
//        currentContentPanel = ADDONS;
//        stretchMode = StretchMode.FIXED_SIDEBAR;
//        break;
      default:
        throw new InvalidParameter("Unexpected card: " + card);
    }
    if (initialized) {
      updateComponents();
    }
  }

  public void updateComponents() {
    for (int i = 0; i < sidebars.length; i++) {
      sidebars[i].setVisible((stretchMode != StretchMode.NO_SIDEBAR) && (i == currentSidebar));
    }
    for (int i = 0; i < contentPanels.length; i++) {
      contentPanels[i].setVisible(i == currentContentPanel);
    }
  }

  private void init(Container parent) {
    for (Component comp : parent.getComponents()) {
      String name = comp.getName();
      if ("actionsBar".equals(name)) {
        actionsBar = comp;
      }
      else if ("timeviewHeader".equals(name)) {
        timeviewHeader = comp;
      }
      else if ("accountView".equals(name)) {
        sidebars[ACCOUNT_VIEW] = comp;
      }
      else if ("categorizationSelector".equals(name)) {
        sidebars[CATEGORIZATION_SELECTOR] = comp;
      }
      else if ("analysisSelector".equals(name)) {
        sidebars[ANALYSIS_SELECTOR] = comp;
      }
      else if ("projectSelector".equals(name)) {
        sidebars[PROJECTS_SELECTOR] = comp;
      }
//      else if ("addonsSelector".equals(name)) {
//        sidebars[ADDONS_SELECTOR] = comp;
//      }
      else if ("home".equals(name)) {
        contentPanels[HOME] = comp;
      }
      else if ("budget".equals(name)) {
        contentPanels[BUDGET] = comp;
      }
      else if ("data".equals(name)) {
        contentPanels[DATA] = comp;
      }
      else if ("categorization".equals(name)) {
        contentPanels[CATEGORIZATION] = comp;
      }
      else if ("analysis".equals(name)) {
        contentPanels[ANALYSIS] = comp;
      }
      else if ("projects".equals(name)) {
        contentPanels[PROJECTS] = comp;
      }
//      else if ("addons".equals(name)) {
//        contentPanels[ADDONS] = comp;
//      }
      else if ("footer".equals(name)) {
        footer = comp;
      }
      else {
        throw new InvalidParameter("Unexpected component: " + comp.getName() + " - " + comp);
      }
    }
    initialized = true;
  }

  public void layoutContainer(Container target) {
    if (!initialized) {
      init(target);
      updateComponents();
    }

    Insets insets = target.getInsets();
    int top = insets.top;
    int bottom = target.getHeight() - insets.bottom;
    int left = insets.left;
    int width = target.getWidth();
    int right = width - insets.right;
    int height = bottom - top;

    int actionsBarWidth = actionsBar.getPreferredSize().width;

    int footerHeight = footer.isVisible() ? footer.getPreferredSize().height : 0;
    int footerTop = bottom - footerHeight;
    int footerLeft = left + actionsBarWidth;
    int footerWidth = width - actionsBarWidth;

    int timeviewWidth = width - actionsBarWidth;
    int timeviewHeight = timeviewHeader.getPreferredSize().height;
    int sidebarTop = top + timeviewHeight;
    int sidebarLeft = left + actionsBarWidth;
    int sidebarHeight = footerTop - sidebarTop;

    int sidebarWidth;
    int contentWidth;
    switch (stretchMode) {
      case NO_SIDEBAR:
        sidebarWidth = 0;
        contentWidth = width - sidebarWidth;
        break;
      case FIXED_SIDEBAR:
        sidebarWidth = sidebars[currentSidebar].getPreferredSize().width;
        contentWidth = width - actionsBarWidth - sidebarWidth;
        break;
      case FIXED_CONTENT:
        contentWidth = contentPanels[currentContentPanel].getPreferredSize().width;
        int preferredSidebarWidth = sidebars[currentSidebar].getPreferredSize().width;
        int spare = width - (contentWidth + preferredSidebarWidth + actionsBarWidth);
        if (spare > 0) {
          contentWidth += spare / 3;
        }
        sidebarWidth = width - actionsBarWidth - contentWidth;
        break;
      default:
        throw new InvalidParameter("Unexpected mode " + stretchMode);
    }

    int contentTop = sidebarTop;
    int contentLeft = sidebarLeft + sidebarWidth;
    int contentHeight = sidebarHeight;

    actionsBar.setBounds(left, top, actionsBar.getPreferredSize().width, height);
    timeviewHeader.setBounds(sidebarLeft, top, timeviewWidth, timeviewHeight);
    sidebars[currentSidebar].setBounds(sidebarLeft, sidebarTop, sidebarWidth > 0 ? sidebarWidth : sidebars[currentSidebar].getPreferredSize().width, sidebarHeight);
    contentPanels[currentContentPanel].setBounds(contentLeft, contentTop, contentWidth, contentHeight);
    footer.setBounds(footerLeft, footerTop, footerWidth, footerHeight);
  }
}