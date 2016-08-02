package com.budgetview.desktop.accounts.actions;

import com.budgetview.model.Account;
import com.budgetview.utils.Lang;
import org.globsframework.gui.actions.SingleGlobAction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

public class MoveAccountDown extends SingleGlobAction {
  protected MoveAccountDown(Key key, GlobRepository repository) {
    super(Lang.get("account.move.down"), key, repository);
  }

  protected void processClick(Glob account, GlobRepository repository) {
    Integer max = repository.getAll(Account.TYPE).getMaxValue(Account.SEQUENCE);
    if (max == null) {
      max = 0;
    }
    repository.update(account.getKey(), Account.SEQUENCE, max + 1);
  }

  protected void processUpdate(Glob glob, GlobRepository repository) {

  }
}
