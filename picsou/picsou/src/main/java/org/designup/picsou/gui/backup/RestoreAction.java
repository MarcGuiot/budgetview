package org.designup.picsou.gui.backup;

import org.designup.picsou.gui.components.dialogs.MessageFileDialog;
import org.designup.picsou.gui.startup.BackupService;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;

public class RestoreAction extends AbstractBackupRestoreAction {
  private JFrame parent;
  private BackupService backupService;

  public RestoreAction(GlobRepository repository, Directory directory) {
    super(Lang.get("restore"), repository, directory);
    this.backupService = directory.get(BackupService.class);
    this.parent = directory.get(JFrame.class);
  }

  public void actionPerformed(ActionEvent e) {
    JFileChooser chooser = getFileChooser();
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    int returnVal = chooser.showOpenDialog(parent);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      try {
        char[] password = null;
        while (true) {
          if (backupService.restore(new FileInputStream(file), password)) {
            MessageFileDialog dialog = new MessageFileDialog(repository, directory);
            dialog.show("restore.ok.title", "restore.ok.message", file);
            return;
          }
          else {
            RestoreChangePasswordDialog dialog = new RestoreChangePasswordDialog(repository, directory);
            password = dialog.show();
            if (password == null || password.length == 0){
              return;
            }
          }
        }
      }
      catch (Exception ex) {
        Log.write("Restore failed", ex);
        MessageFileDialog dialog = new MessageFileDialog(repository, directory);
        dialog.show("restore.error.title", "restore.error.message", file);
      }
    }
  }
}