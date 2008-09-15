package org.designup.picsou.gui.components;

import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.ColorUpdaters;
import org.globsframework.gui.splits.layout.Anchor;
import org.globsframework.gui.splits.layout.Fill;
import org.globsframework.gui.splits.layout.GridBagBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class PicsouDialog extends JDialog {

  public static boolean MODAL = true;
  private static final Insets BUTTON_INSETS = new Insets(0, 10, 10, 10);
  private ColorService colorService;
  private static final int HORIZONTAL_BUTTON_MARGIN = Gui.isMacOSX() ? 20 : 0;
  private Action closeAction;

  public static PicsouDialog create(Window owner, Directory directory) {
    if (owner instanceof JFrame) {
      return new PicsouDialog((JFrame)owner, directory);
    }
    else if (owner instanceof JDialog) {
      return new PicsouDialog((JDialog)owner, directory);
    }
    throw new InvalidParameter("unknown type " + owner.getClass());
  }

  public static PicsouDialog createWithButton(Window owner, JPanel panel, Action closeAction, Directory directory) {
    PicsouDialog dialog = create(owner, directory);
    dialog.setPanelAndButton(panel, closeAction);
    return dialog;
  }

  private void setPanelAndButton(JPanel panel, Action closeAction) {
    this.closeAction = closeAction;
    JPanel contentPane = GridBagBuilder.init()
      .add(panel, 0, 0, 2, 1, Gui.NO_INSETS)
      .add(Box.createHorizontalGlue(), 0, 1, 1, 1, 1000, 0, Fill.HORIZONTAL, Anchor.CENTER)
      .add(createButton(closeAction), 1, 1, 1, 1, 1, 0, Fill.HORIZONTAL, Anchor.CENTER, BUTTON_INSETS)
      .getPanel();
    setContentPane(contentPane);
  }

  private JButton createButton(Action action) {
    JButton button = new JButton(action);
    button.setOpaque(false);
    return button;
  }

  public static PicsouDialog createWithButtons(Window owner, JPanel panel, Action ok, Action cancel, Directory directory) {
    PicsouDialog dialog = create(owner, directory);
    dialog.addInPanelWithButton(panel, ok, cancel);
    return dialog;
  }

  public void addInPanelWithButton(JPanel panel, Action ok, Action cancel) {
    closeAction = cancel;
    int buttonCount = 0;
    if (ok != null) {
      buttonCount++;
    }
    if (cancel != null) {
      buttonCount++;
    }
    Insets noInsets = Gui.NO_INSETS;
    GridBagBuilder builder = GridBagBuilder.init()
      .add(panel, 0, 0, buttonCount + 1, 1, noInsets);

    builder.add(Box.createHorizontalGlue(), 0, 1, 1, 1, 1000, 0, Fill.HORIZONTAL, Anchor.CENTER);

    JButton cancelButton = createButton(cancel);
    JButton okButton = createButton(ok);
    adjustSizes(cancelButton, okButton);

    Insets buttonInsets = new Insets(0, 10, 10, 10);
    if (Gui.isMacOSX()) {
      builder.add(cancelButton, 1, 1, 1, 1, 1, 0, Fill.HORIZONTAL, Anchor.CENTER, buttonInsets);
      builder.add(okButton, 2, 1, 1, 1, 1, 0, Fill.HORIZONTAL, Anchor.CENTER, buttonInsets);
    }
    else {
      builder.add(okButton, 1, 1, 1, 1, 1, 0, Fill.HORIZONTAL, Anchor.CENTER, buttonInsets);
      builder.add(cancelButton, 2, 1, 1, 1, 1, 0, Fill.HORIZONTAL, Anchor.CENTER, buttonInsets);
    }

    JPanel contentPane = builder.getPanel();
    setContentPane(contentPane);
  }

  public void setContentPane(Container contentPane) {
    colorService.install("dialog.bg.bottom", ColorUpdaters.background(contentPane));
    super.setContentPane(contentPane);
  }

  private void adjustSizes(JButton cancelButton, JButton okButton) {
    Dimension preferredSize = getWidest(okButton.getPreferredSize(), cancelButton.getPreferredSize());
    okButton.setPreferredSize(preferredSize);
    cancelButton.setPreferredSize(preferredSize);

    Dimension minimumSize = getWidest(okButton.getMinimumSize(), cancelButton.getMinimumSize());
    okButton.setMinimumSize(minimumSize);
    cancelButton.setMinimumSize(minimumSize);
  }

  private Dimension getWidest(Dimension dimension1, Dimension dimension2) {
    if (dimension1.width > dimension2.width) {
      return new Dimension(dimension1.width + HORIZONTAL_BUTTON_MARGIN, dimension1.height);
    }
    else {
      return new Dimension(dimension2.width + HORIZONTAL_BUTTON_MARGIN, dimension2.height);
    }
  }

  public void setWindowCloseCallback(final DisposeCallback disposeCallback) {
    addWindowListener(new WindowAdapter() {
      public void windowClosed(WindowEvent e) {
        disposeCallback.processDispose();
      }
    });
  }

  private PicsouDialog(JFrame parent, Directory directory) {
    super(parent, MODAL);
    init(directory);
  }

  private PicsouDialog(JDialog parent, Directory directory) {
    super(parent, MODAL);
    init(directory);
  }

  private void init(Directory directory) {
    setTitle(Lang.get("application"));
    colorService = directory.get(ColorService.class);
  }

  protected JRootPane createRootPane() {
    JRootPane rootPane = new JRootPane();
    GuiUtils.addShortcut(rootPane, "ESCAPE", new CloseAction());
    return rootPane;
  }

  private class CloseAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      if (closeAction != null) {
        closeAction.actionPerformed(e);
      }
      else {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            setVisible(false);
          }
        });
      }
    }
  }
}
