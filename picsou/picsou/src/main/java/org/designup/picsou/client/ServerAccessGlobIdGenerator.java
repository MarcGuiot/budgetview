package org.designup.picsou.client;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.impl.DefaultGlobIdGenerator;
import org.crossbowlabs.globs.model.utils.GlobIdGenerator;
import org.designup.picsou.model.TransactionTypeMatcher;

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
