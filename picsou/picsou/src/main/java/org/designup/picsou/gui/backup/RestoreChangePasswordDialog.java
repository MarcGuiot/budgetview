package org.designup.picsou.gui.backup;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RestoreChangePasswordDialog {
  private GlobRepository repository;
  private Directory directory;
  private JPasswordField passwordField;
  private PicsouDialog dialog;

  public RestoreChangePasswordDialog(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
  }

  public char[] show() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/restoreChangePassword.splits", repository, directory);
    passwordField = builder.add("password", new JPasswordField());
    dialog = PicsouDialog.create(directory.get(JFrame.class), directory);

    final AbstractAction okAction = new OkAction();
    passwordField.addActionListener(okAction);

    dialog.addPanelWithButtons(builder.<JPanel>load(), okAction, new CancelAction(dialog));
    dialog.pack();
    passwordField.requestFocusInWindow();
    dialog.showCentered();

    return passwordField.getPassword();
  }

  private class CancelAction extends AbstractAction {
    private final PicsouDialog dialog;

    public CancelAction(PicsouDialog dialog) {
      super(Lang.get("cancel"));
      this.dialog = dialog;
    }

    public void actionPerformed(ActionEvent e) {
      passwordField.setText(null);
      dialog.setVisible(false);
    }
  }

  private class OkAction extends AbstractAction {
    public OkAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      dialog.setVisible(false);
    }
  }
}
