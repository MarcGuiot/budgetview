package com.budgetview.desktop.backup;

import com.budgetview.desktop.components.dialogs.MessageFileDialog;
import com.budgetview.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;

public class RestoreFileAction extends AbstractRestoreAction {

  public RestoreFileAction(GlobRepository repository, Directory directory) {
    super(Lang.get("restore"), repository, directory);
  }

  public void actionPerformed(ActionEvent e) {
    JFileChooser chooser = getFileChooser();
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    int returnVal = chooser.showOpenDialog(frame);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      if (file.exists()) {
        appyWithPasswordManagement(new RestoreFile(file));
      }
    }
  }

  private class RestoreFile implements RestoreDetail {
    private File file;

    RestoreFile(File file) {
      this.file = file;
    }

    public BackupService.Status restore(char[] password) throws Exception {
      return backupService.restore(new FileInputStream(file), password);
    }

    public void showConfirmationDialog() {
      MessageFileDialog dialog = new MessageFileDialog(repository, directory);
      dialog.show("restore.ok.title", "restore.ok.message", file);
    }

    public void showError() {
      MessageFileDialog dialog = new MessageFileDialog(repository, directory);
      dialog.show("restore.error.title", "restore.error.message", file);
    }
  }

}
