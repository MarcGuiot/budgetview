package org.designup.picsou.gui.monthsummary;

import org.designup.picsou.model.AccountPositionThreshold;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.gui.description.Formatting;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AccountPositionThresholdAction extends AbstractAction {
  private GlobRepository repository;
  private Directory directory;

  public AccountPositionThresholdAction(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    repository.addChangeListener(new TypeChangeSetListener(AccountPositionThreshold.TYPE) {
      protected void update(GlobRepository repository) {
        updateName();
      }
    });
    updateName();
  }

  public void actionPerformed(ActionEvent e) {
    AccountPositionThresholdDialog dialog = new AccountPositionThresholdDialog(repository, directory);
    dialog.show();
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
