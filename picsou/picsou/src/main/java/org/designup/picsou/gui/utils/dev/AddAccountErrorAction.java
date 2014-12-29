package org.designup.picsou.gui.utils.dev;

import org.designup.picsou.gui.accounts.utils.AccountMatchers;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountPositionError;
import org.globsframework.model.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Date;

import static org.globsframework.model.FieldValue.*;

public class AddAccountErrorAction extends AbstractAction {

  private GlobRepository repository;

  public AddAccountErrorAction(GlobRepository repository) {
    super("[add position error]");
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent e) {
    GlobList accounts = repository.getAll(Account.TYPE, AccountMatchers.userCreatedMainAccounts());
    if (accounts.isEmpty()) {
      return;
    }
    Glob account = accounts.getFirst();
    Key key = Key.create(AccountPositionError.TYPE, account.get(Account.ID));
    repository.findOrCreate(key);
    repository.update(key,
                      value(AccountPositionError.IMPORTED_POSITION, 10.00),
                      value(AccountPositionError.LAST_REAL_OPERATION_POSITION, 20.00),
                      value(AccountPositionError.UPDATE_DATE, new Date()),
                      value(AccountPositionError.CLEARED, false));
  }
}
