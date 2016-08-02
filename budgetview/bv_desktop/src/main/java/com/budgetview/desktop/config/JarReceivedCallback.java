package com.budgetview.desktop.config;

import com.budgetview.client.ServerAccess;
import com.budgetview.desktop.config.download.AbstractCompletionCallback;
import com.budgetview.model.AppVersionInformation;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.io.File;

public class JarReceivedCallback extends AbstractCompletionCallback {

  private ServerAccess serverAccess;

  public JarReceivedCallback(Directory directory, GlobRepository repository, ServerAccess serverAccess) {
    this.serverAccess = serverAccess;
    set(directory, repository);
  }

  synchronized public void complete(File jarFile, long version) {
    serverAccess.downloadedVersion(version);
    super.complete(jarFile, version);
  }

  protected void loadJar(File jarFile, long version) {
    repository.update(AppVersionInformation.KEY, AppVersionInformation.LATEST_AVALAIBLE_JAR_VERSION, version);
  }
}
