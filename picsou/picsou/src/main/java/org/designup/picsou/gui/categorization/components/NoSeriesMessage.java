package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Set;

public class NoSeriesMessage {

  private JEditorPane htmlEditor;
  private BudgetArea budgetArea;
  protected final GlobRepository repository;
  protected final Directory directory;

  public NoSeriesMessage(BudgetArea budgetArea, GlobRepository repository, Directory directory) {
    this.budgetArea = budgetArea;
    this.repository = repository;
    this.directory = directory;

    htmlEditor = GuiUtils.createReadOnlyHtmlComponent();
    htmlEditor.addHyperlinkListener(new HyperlinkHandler(directory) {
      protected void processCustomLink(String href) {
        NoSeriesMessage.this.processHyperlinkClick(href);
      }
    });

    setDefaultText();

    registerUpdateListener(repository);
    updateVisibility();
  }

  protected void processHyperlinkClick(String href) {
  }

  protected void setDefaultText() {
    setText(Lang.get("categorization.noseries." + budgetArea.getName()));
  }

  protected void setText(String text) {
    htmlEditor.setText(text);
  }

  private void registerUpdateListener(GlobRepository repository) {
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsCreationsOrDeletions(Series.TYPE)) {
          updateVisibility();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(Series.TYPE)) {
          updateVisibility();
        }
      }
    });
  }

  private void updateVisibility() {
    htmlEditor.setVisible(!repository.contains(Series.TYPE, GlobMatchers.fieldEquals(Series.BUDGET_AREA, budgetArea.getId())));
  }

  public JEditorPane getComponent() {
    return htmlEditor;
  }
}
