package org.designup.picsou.gui.actions;

import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.utils.Log;
import org.crossbowlabs.globs.utils.logging.Debug;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.designup.picsou.importer.PicsouImportService;
import org.designup.picsou.importer.TypedInputStream;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.model.Transaction;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class ImportFileAction extends AbstractAction {
  private GlobRepository repository;
  private Directory directory;
  private Frame frame;
  private PicsouImportService importService;

  public ImportFileAction(GlobRepository repository, Directory directory) {
    super(Lang.get("import"));
    this.frame = directory.get(JFrame.class);
    this.repository = repository;
    this.directory = directory;
    this.importService = directory.get(PicsouImportService.class);
  }

  public void actionPerformed(ActionEvent event) {
    File[] file = queryFile(frame);
    if (file != null) {
      for (int i = 0; i < file.length; i++) {
        processFile(file[i], i == file.length - 1);
      }
    }
  }

  public static File[] queryFile(Component parent) {
    JFileChooser chooser = new JFileChooser();
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setMultiSelectionEnabled(true);
    chooser.addChoosableFileFilter(new FileFilter() {
      public boolean accept(File file) {
        return file.getName().endsWith("ofx") || file.getName().endsWith("qif") || file.isDirectory();
      }

      public String getDescription() {
        return Lang.get("bank.file.format");
      }
    });
    int returnVal = chooser.showOpenDialog(parent);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File[] selectedFiles = chooser.getSelectedFiles();
      if (selectedFiles == null || selectedFiles.length == 0){
        System.out.println("no file selected ");
      } else
      for (int i = 0; i < selectedFiles.length; i++) {
        File selectedFile = selectedFiles[i];
        if (!selectedFile.exists()){
          System.out.println("erreur : file " + selectedFile.getName() + " not found");
        }
      }
      return selectedFiles;
    }
    return null;
  }

  public void processFile(File file, boolean isLastFile) {
    String path = file.getPath();
    if (!path.endsWith(".ofx") && !path.endsWith(".qif")) {
      JOptionPane.showMessageDialog(frame, Lang.get("import.invalid.extension", path));
      return;
    }

    try {
      repository.enterBulkDispatchingMode();
      GlobList all1 = repository.getAll(Transaction.TYPE);
      final Key importKey = importService.run(new TypedInputStream(file), repository);
      GlobList all2 = repository.getAll(Transaction.TYPE);
      if (file.getName().toLowerCase().endsWith("qif") && isLastFile) {
        showQifDialog(importKey);
      }
    }
    catch (Exception e) {
      String message = Lang.get("import.file.error", path);
      Log.write(message, e);
      JOptionPane.showMessageDialog(frame, message);
    }
    finally {
      repository.completeBulkDispatchingMode();
    }
  }

  private void showQifDialog(Key importKey) {
    QifBalancePanel detailPanel =
      new QifBalancePanel(repository, directory, importKey);
    directory.get(SelectionService.class)
      .select(repository.get(importKey));
    detailPanel.showDialog(frame);
  }

}
