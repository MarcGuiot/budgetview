package org.designup.picsou.gui.components.dialogs;

import org.designup.picsou.gui.components.CloseDialogAction;
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
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Dates;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.Date;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

public class SendImportedFileDialog {
  private PicsouDialog dialog;
  private JButton copyButton;
  private GlobsPanelBuilder builder;
  private GlobRepository repository;
  private JTextArea textArea;

  public SendImportedFileDialog(Window owner, Directory directory, GlobRepository repository) {
    this.repository = repository;
    directory = new DefaultDirectory(directory);
    SelectionService service = new SelectionService();
    directory.add(SelectionService.class, service);

    builder = new GlobsPanelBuilder(getClass(), "/layout/utils/sendImportedFileDialog.splits", repository, directory);

    builder.addCombo("files", TransactionImport.TYPE)
      .setRenderer(new GlobStringifier() {
        public String toString(Glob glob, GlobRepository repository) {
          Date date = glob.get(TransactionImport.IMPORT_DATE);
          return Dates.toString(date) + ":" + glob.get(TransactionImport.ID) + ":" + glob.get(TransactionImport.SOURCE);
        }

        public Comparator<Glob> getComparator(GlobRepository repository) {
          return new GlobFieldComparator(TransactionImport.ID);
        }
      })
      .setFilter(GlobMatchers.isNotNull(TransactionImport.DATA));
    textArea = new JTextArea();
    textArea.setCaretPosition(0);
    textArea.setEditable(true);
    builder.add("details", textArea);
    service.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        try {
          Glob first = selection.getAll(TransactionImport.TYPE).getFirst();
          if (first != null) {
            byte[] bytes = first.get(TransactionImport.DATA);
            if (bytes != null) {
              ByteArrayInputStream byteArrayOutputStream = new ByteArrayInputStream(bytes);
              ZipInputStream stream = new ZipInputStream(byteArrayOutputStream);
              ZipEntry zipEntry = stream.getNextEntry();
              if (zipEntry != null){
                TypedInputStream typedInputStream = new TypedInputStream(stream);
                Obfuscator obfuscator = new Obfuscator();
                textArea.setText(obfuscator.apply(typedInputStream));
              }
            }
          }
        }
        catch (IOException e) {
          textArea.setText(e.getMessage());
        }
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
    Glob last = repository.getAll(TransactionImport.TYPE, GlobMatchers.isNotNull(TransactionImport.DATA))
      .sort(TransactionImport.ID).getLast();
    if (last != null){
      service.select(last);
    }
  }

  public final void show() {
    dialog.showCentered();
    builder.dispose();
  }

}
