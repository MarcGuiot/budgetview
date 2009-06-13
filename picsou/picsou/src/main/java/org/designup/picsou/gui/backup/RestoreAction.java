package org.designup.picsou.gui.backup;

import org.designup.picsou.gui.components.dialogs.MessageFileDialog;
import org.designup.picsou.gui.startup.BackupService;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.Log;

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
        backupService.restore(new FileInputStream(file));
        MessageFileDialog dialog = new MessageFileDialog(repository, directory);
        dialog.show("restore.ok.title", "restore.ok.message", file);
      }
      catch (Exception ex) {
        Log.write("Restore failed", ex);
        MessageFileDialog dialog = new MessageFileDialog(repository, directory);
        dialog.show("restore.error.title", "restore.error.message", file);
      }
    }
  }
}