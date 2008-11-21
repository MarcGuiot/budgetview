package org.designup.picsou.gui.description;

import org.designup.picsou.model.Account;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.gui.description.AccountComparator;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.Strings;

import java.util.Comparator;

public class AccountStringifier implements GlobStringifier {
  public String toString(Glob account, GlobRepository repository) {
    if (account == null) {
      return "";
    }
    if (account.get(Account.ID).equals(Account.MAIN_SUMMARY_ACCOUNT_ID)) {
      return Lang.get("account.main.summary.name");
    }
    if (account.get(Account.ID).equals(Account.SAVINGS_SUMMARY_ACCOUNT_ID)) {
      return Lang.get("account.savings.summary.name");
    }
    if (account.get(Account.ID).equals(Account.ALL_SUMMARY_ACCOUNT_ID)) {
      return Lang.get("account.summary.name");
    }

    String number = account.get(Account.NUMBER);
    String name = account.get(Account.NAME);
    if (Strings.isNotEmpty(name)) {
      if (Strings.isNotEmpty(number)) {
        return name + " (" + number + ")";
      }
      else {
        return name;
      }
    }
    return number;
  }

  public Comparator<Glob> getComparator(GlobRepository repository) {
    return new AccountComparator();
  }
}
