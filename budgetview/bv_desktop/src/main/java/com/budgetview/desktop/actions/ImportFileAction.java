package com.budgetview.desktop.actions;

import com.budgetview.desktop.components.dialogs.MessageDialog;
import com.budgetview.desktop.components.dialogs.MessageType;
import com.budgetview.desktop.importer.ImportDialog;
import com.budgetview.desktop.license.activation.LicenseActivationDialog;
import com.budgetview.desktop.startup.components.OpenRequestManager;
import com.budgetview.model.SignpostStatus;
import com.budgetview.model.User;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collections;
import java.util.List;

public class ImportFileAction extends AbstractAction {

  private Directory directory;
  private Glob defaulAccount;
  private boolean usePreference;
  private GlobRepository repository;

  public static ImportFileAction initForMenu(String text, final GlobRepository repository, final Directory directory) {
    return new ImportFileAction(text, repository, directory, (Glob) null, true);
  }

  public static void registerToOpenRequestManager(String text, final GlobRepository repository, final Directory directory) {
    new ImportFileAction(text, repository, directory, false);
  }

  public static ImportFileAction init(String text, final GlobRepository repository, final Directory directory, Glob defaulAccount) {
    return new ImportFileAction(text, repository, directory, defaulAccount, true);
  }

  private ImportFileAction(String text, final GlobRepository repository, final Directory directory,
                           boolean usePreference) {
    super(text);
    this.repository = repository;
    this.directory = directory;
    this.usePreference = usePreference;
    OpenRequestManager openRequestManager = directory.get(OpenRequestManager.class);
    openRequestManager.pushCallback(new OpenRequestManager.Callback() {
      public boolean accept() {
        return true;
      }

      public void openFiles(List<File> files) {
        SwingUtilities.invokeLater(new OpenRunnable(files, directory, repository, defaulAccount,
                                                    ImportFileAction.this.usePreference));
        defaulAccount = null;
      }
    });
  }

  private ImportFileAction(String text, final GlobRepository repository, final Directory directory, Glob defaulAccount,
                           boolean usePreference) {
    super(text);
    this.repository = repository;
    this.directory = directory;
    this.defaulAccount = defaulAccount;
    this.usePreference = usePreference;
  }

  public void actionPerformed(ActionEvent event) {
    OpenRunnable runnable = new OpenRunnable(Collections.<File>emptyList(), directory, repository,
                                             defaulAccount, usePreference);
    runnable.run();
    defaulAccount = null;
  }

  private static class OpenRunnable implements Runnable {
    private ImportDialog importDialog = null;
    private Directory directory;
    private GlobRepository repository;

    public OpenRunnable(List<File> files,
                        Directory directory, GlobRepository repository,
                        Glob defaultAccount, boolean usePreferedPath) {
      this.directory = directory;
      this.repository = repository;
      if (!User.isDemoUser(repository.get(User.KEY))) {
        importDialog = new ImportDialog(files, defaultAccount,
                                        directory.get(JFrame.class),
                                        repository, directory,
                                        usePreferedPath);
        if (!files.isEmpty()) {
          importDialog.acceptFiles();
        }
      }
    }

    public void run() {
      if (importDialog != null) {
        SignpostStatus.setCompleted(SignpostStatus.IMPORT_STARTED, repository);
        importDialog.show();
      }
      else {
        if (User.isDemoUser(repository.get(User.KEY))) {
          MessageDialog.show("demo.import.title", MessageType.INFO, directory.get(JFrame.class), directory, "demo.import.content");
        }
        else {
          LicenseActivationDialog dialog = new LicenseActivationDialog(directory.get(JFrame.class),
                                                                       repository, directory);
          dialog.showExpiration();
        }
      }
    }
  }
}
