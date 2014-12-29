package org.designup.picsou.gui.feedback;

import org.designup.picsou.gui.accounts.utils.AccountMatchers;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.model.ProjectStat;
import org.designup.picsou.gui.projects.utils.CurrentProjectsMatcher;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.TreeSet;

public class UserProgressInfoSender {

  public static void send(GlobRepository repository, Directory directory) {
    Glob userPrefs = repository.find(UserPreferences.KEY);
    if (userPrefs == null){
      return;
    }
    int exitCount = userPrefs.get(UserPreferences.EXIT_COUNT, 0);
    if (!repository.get(User.KEY).isTrue(User.CONNECTED) || exitCount >= 3) {
      return;
    }
    try {
      directory.get(ConfigService.class).sendUsageData(getMessage(repository, exitCount));
    }
    catch (Exception exception) {
      // Ignore
    }
  }

  public static String getMessage(GlobRepository repository, int exitCount) {
    StringBuilder builder = new StringBuilder();
    builder.append("uses:").append(exitCount)
      .append(" - lang: " + Lang.getLang())
      .append(" - java: " + System.getProperty("java.version"))
      .append(" - system: " + System.getProperty("os.name") + " " + System.getProperty("os.version"))
      .append(" - initialStepsCompleted: ").append(SignpostSectionType.isAllCompleted(repository))
      .append(" - mainAccounts: ").append(getActiveMainAccountsCount(repository))
      .append(" - projects: " + getCurrentProjectsCount(repository));
    return builder.toString();
  }

  public static int getActiveMainAccountsCount(GlobRepository repository) {
    TreeSet<Integer> monthIds = new TreeSet<Integer>();
    monthIds.add(CurrentMonth.getCurrentMonth(repository));
    return repository.getAll(Account.TYPE, AccountMatchers.activeUserCreatedMainAccounts(monthIds)).size();
  }

  private static int getCurrentProjectsCount(GlobRepository repository) {
    return repository.getAll(ProjectStat.TYPE, new CurrentProjectsMatcher()).size();
  }
}
