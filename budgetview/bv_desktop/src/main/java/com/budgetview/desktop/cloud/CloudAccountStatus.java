package com.budgetview.desktop.cloud;

import com.budgetview.model.RealAccount;
import com.budgetview.shared.cloud.CloudSubscriptionStatus;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.utils.Log;
import org.globsframework.utils.collections.Pair;
import org.globsframework.utils.directory.Directory;

import java.util.ArrayList;
import java.util.List;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.*;

public class CloudAccountStatus {
  public static void processDeletion(Key accountKey, GlobRepository repository, Directory directory) {

    System.out.println("CloudAccountStatus.processDeletion");
    GlobPrinter.print(repository, RealAccount.TYPE);

    Integer providerAccountId = null;
    for (Glob realAccount : repository.getAll(RealAccount.TYPE,
                                              and(linkedTo(accountKey, RealAccount.ACCOUNT),
                                                  isNotNull(RealAccount.PROVIDER_ACCOUNT_ID)))) {
      if (realAccount.isTrue(RealAccount.ENABLED)) {
        providerAccountId = realAccount.get(RealAccount.PROVIDER_ACCOUNT_ID);
      }
      repository.update(realAccount,
                        value(RealAccount.ACCOUNT, null),
                        value(RealAccount.ENABLED, false));
    }
    final List<Pair<Integer, Boolean>> updates = new ArrayList<Pair<Integer, Boolean>>();
    if (providerAccountId != null) {
      System.out.println("CloudAccountStatus.processDeletion: updating " + providerAccountId);
      updates.add(new Pair<Integer, Boolean>(providerAccountId, false));
      directory.get(CloudService.class).updateAccounts(updates, repository, new CloudService.Callback() {
        public void processCompletion() {
        }

        public void processSubscriptionError(CloudSubscriptionStatus status) {
        }

        public void processError(Exception e) {
          Log.write("Failed to update account status", e);
        }
      });
    }
    else {
      System.out.println("CloudAccountStatus.processDeletion: no cloud account");
    }
  }
}
