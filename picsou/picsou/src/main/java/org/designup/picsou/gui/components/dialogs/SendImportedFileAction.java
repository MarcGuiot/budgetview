package org.designup.picsou.gui.components.dialogs;

import org.designup.picsou.utils.Lang;
import org.globsframework.utils.directory.Directory;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SendImportedFileAction extends AbstractAction {
  private Directory directory;
  private GlobRepository repository;

  public SendImportedFileAction(Directory directory, GlobRepository repository){
    super(Lang.get("sendImportedFile.action"));
    this.directory = directory;
    this.repository = repository;
}

  public void actionPerformed(ActionEvent e) {
    SendImportedFileDialog dialog = new SendImportedFileDialog(directory.get(JFrame.class), directory, repository);
    dialog.show();
  }
}
