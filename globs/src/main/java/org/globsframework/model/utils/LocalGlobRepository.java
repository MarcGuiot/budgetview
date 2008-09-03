package org.globsframework.model.utils;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.exceptions.InvalidState;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LocalGlobRepository extends GlobRepositoryDecorator {

  public GlobRepository reference;
  private List<GlobType> globTypes;
  private GlobList globs;
  private ChangeSetAggregator aggregator;
  private Exception ex;

  LocalGlobRepository(GlobRepository reference, GlobRepository temporary,
                      List<GlobType> globTypes, GlobList globs) {
    super(temporary);
    ex = new Exception();
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
    globTypes.addAll(getRepository().getTypes());
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

  public void printStackTrace() {
    ex.printStackTrace();
    if (reference instanceof LocalGlobRepository) {
      ((LocalGlobRepository)reference).printStackTrace();
    }
  }
}
