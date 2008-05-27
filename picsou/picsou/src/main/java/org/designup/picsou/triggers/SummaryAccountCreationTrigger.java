package org.designup.picsou.triggers;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.*;
import org.crossbowlabs.globs.utils.Utils;
import org.designup.picsou.model.Account;
import static org.designup.picsou.model.Account.*;
import org.designup.picsou.utils.Lang;

import java.util.Date;
import java.util.List;

public class SummaryAccountCreationTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, GlobRepository globRepository) {
    if (!changeSet.containsChanges(Account.TYPE)) {
      return;
    }
    updateSummary(globRepository);
  }

  public void globsReset(GlobRepository globRepository, List<GlobType> changedTypes) {
    updateSummary(globRepository);
  }

  public static void updateSummary(GlobRepository globRepository) {
    GlobList accounts = globRepository.getAll(TYPE);

    double balance = 0;
    Date updateDate = null;
    for (Glob account : accounts) {
      if (account.get(ID).equals(SUMMARY_ACCOUNT_ID)) {
        continue;
      }
      Double accountBalance = account.get(BALANCE);
      balance += (accountBalance != null) ? accountBalance : 0;
      updateDate = Utils.min(updateDate, account.get(UPDATE_DATE));
    }

    Glob account = globRepository.findOrCreate(KeyBuilder.newKey(TYPE, SUMMARY_ACCOUNT_ID),
                                                 FieldValue.value(NUMBER, SUMMARY_ACCOUNT_NUMBER),
                                                 FieldValue.value(NAME, Lang.get("account.all")));
    Key summaryKey = account.getKey();
    globRepository.update(summaryKey, BALANCE, balance);
    globRepository.update(summaryKey, UPDATE_DATE, updateDate);
  }
}
