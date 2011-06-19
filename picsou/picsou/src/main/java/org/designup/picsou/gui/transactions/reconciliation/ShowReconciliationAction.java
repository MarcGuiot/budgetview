package org.designup.picsou.gui.transactions.reconciliation;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Lang;
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
    repository.addChangeListener(new TypeChangeSetListener(UserPreferences.TYPE) {
      protected void update(GlobRepository repository) {
        updateLabel();
      }
    });
    updateLabel();
  }

  private void updateLabel() {
    Glob preferences = repository.find(UserPreferences.KEY);
    if (preferences == null) {
      setText("");
    }
    else if (preferences.isTrue(UserPreferences.SHOW_RECONCILIATION)) {
      setText(Lang.get("reconciliation.hide"));
    }
    else {
      setText(Lang.get("reconciliation.show"));
    }
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
      directory.get(NavigationService.class).gotoCategorization();
    }
  }
}
