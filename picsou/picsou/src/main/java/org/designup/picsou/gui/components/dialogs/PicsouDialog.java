package org.designup.picsou.gui.components.dialogs;

import org.designup.picsou.gui.startup.OpenRequestManager;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.components.DisposeCallback;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.ColorUpdater;
import org.globsframework.gui.splits.color.utils.BackgroundColorUpdater;
import org.globsframework.gui.splits.layout.Anchor;
import org.globsframework.gui.splits.layout.Fill;
import org.globsframework.gui.splits.layout.GridBagBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

public class PicsouDialog extends JDialog {

  public static boolean FORCE_NONMODAL = false;
  private static final Insets BUTTON_INSETS = new Insets(0, 10, 10, 10);
  private ColorService colorService;
  private static final int HORIZONTAL_BUTTON_MARGIN = GuiUtils.isMacOSX() ? 20 : 0;
  private Action closeAction;
  private Directory directory;
  private boolean openRequestIsManaged = false;
  private ColorUpdater updater;

  public static PicsouDialog create(Window owner, Directory directory) {
    return create(owner, true, directory);
  }

  public static PicsouDialog create(Window owner, boolean modal, Directory directory) {
    Component parent = GuiUtils.getEnclosingComponent(owner, new GuiUtils.ComponentMatcher() {
      public boolean matches(Component component) {
        return JFrame.class.isInstance(component) || JDialog.class.isInstance(component);
      }
    });
    if (parent instanceof JFrame) {
      return new PicsouDialog((JFrame)parent, modal, directory);
    }
    else if (parent instanceof JDialog) {
      return new PicsouDialog((JDialog)parent, modal, directory);
    }
    else {
      return new PicsouDialog((JFrame)null, true,  directory);
    }
  }

  public static PicsouDialog createWithButton(Window owner, JPanel panel, Action action, Directory directory) {
    return createWithButton(owner, true, panel, action, directory);
  }

  public static PicsouDialog createWithButton(Window owner, boolean modal, JPanel panel, Action closeAction, Directory directory) {
    PicsouDialog dialog = create(owner, modal, directory);
    dialog.setPanelAndButton(panel, closeAction);
    return dialog;
  }

  public static PicsouDialog createWithButtons(Window owner, Directory directory, JPanel panel, Action ok, Action cancel) {
    PicsouDialog dialog = create(owner, directory);
    dialog.addPanelWithButtons(panel, ok, cancel);
    return dialog;
  }

  public void setPanelAndButton(JPanel panel, Action closeAction) {
    this.closeAction = closeAction;
    JPanel contentPane = GridBagBuilder.init()
      .add(panel, 0, 0, 2, 1, Gui.NO_INSETS)
      .add(Box.createHorizontalGlue(), 0, 1, 1, 1, 1000, 0, Fill.HORIZONTAL, Anchor.CENTER)
      .add(createButton(closeAction), 1, 1, 1, 1, 1, 0, Fill.HORIZONTAL, Anchor.CENTER, BUTTON_INSETS)
      .getPanel();
    setContentPane(contentPane);
  }

  public void addPanelWithButton(JPanel panel, Action ok) {
    addPanelWithButtons(panel, ok, null, (JButton)null);
  }

  public void addPanelWithButtons(JPanel panel, Action ok, Action cancel) {
    addPanelWithButtons(panel, ok, cancel, (JButton)null);
  }

  public void addPanelWithButtons(JPanel panel, Action ok, Action cancel, Action additionalAction) {
    addPanelWithButtons(panel, ok, cancel, createButton(additionalAction));
  }

  public void addPanelWithButtons(JPanel panel, Action ok, Action cancel, JButton additionalAction) {
    closeAction = cancel;
    GridBagBuilder builder = GridBagBuilder.init()
      .add(panel, 0, 0, 4 + 1, 1, Gui.NO_INSETS);

    if (additionalAction != null) {
      builder.add(additionalAction, 0, 1, 1, 1, 1, 0, Fill.HORIZONTAL, Anchor.CENTER, BUTTON_INSETS);
    }

    builder.add(Box.createHorizontalGlue(), 1, 1, 1, 1, 1000, 0, Fill.HORIZONTAL, Anchor.CENTER);

    JButton cancelButton = null;
    if (cancel != null) {
      cancelButton = createButton(cancel);
    }
    JButton okButton = createButton(ok);
    if (cancel != null) {
      adjustSizes(cancelButton, okButton);
    }

    Insets buttonInsets = new Insets(0, 10, 10, 10);
    if (GuiUtils.isMacOSX()) {
      if (cancelButton != null) {
        builder.add(cancelButton, 2, 1, 1, 1, 1, 0, Fill.HORIZONTAL, Anchor.CENTER, BUTTON_INSETS);
      }
      builder.add(okButton, 3, 1, 1, 1, 1, 0, Fill.HORIZONTAL, Anchor.CENTER, BUTTON_INSETS);
    }
    else {
      if (cancelButton != null) {
        builder.add(okButton, 2, 1, 1, 1, 1, 0, Fill.HORIZONTAL, Anchor.CENTER, BUTTON_INSETS);
        builder.add(cancelButton, 3, 1, 1, 1, 1, 0, Fill.HORIZONTAL, Anchor.CENTER, BUTTON_INSETS);
      }
      else {
        builder.add(okButton, 3, 1, 1, 1, 1, 0, Fill.HORIZONTAL, Anchor.CENTER, BUTTON_INSETS);
      }
    }

    JPanel contentPane = builder.getPanel();
    setContentPane(contentPane);
  }

  public void dispose() {
    updater.dispose();
    GuiUtils.removeShortcut(getRootPane(), "ESCAPE", KeyStroke.getKeyStroke("ESCAPE"));
    super.dispose();
  }

  public void setContentPane(Container contentPane) {
    updater = new BackgroundColorUpdater("dialog.bg.bottom", contentPane);
    updater.install(colorService);
    super.setContentPane(contentPane);
  }

  public void setVisible(boolean visible) {
    final OpenRequestManager requestManager = directory.get(OpenRequestManager.class);
    if (visible && !openRequestIsManaged) {
      requestManager.pushCallback(new OpenRequestManager.Callback() {
        public boolean accept() {
          return false;
        }

        public void openFiles(final List<File> files) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              requestManager.openFiles(files);
            }
          });
        }
      });
    }
    super.setVisible(visible);
    if (visible && !openRequestIsManaged) {
      requestManager.popCallback();
    }
  }

  private JButton createButton(Action action) {
    JButton button = new JButton(action);
    button.setOpaque(false);
    return button;
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

  private PicsouDialog(JFrame parent, boolean modal, Directory directory) {
    super(parent, !FORCE_NONMODAL && modal);
    init(directory);
  }

  private PicsouDialog(JDialog parent, boolean modal, Directory directory) {
    super(parent, !FORCE_NONMODAL && modal);
    init(directory);
  }

  private void init(Directory directory) {
    this.directory = directory;
    setTitle(Lang.get("application"));
    colorService = directory.get(ColorService.class);
  }

  protected JRootPane createRootPane() {
    JRootPane rootPane = new JRootPane();
    GuiUtils.addShortcut(rootPane, "ESCAPE", new CloseAction());
    return rootPane;
  }

  public void disableEscShortcut() {
    GuiUtils.removeShortcut(getRootPane(), "ESCAPE", KeyStroke.getKeyStroke("ESCAPE"));
  }

  public void setOpenRequestIsManaged(boolean openRequestIsManaged) {
    this.openRequestIsManaged = openRequestIsManaged;
  }

  public void showCentered() {
    GuiUtils.showCentered(this);
  }

  public void setAutoFocusOnOpen(final JTextField editor) {
    addWindowListener(new WindowAdapter() {
      public void windowOpened(WindowEvent e) {
        editor.selectAll();
        editor.requestFocusInWindow();
      }
    });
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
