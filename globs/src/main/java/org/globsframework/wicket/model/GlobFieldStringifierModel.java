package org.globsframework.wicket.model;

import org.globsframework.metamodel.Field;
import org.globsframework.model.Key;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.wicket.GlobRepositoryLoader;

public class GlobFieldStringifierModel extends AbstractGlobStringifierModel {

  private final Field field;

  public GlobFieldStringifierModel(Key key, Field field, GlobRepositoryLoader repositoryLoader) {
    super(key, repositoryLoader);
    this.field = field;
  }

  protected GlobStringifier createStringifier(DescriptionService descriptionService) {
    return descriptionService.getStringifier(field);
  }
}
