package org.designup.picsou.gui.plaf;

import apple.laf.AquaLookAndFeel;
import org.designup.picsou.utils.Lang;

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
  }

  public boolean getSupportsWindowDecorations() {
    return true;
  }
}
