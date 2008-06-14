package org.globsframework.wicket.model;

import org.globsframework.model.Key;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.wicket.GlobRepositoryLoader;

/**
 *
 */
public class GlobStringifierModel extends AbstractGlobStringifierModel {

  public GlobStringifierModel(Key key, GlobRepositoryLoader repositoryLoader) {
    super(key, repositoryLoader);
  }

  protected GlobStringifier createStringifier(DescriptionService descriptionService) {
    return descriptionService.getStringifier(key.getGlobType());
  }
}
