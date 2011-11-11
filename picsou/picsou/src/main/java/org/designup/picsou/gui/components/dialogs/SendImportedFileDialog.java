package org.designup.picsou.gui.components.dialogs;

import org.designup.picsou.gui.components.CloseDialogAction;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.importer.Obfuscator;
import org.designup.picsou.importer.utils.TypedInputStream;
import org.designup.picsou.model.TransactionImport;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.model.utils.ReverseGlobFieldComparator;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.util.Comparator;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.globsframework.model.utils.GlobMatchers.isNotNull;

public class SendImportedFileDialog {
  private GlobRepository repository;
  private GlobsPanelBuilder builder;
  private PicsouDialog dialog;
  private JButton copyButton;
  private JTextArea textArea;

  public SendImportedFileDialog(Window owner, Directory directory, GlobRepository repository) {
    this.repository = repository;
    directory = new DefaultDirectory(directory);

    SelectionService selectionService = new SelectionService();
    directory.add(SelectionService.class, selectionService);

    builder = new GlobsPanelBuilder(getClass(), "/layout/utils/sendImportedFileDialog.splits", repository, directory);

    builder.addCombo("files", TransactionImport.TYPE)
      .setRenderer(new GlobStringifier() {
        public String toString(Glob glob, GlobRepository repository) {
          Date date = glob.get(TransactionImport.IMPORT_DATE);
          return Formatting.toString(date) + " - " + glob.get(TransactionImport.SOURCE);
        }

        public Comparator<Glob> getComparator(GlobRepository repository) {
          return new ReverseGlobFieldComparator(TransactionImport.ID);
        }
      })
      .setFilter(isNotNull(TransactionImport.FILE_CONTENT));

    textArea = new JTextArea();
    textArea.setEditable(true);
    builder.add("details", textArea);

    selectionService.addListener(
      new GlobSelectionListener() {
        public void selectionUpdated(GlobSelection selection) {
          textArea.setText(getObfuscatedText(selection));
          textArea.setCaretPosition(0);
        }
      }, TransactionImport.TYPE);

    copyButton = new JButton(new AbstractAction(Lang.get("exception.copy")) {
      public void actionPerformed(ActionEvent e) {
        GuiUtils.copyTextToClipboard(textArea.getText());
      }
    });
    builder.add("copy", copyButton);

    dialog = PicsouDialog.create(owner, true, directory);
    dialog.addPanelWithButton(builder.<JPanel>load(), new CloseDialogAction(dialog));
    dialog.pack();

    selectionService.select(getLastImport(repository));
  }

  public final void show() {
    dialog.showCentered();
    builder.dispose();
  }

  private String getObfuscatedText(GlobSelection selection) {
    try {
      Glob first = selection.getAll(TransactionImport.TYPE).getFirst();
      if (first != null) {
        byte[] bytes = first.get(TransactionImport.FILE_CONTENT);
        if (bytes != null) {
          ByteArrayInputStream byteArrayOutputStream = new ByteArrayInputStream(bytes);
          ZipInputStream stream = new ZipInputStream(byteArrayOutputStream);
          ZipEntry zipEntry = stream.getNextEntry();
          if (zipEntry != null) {
            TypedInputStream typedInputStream = new TypedInputStream(stream);
            Obfuscator obfuscator = new Obfuscator();
            return obfuscator.apply(typedInputStream);
          }
        }
      }
    }
    catch (Exception e) {
      return e.getMessage();
    }

    return "";
  }

  private Glob getLastImport(GlobRepository repository) {
    return repository.getAll(TransactionImport.TYPE, isNotNull(TransactionImport.FILE_CONTENT))
      .sort(TransactionImport.ID).getLast();
  }

}
