package org.designup.picsou.gui.actions;

import org.designup.picsou.gui.license.LicenseActivationDialog;
import org.designup.picsou.gui.license.LicenseService;
import org.designup.picsou.gui.startup.ImportPanel;
import org.designup.picsou.gui.startup.OpenRequestManager;
import org.designup.picsou.utils.Lang;
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

  static public ImportFileAction initAndRegisterToOpenRequestManager(String text, final GlobRepository repository, final Directory directory) {
    new ImportFileAction(text, repository, directory, false);
    return new ImportFileAction(text, repository, directory, null, true);
  }

  static public ImportFileAction init(String text, final GlobRepository repository, final Directory directory, Glob defaulAccount) {
    return new ImportFileAction(text, repository, directory, defaulAccount, true);
  }

  private ImportFileAction(String text, final GlobRepository repository, final Directory directory, boolean usePreference) {
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
    OpenRunnable runnable = new OpenRunnable(Collections.<File>emptyList(), directory, repository, defaulAccount, usePreference);
    runnable.run();
    defaulAccount = null;
  }

  private static class OpenRunnable implements Runnable {
    private ImportPanel panel = null;
    private Directory directory;
    private GlobRepository repository;

    public OpenRunnable(List<File> files,
                        Directory directory, GlobRepository repository,
                        Glob defaultAccount, boolean usePreferedPath) {
      this.directory = directory;
      this.repository = repository;
      if (!LicenseService.trialExpired(repository)) {
        panel = new ImportPanel(Lang.get("import.step1.close"), files, defaultAccount,
                                directory.get(JFrame.class),
                                repository, directory,
                                usePreferedPath);
      }
    }

    public void run() {
      if (panel != null) {
        panel.show();
      }
      else {
        LicenseActivationDialog dialog = new LicenseActivationDialog(directory.get(JFrame.class),
                                                                     repository, directory);
        dialog.showExpiration();
      }
    }
  }
}
