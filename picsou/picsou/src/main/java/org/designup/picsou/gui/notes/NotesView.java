package org.designup.picsou.gui.notes;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.series.wizard.SeriesWizardDialog;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.model.GlobRepository;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.TypeChangeSetListener;
import static org.globsframework.model.utils.GlobMatchers.fieldIn;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

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

    builder.add("openSeriesWizard", new OpenSeriesWizardAction());

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
    else if (!repository.contains(Series.TYPE, Series.USER_SERIES_MATCHER)) {
      cards.show("noSeries");
    }
    else {
      cards.show("standard");
    }
  }

  private class OpenSeriesWizardAction extends AbstractAction {
    public OpenSeriesWizardAction() {
      super(Lang.get("notesView.openSeriesWizard"));
    }

    public void actionPerformed(ActionEvent e) {
      SeriesWizardDialog dialog = new SeriesWizardDialog(repository, directory);
      dialog.show();
    }
  }
}
