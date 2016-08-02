package com.budgetview.desktop.config.download;

import com.budgetview.desktop.config.ConfigService;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.io.File;

public class ConfigReceivedCallback extends AbstractCompletionCallback {

  private ConfigService configService;

  // directory/repository can be null
  public ConfigReceivedCallback(ConfigService configService, Directory directory, GlobRepository repository) {
    this.configService = configService;
    set(directory, repository);
  }

  protected void loadJar(File jarFile, long version) {
    configService.loadConfigFile(jarFile, version, repository, directory);
  }
}
