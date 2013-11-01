package org.designup.picsou.gui.actions;

import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.components.dialogs.MessageType;
import org.designup.picsou.gui.importer.ImportDialog;
import org.designup.picsou.gui.license.LicenseActivationDialog;
import org.designup.picsou.gui.startup.components.OpenRequestManager;
import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.model.User;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.globsframework.model.FieldValue.value;

public class ImportFileAction extends AbstractAction {

  private Directory directory;
  private GlobList importedAccounts;
  private Glob defaulAccount;
  private boolean usePreference;
  private boolean isSynchro;
  private GlobRepository repository;

  static public ImportFileAction initForMenu(String text, final GlobRepository repository, final Directory directory) {
    return new ImportFileAction(text, repository, directory, (Glob)null, true, false);
  }

  static public void registerToOpenRequestManager(String text, final GlobRepository repository, final Directory directory) {
    new ImportFileAction(text, repository, directory, false, false);
  }

  static public ImportFileAction init(String text, final GlobRepository repository, final Directory directory, Glob defaulAccount) {
    return new ImportFileAction(text, repository, directory, defaulAccount, true, false);
  }

  static public ImportFileAction sync(final GlobRepository repository, final Directory directory, GlobList importedAccounts) {
    return new ImportFileAction(Lang.get("import"), repository, directory, importedAccounts, false, true);
  }

  private ImportFileAction(String text, final GlobRepository repository, final Directory directory,
                           boolean usePreference, final boolean isSynchro) {
    super(text);
    this.repository = repository;
    this.directory = directory;
    this.usePreference = usePreference;
    this.isSynchro = isSynchro;
    OpenRequestManager openRequestManager = directory.get(OpenRequestManager.class);
    openRequestManager.pushCallback(new OpenRequestManager.Callback() {
      public boolean accept() {
        return true;
      }

      public void openFiles(List<File> files) {
        SwingUtilities.invokeLater(new OpenRunnable(files, directory, repository, defaulAccount,
                                                    ImportFileAction.this.usePreference, null, isSynchro));
        defaulAccount = null;
      }
    });
  }

  private ImportFileAction(String text, final GlobRepository repository, final Directory directory, GlobList importedAccounts,
                           boolean usePreference, boolean isSynchro) {
    super(text);
    this.repository = repository;
    this.directory = directory;
    this.importedAccounts = importedAccounts;
    this.usePreference = usePreference;
    this.isSynchro = isSynchro;
  }

  private ImportFileAction(String text, final GlobRepository repository, final Directory directory, Glob defaulAccount,
                           boolean usePreference, boolean isSynchro) {
    super(text);
    this.repository = repository;
    this.directory = directory;
    this.defaulAccount = defaulAccount;
    this.usePreference = usePreference;
    this.isSynchro = isSynchro;
  }

  public void actionPerformed(ActionEvent event) {
    OpenRunnable runnable = new OpenRunnable(Collections.<File>emptyList(), directory, repository,
                                             defaulAccount, usePreference, importedAccounts, isSynchro);
    runnable.run();
    defaulAccount = null;
  }

  private static class OpenRunnable implements Runnable {
    private ImportDialog importDialog = null;
    private Directory directory;
    private GlobRepository repository;

    public OpenRunnable(List<File> files,
                        Directory directory, GlobRepository repository,
                        Glob defaultAccount, boolean usePreferedPath, GlobList importedAccounts, boolean isSynchro) {
      this.directory = directory;
      this.repository = repository;
      importDialog = new ImportDialog(Lang.get("import.fileSelection.close"), files, defaultAccount,
                                      directory.get(JFrame.class),
                                      repository, directory,
                                      usePreferedPath, isSynchro);
      if (!files.isEmpty()) {
        importDialog.acceptFiles();
      }
      if (importedAccounts != null && !importedAccounts.isEmpty()) {
        importDialog.showSynchro(importedAccounts);
      }
    }

    public void run() {
      if (TimeService.reset()) {
        repository.update(CurrentMonth.KEY,
                          value(CurrentMonth.CURRENT_MONTH, TimeService.getCurrentMonth()),
                          value(CurrentMonth.CURRENT_DAY, TimeService.getCurrentDay()));

      }
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
