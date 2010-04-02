package org.designup.picsou.gui.notes;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.model.Notes;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.directory.Directory;

public class NotesView extends View {

  private CardHandler cards;
  private ImportFileAction importFileAction;

  public NotesView(ImportFileAction importFileAction, GlobRepository repository, Directory directory) {
    super(repository, directory);
    this.importFileAction = importFileAction;
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/notesView.splits",
                                                      repository, directory);

    cards = builder.addCardHandler("cards");

    builder.addMultiLineEditor("notesEditor", Notes.TEXT)
      .setNotifyOnKeyPressed(true)
      .forceSelection(Notes.KEY);

    builder.add("import", importFileAction);

    builder.add("hyperlinkHandler", new HyperlinkHandler(directory));

    registerCardUpdater();

    parentBuilder.add("notesView", builder);
  }

  private void registerCardUpdater() {
    repository.addChangeListener(new TypeChangeSetListener(Transaction.TYPE, Series.TYPE) {
      protected void update(GlobRepository repository) {
        updateCard();
      }
    });
  }

  public void init() {
    updateCard();
  }

  private void updateCard() {
    if (!repository.contains(Transaction.TYPE)) {
      cards.show("noData");
    }
    else {
      cards.show("standard");
    }
  }
}
