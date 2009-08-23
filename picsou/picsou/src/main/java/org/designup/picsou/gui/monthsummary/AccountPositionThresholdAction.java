package org.designup.picsou.gui.monthsummary;

import org.designup.picsou.model.AccountPositionThreshold;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.gui.description.Formatting;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class AccountPositionThresholdAction extends AbstractAction implements ChangeSetListener {
  private GlobRepository repository;
  private Directory directory;

  public AccountPositionThresholdAction(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    repository.addChangeListener(this);
    updateName();
  }

  public void actionPerformed(ActionEvent e) {
    AccountPositionThresholdDialog dialog = new AccountPositionThresholdDialog(repository, directory);
    dialog.show();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(AccountPositionThreshold.TYPE)) {
      updateName();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(AccountPositionThreshold.TYPE)) {
      updateName();
    }
  }

  private void updateName() {
    Double limit = AccountPositionThreshold.getValue(repository);
    String displayLimit = "";
    if (limit != null){
      displayLimit = Formatting.DECIMAL_FORMAT.format(limit);
    }
    this.putValue(NAME, Lang.get("accountPositionThresholdAction.label", displayLimit));
  }
}
