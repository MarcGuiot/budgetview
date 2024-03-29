package com.budgetview.desktop.budget;

import com.budgetview.desktop.description.Formatting;
import com.budgetview.model.Account;
import com.budgetview.model.deprecated.AccountPositionThreshold;
import com.budgetview.shared.model.AccountType;
import com.budgetview.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;

public class AccountThresholdMessage {
  public static String getMessage(final Double currentPosition, GlobRepository repository) {
    Double threshold = repository.get(AccountPositionThreshold.KEY).get(AccountPositionThreshold.THRESHOLD);
    Double thresholdWarn = repository.get(AccountPositionThreshold.KEY).get(AccountPositionThreshold.THRESHOLD_FOR_WARN);
    if (currentPosition < (threshold - thresholdWarn)) {
      return Lang.get("accountThresholdMessage.inf", Formatting.toString(threshold));
    }
    else {
      boolean hasSavingsAccount =
        repository.contains(Account.TYPE,
                            GlobMatchers.and(
                              GlobMatchers.fieldEquals(Account.ACCOUNT_TYPE, AccountType.SAVINGS.getId()),
                              GlobMatchers.not(GlobMatchers.fieldIn(Account.ID, Account.SUMMARY_ACCOUNT_IDS))));
      if (currentPosition > threshold + thresholdWarn) {
        if (hasSavingsAccount) {
          return Lang.get("accountThresholdMessage.sup.withSavings", Formatting.toString(threshold));
        }
        else {
          return Lang.get("accountThresholdMessage.sup.withoutSavings", Formatting.toString(threshold));
        }
      }
      else {
        if (hasSavingsAccount) {
          return Lang.get("accountThresholdMessage.zero.withSavings", Formatting.toString(threshold));
        }
        else {
          return Lang.get("accountThresholdMessage.zero.withoutSavings", Formatting.toString(threshold));
        }
      }
    }
  }
}
