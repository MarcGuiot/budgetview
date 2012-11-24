package org.designup.picsou.gui.plaf;

import org.globsframework.gui.splits.ui.UIService;
import org.globsframework.gui.splits.components.HyperlinkButtonUI;
import org.globsframework.gui.splits.components.StyledToggleButtonUI;
import org.globsframework.gui.splits.components.StyledPanelUI;
import org.globsframework.gui.splits.components.ShadowedLabelUI;
import org.designup.picsou.gui.components.ui.SelectionToggleUI;
import org.designup.picsou.gui.components.ui.ArrowButtonUI;
import org.designup.picsou.gui.components.ui.RoundButtonUI;
import org.designup.picsou.gui.components.ui.ColorButtonUI;

import javax.swing.*;

public class ApplicationLAF {

  private static final String PANEL_UI = org() + "designup.picsou.gui.plaf.WavePanelUI";
  private static final String LINK_BUTTON_UI = org() + "globsframework.gui.splits.components.HyperlinkButtonUI";
  private static final String STYLED_TOGGLE_BUTTON_UI = org() + "globsframework.gui.splits.components.StyledToggleButtonUI";
  private static final String STYLED_PANEL_UI = org() + "globsframework.gui.splits.components.StyledPanelUI";
  private static final String SHADOWED_LABEL_UI = org() + "globsframework.gui.splits.components.ShadowedLabelUI";
  private static final String SERIES_TOGGLE_UI = org() + "designup.picsou.gui.components.SelectionToggleUI";
  private static final String BUTTON_PANEL_UI = org() + "designup.picsou.gui.plaf.ButtonPanelItemUI";
  private static final String ARROW_BUTTON_UI = org() + "designup.picsou.gui.components.ArrowButtonUI";
  private static final String ROUND_BUTTON_UI = org() + "designup.picsou.gui.components.RoundButtonUI";
  private static final String COLOR_BUTTON_UI = org() + "designup.picsou.gui.components.ColorButtonUI";

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
    uiService.registerClass(STYLED_TOGGLE_BUTTON_UI, StyledToggleButtonUI.class);
    uiService.registerClass(STYLED_PANEL_UI, StyledPanelUI.class);
    uiService.registerClass(SHADOWED_LABEL_UI, ShadowedLabelUI.class);
    uiService.registerClass(SERIES_TOGGLE_UI, SelectionToggleUI.class);
    uiService.registerClass(ROUND_BUTTON_UI, RoundButtonUI.class);
    uiService.registerClass(BUTTON_PANEL_UI, ButtonPanelItemUI.class);
    uiService.registerClass(ARROW_BUTTON_UI, ArrowButtonUI.class);
    uiService.registerClass(COLOR_BUTTON_UI, ColorButtonUI.class);
    return uiService;
  }

  public static void initUI(UIDefaults defaults, Class componentClass, String uiName) throws ClassNotFoundException {
    String className = componentClass.getName();
    defaults.put(uiName, className);
    defaults.put(className, componentClass);
  }
}
