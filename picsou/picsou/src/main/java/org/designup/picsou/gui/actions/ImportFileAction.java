package org.designup.picsou.gui.actions;

import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.license.LicenseActivationDialog;
import org.designup.picsou.gui.license.LicenseService;
import org.designup.picsou.gui.importer.ImportDialog;
import org.designup.picsou.gui.startup.OpenRequestManager;
import org.designup.picsou.model.User;
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

  static public ImportFileAction initForMenu(String text, final GlobRepository repository, final Directory directory) {
    return new ImportFileAction(text, repository, directory, null, true);
  }

  static public void registerToOpenRequestManager(String text, final GlobRepository repository, final Directory directory){
    new ImportFileAction(text, repository, directory, false);
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
        System.out.println("ImportFileAction.openFiles runable on " + files.get(0).getName());
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
    System.out.println("ImportFileAction.actionPerformed begin");
    runnable.run();
    System.out.println("ImportFileAction.actionPerformed end");
    defaulAccount = null;
  }

  private static class OpenRunnable implements Runnable {
    private ImportDialog dialog = null;
    private Directory directory;
    private GlobRepository repository;

    public OpenRunnable(List<File> files,
                        Directory directory, GlobRepository repository,
                        Glob defaultAccount, boolean usePreferedPath) {
      this.directory = directory;
      this.repository = repository;
      if (!LicenseService.trialExpired(repository) && !User.isDemoUser(repository.get(User.KEY))) {
        System.out.println("ImportFileAction$OpenRunnable.OpenRunnable " + files.get(0).getName());
        dialog = new ImportDialog(Lang.get("import.step1.close"), files, defaultAccount,
                                directory.get(JFrame.class),
                                repository, directory,
                                usePreferedPath);
      }
    }

    public void run() {
      if (dialog != null) {
        dialog.show();
      }
      else {
        if (User.isDemoUser(repository.get(User.KEY))) {
          MessageDialog dialog = new MessageDialog("demo.import.title", "demo.import.content", directory.get(JFrame.class),
                                                   directory);
          dialog.show();
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
