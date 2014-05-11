package org.designup.picsou.gui.plaf;

import org.designup.picsou.gui.accounts.utils.AccountBlockLayout;
import org.designup.picsou.gui.components.tabs.VerticalTabPanelUI;
import org.designup.picsou.gui.components.tabs.VerticalTabToggleUI;
import org.designup.picsou.gui.components.ui.*;
import org.designup.picsou.gui.projects.utils.ProjectItemViewLayout;
import org.designup.picsou.gui.series.ui.SeriesPanelUI;
import org.globsframework.gui.splits.layout.LayoutService;
import org.globsframework.gui.splits.ui.UIService;
import org.globsframework.gui.splits.components.HyperlinkButtonUI;
import org.globsframework.gui.splits.components.StyledToggleButtonUI;
import org.globsframework.gui.splits.components.StyledPanelUI;
import org.globsframework.gui.splits.components.ShadowedLabelUI;

import javax.swing.*;

public class ApplicationLAF {

  private static final String PANEL_UI = org() + "designup.picsou.gui.plaf.WavePanelUI";
  private static final String LINK_BUTTON_UI = org() + "globsframework.gui.splits.components.HyperlinkButtonUI";
  private static final String CUSTOM_BUTTON_UI = org() + "designup.picsou.gui.components.ui.CustomButtonUI";
  private static final String STYLED_TOGGLE_BUTTON_UI = org() + "globsframework.gui.splits.components.StyledToggleButtonUI";
  private static final String STYLED_PANEL_UI = org() + "globsframework.gui.splits.components.StyledPanelUI";
  private static final String SHADOWED_LABEL_UI = org() + "globsframework.gui.splits.components.ShadowedLabelUI";
  private static final String SERIES_TOGGLE_UI = org() + "designup.picsou.gui.components.ui.SelectionToggleUI";
  private static final String BUTTON_PANEL_UI = org() + "designup.picsou.gui.plaf.ButtonPanelItemUI";
  private static final String ARROW_BUTTON_UI = org() + "designup.picsou.gui.components.ui.ArrowButtonUI";
  private static final String PLUS_MINUS_TOGGLE_UI = org() + "designup.picsou.gui.components.ui.PlusMinusToggleUI";
  private static final String ON_OFF_TOGGLE_UI = org() + "designup.picsou.gui.components.ui.OnOffToggleUI";
  private static final String ROUND_BUTTON_UI = org() + "designup.picsou.gui.components.ui.RoundButtonUI";
  private static final String FLAT_SCROLLBAR_UI = org() + "designup.picsou.gui.components.ui.FlatScrollbarUI";
  private static final String NOTIFICATION_FLAG_UI = org() + "designup.picsou.gui.components.ui.NotificationFlagUI";
  private static final String VERTICAL_TAB_TOGGLE_UI = org() + "designup.picsou.gui.components.tabs.VerticalTabToggleUI";
  private static final String VERTICAL_TAB_PANEL_UI = org() + "designup.picsou.gui.components.tabs.VerticalTabPanelUI";

  private static final String ACCOUNT_BLOCK_LAYOUT = org() + "designup.picsou.gui.accounts.utils.AccountBlockLayout";
  private static final String PROJECT_ITEM_VIEW_LAYOUT = org() + "designup.picsou.gui.projects.utils.ProjectItemViewLayout";

  private static final String SERES_PANEL_UI = org() + "designup.picsou.gui.series.ui.SeriesPanelUI";

  private ApplicationLAF() {
  }

  private static String org() {
    return "org.";
  }
  
  public static UIService initUiService() {

    UIManager.put("JideSplitPaneDivider.gripperPainter", new SplitPaneDotsPainter());

    UIService uiService = new UIService();
    uiService.registerClass(PANEL_UI, WavePanelUI.class);
    uiService.registerClass(LINK_BUTTON_UI, HyperlinkButtonUI.class);
    uiService.registerClass(CUSTOM_BUTTON_UI, CustomButtonUI.class);
    uiService.registerClass(STYLED_TOGGLE_BUTTON_UI, StyledToggleButtonUI.class);
    uiService.registerClass(STYLED_PANEL_UI, StyledPanelUI.class);
    uiService.registerClass(SHADOWED_LABEL_UI, ShadowedLabelUI.class);
    uiService.registerClass(SERIES_TOGGLE_UI, SelectionToggleUI.class);
    uiService.registerClass(ROUND_BUTTON_UI, RoundButtonUI.class);
    uiService.registerClass(BUTTON_PANEL_UI, ButtonPanelItemUI.class);
    uiService.registerClass(ARROW_BUTTON_UI, ArrowButtonUI.class);
    uiService.registerClass(PLUS_MINUS_TOGGLE_UI, PlusMinusToggleUI.class);
    uiService.registerClass(ON_OFF_TOGGLE_UI, OnOffToggleUI.class);
    uiService.registerClass(VERTICAL_TAB_TOGGLE_UI, VerticalTabToggleUI.class);
    uiService.registerClass(VERTICAL_TAB_PANEL_UI, VerticalTabPanelUI.class);
    uiService.registerClass(NOTIFICATION_FLAG_UI, NotificationFlagUI.class);
    uiService.registerClass(FLAT_SCROLLBAR_UI, FlatScrollbarUI.class);
    uiService.registerClass(SERES_PANEL_UI, SeriesPanelUI.class);
    return uiService;
  }

  public static LayoutService initLayoutService(){
    LayoutService layoutService = new LayoutService();
    layoutService.registerClass(ACCOUNT_BLOCK_LAYOUT, AccountBlockLayout.class);
    layoutService.registerClass(PROJECT_ITEM_VIEW_LAYOUT, ProjectItemViewLayout.class);
    return layoutService;
  }

  public static void initUI(UIDefaults defaults, Class componentClass, String uiName) throws ClassNotFoundException {
    String className = componentClass.getName();
    defaults.put(uiName, className);
    defaults.put(className, componentClass);
  }
}
