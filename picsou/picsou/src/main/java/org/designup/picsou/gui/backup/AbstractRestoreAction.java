package org.designup.picsou.gui.backup;

import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.undo.UndoRedoService;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;

public abstract class AbstractRestoreAction extends AbstractBackupRestoreAction {
  public AbstractRestoreAction(String name, GlobRepository repository, Directory directory) {
    super(name, repository, directory);
  }

  protected void appyWithPasswordManagement(RestoreDetail action) {
    try {
      char[] password = null;
      while (true) {
        Gui.setWaitCursor(frame);
        BackupService.Status completed;
        try {
          completed = action.restore(password);
        }
        finally {
          Gui.setDefaultCursor(frame);
        }
        if (completed == BackupService.Status.BAD_VERSION) {
          MessageDialog.show("restore.error.title", frame, directory, "restore.bad.version");
          return;
        }
        if (completed == BackupService.Status.OK) {
          resetUndoRedo();
          selectCurrentMonth();
          action.showConfirmationDialog();
          return;
        }
        else {
          AskPasswordDialog dialog =
            new AskPasswordDialog("restore.password.title", "restore.password.label", "restore.password.message", directory);
          password = dialog.show();
          if (password == null || password.length == 0) {
            return;
          }
        }
      }
    }
    catch (Exception ex) {
      Log.write("Restore failed", ex);
      action.showError();
    }
  }

  private void resetUndoRedo() {
    directory.get(UndoRedoService.class).reset();
  }

  private void selectCurrentMonth() {
    Glob month = repository.get(Key.create(Month.TYPE, CurrentMonth.getLastTransactionMonth(repository)));
    directory.get(SelectionService.class).select(month);
  }

  interface RestoreDetail {

    BackupService.Status restore(char[] password) throws Exception;

    void showConfirmationDialog();

    void showError();
  }

}
