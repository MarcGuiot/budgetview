package org.designup.picsou.gui.plaf;

import org.designup.picsou.gui.components.PicsouDialogPainter;
import org.designup.picsou.gui.components.MovingDialog;
import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicOptionPaneUI;
import java.awt.*;

public class PicsouOptionPaneUI extends BasicOptionPaneUI {
  private static final PicsouOptionPaneUI OPTION_PANE_UI = new PicsouOptionPaneUI();

  private PicsouDialogPainter painter;
  private JDialog dialog;

  public static ComponentUI createUI(JComponent c) {
    return OPTION_PANE_UI;
  }

  public void installUI(JComponent c) {
    super.installUI(c);
    dialog = null;
    c.setOpaque(false);
    if (painter == null) {
      painter = new PicsouDialogPainter();
    }
    MovingDialog.installWindowTitle(c, painter, "", 10);
  }

  protected Container createMessageArea() {
    JPanel panel = (JPanel)super.createMessageArea();
    GuiUtils.opacify(panel);
    return panel;
  }

  protected Container createButtonArea() {
    JPanel panel = (JPanel)super.createButtonArea();
    panel.setOpaque(false);
    return panel;
  }

  protected boolean getSizeButtonsToSameWidth() {
    return false;
  }

  public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    painter.paint(g, c.getWidth(), c.getHeight());
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

    MovingDialog.installMovingWindowTitle(dialog);
  }
}
