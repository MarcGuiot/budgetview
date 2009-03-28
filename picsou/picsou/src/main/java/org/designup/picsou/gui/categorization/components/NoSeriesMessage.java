package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;

import javax.swing.*;
import java.util.Set;

public class NoSeriesMessage {

  private JEditorPane htmlEditor;
  private BudgetArea budgetArea;
  private GlobRepository repository;

  public NoSeriesMessage(BudgetArea budgetArea, GlobRepository repository) {
    this.budgetArea = budgetArea;
    this.repository = repository;

    this.htmlEditor = new JEditorPane();
    GuiUtils.initReadOnlyHtmlComponent(htmlEditor);

    this.htmlEditor.setText(Lang.get("categorization.noseries." + budgetArea.getName()));

    registerUpdateListener(repository);
    updateVisibility();
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
