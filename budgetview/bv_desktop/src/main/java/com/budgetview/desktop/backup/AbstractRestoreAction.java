package com.budgetview.desktop.backup;

import com.budgetview.desktop.components.dialogs.MessageDialog;
import com.budgetview.desktop.components.dialogs.MessageType;
import com.budgetview.desktop.undo.UndoRedoService;
import com.budgetview.desktop.utils.Gui;
import com.budgetview.model.CurrentMonth;
import com.budgetview.model.Month;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;

public abstract class AbstractRestoreAction extends AbstractBackupRestoreAction {
  public AbstractRestoreAction(String name, GlobRepository repository, Directory directory) {
    super(name, repository, directory);
  }

  protected void appyWithPasswordManagement(RestoreDetail action) {
    Key[] currentSelection = selectionService.getSelection(Month.TYPE).getKeys();
    try {
      char[] password = null;
      while (true) {
        Gui.setWaitCursor(frame);
        selectionService.clearAll();
        BackupService.Status completed;
        try {
          completed = action.restore(password);
        }
        finally {
          Gui.setDefaultCursor(frame);
        }
        if (completed == BackupService.Status.BAD_VERSION) {
          MessageDialog.show("restore.error.title", MessageType.ERROR, frame, directory, "restore.bad.version");
          return;
        }
        if (completed == BackupService.Status.OK) {
          resetUndoRedo();
          selectCurrentMonth();
          action.showConfirmationDialog();
          return;
        }

        AskPasswordDialog dialog =
          new AskPasswordDialog("restore.password.title", "restore.password.label", "restore.password.message", directory);
        password = dialog.show();
        if (password == null || password.length == 0) {
          restoreMonthSelection(currentSelection);
          return;
        }
      }
    }
    catch (Exception ex) {
      Log.write("[Backup/Restore] Restore failed", ex);
      action.showError();
      restoreMonthSelection(currentSelection);
    }
  }

  private void restoreMonthSelection(Key[] currentSelection) {
    GlobList months = new GlobList();
    for (Key key : currentSelection) {
      Glob month = repository.find(key);
      if (month != null) {
        months.add(month);
      }
    }
    selectionService.select(months, Month.TYPE);
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
