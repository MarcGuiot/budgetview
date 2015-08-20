package org.designup.picsou.gui.feedback;

import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.accounts.utils.AccountMatchers;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.model.ProjectStat;
import org.designup.picsou.gui.projects.utils.CurrentProjectsMatcher;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import java.util.TreeSet;

import static org.globsframework.model.utils.GlobMatchers.isNotNull;
import static org.globsframework.model.utils.GlobMatchers.isTrue;

public class UsageDataSender {

  public static void send(GlobRepository repository, Directory directory) {
    Glob userPrefs = repository.find(UserPreferences.KEY);
    if (userPrefs == null || !repository.get(User.KEY).isTrue(User.CONNECTED)) {
      return;
    }
    int exitCount = userPrefs.get(UserPreferences.EXIT_COUNT, 0);
    if (exitCount > 3 && exitCount % 10 != 0) {
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
    Output out = new Output();
    out.add("uses", exitCount)
      .add("version", PicsouApplication.APPLICATION_VERSION)
      .add("lang", Lang.getLang())
      .add("java", System.getProperty("java.version"))
      .add("systemName", System.getProperty("os.name"))
      .add("systemVersion", System.getProperty("os.version"))
      .add("importStarted", SignpostStatus.isCompleted(SignpostStatus.IMPORT_STARTED, repository))
      .add("importCompleted", SignpostStatus.isSectionCompleted(SignpostSectionType.IMPORT, repository))
      .add("categorizationCompleted", SignpostStatus.isSectionCompleted(SignpostSectionType.CATEGORIZATION, repository))
      .add("initialStepsCompleted", SignpostStatus.isOnboardingCompleted(repository))
      .add("purchased", User.isRegistered(repository))
      .add("mainAccounts", getActiveMainAccountsCount(repository))
      .add("savingsAccounts", getActiveSavingsAccountsCount(repository))
      .add("deferredAccounts", getDeferredAccountsCount(repository))
      .add("manualInput", SignpostStatus.isCompleted(SignpostStatus.CREATED_TRANSACTIONS_MANUALLY, repository))
      .add("split", getSplitUsage(repository))
      .add("reconciliation", hasTransactionsToReconcile(repository))
      .add("groups", getCurrentGroupsCount(repository))
      .add("currentProjects", getCurrentProjectsCount(repository))
      .add("totalProjects", getTotalProjectsCount(repository));
    return out.toString();
  }

  private static class Output {
    private StringBuilder builder = new StringBuilder();
    private boolean first = true;

    Output add(String key, boolean value) {
      return add(key, Boolean.toString(value));
    }

    Output add(String key, int value) {
      return add(key, Integer.toString(value));
    }

    Output add(String key, String value) {
      if (!first) {
        builder.append(" - ");
      }
      builder.append(key).append(":").append(Strings.removeSpaces(value));
      first = false;
      return this;
    }

    public String toString() {
      return builder.toString();
    }
  }

  private static boolean getSplitUsage(GlobRepository repository) {
    return repository.contains(Transaction.TYPE, isTrue(Transaction.SPLIT));
  }

  public static int getActiveMainAccountsCount(GlobRepository repository) {
    TreeSet<Integer> monthIds = new TreeSet<Integer>();
    monthIds.add(CurrentMonth.getCurrentMonth(repository));
    return repository.getAll(Account.TYPE, AccountMatchers.activeUserCreatedMainAccounts(monthIds)).size();
  }

  public static int getActiveSavingsAccountsCount(GlobRepository repository) {
    TreeSet<Integer> monthIds = new TreeSet<Integer>();
    monthIds.add(CurrentMonth.getCurrentMonth(repository));
    return repository.getAll(Account.TYPE, AccountMatchers.activeUserCreatedSavingsAccounts(monthIds)).size();
  }

  private static int getDeferredAccountsCount(GlobRepository repository) {
    return repository.getAll(Account.TYPE, isNotNull(Account.DEFERRED_TARGET_ACCOUNT)).size();
  }

  private static int getCurrentGroupsCount(GlobRepository repository) {
    return repository.getAll(SeriesGroup.TYPE, SeriesGroup.userCreatedGroups()).size();
  }

  private static int getCurrentProjectsCount(GlobRepository repository) {
    return repository.getAll(ProjectStat.TYPE, new CurrentProjectsMatcher()).size();
  }

  private static int getTotalProjectsCount(GlobRepository repository) {
    return repository.getAll(Project.TYPE).size();
  }

  private static boolean hasTransactionsToReconcile(GlobRepository repository) {
    return repository.contains(Transaction.TYPE, GlobMatchers.isTrue(Transaction.TO_RECONCILE));
  }
}
