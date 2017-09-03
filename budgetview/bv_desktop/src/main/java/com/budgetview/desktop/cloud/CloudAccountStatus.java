package com.budgetview.desktop.cloud;

import com.budgetview.desktop.undo.UndoRedoService;
import com.budgetview.model.RealAccount;
import com.budgetview.shared.cloud.CloudSubscriptionStatus;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.Functor;
import org.globsframework.utils.Log;
import org.globsframework.utils.collections.Pair;
import org.globsframework.utils.directory.Directory;
import org.prevayler.foundation.UnexpectedException;

import java.util.ArrayList;
import java.util.List;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.*;

public class CloudAccountStatus {

  public static void processDeletion(Key accountKey, GlobRepository localRepository, GlobRepository targetRepository, Directory directory, Functor functor) {

    System.out.println("CloudAccountStatus.processDeletion");

    try {

      targetRepository.startChangeSet();

      Integer providerAccountId = null;
      for (Glob realAccount : targetRepository.getAll(RealAccount.TYPE,
                                                      and(linkedTo(accountKey, RealAccount.ACCOUNT),
                                                          isNotNull(RealAccount.PROVIDER_ACCOUNT_ID)))) {
        if (realAccount.isTrue(RealAccount.ENABLED)) {
          providerAccountId = realAccount.get(RealAccount.PROVIDER_ACCOUNT_ID);
        }
        localRepository.update(realAccount,
                               value(RealAccount.ACCOUNT, null),
                               value(RealAccount.ENABLED, false));
      }
      if (providerAccountId == null) {
        return;
      }

      functor.run();

      UndoRedoService.Change change = new CloudAccountChange(providerAccountId, directory, targetRepository);
      change.apply();

      targetRepository.completeChangeSet();

      directory.get(UndoRedoService.class).appendToNextUndo(change);
      System.out.println("CloudAccountStatus.Functor.complete - added redo modifier");

    }
    catch (Exception e) {
      throw new UnexpectedException(e);
    }
  }

  private static class CloudAccountChange implements UndoRedoService.Change {
    private final Integer providerAccountId;
    private final Directory directory;
    private final GlobRepository globalRepository;

    public CloudAccountChange(Integer providerAccountId, Directory directory, GlobRepository globalRepository) {
      this.providerAccountId = providerAccountId;
      this.directory = directory;
      this.globalRepository = globalRepository;
    }

    public void apply() {
      System.out.println("CloudAccountStatus.CloudAccountChange.apply");
      send(false);
    }

    public void revert() {
      System.out.println("CloudAccountStatus.CloudAccountChange.revert");
      send(true);
    }

    private void send(boolean enabled) {
      final List<Pair<Integer, Boolean>> updates = new ArrayList<Pair<Integer, Boolean>>();
      System.out.println("CloudAccountStatus.CloudAccountChange.send: updating " + providerAccountId);
      updates.add(new Pair<Integer, Boolean>(providerAccountId, enabled));
      directory.get(CloudService.class).updateAccounts(updates, globalRepository, new CloudService.Callback() {
        public void processCompletion() {
        }

        public void processSubscriptionError(CloudSubscriptionStatus status) {
        }

        public void processError(Exception e) {
          Log.write("Failed to update account status", e);
        }
      });
    }

    public String toString() {
      return "Change cloud provider account " + providerAccountId;
    }
  }
}
