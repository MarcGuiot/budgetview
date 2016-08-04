package com.budgetview.desktop.backup;

import com.budgetview.client.DataAccess;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.Disposable;
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
    final PicsouDialog dialog = PicsouDialog.create(this, directory.get(JFrame.class), directory);

    final GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/backuprestore/restoreSnapshotDialog.splits", repository, directory);

    builder.addRepeat("versionRepeat", getSnapshots(), new RepeatComponentFactory<DataAccess.SnapshotInfo>() {
      public void registerComponents(PanelBuilder cellBuilder, DataAccess.SnapshotInfo item) {
        cellBuilder.add("dateRef", new RestoreSnapshotAction(directory, repository, item, dialog));
      }
    });

    dialog.setPanelAndButton(builder.<JPanel>load(), new AbstractAction(Lang.get("close")) {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
      }
    });

    dialog.registerDisposable(new Disposable() {
      public void dispose() {
        builder.dispose();
      }
    });
    dialog.pack();
    dialog.showCentered();
  }

  private List<DataAccess.SnapshotInfo> getSnapshots() {
    List<DataAccess.SnapshotInfo> snapshotInfoList = backupService.getSnapshotInfos();
    Collections.sort(snapshotInfoList);
    return snapshotInfoList;
  }
}
