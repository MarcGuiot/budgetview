package org.designup.picsou.gui.notes;

import org.designup.picsou.gui.components.CloseDialogAction;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.model.Notes;
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

    dialog = PicsouDialog.create(directory.get(JFrame.class), false, directory);
    dialog.setPanelAndButton(builder.<JPanel>load(), new CloseDialogAction(dialog));
    dialog.pack();
  }
}
