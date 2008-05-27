package org.designup.picsou.gui.plaf;

import apple.laf.AquaFileChooserUI;
import org.crossbowlabs.splits.utils.GuiUtils;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.PicsouDialogPainter;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;

public class PicsouMacFileChooserUI extends AquaFileChooserUI {

  private PicsouDialogPainter painter;
  private JDialog dialog;

  public PicsouMacFileChooserUI(JFileChooser fileChooser) {
    super(fileChooser);
  }

  public static ComponentUI createUI(JComponent c) {
    return new PicsouMacFileChooserUI((JFileChooser)c);
  }

  public void installUI(JComponent c) {
    super.installUI(c);
    dialog = null;
    if (painter == null) {
      painter = new PicsouDialogPainter();
    }
    Gui.installWindowTitle(c, painter, getDialogTitle((JFileChooser)c), 10);
  }

  public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    installMovingWindowTitle(c);
  }

  private void installMovingWindowTitle(JComponent c) {
    if (dialog != null) {
      return;
    }

    dialog = (JDialog)GuiUtils.getEnclosingComponent(c, new GuiUtils.ComponentMatcher() {
      public boolean matches(Component component) {
        return JDialog.class.isInstance(component);
      }
    });

    Gui.installMovingWindowTitle(dialog);
  }
}
