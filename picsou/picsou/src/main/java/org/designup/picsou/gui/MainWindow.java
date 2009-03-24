package org.designup.picsou.gui;

import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PicsouWindowsLookAndFeel;
import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.gui.plaf.PicsouMacLookAndFeel;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;

public class MainWindow {
  private PicsouFrame frame;

  static {
    try {
      if (GuiUtils.isMacOSX()) {
        UIManager.setLookAndFeel(new PicsouMacLookAndFeel());
      }
      else {
        Options.setUseSystemFonts(true);
        Options.setUseNarrowButtons(false);

        PicsouWindowsLookAndFeel.set3DEnabled(true);
        PicsouWindowsLookAndFeel.setHighContrastFocusColorsEnabled(false);
        PicsouWindowsLookAndFeel.setSelectTextOnKeyboardFocusGained(false);

        UIManager.put("FileChooser.useSystemIcons", Boolean.TRUE);
        UIManager.setLookAndFeel(new PicsouWindowsLookAndFeel());
      }
      JDialog.setDefaultLookAndFeelDecorated(false);
      ToolTipManager.sharedInstance().setInitialDelay(500);
      ToolTipManager.sharedInstance().setDismissDelay(100000);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public MainWindow() throws Exception {
    frame = new PicsouFrame(Lang.get("application"));
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    ImageIcon icon = Gui.IMAGE_LOCATOR.get("app_icon_128.png");
    frame.setIconImage(icon.getImage());
  }

  public void setPanel(JPanel panel) {
    frame.setContentPane(panel);
    frame.validate();
  }

  public PicsouFrame getFrame() {
    return frame;
  }

  public void show() {
    frame.setSize(Gui.getWindowSize(1100, 800));
    GuiUtils.showCentered(frame);
  }
}
