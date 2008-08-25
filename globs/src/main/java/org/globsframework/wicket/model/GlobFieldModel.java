package org.globsframework.wicket.model;

import org.apache.wicket.model.Model;
import org.globsframework.metamodel.Field;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.wicket.GlobRepositoryLoader;

/**
 *
 */
public class GlobFieldModel extends Model {

  private Key key;
  private Field field;
  private GlobRepositoryLoader repositoryLoader;

  public GlobFieldModel(Key key, Field field, GlobRepositoryLoader repositoryLoader) {
    this.key = key;
    this.field = field;
    this.repositoryLoader = repositoryLoader;
  }

  public Object getObject() {
    Glob glob = repositoryLoader.getRepository().get(key);
    return glob.getValue(field);
  }

  public void setObject(final Object object) {
    GlobRepository repository = repositoryLoader.getRepository();
    repository.update(key, field, object);
  }
}
