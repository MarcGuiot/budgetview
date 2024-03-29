package com.budgetview.desktop.actions;

import com.budgetview.desktop.MainPanel;
import com.budgetview.desktop.backup.AskPasswordDialog;
import com.budgetview.desktop.components.dialogs.ConfirmationDialog;
import com.budgetview.model.User;
import com.budgetview.utils.Lang;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class DeleteUserAction extends AbstractAction implements ChangeSetListener {
  private MainPanel mainPanel;
  private GlobRepository repository;
  private Directory directory;

  public DeleteUserAction(MainPanel mainPanel, GlobRepository repository, Directory directory) {
    super(Lang.get("deleteUser"));
    this.mainPanel = mainPanel;
    this.repository = repository;
    this.directory = directory;
    repository.addChangeListener(this);
  }

  public void actionPerformed(ActionEvent e) {
    final Glob user = repository.get(User.KEY);

    if (user.isTrue(User.AUTO_LOGIN)) {
      ConfirmationDialog dialog = new ConfirmationDialog("delete.autologin.title",
                                                         Lang.get("delete.autologin.message"),
                                                         directory.get(JFrame.class), directory) {
        protected void processOk() {
          mainPanel.deleteUser(user.get(User.NAME), user.get(User.NAME).toCharArray());
        }
      };
      dialog.show();
    }
    else {
      char[] chars;
      AskPasswordDialog askPasswordDialog =
        new AskPasswordDialog("delete.password.title", "delete.password.label", "delete.password.message",
                              directory, user.get(User.NAME));
      chars = askPasswordDialog.show();
      if (chars == null || chars.length == 0) {
        return;
      }
      mainPanel.deleteUser(user.get(User.NAME), chars);
    }
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(User.KEY)) {
      setEnabled(!repository.get(User.KEY).isTrue(User.IS_DEMO_USER));
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    Glob user = repository.find(User.KEY);
    if (user != null) {
      setEnabled(!user.isTrue(User.IS_DEMO_USER));
    }
  }
}
