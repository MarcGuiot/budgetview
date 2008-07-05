package com.jgoodies.looks.plastic;

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
    return false;
  }
}
