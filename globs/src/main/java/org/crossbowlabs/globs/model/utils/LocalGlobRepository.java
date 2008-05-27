package org.crossbowlabs.globs.model.utils;

import org.crossbowlabs.globs.model.ChangeSet;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.utils.exceptions.InvalidState;

public class LocalGlobRepository extends GlobRepositoryDecorator {

  private GlobRepository source;
  private ChangeSetAggregator aggregator;

  LocalGlobRepository(GlobRepository source, GlobRepository local) {
    super(local);
    this.source = source;
    this.aggregator = new ChangeSetAggregator(getRepository());
  }

  public ChangeSet getCurrentChanges() {
    return aggregator.getCurrentChanges();
  }

  public void commitChanges(boolean dispose) {
    source.apply(aggregator.getCurrentChanges());
    aggregator.reset();
    if (dispose) {
      dispose();
    }
  }

  public void dispose() {
    setRepository(null);
    aggregator.dispose();
  }

  protected GlobRepository getRepository() {
    GlobRepository repository = super.getRepository();
    if (repository == null) {
      throw new InvalidState("repository is disabled");
    }
    return repository;
  }

}
