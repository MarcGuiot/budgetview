package com.jgoodies.looks.plastic;

import com.jgoodies.looks.LookUtils;
import com.budgetview.gui.components.ui.FlatScrollbarUI;
import com.budgetview.gui.components.ui.RoundButtonUI;
import com.budgetview.gui.plaf.ApplicationLAF;
import com.budgetview.gui.plaf.PicsouButtonUI;
import com.budgetview.gui.plaf.PicsouWindowsLabelUI;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.plaf.basic.BasicMonthViewUI;
import org.jdesktop.swingx.plaf.basic.BasicHyperlinkUI;
import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import javax.swing.plaf.InsetsUIResource;

public class PicsouWindowsLookAndFeel extends Plastic3DLookAndFeel {
  protected void initClassDefaults(UIDefaults defaults) {
    super.initClassDefaults(defaults);
    try {
      ApplicationLAF.initUI(defaults, PicsouButtonUI.class, "ButtonUI");
      ApplicationLAF.initUI(defaults, PicsouWindowsLabelUI.class, "LabelUI");
      ApplicationLAF.initUI(defaults, PicsouButtonUI.class, "ToggleButtonUI");
      ApplicationLAF.initUI(defaults, RoundButtonUI.class, "RoundButtonUI");
      ApplicationLAF.initUI(defaults, FlatScrollbarUI.class, "FlatScrollbarUI");
      ApplicationLAF.initUI(defaults, com.jgoodies.looks.plastic.PlasticComboBoxUI.class, "ComboBoxUI");
      ApplicationLAF.initUI(defaults, com.jgoodies.looks.plastic.PlasticScrollBarUI.class, "ScrollBarUI");
      ApplicationLAF.initUI(defaults, com.jgoodies.looks.plastic.PlasticSpinnerUI.class, "SpinnerUI");
      ApplicationLAF.initUI(defaults, com.jgoodies.looks.plastic.PlasticMenuBarUI.class, "MenuBarUI");
      ApplicationLAF.initUI(defaults, com.jgoodies.looks.plastic.PlasticToolBarUI.class, "ToolBarUI");
      ApplicationLAF.initUI(defaults, com.jgoodies.looks.plastic.PlasticMenuUI.class, "MenuUI");
      ApplicationLAF.initUI(defaults, com.jgoodies.looks.common.ExtBasicMenuItemUI.class, "MenuItemUI");
      ApplicationLAF.initUI(defaults, com.jgoodies.looks.common.ExtBasicCheckBoxMenuItemUI.class, "CheckBoxMenuItemUI");
      ApplicationLAF.initUI(defaults, com.jgoodies.looks.common.ExtBasicRadioButtonMenuItemUI.class, "RadioButtonMenuItemUI");
      ApplicationLAF.initUI(defaults, com.jgoodies.looks.plastic.PlasticPopupMenuUI.class, "PopupMenuUI");
      ApplicationLAF.initUI(defaults, com.jgoodies.looks.common.ExtBasicPopupMenuSeparatorUI.class, "PopupMenuSeparatorUI");
      ApplicationLAF.initUI(defaults, com.jgoodies.looks.plastic.PlasticOptionPaneUI.class, "OptionPaneUI");
      ApplicationLAF.initUI(defaults, com.jgoodies.looks.plastic.PlasticScrollPaneUI.class, "ScrollPaneUI");
      ApplicationLAF.initUI(defaults, com.jgoodies.looks.plastic.PlasticSplitPaneUI.class, "SplitPaneUI");
      ApplicationLAF.initUI(defaults, com.jgoodies.looks.plastic.PlasticPasswordFieldUI.class, "PasswordFieldUI");
      ApplicationLAF.initUI(defaults, com.jgoodies.looks.plastic.PlasticTextAreaUI.class, "TextAreaUI");
      ApplicationLAF.initUI(defaults, com.jgoodies.looks.plastic.PlasticTreeUI.class, "TreeUI");
      ApplicationLAF.initUI(defaults, com.jgoodies.looks.plastic.PlasticInternalFrameUI.class, "InternalFrameUI");
      ApplicationLAF.initUI(defaults, com.jgoodies.looks.plastic.PlasticSeparatorUI.class, "SeparatorUI");
      ApplicationLAF.initUI(defaults, com.jgoodies.looks.plastic.PlasticToolBarSeparatorUI.class, "ToolBarSeparatorUI");
      ApplicationLAF.initUI(defaults, com.jgoodies.looks.plastic.PlasticFileChooserUI.class, "FileChooserUI");
      ApplicationLAF.initUI(defaults, org.jdesktop.swingx.plaf.basic.BasicDatePickerUI.class, JXDatePicker.uiClassID);
      ApplicationLAF.initUI(defaults, BasicMonthViewUI.class, JXMonthView.uiClassID);
      ApplicationLAF.initUI(defaults, BasicHyperlinkUI.class, JXHyperlink.uiClassID);
      
      boolean useMetalTabs = LookUtils.getSystemProperty(TAB_STYLE_KEY, "").
        equalsIgnoreCase(TAB_STYLE_METAL_VALUE);
      if (!useMetalTabs) {
        ApplicationLAF.initUI(defaults, com.jgoodies.looks.plastic.PlasticTabbedPaneUI.class, "TabbedPaneUI");
      }
      if (isSelectTextOnKeyboardFocusGained()) {
        ApplicationLAF.initUI(defaults, com.jgoodies.looks.plastic.PlasticTextFieldUI.class, "TextFieldUI");
        ApplicationLAF.initUI(defaults, com.jgoodies.looks.plastic.PlasticFormattedTextFieldUI.class, "FormattedTextFieldUI");
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected void initComponentDefaults(UIDefaults defaults) {
    super.initComponentDefaults(defaults);

    if (!GuiUtils.isOpenJDK()){
      Object[] properties = {
        "Button.margin", new InsetsUIResource(2, 8, 2, 8),
      };

      defaults.putDefaults(properties);
    }
  }

  public boolean getSupportsWindowDecorations() {
    return false;
  }
}

