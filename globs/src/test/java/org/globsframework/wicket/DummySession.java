package org.globsframework.wicket;

import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.globsframework.model.GlobRepository;

public class DummySession extends GlobSession {
  public DummySession(Request request, Response response, GlobApplication application) {
    super(request, response, application);
  }


  public GlobRepository getRepository() {
    return null;  // Todo
  }
}
