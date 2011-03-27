package org.designup.picsou.gui.backup;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.components.dialogs.ConfirmationDialog;
import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.designup.picsou.server.model.SerializableGlobType;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Dates;
import org.globsframework.utils.MapOfMaps;
import org.globsframework.utils.directory.Directory;

import java.awt.event.ActionEvent;
import java.util.Date;

public class RestoreSnapshotAction extends AbstractRestoreAction {
  private ServerAccess.SnapshotInfo snapshotInfo;
  private PicsouDialog dialog;
  private String date;

  public RestoreSnapshotAction(Directory directory, GlobRepository repository, ServerAccess.SnapshotInfo snapshotInfo,
                               PicsouDialog dialog) {
    super(Lang.get("restore.snapshot.button", getDate(snapshotInfo)), repository, directory);
    date = getDate(snapshotInfo);
    this.snapshotInfo = snapshotInfo;
    this.dialog = dialog;
  }

  private static String getDate(ServerAccess.SnapshotInfo snapshotInfo) {
    return PicsouDescriptionService.LOCAL_TIME_STAMP.format(new Date(snapshotInfo.timestamp));
  }

  public void actionPerformed(ActionEvent e) {
    ConfirmationDialog confirmationDialog =
      new ConfirmationDialog("restore.snapshot.confirmation.title",
                             "restore.snapshot.confirmation.message", dialog, directory, date) {
        protected void postValidate() {
          appyWithPasswordManagement(new RestoreSnapshot());
        }
      };
    confirmationDialog.show();
  }

  class RestoreSnapshot implements AbstractRestoreAction.RestoreDetail {

    public BackupService.Status restore(char[] password) throws Exception {
      MapOfMaps<String, Integer, SerializableGlobType> serializableGlobTypeMapOfMaps = backupService.restore(snapshotInfo);
      return backupService.restore(password, serializableGlobTypeMapOfMaps,
                                   snapshotInfo.password == null ? null : snapshotInfo.password.toCharArray());
    }

    public void showConfirmationDialog() {
      MessageDialog.show("restore.snapshot.ok.title", dialog, directory,
                         "restore.snapshot.ok.message",
                         Dates.DEFAULT_TIMESTAMP_FORMAT.format(new Date(snapshotInfo.timestamp)));
      dialog.setVisible(false);
    }

    public void showError() {
      MessageDialog.show("restore.snapshot.error.title", dialog, directory,
                         "restore.snapshot.error.message",
                         Dates.DEFAULT_TIMESTAMP_FORMAT.format(new Date(snapshotInfo.timestamp)));
    }
  }
}