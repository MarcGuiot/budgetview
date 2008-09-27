package org.designup.picsou.gui.actions;

import org.designup.picsou.importer.ofx.OfxExporter;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ExportFileAction extends AbstractAction {
  private GlobRepository repository;
  private Component parent;

  public ExportFileAction(GlobRepository repository, Directory directory) {
    super(Lang.get("export"));
    this.parent = directory.get(JFrame.class);
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent event) {
    JFileChooser chooser = new JFileChooser();
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.addChoosableFileFilter(new FileFilter() {
      public boolean accept(File file) {
        return file.getName().endsWith("ofx") || file.isDirectory();
      }

      public String getDescription() {
        return Lang.get("bank.file.formatExport");
      }
    });
    int returnVal = chooser.showSaveDialog(parent);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      if (!file.getName().endsWith("ofx")) {
        file = new File(file.getParentFile(), file.getName() + ".ofx");
      }
      if (file.exists()) {
        int result = JOptionPane.showConfirmDialog(parent,
                                                   Lang.get("export.confirm.message"),
                                                   Lang.get("export.confirm.title"),
                                                   JOptionPane.YES_NO_OPTION);
        if (result != JOptionPane.YES_OPTION) {
          return;
        }
      }
      try {
        writeFile(file);
      }
      catch (IOException e) {
        JOptionPane.showMessageDialog(chooser, "Error writing file: " + file.getName());
      }
    }
  }

  private void writeFile(File file) throws IOException {
    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
    try {
      OfxExporter.write(repository, bufferedWriter);
    }
    finally {
      bufferedWriter.close();
    }
  }
}
