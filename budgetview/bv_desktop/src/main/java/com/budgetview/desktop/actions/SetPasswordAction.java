package com.budgetview.desktop.actions;

import com.budgetview.desktop.backup.AbstractBackupRestoreAction;
import com.budgetview.desktop.backup.SetPasswordDialog;
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

public class SetPasswordAction extends AbstractBackupRestoreAction {

  public SetPasswordAction(GlobRepository repository, Directory directory) {
    super(Lang.get("setPassword.menu.protect"), repository, directory);
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(User.KEY)){
          updateLabel(repository);
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(User.TYPE)){
          updateLabel(repository);
        }
      }
    });
  }

  private void updateLabel(GlobRepository repository) {
    Glob user = repository.find(User.KEY);
    if (user != null){
      if (user.get(User.AUTO_LOGIN)){
        putValue(Action.NAME, Lang.get("setPassword.menu.protect"));
      }
      else {
        putValue(Action.NAME, Lang.get("setPassword.menu.change"));
      }
    }
  }

  public void actionPerformed(ActionEvent e) {
    SetPasswordDialog dialog = new SetPasswordDialog(repository, directory);
    dialog.show();
  }
}
