package org.crossbowlabs.globs.wicket.model;

import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.model.format.GlobStringifier;
import org.crossbowlabs.globs.wicket.GlobRepositoryLoader;

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
