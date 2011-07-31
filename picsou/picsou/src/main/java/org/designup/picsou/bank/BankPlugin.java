package org.designup.picsou.bank;

import org.globsframework.model.*;
import org.globsframework.model.delta.MutableChangeSet;

public interface BankPlugin {

  boolean useCreatedAccount();

  boolean apply(Glob newAccount, ReadOnlyGlobRepository referenceRepository,
             GlobRepository localRepository, MutableChangeSet changeSet);

  void postApply(GlobList list, Glob account, GlobRepository repository, GlobRepository localRepository, ChangeSet set);

  int getVersion();
}
