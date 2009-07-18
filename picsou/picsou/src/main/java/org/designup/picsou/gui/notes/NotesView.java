package org.designup.picsou.gui.notes;

import org.designup.picsou.gui.View;
import org.designup.picsou.model.Notes;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class NotesView extends View {

  public NotesView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/notesView.splits",
                                                      repository, directory);

    builder.addMultiLineEditor("notesEditor", Notes.TEXT)
      .setNotifyOnKeyPressed(true)
      .forceSelection(repository.findOrCreate(Notes.KEY));

    parentBuilder.add("notesView", builder);
  }
}
