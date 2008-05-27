package org.crossbowlabs.globs.wicket;

import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.utils.directory.Directory;
import wicket.markup.html.WebPage;

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
