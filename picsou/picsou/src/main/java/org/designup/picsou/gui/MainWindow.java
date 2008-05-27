package org.designup.picsou.gui;

import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PicsouWindowsLookAndFeel;
import org.crossbowlabs.splits.utils.GuiUtils;
import org.designup.picsou.gui.plaf.PicsouMacLookAndFeel;
import org.designup.picsou.gui.utils.FadingSwapper;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.PicsouDialogPainter;

import javax.swing.*;

public class MainWindow {
  private JFrame frame;
  private PicsouDialogPainter painter = new PicsouDialogPainter();

  static {
    try {
      if (Gui.isMacOSX()) {
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
      JDialog.setDefaultLookAndFeelDecorated(true);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public MainWindow() throws Exception {
    frame = new JFrame("Picsou");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setUndecorated(false);
//    Gui.installWindowTitle(frame.getRootPane(), painter, frame.getTitle(), 0);
//    Gui.installMovingWindowTitle(frame);
  }

  public void setPanel(JPanel panel) {
    frame.setContentPane(panel);
  }

  public void fadeTo(JPanel panel) {
    FadingSwapper.init(frame).swapTo(panel);
  }

  public JFrame getFrame() {
    return frame;
  }

  public void show() {
    frame.setSize(1100, 800);
    GuiUtils.showCentered(frame);
  }
}
