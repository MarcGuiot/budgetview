package org.designup.picsou.gui.accounts;

import static org.designup.picsou.model.Account.*;
import org.globsframework.model.Glob;
import org.globsframework.utils.Utils;

import java.util.Comparator;

public class AccountComparator implements Comparator<Glob> {

  public int compare(Glob account1, Glob account2) {
    if (Utils.equal(account1.get(ID), SUMMARY_ACCOUNT_ID)) {
      return -1;
    }
    if (Utils.equal(account2.get(ID), SUMMARY_ACCOUNT_ID)) {
      return 1;
    }
    if ((account1.get(IS_CARD_ACCOUNT) == Boolean.TRUE)
        && (account2.get(IS_CARD_ACCOUNT) != Boolean.TRUE)) {
      return 1;
    }
    if ((account1.get(IS_CARD_ACCOUNT) != Boolean.TRUE)
        && (account2.get(IS_CARD_ACCOUNT) == Boolean.TRUE)) {
      return -1;
    }
    return Utils.compare(account1.get(NAME), account2.get(NAME));
  }
}
