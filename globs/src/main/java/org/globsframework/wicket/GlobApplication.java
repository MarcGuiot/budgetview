package org.globsframework.wicket;

import org.globsframework.model.format.DescriptionService;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;
import wicket.ISessionFactory;
import wicket.Session;
import wicket.protocol.http.WebApplication;

public abstract class GlobApplication extends WebApplication {

  protected Directory directory;

  protected void init() {
    super.init();

    directory = new DefaultDirectory();
    DescriptionService descriptionService = createDescriptionService();
    directory.add(DescriptionService.class, descriptionService);
  }

  protected abstract DescriptionService createDescriptionService();

  public Directory getDirectory() {
    return directory;
  }

  protected ISessionFactory getSessionFactory() {
    return new ISessionFactory() {
      public Session newSession() {
        return new GlobSession(GlobApplication.this);
      }
    };
  }
}
