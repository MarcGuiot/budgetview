package org.designup.picsou.gui.components;

import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.PicsouDialogPainter;
import org.globsframework.gui.splits.layout.GridBagBuilder;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;
import java.awt.*;

public class PicsouDialog extends JDialog {

  private static PicsouDialogPainter painter = new PicsouDialogPainter();

  public static PicsouDialog create(Window owner) {
    if (owner instanceof JFrame) {
      return create((JFrame)owner, "");
    }
    else if (owner instanceof JDialog) {
      return create((JDialog)owner, "");
    }
    throw new InvalidParameter("unknown type " + owner.getClass());
  }

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

  public static PicsouDialog createWithButtons(Window owner, JPanel panel, Action... buttonActions) {
    Insets noInsets = new Insets(0, 0, 0, 0);
    GridBagBuilder builder = GridBagBuilder.init()
      .add(panel, 0, 0, buttonActions.length + 1, 1, noInsets)
      .add(Box.createHorizontalGlue(), 0, 1, 1, 1, noInsets);

    Insets buttonInsets = new Insets(10, 10, 10, 10);
    int index = 1;
    for (Action action : buttonActions) {
      builder.add(new JButton(action), index++, 1, 1, 1, buttonInsets);
    }
    PicsouDialog dialog = create(owner);
    dialog.setContentPane(builder.getPanel());
    return dialog;
  }

  private static PicsouDialog initBorder(String title, PicsouDialog modalWindow) {
    if (title != null) {
      modalWindow.setTitle(title);
    }
    JPanel container = (JPanel)modalWindow.getContentPane();
    Gui.installWindowTitle(container, painter, title == null ? "" : title, 0);
    Gui.installMovingWindowTitle(modalWindow);
    return modalWindow;
  }

  private PicsouDialog(JFrame parent) {
    super(parent, true);
    setUndecorated(true);
  }

  private PicsouDialog(JDialog parent) {
    super(parent, true);
    setUndecorated(true);
  }

  public void setContentPane(Container contentPane) {
    Gui.installWindowTitle((JComponent)contentPane, painter, getTitle() == null ? "" : getTitle(), 0);
    Gui.installMovingWindowTitle(this);
    super.setContentPane(contentPane);
  }
}
