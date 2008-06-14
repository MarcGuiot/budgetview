package org.designup.picsou.client;

import org.designup.picsou.model.TransactionTypeMatcher;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.impl.DefaultGlobIdGenerator;
import org.globsframework.model.utils.GlobIdGenerator;

public class ServerAccessGlobIdGenerator implements GlobIdGenerator {
  private final ServerAccess serverAccess;
  private DefaultGlobIdGenerator transientIdGenerator;

  public ServerAccessGlobIdGenerator(ServerAccess serverAccess) {
    this.serverAccess = serverAccess;
  }

  public int getNextId(IntegerField keyField, int idCount) {
    GlobType globType = keyField.getGlobType();
    if (globType.equals(TransactionTypeMatcher.TYPE)) {
      return transientIdGenerator.getNextId(keyField, idCount);
    }
    return serverAccess.getNextId(keyField.getGlobType().getName(), idCount);
  }

  public void set(GlobRepository repository) {
    transientIdGenerator = new DefaultGlobIdGenerator();
    transientIdGenerator.setRepository(repository);
  }
}
