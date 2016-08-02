package com.budgetview.desktop.notes;

import com.budgetview.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ShowNotesAction extends AbstractAction {
  private NotesDialog dialog;
  private GlobRepository repository;
  private Directory directory;

  public ShowNotesAction(GlobRepository repository, Directory directory) {
    super(Lang.get("notesDialog.action"));
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    if (dialog == null) {
      dialog = new NotesDialog(repository, directory);
    }
    dialog.show();
  }
}
