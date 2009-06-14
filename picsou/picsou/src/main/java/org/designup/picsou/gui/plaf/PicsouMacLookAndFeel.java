package org.designup.picsou.gui.plaf;

import apple.laf.AquaLookAndFeel;
import org.designup.picsou.utils.Lang;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.plaf.basic.BasicMonthViewUI;
import org.jdesktop.swingx.plaf.basic.BasicHyperlinkUI;

import javax.swing.*;

public class PicsouMacLookAndFeel extends AquaLookAndFeel {

  public static void initApplicationName() {
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", Lang.get("application"));
  }

  protected void initClassDefaults(UIDefaults defaults) {
    super.initClassDefaults(defaults);
    if (System.getProperty("mrj.version") != null) {
      System.setProperty("apple.laf.useScreenMenuBar", "true");
    }
    try {
      LafUtils.initUI(defaults, org.jdesktop.swingx.plaf.basic.BasicDatePickerUI.class,
                      JXDatePicker.uiClassID);
      LafUtils.initUI(defaults, BasicMonthViewUI.class,
                      JXMonthView.uiClassID);
      LafUtils.initUI(defaults, BasicHyperlinkUI.class,
                      JXHyperlink.uiClassID);
      
    }
    catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  public boolean getSupportsWindowDecorations() {
    return true;
  }
}
