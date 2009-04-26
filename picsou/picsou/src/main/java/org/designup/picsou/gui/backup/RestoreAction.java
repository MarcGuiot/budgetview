package org.designup.picsou.gui.backup;

import org.designup.picsou.gui.BackupGenerator;
import org.designup.picsou.gui.components.dialogs.MessageFileDialog;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;

public class RestoreAction extends AbstractBackupRestoreAction {
  private JFrame parent;
  private BackupGenerator generator;

  public RestoreAction(GlobRepository repository, Directory directory, BackupGenerator generator) {
    super(Lang.get("restore"), repository, directory);
    this.generator = generator;
    this.parent = directory.get(JFrame.class);
  }

  public void actionPerformed(ActionEvent e) {
    JFileChooser chooser = getFileChooser();
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    int returnVal = chooser.showOpenDialog(parent);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      try {
        generator.restore(new FileInputStream(file));
        MessageFileDialog dialog = new MessageFileDialog(repository, directory);
        dialog.show("restore.ok", null);
      }
      catch (Exception ex) {
        ex.printStackTrace();
        MessageFileDialog dialog = new MessageFileDialog(repository, directory);
        dialog.show("restore.error", null);
      }
    }
  }
}