package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Set;

public class NoSeriesMessage extends DynamicMessage {

  private BudgetArea budgetArea;

  public NoSeriesMessage(BudgetArea budgetArea, GlobRepository repository, Directory directory) {
    super(repository, directory);
    this.budgetArea = budgetArea;

    setDefaultText();

    registerUpdateListener(repository);
    updateVisibility();
  }

  protected void setDefaultText() {
    setText(Lang.get("categorization.noseries." + budgetArea.getName()));
  }

  private void registerUpdateListener(GlobRepository repository) {
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsCreationsOrDeletions(Series.TYPE) ||
            changeSet.containsUpdates(Series.BUDGET_AREA)) {
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

  protected boolean isVisible() {
    return !repository.contains(Series.TYPE, GlobMatchers.fieldEquals(Series.BUDGET_AREA, budgetArea.getId()));
  }
}
