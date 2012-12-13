package org.designup.picsou.gui.accounts.utils;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.notifications.GlobNotification;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountPositionError;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AccountPositionNotification extends GlobNotification {

  private Directory directory;
  private Key accountKey;

  public AccountPositionNotification(GlobRepository repository, Directory directory, final Glob error, final String date, final Glob account, final int id) {
    super(repository, id, error,
          AccountPositionError.UPDATE_DATE, AccountPositionError.CLEARED,
          Lang.get("messages.account.position.error.msg" + (date != null ? ".date" : ""),
                   account.get(Account.NAME),
                   Formatting.toString(error.get(AccountPositionError.LAST_REAL_OPERATION_POSITION)),
                   Formatting.toString(error.get(AccountPositionError.IMPORTED_POSITION)),
                   date));
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
