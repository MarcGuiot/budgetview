package org.crossbowlabs.globs.model;

import org.crossbowlabs.globs.model.impl.DefaultGlobIdGenerator;
import org.crossbowlabs.globs.model.impl.DefaultGlobRepository;
import org.crossbowlabs.globs.model.utils.GlobConstantContainer;
import org.crossbowlabs.globs.model.utils.GlobIdGenerator;

public class GlobRepositoryBuilder {
  private GlobList globList = new GlobList();
  private final GlobIdGenerator idGenerator;

  public static GlobRepository createEmpty() {
    return init().get();
  }

  public static GlobRepositoryBuilder init() {
    return new GlobRepositoryBuilder(new DefaultGlobIdGenerator());
  }

  public static GlobRepositoryBuilder init(GlobIdGenerator idGenerator) {
    return new GlobRepositoryBuilder(idGenerator);
  }

  private GlobRepositoryBuilder(GlobIdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  public GlobRepositoryBuilder add(Glob... globs) {
    globList.addAll(globs);
    return this;
  }

  public GlobRepositoryBuilder add(GlobList globs) {
    globList.addAll(globs);
    return this;
  }

  public GlobRepositoryBuilder add(GlobConstantContainer[] constants) {
    for (GlobConstantContainer container : constants) {
      globList.add(container.getGlob());
    }
    return this;
  }

  public GlobRepository get() {
    DefaultGlobRepository repository = new DefaultGlobRepository(idGenerator);
    repository.add(globList);
    if (idGenerator instanceof DefaultGlobIdGenerator) {
      ((DefaultGlobIdGenerator) idGenerator).setRepository(repository);
    }
    return repository;
  }
}
