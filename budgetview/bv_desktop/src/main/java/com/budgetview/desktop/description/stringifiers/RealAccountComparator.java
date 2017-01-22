package com.budgetview.desktop.description.stringifiers;

import com.budgetview.model.Account;
import com.budgetview.model.RealAccount;
import org.globsframework.model.Glob;
import org.globsframework.utils.Utils;

import java.util.Comparator;

import static com.budgetview.model.Account.ID;
import static com.budgetview.model.Account.NAME;

public class RealAccountComparator implements Comparator<Glob> {

  public int compare(Glob account1, Glob account2) {
    if (account1 == null && account2 == null) {
      return 0;
    }
    if (account1 == null) {
      return -1;
    }
    if (account2 == null) {
      return 1;
    }
    int accountTypeDiff = Utils.compare(account1.get(RealAccount.ACCOUNT_TYPE), account2.get(RealAccount.ACCOUNT_TYPE));
    if (accountTypeDiff < 0) {
      return -1;
    }
    else if (accountTypeDiff > 0) {
      return 1;
    }
    return Utils.compare(account1.get(RealAccount.NAME), account2.get(RealAccount.NAME));
  }
}
