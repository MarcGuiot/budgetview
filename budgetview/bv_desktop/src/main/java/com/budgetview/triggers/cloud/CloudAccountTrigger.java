package com.budgetview.triggers.cloud;

import com.budgetview.model.CloudProviderAccount;
import com.budgetview.model.CloudProviderConnection;
import com.budgetview.model.RealAccount;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetVisitor;

import java.util.Set;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.and;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class CloudAccountTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {

    if (changeSet.containsDeletions(CloudProviderConnection.TYPE)) {
      changeSet.safeVisit(CloudProviderConnection.TYPE, new DefaultChangeSetVisitor() {
        public void visitDeletion(Key key, FieldValues values) throws Exception {
          Integer connectionId = values.get(CloudProviderConnection.PROVIDER_CONNECTION_ID);
          repository.updateAll(RealAccount.TYPE,
                               fieldEquals(RealAccount.PROVIDER_CONNECTION_ID, connectionId),
                               value(RealAccount.PROVIDER_CONNECTION_ID, null),
                               value(RealAccount.PROVIDER_ACCOUNT_ID, null));
        }
      });
    }

    if (changeSet.containsDeletions(CloudProviderAccount.TYPE)) {
      changeSet.safeVisit(CloudProviderAccount.TYPE, new DefaultChangeSetVisitor() {
        public void visitDeletion(Key key, FieldValues values) throws Exception {
          Integer connectionId = values.get(CloudProviderAccount.PROVIDER_CONNECTION_ID);
          Integer accountId = values.get(CloudProviderAccount.PROVIDER_ACCOUNT_ID);
          repository.updateAll(RealAccount.TYPE,
                               and(fieldEquals(RealAccount.PROVIDER_CONNECTION_ID, connectionId),
                                   fieldEquals(RealAccount.PROVIDER_ACCOUNT_ID, accountId)),
                               value(RealAccount.PROVIDER_CONNECTION_ID, null),
                               value(RealAccount.PROVIDER_ACCOUNT_ID, null));
        }
      });
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(CloudProviderConnection.TYPE) || changedTypes.contains(CloudProviderAccount.TYPE) || changedTypes.contains(RealAccount.TYPE)) {

      for (Glob realAccount : repository.getAll(RealAccount.TYPE)) {
        Integer connectionId = realAccount.get(RealAccount.PROVIDER_CONNECTION_ID);
        Integer accountId = realAccount.get(RealAccount.PROVIDER_ACCOUNT_ID);

        if (accountId != null && connectionId == null) {
          repository.update(realAccount,
                            value(RealAccount.PROVIDER_CONNECTION_ID, null),
                            value(RealAccount.PROVIDER_ACCOUNT_ID, null));
          continue;
        }

        if (accountId != null &&
            !repository.contains(CloudProviderAccount.TYPE,
                                 and(fieldEquals(CloudProviderAccount.PROVIDER_CONNECTION_ID, connectionId),
                                     fieldEquals(CloudProviderAccount.PROVIDER_ACCOUNT_ID, accountId)))) {
          repository.update(realAccount,
                            value(RealAccount.PROVIDER_CONNECTION_ID, null),
                            value(RealAccount.PROVIDER_ACCOUNT_ID, null));
          continue;

        }

        if (connectionId != null &&
            !repository.contains(CloudProviderConnection.TYPE, fieldEquals(CloudProviderConnection.PROVIDER_CONNECTION_ID, connectionId))) {
          repository.update(realAccount,
                            value(RealAccount.PROVIDER_CONNECTION_ID, null),
                            value(RealAccount.PROVIDER_ACCOUNT_ID, null));
        }
      }
    }
  }
}
