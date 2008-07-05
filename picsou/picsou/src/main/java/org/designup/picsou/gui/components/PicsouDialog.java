package org.designup.picsou.gui.components;

import org.globsframework.gui.splits.layout.GridBagBuilder;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class PicsouDialog extends JDialog {

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

  public void setWindowCloseCallback(final DisposeCallback disposeCallback) {
    addWindowListener(new WindowAdapter() {
      public void windowClosed(WindowEvent e) {
        disposeCallback.processDispose();
      }
    });
  }

  private PicsouDialog(JFrame parent) {
    super(parent, true);
  }

  private PicsouDialog(JDialog parent) {
    super(parent, true);
  }

  protected JRootPane createRootPane() {
    JRootPane rootPane = new JRootPane();
    KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");
    InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    inputMap.put(stroke, "ESCAPE");

    rootPane.getActionMap().put("ESCAPE", new CloseAction());
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
