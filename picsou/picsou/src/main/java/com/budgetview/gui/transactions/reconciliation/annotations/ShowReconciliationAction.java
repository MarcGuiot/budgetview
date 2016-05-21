package com.budgetview.gui.transactions.reconciliation.annotations;

import com.budgetview.gui.card.NavigationService;
import com.budgetview.model.SignpostStatus;
import com.budgetview.model.Transaction;
import com.budgetview.model.UserPreferences;
import com.budgetview.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ShowReconciliationAction extends AbstractAction {

  private GlobRepository repository;
  private Directory directory;

  public ShowReconciliationAction(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    repository.addChangeListener(new TypeChangeSetListener(UserPreferences.TYPE, Transaction.TYPE) {
      public void update(GlobRepository repository) {
        doUpdate();
      }
    });
    doUpdate();
  }

  private void doUpdate() {
    Glob preferences = repository.find(UserPreferences.KEY);
    if (preferences == null) {
      setText("");
    }
    else if (preferences.isTrue(UserPreferences.SHOW_RECONCILIATION)) {
      setText(Lang.get("reconciliation.annotation.hide"));
    }
    else {
      setText(Lang.get("reconciliation.annotation.show"));
    }

    setEnabled(repository.contains(Transaction.TYPE));
  }

  private void setText(String label) {
    putValue(NAME, label);
  }

  public void actionPerformed(ActionEvent actionEvent) {
    Glob preferences = repository.find(UserPreferences.KEY);
    boolean wasShowing = preferences != null && preferences.isTrue(UserPreferences.SHOW_RECONCILIATION);
    boolean doShow = !wasShowing;
    repository.update(UserPreferences.KEY, UserPreferences.SHOW_RECONCILIATION, doShow);
    if (doShow) {
      if (!SignpostStatus.isCompleted(SignpostStatus.FIRST_RECONCILIATION_SHOWN, repository)) {
        SignpostStatus.setCompleted(SignpostStatus.FIRST_RECONCILIATION_SHOWN, repository);
        directory.get(NavigationService.class).gotoCategorizationAndShowAll();
      }
      else {
        directory.get(NavigationService.class).gotoCategorization();
      }
    }
  }
}
