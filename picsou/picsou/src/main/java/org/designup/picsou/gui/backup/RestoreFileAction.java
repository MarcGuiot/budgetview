package org.designup.picsou.gui.backup;

import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.components.dialogs.MessageFileDialog;
import org.designup.picsou.gui.license.LicenseService;
import org.designup.picsou.model.User;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
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

    if (LicenseService.trialInProgress(repository)) {
      MessageDialog.show("restore.trial.title", frame, directory, "restore.trial.content");
      return;
    }

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

  class RestoreFile implements RestoreDetail {
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
