package org.crossbowlabs.globs.model.utils;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.impl.DefaultGlobRepository;

public class LocalGlobRepositoryBuilder {
  private GlobRepository reference;
  private DefaultGlobRepository temporary;

  public static LocalGlobRepositoryBuilder init(GlobRepository reference) {
    return new LocalGlobRepositoryBuilder(reference);
  }

  private LocalGlobRepositoryBuilder(GlobRepository reference) {
    this.reference = reference;
    this.temporary = new DefaultGlobRepository(reference.getIdGenerator());
  }

  public LocalGlobRepositoryBuilder copy(GlobType... types) {
    for (GlobType type : types) {
      copy(reference.getAll(type));
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
    temporary.add(glob.duplicate());
  }

  public LocalGlobRepository get() {
    return new LocalGlobRepository(reference, temporary);
  }
}
