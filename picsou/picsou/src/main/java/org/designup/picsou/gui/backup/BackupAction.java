package org.designup.picsou.gui.backup;

import org.designup.picsou.gui.BackupGenerator;
import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.components.dialogs.MessageFileDialog;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.Log;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.SimpleDateFormat;

public class BackupAction extends AbstractBackupRestoreAction {
  private JFrame parent;
  private BackupGenerator generator;

  public BackupAction(GlobRepository repository, Directory directory, BackupGenerator generator) {
    super(Lang.get("backup"), repository, directory);
    this.generator = generator;
    this.parent = directory.get(JFrame.class);
  }

  public void actionPerformed(ActionEvent e) {
    JFileChooser chooser = getFileChooser();
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

    int returnVal = chooser.showSaveDialog(parent);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      File backupFile = null;
      try {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        backupFile = new File(file, format.format(TimeService.getToday()) + ".snapshot");
        int count = 2;
        while (backupFile.exists()) {
          backupFile = new File(file, format.format(TimeService.getToday()) + "-" + count + ".snapshot");
          count++;
        }
        generator.generateIn(backupFile.getAbsolutePath());
        MessageFileDialog dialog = new MessageFileDialog(repository, directory);
        dialog.show("backup.ok", backupFile.getAbsolutePath());
      }
      catch (Exception ex) {
        ex.printStackTrace();
        Log.write("During backup", ex);
        MessageFileDialog dialog = new MessageFileDialog(repository, directory);
        dialog.show("backup.error", backupFile.getAbsolutePath());
      }
    }
  }
}
