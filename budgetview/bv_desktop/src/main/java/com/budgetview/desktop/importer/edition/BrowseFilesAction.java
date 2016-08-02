package com.budgetview.desktop.importer.edition;

import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.io.importer.BankFileType;
import com.budgetview.model.UserPreferences;
import com.budgetview.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.repository.LocalGlobRepository;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class BrowseFilesAction extends AbstractAction {
  private Action importAction;
  private boolean usePreferredPath;
  private LocalGlobRepository localRepository;
  private PicsouDialog dialog;
  private JTextField fileField;

  public BrowseFilesAction(Action importAction,
                           JTextField fileField,
                           LocalGlobRepository localRepository,
                           boolean usePreferredPath,
                           PicsouDialog dialog) {
    super(Lang.get("browse"));
    this.importAction = importAction;
    this.usePreferredPath = usePreferredPath;
    this.localRepository = localRepository;
    this.dialog = dialog;
    this.fileField = fileField;
  }

  public void actionPerformed(ActionEvent e) {
    File path = null;
    if (usePreferredPath) {
      Glob preferences = localRepository.get(UserPreferences.KEY);
      String directory = preferences.get(UserPreferences.LAST_IMPORT_DIRECTORY);
      if (directory != null) {
        path = new File(directory);
      }
    }
    File[] files = queryFiles(dialog, path);
    if (files == null || files.length == 0) {
      return;
    }

    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < files.length; i++) {
      buffer.append(files[i].getPath());
      if (i < files.length - 1) {
        buffer.append(";");
      }
      if (usePreferredPath) {
        localRepository.update(UserPreferences.KEY, UserPreferences.LAST_IMPORT_DIRECTORY,
                               files[i].getAbsoluteFile().getParent());
      }
    }
    fileField.setText(buffer.toString());
    if (files.length == 1) {
      importAction.actionPerformed(null);
    }
  }

  private File[] queryFiles(Component parent, File path) {
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
    if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
      return null;
    }
    return chooser.getSelectedFiles();
  }
}
