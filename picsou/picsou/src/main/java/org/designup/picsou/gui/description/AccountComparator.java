package org.designup.picsou.gui.description;

import org.designup.picsou.gui.time.TimeService;
import static org.designup.picsou.model.Account.*;

import org.globsframework.model.Glob;
import org.globsframework.utils.Utils;

import java.util.Comparator;
import java.util.Date;

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
    int diffClosed = diffClosedDate(account1, account2);
    if (diffClosed != 0) {
      return diffClosed;
    }
    int diff = Utils.compare(account1.get(CARD_TYPE), account2.get(CARD_TYPE));
    if (diff != 0) {
      return diff;
    }
    int compare = Utils.compareIgnoreCase(account1.get(NAME), account2.get(NAME));
    if (compare == 0) {
      return account2.get(ID) - account1.get(ID);
    }
    return compare;
  }

  private int diffClosedDate(Glob account1, Glob account2) {
    Date closedDate1 = account1.get(CLOSED_DATE);
    boolean before1 = false;
    if (closedDate1 != null) {
      if (TimeService.getToday().after(closedDate1)) {
        before1 = true;
      }
    }
    Date closedDate2 = account2.get(CLOSED_DATE);
    boolean before2 = false;
    if (closedDate2 != null) {
      if (TimeService.getToday().after(closedDate2)) {
        before2 = true;
      }
    }
    if (before1 == before2) {
      return 0;
    }
    if (before1) {
      return 1;
    }
    return -1;
  }
}
