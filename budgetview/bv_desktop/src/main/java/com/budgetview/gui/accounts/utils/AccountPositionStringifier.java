package com.budgetview.gui.accounts.utils;

import com.budgetview.gui.accounts.position.AccountPositionLabels;
import com.budgetview.gui.description.Formatting;
import com.budgetview.model.Account;
import com.budgetview.utils.Lang;
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
    Double position = account.get(Account.POSITION_WITH_PENDING);
    if ((position == null) || (account.get(Account.POSITION_DATE) == null)) {
      return "";
    }
    return Lang.get("summaryView.account.position",
                    Formatting.toString(position),
                    AccountPositionLabels.getAccountPositionDate(account));
  }
}
