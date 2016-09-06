package com.budgetview.desktop.backup;

import com.budgetview.client.DataAccess;
import com.budgetview.desktop.components.dialogs.ConfirmationDialog;
import com.budgetview.desktop.components.dialogs.MessageDialog;
import com.budgetview.desktop.components.dialogs.MessageType;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.description.PicsouDescriptionService;
import com.budgetview.session.serialization.SerializedGlob;
import com.budgetview.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Dates;
import org.globsframework.utils.collections.MapOfMaps;
import org.globsframework.utils.directory.Directory;

import java.awt.event.ActionEvent;
import java.util.Date;

public class RestoreSnapshotAction extends AbstractRestoreAction {
  private DataAccess.SnapshotInfo snapshotInfo;
  private PicsouDialog dialog;
  private String date;

  public RestoreSnapshotAction(Directory directory, GlobRepository repository, DataAccess.SnapshotInfo snapshotInfo,
                               PicsouDialog dialog) {
    super(Lang.get("restore.snapshot.button", getDate(snapshotInfo)), repository, directory);
    date = getDate(snapshotInfo);
    this.snapshotInfo = snapshotInfo;
    this.dialog = dialog;
  }

  private static String getDate(DataAccess.SnapshotInfo snapshotInfo) {
    return PicsouDescriptionService.LOCAL_TIME_STAMP.format(new Date(snapshotInfo.timestamp));
  }

  public void actionPerformed(ActionEvent e) {
    ConfirmationDialog confirmationDialog =
      new ConfirmationDialog("restore.snapshot.confirmation.title",
                             Lang.get("restore.snapshot.confirmation.message", date),
                             dialog, directory) {
        protected void processOk() {
          appyWithPasswordManagement(new RestoreSnapshot());
        }
      };
    confirmationDialog.show();
  }

  class RestoreSnapshot implements AbstractRestoreAction.RestoreDetail {

    public BackupService.Status restore(char[] password) throws Exception {
      MapOfMaps<String, Integer, SerializedGlob> serializableGlobTypeMapOfMaps = backupService.restore(snapshotInfo);
      return backupService.restore(password, serializableGlobTypeMapOfMaps,
                                   snapshotInfo.password == null ? null : snapshotInfo.password.toCharArray());
    }

    public void showConfirmationDialog() {
      MessageDialog.show("restore.snapshot.ok.title", MessageType.SUCCESS, dialog, directory,
                         "restore.snapshot.ok.message",
                         Dates.DEFAULT_TIMESTAMP_FORMAT.format(new Date(snapshotInfo.timestamp)));
      dialog.setVisible(false);
    }

    public void showError() {
      MessageDialog.show("restore.snapshot.error.title", MessageType.ERROR, dialog, directory,
                         "restore.snapshot.error.message",
                         Dates.DEFAULT_TIMESTAMP_FORMAT.format(new Date(snapshotInfo.timestamp)));
    }
  }
}
