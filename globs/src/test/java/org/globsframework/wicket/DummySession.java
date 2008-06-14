package org.globsframework.wicket;

import org.globsframework.model.GlobRepository;

public class DummySession extends GlobSession {
  public DummySession(GlobApplication application) {
    super(application);
  }


  public GlobRepository getRepository() {
    return null;  // Todo
  }
}
