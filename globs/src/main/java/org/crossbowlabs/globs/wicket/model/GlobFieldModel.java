package org.crossbowlabs.globs.wicket.model;

import wicket.model.AbstractModel;
import wicket.Component;
import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.wicket.GlobRepositoryLoader;

/**
 *
 */
public class GlobFieldModel extends AbstractModel {

  private Key key;
  private Field field;
  private GlobRepositoryLoader repositoryLoader;

  public GlobFieldModel(Key key, Field field, GlobRepositoryLoader repositoryLoader) {
    this.key = key;
    this.field = field;
    this.repositoryLoader = repositoryLoader;
  }

  public Object getObject(final Component component) {
    Glob glob = repositoryLoader.getRepository().get(key);
    return glob.getValue(field);
  }

  public void setObject(final Component component, final Object object) {
    GlobRepository repository = repositoryLoader.getRepository();
    repository.update(key, field, object);
  }
}
