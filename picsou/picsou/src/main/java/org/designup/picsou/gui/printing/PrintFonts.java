package org.designup.picsou.gui.printing;

import java.awt.*;

public class PrintFonts {
  private final Font titleFont = new Font("Arial", Font.BOLD, 20);
  private final Font tableTextFont = new Font("Arial", Font.PLAIN, 9);

  public Font getTableTextFont() {
    return tableTextFont;
  }

  public Font getTitleFont() {
    return titleFont;
  }
}
