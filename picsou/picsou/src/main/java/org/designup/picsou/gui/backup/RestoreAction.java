package org.designup.picsou.gui.backup;

import org.designup.picsou.gui.components.dialogs.MessageFileDialog;
import org.designup.picsou.gui.startup.BackupService;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Glob;
import org.globsframework.model.Key;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;
import org.globsframework.gui.SelectionService;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;

public class RestoreAction extends AbstractBackupRestoreAction {

  public RestoreAction(GlobRepository repository, Directory directory) {
    super(Lang.get("restore"), repository, directory);
  }

  public void actionPerformed(ActionEvent e) {
    JFileChooser chooser = getFileChooser();
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    int returnVal = chooser.showOpenDialog(frame);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      try {
        char[] password = null;
        while (true) {
          Gui.setWaitCursor(frame);
          boolean completed;
          try {
            completed = backupService.restore(new FileInputStream(file), password);
          }
          finally {
            Gui.setDefaultCursor(frame);
          }
          if (completed) {
            restoreCompleted(file);
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

  private void restoreCompleted(File file) {

    Glob month = repository.get(Key.create(Month.TYPE, CurrentMonth.getLastTransactionMonth(repository)));
    directory.get(SelectionService.class).select(month);

    MessageFileDialog dialog = new MessageFileDialog(repository, directory);
    dialog.show("restore.ok.title", "restore.ok.message", file);
  }
}