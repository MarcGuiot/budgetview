package org.crossbowlabs.globs.model.utils;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.impl.DefaultGlobRepository;

public class LocalGlobRepositoryBuilder {
  private GlobRepository source;
  private DefaultGlobRepository local;

  public static LocalGlobRepositoryBuilder init(GlobRepository source) {
    return new LocalGlobRepositoryBuilder(source);
  }

  private LocalGlobRepositoryBuilder(GlobRepository source) {
    this.source = source;
    this.local = new DefaultGlobRepository(source.getIdGenerator());
  }

  public LocalGlobRepositoryBuilder copy(GlobType... types) {
    for (GlobType type : types) {
      copy(source.getAll(type));
    }
    return this;
  }

  public LocalGlobRepositoryBuilder copy(GlobList list) {
    for (Glob glob : list) {
      doCopy(glob);
    }
    return this;
  }

  public LocalGlobRepositoryBuilder copy(Glob... globs) {
    for (Glob glob : globs) {
      doCopy(glob);
    }
    return this;
  }

  private final void doCopy(Glob glob) {
    local.add(GlobBuilder.copy(glob));
  }

  public LocalGlobRepository get() {
    return new LocalGlobRepository(source, local);
  }
}
