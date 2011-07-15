package org.designup.picsou.gui.license;

import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.model.User;
import org.designup.picsou.model.UserPreferences;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Utils;

import java.util.Date;

public class LicenseService {

  public static boolean trialInProgress(GlobRepository repository) {

    Utils.beginRemove();
    if ("true".equals(System.getProperty("license.disable"))) {
      return false;
    }
    Utils.endRemove();

    Glob user = repository.get(User.KEY);
    return !user.isTrue(User.IS_REGISTERED_USER);
  }

  public static boolean trialExpired(GlobRepository repository) {
    Utils.beginRemove();
    if ("true".equals(System.getProperty("license.disable"))) {
      return false;
    }
    Utils.endRemove();

    Glob user = repository.get(User.KEY);
    Date lastValidDay = repository.get(UserPreferences.KEY).get(UserPreferences.LAST_VALID_DAY);
    return !user.isTrue(User.IS_REGISTERED_USER) && !TimeService.getToday().before(lastValidDay);
  }
}
