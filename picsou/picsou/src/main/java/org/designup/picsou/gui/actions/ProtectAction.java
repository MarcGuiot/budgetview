package org.designup.picsou.gui.actions;

import org.designup.picsou.gui.backup.AbstractBackupRestoreAction;
import org.designup.picsou.gui.backup.RenameDialog;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.model.User;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.Glob;
import org.globsframework.utils.directory.Directory;
import org.globsframework.metamodel.GlobType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class ProtectAction extends AbstractBackupRestoreAction {

  public ProtectAction(GlobRepository repository, Directory directory) {
    super(Lang.get("protect"), repository, directory);
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
        putValue(Action.NAME, Lang.get("protect"));
      }
      else {
        putValue(Action.NAME, Lang.get("changePassword"));
      }
    }
  }

  public void actionPerformed(ActionEvent e) {
    RenameDialog dialog = new RenameDialog(repository, directory);
    dialog.show();
  }
}
