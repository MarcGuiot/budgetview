package com.jgoodies.looks.plastic;

import com.budgetview.desktop.components.ui.FlatScrollbarUI;
import com.budgetview.desktop.components.ui.RoundButtonUI;
import com.budgetview.desktop.plaf.*;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.plaf.basic.BasicMonthViewUI;
import org.jdesktop.swingx.plaf.basic.BasicHyperlinkUI;

import javax.swing.*;
import javax.swing.plaf.InsetsUIResource;

public class PicsouLinuxLookAndFeel extends PlasticXPLookAndFeel {
  protected void initClassDefaults(UIDefaults defaults) {
    super.initClassDefaults(defaults);
    try {
      ApplicationLAF.initUI(defaults, PicsouButtonUI.class, "ButtonUI");
      ApplicationLAF.initUI(defaults, RoundButtonUI.class, "RoundButtonUI");
      ApplicationLAF.initUI(defaults, FlatScrollbarUI.class, "FlatScrollbarUI");
      ApplicationLAF.initUI(defaults, PicsouWindowsLabelUI.class, "LabelUI");
      ApplicationLAF.initUI(defaults, PicsouOptionPaneUI.class, "OptionPaneUI");
      ApplicationLAF.initUI(defaults, PicsouWindowsFileChooserUI.class, "FileChooserUI");
      ApplicationLAF.initUI(defaults, PicsouRootPaneUI.class, "RootPaneUI");
      ApplicationLAF.initUI(defaults, org.jdesktop.swingx.plaf.basic.BasicDatePickerUI.class,
                      JXDatePicker.uiClassID);
      ApplicationLAF.initUI(defaults, BasicMonthViewUI.class,
                      JXMonthView.uiClassID);
      ApplicationLAF.initUI(defaults, BasicHyperlinkUI.class,
                      JXHyperlink.uiClassID);
      
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected void initComponentDefaults(UIDefaults defaults) {
    super.initComponentDefaults(defaults);

    Object[] properties = {
      "Button.margin", new InsetsUIResource(6, 12, 6, 12),
    };

    defaults.putDefaults(properties);
  }

  public boolean getSupportsWindowDecorations() {
    return true;
  }
}
