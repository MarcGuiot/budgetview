package org.globsframework.wicket;

import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.protocol.http.WebSession;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.utils.directory.Directory;

public class GlobSession extends WebSession {
  private GlobApplication application;

  public GlobSession(Request request, Response response, GlobApplication application) {
    super(request);
    this.application = application;
  }

  public Directory getDirectory() {
    return application.getDirectory();
  }

  public DescriptionService getDescriptionService() {
    return getDirectory().get(DescriptionService.class);
  }
}
