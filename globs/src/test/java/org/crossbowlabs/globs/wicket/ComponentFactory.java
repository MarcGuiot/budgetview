package org.crossbowlabs.globs.wicket;

import wicket.Component;

public interface ComponentFactory {
  public Component create(String componentId, GlobRepositoryLoader repositoryLoader);
}
