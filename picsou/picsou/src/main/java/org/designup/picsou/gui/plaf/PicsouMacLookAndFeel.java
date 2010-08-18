package org.designup.picsou.gui.plaf;

import apple.laf.AquaLookAndFeel;
import com.apple.laf.AquaLabelUI;
import org.designup.picsou.utils.Lang;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.plaf.basic.BasicHyperlinkUI;
import org.jdesktop.swingx.plaf.basic.BasicMonthViewUI;

import javax.swing.*;
import java.awt.*;

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
      ApplicationLAF.initUI(defaults, org.jdesktop.swingx.plaf.basic.BasicDatePickerUI.class,
                            JXDatePicker.uiClassID);
      ApplicationLAF.initUI(defaults, BasicMonthViewUI.class, JXMonthView.uiClassID);
      ApplicationLAF.initUI(defaults, BasicHyperlinkUI.class, JXHyperlink.uiClassID);
      ApplicationLAF.initUI(defaults, CustomLabelUI.class, "LabelUI");
    }
    catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  public boolean getSupportsWindowDecorations() {
    return true;
  }

  /* Fixes a random exception thrown when displaying a disabled slider on MacOSX */
  private static class CustomLabelUI extends AquaLabelUI {
    protected Color getDisabledLabelColor(JLabel jLabel) {
      if (jLabel == null) {
        return Color.GRAY;
      }
      return super.getDisabledLabelColor(jLabel);
    }
  }
}
