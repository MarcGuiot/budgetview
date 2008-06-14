package org.globsframework.wicket.model;

import org.globsframework.metamodel.Field;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.wicket.GlobRepositoryLoader;
import wicket.Component;
import wicket.model.AbstractModel;

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
