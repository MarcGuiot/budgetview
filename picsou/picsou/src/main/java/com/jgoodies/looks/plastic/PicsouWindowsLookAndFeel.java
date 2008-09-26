package com.jgoodies.looks.plastic;

import com.jgoodies.looks.LookUtils;
import org.designup.picsou.gui.plaf.LafUtils;
import org.designup.picsou.gui.plaf.PicsouButtonUI;
import org.designup.picsou.gui.plaf.PicsouWindowsLabelUI;

import javax.swing.*;
import javax.swing.plaf.InsetsUIResource;

public class PicsouWindowsLookAndFeel extends Plastic3DLookAndFeel {
  protected void initClassDefaults(UIDefaults defaults) {
    super.initClassDefaults(defaults);
    try {
      LafUtils.initUI(defaults, PicsouButtonUI.class, "ButtonUI");
      LafUtils.initUI(defaults, PicsouWindowsLabelUI.class, "LabelUI");
      LafUtils.initUI(defaults, com.jgoodies.looks.plastic.PlasticToggleButtonUI.class, "ToggleButtonUI");
      LafUtils.initUI(defaults, com.jgoodies.looks.plastic.PlasticComboBoxUI.class, "ComboBoxUI");
      LafUtils.initUI(defaults, com.jgoodies.looks.plastic.PlasticScrollBarUI.class, "ScrollBarUI");
      LafUtils.initUI(defaults, com.jgoodies.looks.plastic.PlasticSpinnerUI.class, "SpinnerUI");
      LafUtils.initUI(defaults, com.jgoodies.looks.plastic.PlasticMenuBarUI.class, "MenuBarUI");
      LafUtils.initUI(defaults, com.jgoodies.looks.plastic.PlasticToolBarUI.class, "ToolBarUI");
      LafUtils.initUI(defaults, com.jgoodies.looks.plastic.PlasticMenuUI.class, "MenuUI");
      LafUtils.initUI(defaults, com.jgoodies.looks.common.ExtBasicMenuItemUI.class, "MenuItemUI");
      LafUtils.initUI(defaults, com.jgoodies.looks.common.ExtBasicCheckBoxMenuItemUI.class, "CheckBoxMenuItemUI");
      LafUtils.initUI(defaults, com.jgoodies.looks.common.ExtBasicRadioButtonMenuItemUI.class, "RadioButtonMenuItemUI");
      LafUtils.initUI(defaults, com.jgoodies.looks.plastic.PlasticPopupMenuUI.class, "PopupMenuUI");
      LafUtils.initUI(defaults, com.jgoodies.looks.common.ExtBasicPopupMenuSeparatorUI.class, "PopupMenuSeparatorUI");
      LafUtils.initUI(defaults, com.jgoodies.looks.plastic.PlasticOptionPaneUI.class, "OptionPaneUI");
      LafUtils.initUI(defaults, com.jgoodies.looks.plastic.PlasticScrollPaneUI.class, "ScrollPaneUI");
      LafUtils.initUI(defaults, com.jgoodies.looks.plastic.PlasticSplitPaneUI.class, "SplitPaneUI");
      LafUtils.initUI(defaults, com.jgoodies.looks.plastic.PlasticPasswordFieldUI.class, "PasswordFieldUI");
      LafUtils.initUI(defaults, com.jgoodies.looks.plastic.PlasticTextAreaUI.class, "TextAreaUI");
      LafUtils.initUI(defaults, com.jgoodies.looks.plastic.PlasticTreeUI.class, "TreeUI");
      LafUtils.initUI(defaults, com.jgoodies.looks.plastic.PlasticInternalFrameUI.class, "InternalFrameUI");
      LafUtils.initUI(defaults, com.jgoodies.looks.plastic.PlasticSeparatorUI.class, "SeparatorUI");
      LafUtils.initUI(defaults, com.jgoodies.looks.plastic.PlasticToolBarSeparatorUI.class, "ToolBarSeparatorUI");
      LafUtils.initUI(defaults, com.jgoodies.looks.plastic.PlasticFileChooserUI.class, "FileChooserUI");
      boolean useMetalTabs = LookUtils.getSystemProperty(TAB_STYLE_KEY, "").
        equalsIgnoreCase(TAB_STYLE_METAL_VALUE);
      if (!useMetalTabs) {
        LafUtils.initUI(defaults, com.jgoodies.looks.plastic.PlasticTabbedPaneUI.class, "TabbedPaneUI");
      }
      if (isSelectTextOnKeyboardFocusGained()) {
        LafUtils.initUI(defaults, com.jgoodies.looks.plastic.PlasticTextFieldUI.class, "TextFieldUI");
        LafUtils.initUI(defaults, com.jgoodies.looks.plastic.PlasticFormattedTextFieldUI.class, "FormattedTextFieldUI");
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected void initComponentDefaults(UIDefaults defaults) {
    super.initComponentDefaults(defaults);

    Object[] properties = {
      "Button.margin", new InsetsUIResource(2, 8, 2, 8),
    };

    defaults.putDefaults(properties);
  }

  public boolean getSupportsWindowDecorations() {
    return false;
  }
}

