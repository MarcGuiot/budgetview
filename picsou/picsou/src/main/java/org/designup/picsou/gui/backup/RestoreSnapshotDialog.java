package org.designup.picsou.gui.backup;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

public class RestoreSnapshotDialog {
  private BackupService backupService;
  private Directory directory;

  public RestoreSnapshotDialog(Directory directory) {
    this.directory = directory;
    backupService = directory.get(BackupService.class);
  }

  public void show(final GlobRepository repository) {
    final PicsouDialog dialog = PicsouDialog.create(directory.get(JFrame.class), directory);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/backuprestore/restoreSnapshotDialog.splits", repository, directory);

    builder.addRepeat("versionRepeat", getSnapshots(), new RepeatComponentFactory<ServerAccess.SnapshotInfo>() {
      public void registerComponents(RepeatCellBuilder cellBuilder, ServerAccess.SnapshotInfo item) {
        cellBuilder.add("dateRef", new RestoreSnapshotAction(directory, repository, item, dialog));
      }
    });

    dialog.setPanelAndButton(builder.<JPanel>load(), new AbstractAction(Lang.get("close")) {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
      }
    });

    dialog.pack();
    dialog.showCentered();
  }

  private List<ServerAccess.SnapshotInfo> getSnapshots() {
    List<ServerAccess.SnapshotInfo> snapshotInfoList = backupService.getSnapshotInfos();
    Collections.sort(snapshotInfoList);
    return snapshotInfoList;
  }
}
