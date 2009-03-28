package org.designup.picsou.gui.actions;

import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ExportFileAction extends AbstractAction {
  private GlobRepository repository;
  private Directory directory;

  public ExportFileAction(GlobRepository repository, Directory directory) {
    super(Lang.get("export"));
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent e) {
    ExportDialog dialog = new ExportDialog(repository, directory);
    dialog.show();
  }
}
