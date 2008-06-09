package org.designup.picsou.gui.plaf;

import apple.laf.AquaLookAndFeel;

import javax.swing.*;
import javax.swing.plaf.InsetsUIResource;

public class PicsouMacLookAndFeel extends AquaLookAndFeel {

  public static void initApplicationName() {
      System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Picsou");
  }

  protected void initClassDefaults(UIDefaults defaults) {
    super.initClassDefaults(defaults);
    if (System.getProperty("mrj.version") != null) {
      System.setProperty("apple.laf.useScreenMenuBar", "true");
    }
    try {
//      LafUtils.initUI(defaults, "org.designup.picsou.gui.plaf.PicsouButtonUI", "ButtonUI");
//      LafUtils.initUI(defaults, "org.designup.picsou.gui.plaf.PicsouOptionPaneUI", "OptionPaneUI");
//      LafUtils.initUI(defaults, "org.designup.picsou.gui.plaf.PicsouMacFileChooserUI", "FileChooserUI");
//      LafUtils.initUI(defaults, "org.designup.picsou.gui.plaf.PicsouRootPaneUI", "RootPaneUI");
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
