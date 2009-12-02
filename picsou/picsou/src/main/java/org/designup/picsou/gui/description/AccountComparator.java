package org.designup.picsou.gui.description;

import static org.designup.picsou.model.Account.*;
import org.designup.picsou.model.AccountCardType;
import org.globsframework.model.Glob;
import org.globsframework.utils.Utils;

import java.util.Comparator;

public class AccountComparator implements Comparator<Glob> {

  public int compare(Glob account1, Glob account2) {
    if (account1 == null && account2 == null){
      return 0;
    }
    if (account1 == null){
      return -1;
    }
    if (account2 == null){
      return 1;
    }
    if (SUMMARY_ACCOUNT_IDS.contains(account1.get(ID)) &&
        SUMMARY_ACCOUNT_IDS.contains(account2.get(ID))) {
      return account1.get(ID) - account2.get(ID);
    }
    if (SUMMARY_ACCOUNT_IDS.contains(account1.get(ID))) {
      return -1;
    }
    if (SUMMARY_ACCOUNT_IDS.contains(account2.get(ID))) {
      return 1;
    }
    int diff = account1.get(CARD_TYPE).compareTo(account2.get(CARD_TYPE));
    if (diff != 0){
      return diff;
    }
    int compare = Utils.compareIgnoreCase(account1.get(NAME), account2.get(NAME));
    if (compare == 0){
      return account2.get(ID) - account1.get(ID);
    }
    return compare;
  }
}
