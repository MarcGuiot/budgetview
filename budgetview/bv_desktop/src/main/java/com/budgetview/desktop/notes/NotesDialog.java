package com.budgetview.desktop.notes;

import com.budgetview.desktop.components.dialogs.CloseDialogAction;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.model.Notes;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class NotesDialog  {

  private PicsouDialog dialog;
  private GlobRepository repository;
  private Directory directory;

  public NotesDialog(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    createDialog();
  }

  public void show() {
    GuiUtils.showCentered(dialog);
  }

  public void createDialog( ) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/home/notesDialog.splits",
                                                      repository, directory);

    builder.addMultiLineEditor("notesEditor", Notes.TEXT)
      .setNotifyOnKeyPressed(true)
      .forceSelection(Notes.KEY);

    dialog = PicsouDialog.create(this, directory.get(JFrame.class), false, directory);
    dialog.setPanelAndButton(builder.<JPanel>load(), new CloseDialogAction(dialog));
    dialog.pack();
  }
}
