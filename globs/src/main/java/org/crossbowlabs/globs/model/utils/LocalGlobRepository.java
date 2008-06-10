package org.crossbowlabs.globs.model.utils;

import org.crossbowlabs.globs.model.ChangeSet;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.utils.exceptions.InvalidState;

public class LocalGlobRepository extends GlobRepositoryDecorator {

  private GlobRepository reference;
  private ChangeSetAggregator aggregator;

  LocalGlobRepository(GlobRepository reference, GlobRepository temporary) {
    super(temporary);
    this.reference = reference;
    this.aggregator = new ChangeSetAggregator(temporary);
  }

  public ChangeSet getCurrentChanges() {
    return aggregator.getCurrentChanges();
  }

  public void commitChanges(boolean dispose) {
    reference.apply(aggregator.getCurrentChanges());
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
