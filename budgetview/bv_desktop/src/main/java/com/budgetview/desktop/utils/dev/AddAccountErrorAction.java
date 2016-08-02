package com.budgetview.desktop.utils.dev;

import com.budgetview.desktop.accounts.utils.AccountMatchers;
import com.budgetview.desktop.time.TimeService;
import com.budgetview.model.Account;
import com.budgetview.model.AccountPositionError;
import org.globsframework.model.*;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static org.globsframework.model.FieldValue.value;

public class AddAccountErrorAction extends AbstractAction {

  private GlobRepository repository;

  public AddAccountErrorAction(GlobRepository repository) {
    super("Add position error");
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
                      FieldValue.value(AccountPositionError.UPDATE_DATE, TimeService.getToday()),
                      value(AccountPositionError.CLEARED, false));
  }
}
