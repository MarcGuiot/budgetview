package org.designup.picsou.gui.actions;

import org.designup.picsou.gui.components.PicsouDialog;
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
  private GlobRepository repository;

  static public ImportFileAction initAndRegisterInOpenRequestManager(
    String text, final GlobRepository repository, final Directory directory) {
    return new ImportFileAction(text, repository, directory);
  }

  static public ImportFileAction init(String text, final GlobRepository repository, final Directory directory, Glob defaulAccount) {
    return new ImportFileAction(text, repository, directory, defaulAccount);
  }

  private ImportFileAction(String text, final GlobRepository repository, final Directory directory) {
    super(text);
    this.repository = repository;
    this.directory = directory;
    OpenRequestManager openRequestManager = directory.get(OpenRequestManager.class);
    openRequestManager.pushCallback(new OpenRequestManager.Callback() {
      public void openFiles(List<File> files) {
        SwingUtilities.invokeLater(new OpenRunnable(files, directory, repository, defaulAccount));
        defaulAccount = null;
      }
    });
  }

  private ImportFileAction(String text, final GlobRepository repository, final Directory directory, Glob defaulAccount) {
    super(text);
    this.repository = repository;
    this.directory = directory;
    this.defaulAccount = defaulAccount;
  }

  public void actionPerformed(ActionEvent event) {
    OpenRunnable runnable = new OpenRunnable(Collections.<File>emptyList(), directory, repository, defaulAccount);
    runnable.run();
    defaulAccount = null;
  }

  private static class OpenRunnable implements Runnable {
    private PicsouDialog dialog;
    private ImportPanel panel;

    public OpenRunnable(List<File> files, Directory directory, GlobRepository repository, Glob defaultAccount) {
      JFrame frame = directory.get(JFrame.class);
      panel = new ImportPanel(Lang.get("import.step1.close"), files, defaultAccount, 
                              frame, repository, directory);
    }

    public void run() {
      panel.show();
    }
  }
}
