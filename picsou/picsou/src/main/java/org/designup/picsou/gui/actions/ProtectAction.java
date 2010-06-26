package org.designup.picsou.gui.actions;

import org.designup.picsou.gui.backup.AbstractBackupRestoreAction;
import org.designup.picsou.gui.backup.RenameDialog;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.awt.event.ActionEvent;

public class ProtectAction extends AbstractBackupRestoreAction {

  public ProtectAction(GlobRepository repository, Directory directory) {
    super(Lang.get("protect"), repository, directory);
  }

  public void actionPerformed(ActionEvent e) {
    RenameDialog dialog = new RenameDialog(repository, directory);
    dialog.show();
  }
}
