package com.budgetview.bank;

import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public interface BankConnectorFactory {
  BankConnector create(GlobRepository repository, Directory directory, boolean syncExistingAccount, Glob synchro);
}
