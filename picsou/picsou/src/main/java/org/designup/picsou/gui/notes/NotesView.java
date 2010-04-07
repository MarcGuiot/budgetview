package org.designup.picsou.gui.notes;

import org.designup.picsou.gui.View;
import org.designup.picsou.model.Notes;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class NotesView extends View {

  private JPanel panel = new JPanel();

  public NotesView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/home/notesView.splits",
                                                      repository, directory);

    builder.add("notesPanel", panel);

    builder.addMultiLineEditor("notesEditor", Notes.TEXT)
      .setNotifyOnKeyPressed(true)
      .forceSelection(Notes.KEY);

    parentBuilder.add("notesView", builder);
  }
}
