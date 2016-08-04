package com.budgetview.desktop.userconfig.download;

import com.budgetview.desktop.userconfig.UserConfigService;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.io.File;

public class ConfigReceivedCallback extends AbstractCompletionCallback {

  private UserConfigService userConfigService;

  // directory/repository can be null
  public ConfigReceivedCallback(UserConfigService userConfigService, Directory directory, GlobRepository repository) {
    this.userConfigService = userConfigService;
    set(directory, repository);
  }

  protected void loadJar(File jarFile, long version) {
    userConfigService.loadConfigFile(jarFile, version, repository, directory);
  }
}
