package org.designup.picsou.gui.description;

import static org.designup.picsou.model.Account.*;
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
    if ((account1.isTrue(IS_CARD_ACCOUNT))
        && (!account2.isTrue(IS_CARD_ACCOUNT))) {
      return 1;
    }
    if ((!account1.isTrue(IS_CARD_ACCOUNT))
        && (account2.isTrue(IS_CARD_ACCOUNT))) {
      return -1;
    }
    return Utils.compareIgnoreCase(account1.get(NAME), account2.get(NAME));
  }
}
