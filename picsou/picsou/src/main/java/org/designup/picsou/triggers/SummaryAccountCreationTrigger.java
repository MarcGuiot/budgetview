package org.designup.picsou.triggers;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.*;
import static org.crossbowlabs.globs.model.FieldValue.value;
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

  public static void updateSummary(GlobRepository repository) {
    GlobList accounts = repository.getAll(TYPE);

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

    Glob account = repository.findOrCreate(KeyBuilder.newKey(Account.TYPE, SUMMARY_ACCOUNT_ID),
                                           value(NUMBER, SUMMARY_ACCOUNT_NUMBER),
                                           value(NAME, Lang.get("account.all")));
    Key summaryKey = account.getKey();
    repository.update(summaryKey, BALANCE, balance);
    repository.update(summaryKey, UPDATE_DATE, updateDate);
  }
}
