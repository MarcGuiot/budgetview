package org.designup.picsou.triggers;

import org.designup.picsou.model.Account;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.DefaultChangeSetVisitor;

import static org.globsframework.model.FieldValue.value;

public class AccountGraphTrigger extends DefaultChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (changeSet.containsChanges(Account.TYPE)) {
      changeSet.safeVisit(Account.TYPE, new DefaultChangeSetVisitor() {
        public void visitCreation(Key key, FieldValues values) throws Exception {
          repository.update(key, value(Account.SHOW_CHART, Account.isMain(repository.get(key))));
        }

        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
          if (values.contains(Account.ACCOUNT_TYPE)) {
            repository.update(key, value(Account.SHOW_CHART, Account.isMain(values)));
          }
        }
      });
    }
  }


}
