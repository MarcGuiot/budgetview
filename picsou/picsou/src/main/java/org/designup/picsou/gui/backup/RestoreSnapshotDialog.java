package org.designup.picsou.gui.backup;

import org.globsframework.utils.directory.Directory;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.model.GlobRepository;
import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;

import javax.swing.*;
import java.util.List;
import java.awt.event.ActionEvent;

public class RestoreSnapshotDialog {
  private BackupService backupService;
  private Directory directory;

  public RestoreSnapshotDialog(Directory directory) {
    this.directory = directory;
    backupService = directory.get(BackupService.class);
  }

  public void show(final GlobRepository repository){
    final PicsouDialog dialog = PicsouDialog.create(directory.get(JFrame.class), directory);
    List<ServerAccess.SnapshotInfo> snapshotInfoList = backupService.getSnapshotInfos();
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/backuprestore/restoreSnapshotDialog.splits", repository, directory);
    builder.addRepeat("versionRepeat", snapshotInfoList, new RepeatComponentFactory<ServerAccess.SnapshotInfo>() {
      public void registerComponents(RepeatCellBuilder cellBuilder, ServerAccess.SnapshotInfo item) {
        cellBuilder.add("dateRef", new RestoreSnapshotAction(directory, repository, item, dialog));
      }
    });
    dialog.setPanelAndButton(builder.<JPanel>load(), new AbstractAction("close") {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
      }
    });
    dialog.pack();
    dialog.showCentered();
  }
}
