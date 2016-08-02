package com.budgetview.desktop.accounts.actions;

import com.budgetview.model.Account;
import com.budgetview.utils.Lang;
import org.globsframework.gui.actions.SingleGlobAction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

public class MoveAccountUp extends SingleGlobAction {
  protected MoveAccountUp(Key key, GlobRepository repository) {
    super(Lang.get("account.move.up"), key, repository);
  }

  protected void processClick(Glob account, GlobRepository repository) {
    Integer min = repository.getAll(Account.TYPE).getMinValue(Account.SEQUENCE);
    if (min == null) {
      min = 1;
    }
    repository.update(account.getKey(), Account.SEQUENCE, min - 1);
  }

  protected void processUpdate(Glob glob, GlobRepository repository) {

  }
}
