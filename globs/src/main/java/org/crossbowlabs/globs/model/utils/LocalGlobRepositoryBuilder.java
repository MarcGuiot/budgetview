package org.crossbowlabs.globs.model.utils;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.impl.DefaultGlobRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocalGlobRepositoryBuilder {
  private GlobRepository reference;
  private DefaultGlobRepository temporary;
  private List<GlobType> globTypes = new ArrayList<GlobType>();
  private GlobList globs = new GlobList();

  public static LocalGlobRepositoryBuilder init(GlobRepository reference) {
    return new LocalGlobRepositoryBuilder(reference);
  }

  private LocalGlobRepositoryBuilder(GlobRepository reference) {
    this.reference = reference;
    this.temporary = new DefaultGlobRepository(reference.getIdGenerator());
  }

  public LocalGlobRepositoryBuilder copy(GlobType... types) {
    globTypes.addAll(Arrays.asList(types));
    for (GlobType type : types) {
      for (Glob glob : reference.getAll(type)) {
        doCopy(glob);
      }
    }
    return this;
  }

  public LocalGlobRepositoryBuilder copy(GlobList list) {
    globs.addAll(list);
    for (Glob glob : list) {
      doCopy(glob);
    }
    return this;
  }

  public LocalGlobRepositoryBuilder copy(Glob... globs) {
    this.globs.addAll(Arrays.asList(globs));
    for (Glob glob : globs) {
      doCopy(glob);
    }
    return this;
  }

  private final void doCopy(Glob glob) {
    temporary.add(glob.duplicate());
  }

  public LocalGlobRepository get() {
    return new LocalGlobRepository(reference, temporary, globTypes, globs);
  }
}
