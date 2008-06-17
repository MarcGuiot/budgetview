package org.designup.picsou.gui.description;

import org.designup.picsou.model.Account;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.utils.Strings;

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
