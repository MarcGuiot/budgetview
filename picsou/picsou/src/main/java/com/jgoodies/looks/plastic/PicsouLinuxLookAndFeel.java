package com.jgoodies.looks.plastic;

import org.designup.picsou.gui.plaf.LafUtils;
import org.designup.picsou.gui.plaf.PicsouButtonUI;
import org.designup.picsou.gui.plaf.PicsouWindowsLabelUI;
import org.designup.picsou.gui.plaf.PicsouOptionPaneUI;
import org.designup.picsou.gui.plaf.PicsouRootPaneUI;
import org.designup.picsou.gui.plaf.PicsouWindowsFileChooserUI;
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
      LafUtils.initUI(defaults, PicsouButtonUI.class, "ButtonUI");
      LafUtils.initUI(defaults, PicsouWindowsLabelUI.class, "LabelUI");
      LafUtils.initUI(defaults, PicsouOptionPaneUI.class, "OptionPaneUI");
      LafUtils.initUI(defaults, PicsouWindowsFileChooserUI.class, "FileChooserUI");
      LafUtils.initUI(defaults, PicsouRootPaneUI.class, "RootPaneUI");
      LafUtils.initUI(defaults, org.jdesktop.swingx.plaf.basic.BasicDatePickerUI.class,
                      JXDatePicker.uiClassID);
      LafUtils.initUI(defaults, BasicMonthViewUI.class,
                      JXMonthView.uiClassID);
      LafUtils.initUI(defaults, BasicHyperlinkUI.class,
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
