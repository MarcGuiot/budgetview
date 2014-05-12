package org.designup.picsou.gui.accounts.utils;

import org.designup.picsou.gui.accounts.position.AccountPositionLabels;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.model.Account;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;

public class AccountPositionStringifier implements GlobListStringifier {
  public String toString(GlobList list, GlobRepository repository) {
    if (list.isEmpty()) {
      return "";
    }
    Glob account = list.get(0);
    return Lang.get("summaryView.account.position",
                    Formatting.toString(account.get(Account.POSITION_WITH_PENDING)),
                    AccountPositionLabels.getAccountPositionDate(account));
  }
}
