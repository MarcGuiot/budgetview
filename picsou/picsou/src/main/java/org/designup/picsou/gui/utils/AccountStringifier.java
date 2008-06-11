package org.designup.picsou.gui.utils;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.utils.AbstractGlobStringifier;
import org.crossbowlabs.globs.utils.Strings;
import org.designup.picsou.model.Account;
import org.designup.picsou.utils.Lang;

public class AccountStringifier extends AbstractGlobStringifier {
  public String toString(Glob account, GlobRepository repository) {
    if (account == null) {
      return "";
    }
    if (account.get(Account.ID).equals(Account.SUMMARY_ACCOUNT_ID)) {
      return Lang.get("account.summary.name");
    }
    String number = account.get(Account.NUMBER);
    String name = account.get(Account.NAME);
    if (Strings.isNotEmpty(name)) {
      if (Strings.isNotEmpty(number)) {
        return name + "(" + number + ")";
      }
      else {
        return name;
      }
    }
    return number;
  }
}
