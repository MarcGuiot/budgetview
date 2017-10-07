package com.budgetview.triggers.cloud;

import com.budgetview.model.CloudProviderAccount;
import com.budgetview.model.CloudProviderConnection;
import com.budgetview.model.RealAccount;
import com.budgetview.model.Transaction;
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
          repository.updateAll(Transaction.TYPE,
                               fieldEquals(Transaction.PROVIDER_CONNECTION_ID, connectionId),
                               value(Transaction.PROVIDER_CONNECTION_ID, null),
                               value(Transaction.PROVIDER_ACCOUNT_ID, null),
                               value(Transaction.PROVIDER_TRANSACTION_ID, null));
        }
      });
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(CloudProviderConnection.TYPE) || changedTypes.contains(CloudProviderAccount.TYPE) || changedTypes.contains(RealAccount.TYPE)) {

      for (Glob realAccount : repository.getAll(RealAccount.TYPE)) {
        Integer connectionId = realAccount.get(RealAccount.PROVIDER_CONNECTION_ID);
        if (connectionId == null ||
            !repository.contains(CloudProviderConnection.TYPE, fieldEquals(CloudProviderConnection.PROVIDER_CONNECTION_ID, connectionId))) {
          repository.update(realAccount,
                            value(RealAccount.PROVIDER_CONNECTION_ID, null),
                            value(RealAccount.PROVIDER_ACCOUNT_ID, null));
        }
      }
    }

    for (Glob transaction : repository.getAll(Transaction.TYPE)) {
      Integer connectionId = transaction.get(Transaction.PROVIDER_CONNECTION_ID);
      if (connectionId == null ||
          !repository.contains(CloudProviderAccount.TYPE,
                               and(fieldEquals(CloudProviderAccount.PROVIDER_CONNECTION_ID, connectionId)))) {
        repository.update(transaction,
                          value(Transaction.PROVIDER_CONNECTION_ID, null),
                          value(Transaction.PROVIDER_ACCOUNT_ID, null),
                          value(Transaction.PROVIDER_TRANSACTION_ID, null));
      }
    }
  }
}
