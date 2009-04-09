package org.designup.picsou.gui.upgrade;

import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.TimeService;
import org.designup.picsou.importer.analyzer.TransactionAnalyzerFactory;
import org.designup.picsou.model.*;
import org.designup.picsou.model.initial.InitialCategories;
import org.designup.picsou.model.initial.InitialSeries;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
import static org.globsframework.model.utils.GlobMatchers.fieldIsNull;
import org.globsframework.model.utils.GlobUtils;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class UpgradeTrigger implements ChangeSetListener {
  private Directory directory;
  private String user;
  private boolean validUser;

  public UpgradeTrigger(Directory directory, String user, boolean validUser) {
    this.directory = directory;
    this.user = user;
    this.validUser = validUser;
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    createDataForNewUser(repository);
    Glob version = repository.find(VersionInformation.KEY);
    if (!version.get(VersionInformation.CURRENT_BANK_CONFIG_VERSION).equals(version.get(VersionInformation.LATEST_BANK_CONFIG_SOFTWARE_VERSION))) {
      directory.get(UpgradeService.class).upgradeBankData(repository, version);
    }

    Glob userPreferences = repository.findOrCreate(UserPreferences.KEY);
    if (userPreferences.get(UserPreferences.LAST_VALID_DAY) == null) {
      repository.update(userPreferences.getKey(), UserPreferences.LAST_VALID_DAY,
                        Month.addOneMonth(TimeService.getToday()));
    }

    repository.update(CurrentMonth.KEY,
                      value(CurrentMonth.CURRENT_MONTH, TimeService.getCurrentMonth()),
                      value(CurrentMonth.CURRENT_DAY, TimeService.getCurrentDay()));


    final Long currentJarVersion = version.get(VersionInformation.CURRENT_JAR_VERSION);
    if (currentJarVersion.equals(PicsouApplication.JAR_VERSION)) {
      return;
    }

    if (currentJarVersion <= 4) {
      if (repository.find(Series.OCCASIONAL_SERIES) != null) {
        repository.update(Series.OCCASIONAL_SERIES,
                          value(Series.PROFILE_TYPE, ProfileType.EVERY_MONTH.getId()),
                          value(Series.DEFAULT_CATEGORY, Category.NONE),
                          value(Series.DAY, 1),
                          value(Series.NAME, Series.getOccasionalName()));
      }
    }

    if (currentJarVersion <= 9) {

      repository
        .getAll(SeriesBudget.TYPE, fieldIsNull(SeriesBudget.DAY))
        .safeApply(new GlobFunctor() {
          public void run(Glob seriesBudget, GlobRepository repository) throws Exception {
            final int lastDay = Month.getLastDayNumber(seriesBudget.get(SeriesBudget.MONTH));
            repository.update(seriesBudget.getKey(), value(SeriesBudget.DAY, lastDay));
          }
        }, repository);

      GlobUtils.updateIfExists(repository,
                               Series.OCCASIONAL_SERIES,
                               Series.NAME,
                               Series.getOccasionalName());
      GlobUtils.updateIfExists(repository,
                               Series.UNCATEGORIZED_SERIES,
                               Series.NAME,
                               Series.getUncategorizedName());

      repository.update(Series.UNCATEGORIZED_SERIES, Series.IS_AUTOMATIC, false);

      GlobList uncategorizedTransactions =
        repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID)
          .getGlobs();

      if (!uncategorizedTransactions.isEmpty()) {
        final Integer lastMonthId = uncategorizedTransactions.getSortedSet(Transaction.MONTH).last();
        repository
          .getAll(SeriesBudget.TYPE,
                  GlobMatchers.and(
                    GlobMatchers.fieldEquals(SeriesBudget.SERIES, Series.UNCATEGORIZED_SERIES_ID),
                    GlobMatchers.fieldStrictlyGreaterThan(SeriesBudget.MONTH, lastMonthId)))
          .safeApply(new GlobFunctor() {
            public void run(Glob seriesBudget, GlobRepository repository) throws Exception {
              repository.update(seriesBudget.getKey(), value(SeriesBudget.AMOUNT, 0.00));
            }
          }, repository);
      }

      repository.safeApply(Transaction.TYPE, GlobMatchers.fieldEquals(Transaction.PLANNED, true),
                           new GlobFunctor() {
                             public void run(Glob transaction, GlobRepository repository) throws Exception {
                               Glob series = repository.findLinkTarget(transaction, Transaction.SERIES);
                               repository.update(transaction.getKey(), Transaction.LABEL, Transaction.getLabel(true, series));
                             }
                           });

      GlobList globList = repository.getAll(Series.TYPE, GlobMatchers.fieldEquals(Series.BUDGET_AREA, BudgetArea.SAVINGS.getId()));
      for (Glob glob : globList) {
        if (glob.get(Series.TO_ACCOUNT) == null && glob.get(Series.FROM_ACCOUNT) == null) {
          repository.update(glob.getKey(), Series.FROM_ACCOUNT, Account.MAIN_SUMMARY_ACCOUNT_ID);
        }
      }
    }

    if (currentJarVersion < 10) {
      repository.safeApply(Transaction.TYPE, GlobMatchers.fieldEquals(Transaction.PLANNED, false),
                           new GlobFunctor() {
                             public void run(Glob transaction, GlobRepository repository) throws Exception {
                               String originalLabel = transaction.get(Transaction.ORIGINAL_LABEL);
                               String newLabel = TransactionAnalyzerFactory.removeBlankAndToUpercase(originalLabel);
                               repository.update(transaction.getKey(), Transaction.ORIGINAL_LABEL, newLabel);
                             }
                           });
    }

    repository.update(VersionInformation.KEY, VersionInformation.CURRENT_JAR_VERSION, PicsouApplication.JAR_VERSION);
  }

  public void createDataForNewUser(GlobRepository repository) {
    repository.findOrCreate(User.KEY,
                            value(User.NAME, user),
                            value(User.IS_REGISTERED_USER, validUser));
    repository.findOrCreate(VersionInformation.KEY,
                            value(VersionInformation.CURRENT_JAR_VERSION, PicsouApplication.JAR_VERSION),
                            value(VersionInformation.CURRENT_BANK_CONFIG_VERSION, PicsouApplication.BANK_CONFIG_VERSION),
                            value(VersionInformation.CURRENT_SOFTWARE_VERSION, PicsouApplication.APPLICATION_VERSION),
                            value(VersionInformation.LATEST_AVALAIBLE_JAR_VERSION, PicsouApplication.JAR_VERSION),
                            value(VersionInformation.LATEST_BANK_CONFIG_SOFTWARE_VERSION, PicsouApplication.BANK_CONFIG_VERSION),
                            value(VersionInformation.LATEST_AVALAIBLE_SOFTWARE_VERSION, PicsouApplication.APPLICATION_VERSION));
    Glob userPreferences = repository.findOrCreate(UserPreferences.KEY);
    if (userPreferences.get(UserPreferences.LAST_VALID_DAY) == null) {
      repository.update(userPreferences.getKey(), UserPreferences.LAST_VALID_DAY,
                        Month.addOneMonth(TimeService.getToday()));
    }

    repository.findOrCreate(CurrentMonth.KEY,
                            value(CurrentMonth.LAST_TRANSACTION_MONTH, 0),
                            value(CurrentMonth.LAST_TRANSACTION_DAY, 0),
                            value(CurrentMonth.CURRENT_MONTH, TimeService.getCurrentMonth()),
                            value(CurrentMonth.CURRENT_DAY, TimeService.getCurrentDay()));
    repository.findOrCreate(Account.MAIN_SUMMARY_KEY,
                            value(Account.ACCOUNT_TYPE, AccountType.MAIN.getId()),
                            value(Account.IS_IMPORTED_ACCOUNT, true));
    repository.findOrCreate(Account.SAVINGS_SUMMARY_KEY,
                            FieldValue.value(Account.ACCOUNT_TYPE, AccountType.SAVINGS.getId()));
    repository.findOrCreate(Account.ALL_SUMMARY_KEY);
    InitialCategories.run(repository);
    InitialSeries.run(repository);
  }
}
