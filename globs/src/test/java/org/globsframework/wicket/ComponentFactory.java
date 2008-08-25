package org.globsframework.wicket;

import org.apache.wicket.Component;

public interface ComponentFactory {
  public Component create(String componentId, GlobRepositoryLoader repositoryLoader);
}
