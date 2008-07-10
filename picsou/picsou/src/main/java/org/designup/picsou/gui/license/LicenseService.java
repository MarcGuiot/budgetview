package org.designup.picsou.gui.license;

import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class LicenseService {
  boolean isPro = true;

  public boolean isPro() {
    return isPro;
  }

  public void register(GlobRepository repository, Directory directory) {
    isPro = true;
  }
}
