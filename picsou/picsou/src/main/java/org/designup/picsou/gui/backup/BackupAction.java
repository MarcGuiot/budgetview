package org.designup.picsou.gui.backup;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.startup.BackupService;
import org.designup.picsou.gui.components.dialogs.MessageFileDialog;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.SimpleDateFormat;

public class BackupAction extends AbstractBackupRestoreAction {
  private JFrame parent;
  private BackupService backupService;

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  private static final String PREFIX = PicsouApplication.APPNAME.toLowerCase() + "-";
  private static final String EXTENSION = ".backup";

  public BackupAction(GlobRepository repository, Directory directory) {
    super(Lang.get("backup"), repository, directory);
    this.backupService = directory.get(BackupService.class);
    this.parent = directory.get(JFrame.class);
  }

  public void actionPerformed(ActionEvent e) {
    JFileChooser chooser = getFileChooser();
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setSelectedFile(getSafeBackupFile());

    int returnVal = chooser.showSaveDialog(parent);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      if (file.exists()) {
        int result = JOptionPane.showConfirmDialog(parent,
                                                   Lang.get("backup.confirm.message"),
                                                   Lang.get("backup.confirm.title"),
                                                   JOptionPane.YES_NO_OPTION);
        if (result != JOptionPane.YES_OPTION) {
          return;
        }
      }

      try {
        backupService.generate(file);
        MessageFileDialog dialog = new MessageFileDialog(repository, directory);
        dialog.show("backup.ok.title", "backup.ok.message", file);
      }
      catch (Exception ex) {
        Log.write("During backup", ex);
        MessageFileDialog dialog = new MessageFileDialog(repository, directory);
        dialog.show("backup.error.title", "backup.error.message", file);
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
