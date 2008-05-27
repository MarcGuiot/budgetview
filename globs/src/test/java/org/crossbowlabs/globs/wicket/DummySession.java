package org.crossbowlabs.globs.wicket;
import org.crossbowlabs.globs.model.GlobRepository;

public class DummySession extends GlobSession {
  public DummySession(GlobApplication application) {
    super(application);
  }


    public GlobRepository getRepository() {
        return null;  // Todo
    }
}
