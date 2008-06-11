package org.crossbowlabs.globs.model.utils;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.ChangeSet;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.utils.exceptions.InvalidState;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LocalGlobRepository extends GlobRepositoryDecorator {

  private GlobRepository reference;
  private List<GlobType> globTypes;
  private GlobList globs;
  private ChangeSetAggregator aggregator;

  LocalGlobRepository(GlobRepository reference, GlobRepository temporary,
                      List<GlobType> globTypes, GlobList globs) {
    super(temporary);
    this.reference = reference;
    this.globTypes = globTypes;
    this.globs = globs;
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

  public void rollback() {
    aggregator.dispose();
    GlobList list = reference.getAll(globTypes.toArray(new GlobType[globTypes.size()]));
    list.addAll(globs);
    Set<GlobType> globTypes = new HashSet<GlobType>(this.globTypes);
    globTypes.addAll(list.getTypes());
    getRepository().reset(list, globTypes.toArray(new GlobType[globTypes.size()]));
    aggregator = new ChangeSetAggregator(getRepository());
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
