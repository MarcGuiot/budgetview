package org.designup.picsou.bank;

import org.globsframework.model.GlobRepository;
import org.globsframework.model.ReadOnlyGlobRepository;
import org.globsframework.model.Glob;
import org.globsframework.model.delta.MutableChangeSet;

public interface BankPlugin {
  void apply(Glob newAccount, ReadOnlyGlobRepository referenceRepository,
             GlobRepository localRepository, MutableChangeSet changeSet);

  int getVersion();
}
