package org.globsframework.wicket.model;

import org.apache.wicket.Session;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.wicket.GlobRepositoryLoader;
import org.globsframework.wicket.GlobSession;

public abstract class AbstractGlobStringifierModel extends AbstractReadOnlyModel {
  protected final Key key;
  protected final GlobRepositoryLoader repositoryLoader;
  protected transient GlobStringifier stringifier;

  public AbstractGlobStringifierModel(Key key, GlobRepositoryLoader repositoryLoader) {
    this.key = key;
    this.repositoryLoader = repositoryLoader;
  }

  public Object getObject() {
    GlobRepository repository = repositoryLoader.getRepository();
    Glob glob = repository.get(key);
    return getStringifier().toString(glob, repository);
  }

  protected GlobStringifier getStringifier() {
    if (stringifier == null) {
      GlobSession session = (GlobSession)Session.get();
      DescriptionService descriptionService = session.getDescriptionService();
      this.stringifier = createStringifier(descriptionService);
    }
    return stringifier;
  }

  protected abstract GlobStringifier createStringifier(DescriptionService descriptionService);

  public void detach() {
    this.stringifier = null;
  }
}
