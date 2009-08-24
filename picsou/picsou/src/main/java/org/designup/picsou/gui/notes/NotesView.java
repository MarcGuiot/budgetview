package org.designup.picsou.gui.notes;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.series.wizard.SeriesWizardDialog;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.model.BalanceStat;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.FieldValue;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.GlobMatcher;
import static org.globsframework.model.utils.GlobMatchers.fieldIn;
import org.globsframework.utils.directory.Directory;
import org.globsframework.metamodel.GlobType;

import javax.swing.*;
import java.util.Set;
import java.awt.event.ActionEvent;

public class NotesView extends View {
  private static final GlobMatcher USER_SERIES_MATCHER =
    fieldIn(Series.BUDGET_AREA,
            BudgetArea.INCOME.getId(),
            BudgetArea.RECURRING.getId(),
            BudgetArea.ENVELOPES.getId(),
            BudgetArea.SPECIAL.getId(),
            BudgetArea.SAVINGS.getId());

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
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsCreationsOrDeletions(Transaction.TYPE) ||
            changeSet.containsCreationsOrDeletions(Series.TYPE)) {
          updateCard();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(Transaction.TYPE) || changedTypes.contains(Series.TYPE)) {
          updateCard();
        }
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
    else if (!repository.contains(Series.TYPE, USER_SERIES_MATCHER)) {
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
