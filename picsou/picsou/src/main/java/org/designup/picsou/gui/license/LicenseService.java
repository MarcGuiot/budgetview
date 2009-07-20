package org.designup.picsou.gui.license;

import org.globsframework.model.GlobRepository;
import org.globsframework.model.Glob;
import org.designup.picsou.model.User;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.PicsouApplication;

import java.util.Date;

public class LicenseService {
  public static boolean trialExpired(GlobRepository repository) {
    Glob user = repository.get(User.KEY);
    if (PicsouApplication.DEMO_USER_NAME.equals(user.get(User.NAME))){
      return true;
    }
    Date lastValidDay = repository.get(UserPreferences.KEY).get(UserPreferences.LAST_VALID_DAY);
    return !user.get(User.IS_REGISTERED_USER) && !TimeService.getToday().before(lastValidDay);
  }
}
