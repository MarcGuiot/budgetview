package org.globsframework.wicket;

import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

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


  public Session newSession(Request request, Response response) {
    return new GlobSession(request, response, this);
//    return super.newSession(request, response);
  }

//  protected ISessionFactory getSessionFactory() {
//    return new ISessionFactory() {
//      public Session newSession() {
//        return new GlobSession(GlobApplication.this);
//      }
//    };
//  }
}
