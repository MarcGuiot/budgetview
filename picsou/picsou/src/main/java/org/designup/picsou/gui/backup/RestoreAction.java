package org.designup.picsou.gui.backup;

import org.designup.picsou.gui.components.dialogs.MessageFileDialog;
import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.undo.UndoRedoService;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.User;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Glob;
import org.globsframework.model.Key;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;
import org.globsframework.gui.SelectionService;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;

public class RestoreAction extends AbstractBackupRestoreAction {

  public RestoreAction(GlobRepository repository, Directory directory) {
    super(Lang.get("restore"), repository, directory);
  }

  public void actionPerformed(ActionEvent e) {

    Glob user = repository.get(User.KEY);
    if (!user.isTrue(User.IS_REGISTERED_USER)){
      MessageDialog.show("restore.trial.title", "restore.trial.content", frame, directory);
      return;
    }

    JFileChooser chooser = getFileChooser();
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    int returnVal = chooser.showOpenDialog(frame);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      try {
        char[] password = null;
        while (true) {
          Gui.setWaitCursor(frame);
          BackupService.Status completed;
          try {
            completed = backupService.restore(new FileInputStream(file), password);
          }
          finally {
            Gui.setDefaultCursor(frame);
          }
          if (completed == BackupService.Status.BAD_VERSION){
            MessageDialog.show("restore.error.title", "restore.bad.version", frame, directory);
            return;
          }
          if (completed == BackupService.Status.OK) {
            resetUndoRedo();
            selectCurrentMonth();
            showConfirmationDialog(file);
            return;
          }
          else {
            AskPasswordDialog dialog = 
              new AskPasswordDialog("restore.password.title", "restore.password.label", "restore.password.message", directory);
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

  private void resetUndoRedo() {
    directory.get(UndoRedoService.class).reset();
  }

  private void showConfirmationDialog(File file) {
    MessageFileDialog dialog = new MessageFileDialog(repository, directory);
    dialog.show("restore.ok.title", "restore.ok.message", file);
  }

  private void selectCurrentMonth() {
    Glob month = repository.get(Key.create(Month.TYPE, CurrentMonth.getLastTransactionMonth(repository)));
    directory.get(SelectionService.class).select(month);
  }
}
