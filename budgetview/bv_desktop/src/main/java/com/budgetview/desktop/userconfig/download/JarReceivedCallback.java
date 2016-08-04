package com.budgetview.desktop.userconfig.download;

import com.budgetview.client.DataAccess;
import com.budgetview.model.AppVersionInformation;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.io.File;

public class JarReceivedCallback extends AbstractCompletionCallback {

  private DataAccess dataAccess;

  public JarReceivedCallback(Directory directory, GlobRepository repository, DataAccess dataAccess) {
    this.dataAccess = dataAccess;
    set(directory, repository);
  }

  synchronized public void complete(File jarFile, long version) {
    dataAccess.downloadedVersion(version);
    super.complete(jarFile, version);
  }

  protected void loadJar(File jarFile, long version) {
    repository.update(AppVersionInformation.KEY, AppVersionInformation.LATEST_AVALAIBLE_JAR_VERSION, version);
  }
}
