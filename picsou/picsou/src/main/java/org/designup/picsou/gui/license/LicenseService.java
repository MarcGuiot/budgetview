package org.designup.picsou.gui.license;

import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.PremiumEvolutionState;
import org.designup.picsou.model.User;
import org.designup.picsou.model.UserPreferences;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Utils;

import java.util.Calendar;
import java.util.Date;

public class LicenseService {

  public static final int TRIAL_DURATION = 46;
  public static final int TRIAL_SHOWN_DURATION = 20;

  public static void activateTrial(GlobRepository repository) {
    repository.update(User.KEY, User.PREMIUM_EVOLUTION_STATE, PremiumEvolutionState.TRIAL_IN_PROGRESS.getId());
  }

  public static Date getEndOfTrialPeriod() {
    return addTrialPeriod(TimeService.getToday());
  }

  public static Date addTrialPeriod(Date date) {
    return Month.addDays(date, TRIAL_DURATION);
  }
}
