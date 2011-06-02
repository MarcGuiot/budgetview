package org.designup.picsou.gui.backup;

import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.components.dialogs.ConfirmationDialog;
import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.components.dialogs.MessageFileDialog;
import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.User;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.SimpleDateFormat;

public class BackupAction extends AbstractBackupRestoreAction {

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  private static final String PREFIX = "backup-";
  private static final String EXTENSION = "." + PicsouApplication.APPNAME.toLowerCase();

  public BackupAction(GlobRepository repository, Directory directory) {
    super(Lang.get("backup"), repository, directory);
  }

  public void actionPerformed(ActionEvent event) {

    Glob glob = repository.get(User.KEY);
    if (!glob.isTrue(User.IS_REGISTERED_USER)) {
      MessageDialog.show("backup.trial.title", frame, directory, "backup.trial.content");
    }

    JFileChooser chooser = getFileChooser();
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setSelectedFile(getSafeBackupFile());

    int returnVal = chooser.showSaveDialog(frame);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      final File file = chooser.getSelectedFile();
      if (file.exists() && !ConfirmationDialog.confirmed("backup.confirm.title",
                                                         Lang.get("backup.confirm.message"),
                                                         frame, directory)) {
        return;
      }

      try {
        Gui.setWaitCursor(frame);
        backupService.generate(file);
        Gui.setDefaultCursor(frame);
        MessageFileDialog dialog = new MessageFileDialog(repository, directory);
        dialog.show("backup.ok.title", "backup.ok.message", file);
      }
      catch (Exception e) {
        Gui.setDefaultCursor(frame);
        Log.write("During backup", e);
        MessageFileDialog dialog = new MessageFileDialog(repository, directory);
        dialog.show("backup.error.title", "backup.error.message", file);
      }
      finally {
        Gui.setDefaultCursor(frame);
      }
    }

  }

  private File getSafeBackupFile() {
    File defaultDir = new File(System.getProperty("user.home"));
    File backupDir = new File(defaultDir, getSnapshotFileName());
    int count = 2;
    while (backupDir.exists()) {
      backupDir = new File(defaultDir, getSnapshotFileName(count));
      count++;
    }
    return backupDir;
  }

  private String getSnapshotFileName() {
    return PREFIX + DATE_FORMAT.format(TimeService.getToday()) + EXTENSION;
  }

  private String getSnapshotFileName(int count) {
    return PREFIX + DATE_FORMAT.format(TimeService.getToday()) + "-" + count + EXTENSION;
  }
}
