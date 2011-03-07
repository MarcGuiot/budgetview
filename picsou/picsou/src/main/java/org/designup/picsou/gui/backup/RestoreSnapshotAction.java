package org.designup.picsou.gui.backup;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
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

  public RestoreSnapshotAction(Directory directory, GlobRepository repository, ServerAccess.SnapshotInfo snapshotInfo,
                               PicsouDialog dialog) {
    super(Lang.get("restore.snapshot.button", Dates.DEFAULT_TIMESTAMP_FORMAT.format(new Date(snapshotInfo.timestamp))),
          repository, directory);
    this.snapshotInfo = snapshotInfo;
    this.dialog = dialog;
  }

  public void actionPerformed(ActionEvent e) {
    appyWithPasswordManagement(new RestoreSnapshot());
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
