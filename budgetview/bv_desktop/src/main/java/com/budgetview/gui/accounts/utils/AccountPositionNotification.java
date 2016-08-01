package com.budgetview.gui.accounts.utils;

import com.budgetview.gui.card.NavigationService;
import com.budgetview.gui.notifications.GlobNotification;
import com.budgetview.model.AccountPositionError;
import com.budgetview.gui.description.Formatting;
import com.budgetview.model.Account;
import com.budgetview.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AccountPositionNotification extends GlobNotification {

  private Directory directory;
  private Key accountKey;

  public AccountPositionNotification(final Glob error, final String date, final Glob account, GlobRepository repository, Directory directory) {
    super(error, Lang.get("messages.account.position.error.msg" + (date != null ? ".date" : ""),
             account.get(Account.NAME),
             Formatting.toString(error.get(AccountPositionError.LAST_REAL_OPERATION_POSITION)),
             Formatting.toString(error.get(AccountPositionError.IMPORTED_POSITION)),
             date), AccountPositionError.UPDATE_DATE, AccountPositionError.CLEARED,
          repository);
    this.directory = directory;
    this.accountKey = account.getKey();
  }

  public Action getAction() {
    return new AbstractAction(Lang.get("messages.account.position.error.show")) {
      public void actionPerformed(ActionEvent actionEvent) {
        directory.get(NavigationService.class).gotoDataForAccount(accountKey);
      }
    };
  }
}
