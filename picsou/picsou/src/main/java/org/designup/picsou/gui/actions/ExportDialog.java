package org.designup.picsou.gui.actions;

import org.designup.picsou.exporter.Exporter;
import org.designup.picsou.exporter.Exporters;
import org.designup.picsou.gui.components.CloseAction;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.GuiUtils;
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

public class ExportDialog {
  private GlobRepository repository;
  private Component parent;
  private PicsouDialog dialog;
  private Directory directory;
  private Exporters exporters;
  private ButtonGroup radioGroup = new ButtonGroup();

  public ExportDialog(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    this.exporters = new Exporters(directory);
    this.parent = directory.get(JFrame.class);
    createDialog();
  }

  public void show() {
    GuiUtils.showCentered(dialog);
  }

  private void createDialog() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/exportDialog.splits", repository, directory);

    builder.addRepeat("types",
                      exporters.getAll(),
                      new RepeatComponentFactory<Exporter>() {
                        public void registerComponents(RepeatCellBuilder cellBuilder, Exporter exporter) {
                          final String exporterType = exporter.getType();

                          JRadioButton radio = new JRadioButton();
                          radio.setActionCommand(exporterType);
                          cellBuilder.add("radio", radio);
                          radio.setText(Lang.get("exportDialog." + exporterType + ".type"));

                          radioGroup.add(radio);

                          JEditorPane htmlEditor = new JEditorPane();
                          GuiUtils.initReadOnlyHtmlComponent(htmlEditor);
                          htmlEditor.setText(Lang.get("exportDialog." + exporterType + ".description"));
                          cellBuilder.add("description", htmlEditor);
                        }
                      });

    builder.addLoader(new SplitsLoader() {
      public void load(Component component) {
        radioGroup.getElements().nextElement().setSelected(true);
      }
    });

    dialog = PicsouDialog.create(directory.get(JFrame.class), directory);
    dialog.addPanelWithButtons(builder.<JPanel>load(), new OkAction(), new CloseAction(dialog));
    dialog.pack();
  }

  private class OkAction extends AbstractAction {

    private OkAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent event) {
      Exporter exporter = exporters.get(radioGroup.getSelection().getActionCommand());
      doExport(exporter);
    }
  }

  private void doExport(final Exporter exporter) {

    final String type = exporter.getType();
    final String extension = exporter.getExtension();

    JFileChooser chooser = new JFileChooser();
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.addChoosableFileFilter(new FileFilter() {
      public boolean accept(File file) {
        return file.getName().endsWith(extension) || file.isDirectory();
      }

      public String getDescription() {
        return Lang.get("exportDialog." + type + ".filter");
      }
    });

    dialog.setVisible(false);

    int returnVal = chooser.showSaveDialog(parent);
    if (returnVal != JFileChooser.APPROVE_OPTION) {
      return;
    }

    File file = chooser.getSelectedFile();
    if (!file.getName().endsWith(extension)) {
      file = new File(file.getParentFile(), file.getName() + "." + extension);
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
      writeFile(exporter, file);
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog(chooser, "Error writing file: " + file.getName());
    }
  }

  private void writeFile(Exporter exporter, File file) throws IOException {
    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
    try {
      exporter.export(repository, bufferedWriter);
    }
    finally {
      bufferedWriter.close();
    }
  }
}
