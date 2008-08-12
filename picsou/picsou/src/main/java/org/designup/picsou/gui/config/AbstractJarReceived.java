package org.designup.picsou.gui.config;

import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.io.File;

public abstract class AbstractJarReceived implements DownloadThread.Completed {
  protected Directory directory;
  protected GlobRepository repository;
  protected File jarFile;
  private long version;

  synchronized public boolean set(Directory directory, GlobRepository repository) {
    this.directory = directory;
    this.repository = repository;
    if (jarFile != null) {
      loadJar(jarFile, version);
      return true;
    }
    return false;
  }

  protected abstract void loadJar(File jarFile, long version);

  synchronized public void complete(final File jarFile, final long version) {
    if (directory != null) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          loadJar(jarFile, version);
        }
      });
    }
    else {
      this.jarFile = jarFile;
      this.version = version;
    }
  }
}
