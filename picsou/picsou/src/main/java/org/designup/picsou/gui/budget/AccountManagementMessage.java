package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;

import static org.globsframework.model.utils.GlobMatchers.*;

public class AccountManagementMessage {
  public static String getMessage(final Double minPosition, GlobRepository repository) {
    if (minPosition < 0) {
      return Lang.get("accountManagementMessage.inf", Formatting.toString(minPosition));
    }
    else {
      boolean hasSavingsAccount =
        repository.contains(Account.TYPE,
                            and(fieldEquals(Account.ACCOUNT_TYPE, AccountType.SAVINGS.getId()),
                                not(fieldIn(Account.ID, Account.SUMMARY_ACCOUNT_IDS))));
      if (minPosition > 0) {
        if (hasSavingsAccount) {
          return Lang.get("accountManagementMessage.sup.withSavings", Formatting.toString(minPosition));
        }
        else {
          return Lang.get("accountManagementMessage.sup.withoutSavings", Formatting.toString(minPosition));
        }
      }
      else {
        if (hasSavingsAccount) {
          return Lang.get("accountManagementMessage.zero.withSavings", Formatting.toString(minPosition));
        }
        else {
          return Lang.get("accountManagementMessage.zero.withoutSavings", Formatting.toString(minPosition));
        }
      }
    }
  }
}
