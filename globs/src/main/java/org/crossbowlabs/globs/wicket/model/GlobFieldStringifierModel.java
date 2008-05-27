package org.crossbowlabs.globs.wicket.model;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.model.format.GlobStringifier;
import org.crossbowlabs.globs.wicket.GlobRepositoryLoader;

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
