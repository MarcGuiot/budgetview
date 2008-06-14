package org.globsframework.wicket.model;

import org.globsframework.metamodel.Link;
import org.globsframework.model.Key;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.wicket.GlobRepositoryLoader;

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
