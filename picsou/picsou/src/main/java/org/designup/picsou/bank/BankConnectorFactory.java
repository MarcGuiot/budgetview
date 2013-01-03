package org.designup.picsou.bank;

import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public interface BankConnectorFactory {
  BankConnector create(GlobRepository repository, Directory directory);
}
