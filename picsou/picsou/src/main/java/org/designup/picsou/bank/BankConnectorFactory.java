package org.designup.picsou.bank;

import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public interface BankConnectorFactory {
  BankConnector create(GlobRepository repository, Directory directory);
}