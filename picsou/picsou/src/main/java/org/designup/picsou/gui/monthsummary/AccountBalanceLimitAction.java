package org.designup.picsou.gui.monthsummary;

import org.designup.picsou.model.AccountBalanceLimit;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class AccountBalanceLimitAction extends AbstractAction implements ChangeSetListener {
  private GlobRepository repository;
  private Directory directory;

  public AccountBalanceLimitAction(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    repository.addChangeListener(this);
    updateName();
  }

  public void actionPerformed(ActionEvent e) {
    AccountBalanceLimitDialog dialog = new AccountBalanceLimitDialog(repository, directory);
    dialog.show();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(AccountBalanceLimit.TYPE)) {
      updateName();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(AccountBalanceLimit.TYPE)) {
      updateName();
    }
  }

  private void updateName() {
    double limit = AccountBalanceLimit.getLimit(repository);
    this.putValue(NAME, Lang.get("accountLimitAction.label", PicsouDescriptionService.DECIMAL_FORMAT.format(limit)));
  }
}
