package org.crossbowlabs.globs.wicket.model;

import org.crossbowlabs.globs.metamodel.Link;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.model.format.GlobStringifier;
import org.crossbowlabs.globs.wicket.GlobRepositoryLoader;

public class GlobLinkStringifierModel extends AbstractGlobStringifierModel {

  private final Link link;

  public GlobLinkStringifierModel(Key key, Link link, GlobRepositoryLoader repositoryLoader) {
    super(key, repositoryLoader);
    this.link = link;
  }

  protected GlobStringifier createStringifier(DescriptionService descriptionService) {
    return descriptionService.getStringifier(link);
  }
}
