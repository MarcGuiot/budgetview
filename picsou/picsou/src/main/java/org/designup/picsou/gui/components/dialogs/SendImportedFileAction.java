package org.designup.picsou.gui.components.dialogs;

import org.designup.picsou.model.TransactionImport;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static org.globsframework.model.utils.GlobMatchers.isNotNull;

public class SendImportedFileAction extends AbstractAction {
  private Directory directory;
  private GlobRepository repository;

  public SendImportedFileAction(Directory directory, GlobRepository repository) {
    super(Lang.get("sendImportedFile.action"));
    this.directory = directory;
    this.repository = repository;
    repository.addChangeListener(new TypeChangeSetListener(TransactionImport.TYPE) {
      protected void update(GlobRepository repository) {
        updateAction(repository);
      }
    });
    updateAction(repository);
  }

  private void updateAction(GlobRepository repository) {
    setEnabled(repository.contains(TransactionImport.TYPE, isNotNull(TransactionImport.FILE_CONTENT)));
  }

  public void actionPerformed(ActionEvent e) {
    SendImportedFileDialog dialog = new SendImportedFileDialog(directory.get(JFrame.class), directory, repository);
    dialog.show();
  }
}
