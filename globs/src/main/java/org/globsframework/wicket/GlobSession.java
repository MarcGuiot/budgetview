package org.globsframework.wicket;

import org.globsframework.model.format.DescriptionService;
import org.globsframework.utils.directory.Directory;
import wicket.protocol.http.WebSession;

public class GlobSession extends WebSession {

  public GlobSession(GlobApplication application) {
    super(application);
  }

  public Directory getDirectory() {
    return getGlobApplication().getDirectory();
  }

  public GlobApplication getGlobApplication() {
    return ((GlobApplication)getApplication());
  }

  public DescriptionService getDescriptionService() {
    return getDirectory().get(DescriptionService.class);
  }
}
