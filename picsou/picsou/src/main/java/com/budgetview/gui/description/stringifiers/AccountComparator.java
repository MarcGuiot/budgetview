package com.budgetview.gui.description.stringifiers;

import com.budgetview.model.Account;
import org.globsframework.model.Glob;
import org.globsframework.utils.Utils;

import java.util.Comparator;

import static com.budgetview.model.Account.ID;
import static com.budgetview.model.Account.NAME;

public class AccountComparator implements Comparator<Glob> {

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
    if (!Account.isUserCreatedAccount(account1) && !Account.isUserCreatedAccount(account2)) {
      return account2.get(ID) - account1.get(ID);
    }
    else if (Account.isUserCreatedAccount(account1) && !Account.isUserCreatedAccount(account2)) {
      return -1;
    }
    else if (!Account.isUserCreatedAccount(account1) && Account.isUserCreatedAccount(account2)) {
      return 1;
    }
    Integer sequence1 = account1.get(Account.SEQUENCE);
    Integer sequence2 = account2.get(Account.SEQUENCE);
    if (sequence1 == null && sequence2 == null) {
      int compare = Utils.compareIgnoreCase(account1.get(NAME), account2.get(NAME));
      if (compare == 0) {
        return account2.get(ID) - account1.get(ID);
      }
    }
    if (sequence1 == null) {
      return -1;
    }
    if (sequence2 == null) {
      return 1;
    }
    return sequence1 - sequence2;
  }
}
