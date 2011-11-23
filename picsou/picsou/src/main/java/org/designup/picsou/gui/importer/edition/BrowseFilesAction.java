package org.designup.picsou.gui.importer.edition;

import org.designup.picsou.utils.Lang;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.importer.BankFileType;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.globsframework.model.Glob;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.utils.Log;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.io.File;

public class BrowseFilesAction extends AbstractAction {
  private boolean usePreferedPath;
  private LocalGlobRepository localRepository;
  private PicsouDialog dialog;
  private JTextField fileField;

  public BrowseFilesAction(JTextField fileField,
                           LocalGlobRepository localRepository,
                           boolean usePreferredPath,
                           PicsouDialog dialog) {
    super(Lang.get("browse"));
    this.usePreferedPath = usePreferredPath;
    this.localRepository = localRepository;
    this.dialog = dialog;
    this.fileField = fileField;
  }

  public void actionPerformed(ActionEvent e) {
    File path = null;
    if (usePreferedPath) {
      Glob preferences = localRepository.get(UserPreferences.KEY);
      String directory = preferences.get(UserPreferences.LAST_IMPORT_DIRECTORY);
      if (directory != null) {
        path = new File(directory);
      }
    }
    File[] files = queryFile(dialog, path);
    if (files != null) {
      StringBuffer buffer = new StringBuffer();
      for (int i = 0; i < files.length; i++) {
        buffer.append(files[i].getPath());
        if (i + 1 < files.length) {
          buffer.append(";");
        }
        if (usePreferedPath) {
          localRepository.update(UserPreferences.KEY, UserPreferences.LAST_IMPORT_DIRECTORY,
                                 files[i].getAbsoluteFile().getParent());
        }
      }
      fileField.setText(buffer.toString());
    }
  }

  private File[] queryFile(Component parent, File path) {
    JFileChooser chooser = new JFileChooser();
    if (path != null && path.exists()) {
      chooser.setCurrentDirectory(path);
    }
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setMultiSelectionEnabled(true);
    chooser.addChoosableFileFilter(new FileFilter() {
      public boolean accept(File file) {
        return BankFileType.isFileNameSupported(file.getName()) || file.isDirectory();
      }

      public String getDescription() {
        return Lang.get("bank.file.format");
      }
    });
    int returnVal = chooser.showOpenDialog(parent);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File[] selectedFiles = chooser.getSelectedFiles();
      if (selectedFiles == null || selectedFiles.length == 0) {
        Log.write("import : no file selected ");
      }
      else {
        for (File selectedFile : selectedFiles) {
          if (!selectedFile.exists()) {
            Log.write("import : Error: file " + selectedFile.getName() + " not found");
          }
        }
      }
      return selectedFiles;
    }
    return null;
  }
}
