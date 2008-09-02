package org.designup.picsou.gui.components;

import org.designup.picsou.gui.utils.Gui;
import org.globsframework.gui.splits.layout.Anchor;
import org.globsframework.gui.splits.layout.Fill;
import org.globsframework.gui.splits.layout.GridBagBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class PicsouDialog extends JDialog {

  public static boolean MODAL = true;

  public static PicsouDialog create(JFrame owner) {
    return new PicsouDialog(owner);
  }

  public static PicsouDialog create(JDialog owner) {
    return new PicsouDialog(owner);
  }

  public static PicsouDialog create(Window owner, String title) {
    PicsouDialog modalWindow = create(owner);
    if (title != null) {
      modalWindow.setTitle(title);
    }
    return modalWindow;
  }

  public static PicsouDialog create(JFrame owner, String title) {
    PicsouDialog modalWindow = create(owner);
    if (title != null) {
      modalWindow.setTitle(title);
    }
    return modalWindow;
  }

  public static PicsouDialog create(JDialog owner, String title) {
    PicsouDialog modalWindow = create(owner);
    if (title != null) {
      modalWindow.setTitle(title);
    }
    return modalWindow;
  }

  public static PicsouDialog createWithButtons(String name, Window owner, JPanel panel, Action ok, Action cancel) {
    PicsouDialog dialog = create(owner, name);
    dialog.addInPanelWithButton(panel, ok, cancel);
    return dialog;
  }

  public void addInPanelWithButton(JPanel panel, Action ok, Action cancel) {
    int buttonCount = 0;
    if (ok != null) {
      buttonCount++;
    }
    if (cancel != null) {
      buttonCount++;
    }
    Insets noInsets = new Insets(0, 0, 0, 0);
    GridBagBuilder builder = GridBagBuilder.init()
      .add(panel, 0, 0, buttonCount + 1, 1, noInsets);

    int index = 0;
    builder.add(Box.createHorizontalGlue(), index++, 1, 1, 1, 1000, 0, Fill.HORIZONTAL, Anchor.CENTER);

    JButton cancelButton = new JButton(cancel);
    JButton okButton = new JButton(ok);
    adjustSizes(cancelButton, okButton);

    Insets buttonInsets = new Insets(0, 10, 10, 10);
    if (Gui.isMacOSX()) {
      builder.add(cancelButton, index++, 1, 1, 1, 1, 0, Fill.HORIZONTAL, Anchor.CENTER, buttonInsets);
      builder.add(okButton, index++, 1, 1, 1, 1, 0, Fill.HORIZONTAL, Anchor.CENTER, buttonInsets);
    }
    else {
      builder.add(okButton, index++, 1, 1, 1, 1, 0, Fill.HORIZONTAL, Anchor.CENTER, buttonInsets);
      builder.add(cancelButton, index++, 1, 1, 1, 1, 0, Fill.HORIZONTAL, Anchor.CENTER, buttonInsets);
    }

    JPanel contentPane = builder.getPanel();
    setContentPane(contentPane);
  }

  private void adjustSizes(JButton cancelButton, JButton okButton) {
    Dimension cancelSize = cancelButton.getPreferredSize();
    Dimension okSize = okButton.getPreferredSize();
    if (cancelSize.width > okSize.width) {
      okButton.setPreferredSize(cancelSize);
    }
    else {
      cancelButton.setPreferredSize(okSize);
    }
    Dimension cancelMinSize = cancelButton.getMinimumSize();
    Dimension okMinSize = okButton.getMinimumSize();
    if (cancelMinSize.width > okMinSize.width) {
      okButton.setMinimumSize(cancelMinSize);
    }
    else {
      cancelButton.setMinimumSize(okMinSize);
    }
  }

  private static PicsouDialog create(Window owner) {
    if (owner instanceof JFrame) {
      return create((JFrame)owner, "");
    }
    else if (owner instanceof JDialog) {
      return create((JDialog)owner, "");
    }
    throw new InvalidParameter("unknown type " + owner.getClass());
  }

  public void setWindowCloseCallback(final DisposeCallback disposeCallback) {
    addWindowListener(new WindowAdapter() {
      public void windowClosed(WindowEvent e) {
        disposeCallback.processDispose();
      }
    });
  }

  private PicsouDialog(JFrame parent) {
    super(parent, MODAL);
  }

  private PicsouDialog(JDialog parent) {
    super(parent, MODAL);
  }

  protected JRootPane createRootPane() {
    JRootPane rootPane = new JRootPane();
    GuiUtils.addShortcut(rootPane, "ESCAPE", new CloseAction());
    return rootPane;
  }

  private class CloseAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          setVisible(false);
        }
      });
    }
  }
}
