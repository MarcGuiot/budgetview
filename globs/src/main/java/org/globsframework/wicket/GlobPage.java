package org.globsframework.wicket;

import org.apache.wicket.markup.html.WebPage;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.utils.directory.Directory;

public abstract class GlobPage extends WebPage {
  private GlobRepositoryLoader repositoryLoader;

  protected GlobPage() {
  }

  public final GlobRepository getRepository() {
    return getRepositoryLoader().getRepository();
  }

  public GlobRepositoryLoader getRepositoryLoader() {
    if (repositoryLoader == null) {
      repositoryLoader = newGlobRepositoryLoader();
    }
    return repositoryLoader;
  }

  public GlobApplication getGlobApplication() {
    return ((GlobApplication)getApplication());
  }

  protected void onDetach() {
    getRepositoryLoader().detach();
    super.onDetach();
  }

  protected abstract GlobRepositoryLoader newGlobRepositoryLoader();

  public Directory getDirectory() {
    GlobApplication application = ((GlobApplication)getApplication());
    return application.getDirectory();
  }

  public DescriptionService getDescriptionService() {
    return getDirectory().get(DescriptionService.class);
  }
}
