package org.crossbowlabs.globs.wicket.model;

import wicket.model.AbstractReadOnlyModel;
import wicket.Component;
import wicket.Session;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.model.format.GlobStringifier;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.wicket.GlobRepositoryLoader;
import org.crossbowlabs.globs.wicket.GlobSession;

public abstract class AbstractGlobStringifierModel extends AbstractReadOnlyModel {
  protected final Key key;
  protected final GlobRepositoryLoader repositoryLoader;
  protected transient GlobStringifier stringifier;

  public AbstractGlobStringifierModel(Key key, GlobRepositoryLoader repositoryLoader) {
    this.key = key;
    this.repositoryLoader = repositoryLoader;
  }

  public Object getObject(final Component component) {
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
