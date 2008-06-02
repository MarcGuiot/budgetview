package org.designup.picsou.gui.utils;

import javax.swing.*;
import java.awt.*;

public class PicsouDialog extends JDialog {

  private static PicsouDialogPainter painter = new PicsouDialogPainter();

  public static PicsouDialog create(JFrame owner) {
    return new PicsouDialog(owner);
  }

  public static PicsouDialog create(JDialog owner) {
    return new PicsouDialog(owner);
  }

  public static PicsouDialog create(JFrame owner, String title) {
    return initBorder(title, create(owner));
  }

  public static PicsouDialog create(JDialog owner, String title) {
    return initBorder(title, create(owner));
  }

  private static PicsouDialog initBorder(String title, PicsouDialog modalWindow) {
    modalWindow.setTitle(title);
    JPanel container = (JPanel) modalWindow.getContentPane();
    if (title != null) {
      Gui.installWindowTitle(container, painter, title, 0);
      Gui.installMovingWindowTitle(modalWindow);
    }
    return modalWindow;
  }

  private PicsouDialog(JFrame parentFrame) {
    super(parentFrame, true);
    setUndecorated(true);
  }

  private PicsouDialog(JDialog parentFrame) {
    super(parentFrame, true);
    setUndecorated(true);
  }

  public void setContentPane(Container contentPane) {
    if (getTitle() != null) {
      Gui.installWindowTitle((JComponent) contentPane, painter, getTitle(), 0);
      Gui.installMovingWindowTitle(this);
    }
    super.setContentPane(contentPane);
  }
}
