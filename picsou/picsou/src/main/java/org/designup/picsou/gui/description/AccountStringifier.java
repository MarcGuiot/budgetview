package org.designup.picsou.gui.description;

import org.designup.picsou.model.Account;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobStringifier;

import java.util.Comparator;

public class AccountStringifier implements GlobStringifier {
  public String toString(Glob account, GlobRepository repository) {
    if (account == null) {
      return "";
    }
    if (account.get(Account.ID).equals(Account.MAIN_SUMMARY_ACCOUNT_ID)) {
      return Lang.get("account.summary.main");
    }
    if (account.get(Account.ID).equals(Account.SAVINGS_SUMMARY_ACCOUNT_ID)) {
      return Lang.get("account.summary.savings");
    }
    if (account.get(Account.ID).equals(Account.ALL_SUMMARY_ACCOUNT_ID)) {
      return Lang.get("account.summary.all");
    }
    if (account.get(Account.ID).equals(Account.EXTERNAL_ACCOUNT_ID)) {
      return (Lang.get("seriesEdition.account.external"));
    }

    return account.get(Account.NAME);
  }

  public Comparator<Glob> getComparator(GlobRepository repository) {
    return new AccountComparator();
  }
}
