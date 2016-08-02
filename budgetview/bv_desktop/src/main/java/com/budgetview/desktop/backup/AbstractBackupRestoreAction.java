package com.budgetview.desktop.backup;

import com.budgetview.model.UserPreferences;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public abstract class AbstractBackupRestoreAction extends AbstractAction {
  protected GlobRepository repository;
  protected Directory directory;
  protected JFrame frame;
  protected BackupService backupService;
  protected SelectionService selectionService;

  public AbstractBackupRestoreAction(String name, GlobRepository repository, Directory directory) {
    super(name);
    this.repository = repository;
    this.directory = directory;
    this.backupService = directory.get(BackupService.class);
    this.frame = directory.get(JFrame.class);
    this.selectionService = directory.get(SelectionService.class);
  }

  protected JFileChooser getFileChooser() {
    String directory = System.getProperty("user.home");

    Glob userPreferences = repository.get(UserPreferences.KEY);
    if (userPreferences.get(UserPreferences.LAST_BACKUP_RESTORE_DIRECTORY) != null) {
      directory = userPreferences.get(UserPreferences.LAST_BACKUP_RESTORE_DIRECTORY);
    }
    return new JFileChooser(directory);
  }
}
