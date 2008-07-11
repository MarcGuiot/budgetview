package org.designup.picsou.gui.license;

import org.designup.picsou.model.UserPreferences;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

public class LicenseService {
  boolean isPro = true;

  public boolean isPro() {
    return isPro;
  }

  public void register(GlobRepository repository, Directory directory) {
    isPro = true;
    repository.update(Key.create(UserPreferences.TYPE, UserPreferences.SINGLETON_ID),
                      UserPreferences.FUTURE_MONTH_COUNT, 24);
  }
}
