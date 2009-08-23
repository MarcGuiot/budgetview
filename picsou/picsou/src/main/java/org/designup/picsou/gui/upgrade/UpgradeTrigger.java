package org.designup.picsou.gui.upgrade;

import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.TimeService;
import org.designup.picsou.importer.analyzer.TransactionAnalyzerFactory;
import org.designup.picsou.model.*;
import org.designup.picsou.model.initial.InitialSeries;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.model.utils.GlobUtils;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class UpgradeTrigger implements ChangeSetListener {
  private Directory directory;

  public UpgradeTrigger(Directory directory) {
    this.directory = directory;
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    createDataForNewUser(repository);
    Glob appVersion = repository.get(AppVersionInformation.KEY);
    Glob userVersion = repository.get(UserVersionInformation.KEY);
    if (userVersion.get(UserVersionInformation.CURRENT_BANK_CONFIG_VERSION)
        < (appVersion.get(AppVersionInformation.LATEST_BANK_CONFIG_SOFTWARE_VERSION))) {
      directory.get(UpgradeService.class).upgradeBankData(repository, appVersion);
    }

    Glob userPreferences = repository.findOrCreate(UserPreferences.KEY);
    if (userPreferences.get(UserPreferences.LAST_VALID_DAY) == null) {
      repository.update(userPreferences.getKey(), UserPreferences.LAST_VALID_DAY,
                        Month.addOneMonth(TimeService.getToday()));
    }

    repository.update(CurrentMonth.KEY,
                      value(CurrentMonth.CURRENT_MONTH, TimeService.getCurrentMonth()),
                      value(CurrentMonth.CURRENT_DAY, TimeService.getCurrentDay()));

    final Long currentJarVersion = userVersion.get(UserVersionInformation.CURRENT_JAR_VERSION);
    if (currentJarVersion.equals(PicsouApplication.JAR_VERSION)) {
      return;
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
                  and(
                    fieldEquals(SeriesBudget.SERIES, Series.UNCATEGORIZED_SERIES_ID),
                    GlobMatchers.fieldStrictlyGreaterThan(SeriesBudget.MONTH, lastMonthId)))
          .safeApply(new GlobFunctor() {
            public void run(Glob seriesBudget, GlobRepository repository) throws Exception {
              repository.update(seriesBudget.getKey(), value(SeriesBudget.AMOUNT, 0.00));
            }
          }, repository);
      }

      repository.safeApply(Transaction.TYPE, fieldEquals(Transaction.PLANNED, true),
                           new GlobFunctor() {
                             public void run(Glob transaction, GlobRepository repository) throws Exception {
                               Glob series = repository.findLinkTarget(transaction, Transaction.SERIES);
                               repository.update(transaction.getKey(), Transaction.LABEL, Transaction.getLabel(true, series));
                             }
                           });

      GlobList globList = repository.getAll(Series.TYPE, fieldEquals(Series.BUDGET_AREA, BudgetArea.SAVINGS.getId()));
      for (Glob glob : globList) {
        if (glob.get(Series.TO_ACCOUNT) == null && glob.get(Series.FROM_ACCOUNT) == null) {
          repository.update(glob.getKey(), Series.FROM_ACCOUNT, Account.MAIN_SUMMARY_ACCOUNT_ID);
        }
      }
    }

    if (currentJarVersion < 10) {
      repository.safeApply(Transaction.TYPE, fieldEquals(Transaction.PLANNED, false),
                           new GlobFunctor() {
                             public void run(Glob transaction, GlobRepository repository) throws Exception {
                               String originalLabel = transaction.get(Transaction.ORIGINAL_LABEL);
                               String newLabel = TransactionAnalyzerFactory.removeBlankAndToUpercase(originalLabel);
                               repository.update(transaction.getKey(), Transaction.ORIGINAL_LABEL, newLabel);

                               String label = transaction.get(Transaction.LABEL);
                               String newVisibleLabel = TransactionAnalyzerFactory.removeBlankAndToUpercase(label);
                               repository.update(transaction.getKey(), Transaction.LABEL, newVisibleLabel);

                             }
                           });
    }

    if (currentJarVersion < 16) {
      removeOccasionalBudgetArea(repository);
      migrateCategoriesToSubSeries(repository);
    }

    if (currentJarVersion < 19) {
      migrateProfileTypes(repository);
    }

    repository.update(UserVersionInformation.KEY, UserVersionInformation.CURRENT_JAR_VERSION, PicsouApplication.JAR_VERSION);
  }

  private void migrateProfileTypes(GlobRepository repository) {
    repository.safeApply(Series.TYPE, GlobMatchers.ALL,
                         new GlobFunctor() {
                           public void run(Glob series, GlobRepository repository) throws Exception {
                             Integer profileType = series.get(Series.PROFILE_TYPE);
                             if ((profileType == null) ||
                                 profileType.equals(4) || // THREE_MONTHS
                                 profileType.equals(5)) { // FOUR_MONTHS
                               repository.update(series.getKey(),
                                                 Series.PROFILE_TYPE,
                                                 ProfileType.CUSTOM.getId());
                             }
                           }
                         });
  }

  private void removeOccasionalBudgetArea(GlobRepository repository) {
    repository.safeApply(Transaction.TYPE, GlobMatchers.ALL,
                         new GlobFunctor() {
                           public void run(Glob transaction, GlobRepository repository) throws Exception {
                             if (Series.OCCASIONAL_SERIES_ID.equals(transaction.get(Transaction.SERIES))) {
                               repository.update(transaction.getKey(),
                                                 Transaction.SERIES,
                                                 Series.UNCATEGORIZED_SERIES_ID);
                             }
                           }
                         }
    );

    GlobList budgets = repository.getAll(SeriesBudget.TYPE,
                                         fieldEquals(SeriesBudget.SERIES,
                                                     Series.OCCASIONAL_SERIES_ID));
    repository.delete(budgets);

    Key occasionalSeriesKey = Key.create(Series.TYPE, Series.OCCASIONAL_SERIES_ID);
    if (repository.contains(occasionalSeriesKey)) {
      repository.delete(occasionalSeriesKey);
    }
  }

  private void migrateCategoriesToSubSeries(GlobRepository repository) {
    for (Glob series : repository.getAll(Series.TYPE)) {

      Integer seriesId = series.get(Series.ID);
      GlobList seriesToCategoriesList =
        repository.getAll(SeriesToCategory.TYPE, GlobMatchers.linkedTo(series, SeriesToCategory.SERIES));
      if (seriesToCategoriesList.size() > 1) {

        for (Glob seriesToCategory : seriesToCategoriesList) {
          Integer categoryId = seriesToCategory.get(SeriesToCategory.CATEGORY);
          String categoryName = Category.getName(categoryId, repository);
          if (!Utils.equalIgnoreCase(categoryName, series.get(Series.NAME))) {
            Glob subSeries =
              repository.create(SubSeries.TYPE,
                                value(SubSeries.SERIES, seriesId),
                                value(SubSeries.NAME, categoryName));

            GlobList transactions =
              repository.getAll(Transaction.TYPE,
                                and(
                                  fieldEquals(Transaction.SERIES, seriesId),
                                  fieldEquals(Transaction.CATEGORY, categoryId)
                                ));
            for (Glob transaction : transactions) {
              repository.setTarget(transaction.getKey(), Transaction.SUB_SERIES, subSeries.getKey());
            }
          }
        }
      }
    }

    repository.deleteAll(SeriesToCategory.TYPE);
    repository.deleteAll(Category.TYPE);
  }

  public void createDataForNewUser(GlobRepository repository) {
    repository.startChangeSet();
    try {
      repository.findOrCreate(Notes.KEY);
      repository.findOrCreate(AccountPositionThreshold.KEY);
      repository.findOrCreate(UserVersionInformation.KEY,
                              value(UserVersionInformation.CURRENT_JAR_VERSION, PicsouApplication.JAR_VERSION),
                              value(UserVersionInformation.CURRENT_BANK_CONFIG_VERSION, PicsouApplication.BANK_CONFIG_VERSION),
                              value(UserVersionInformation.CURRENT_SOFTWARE_VERSION, PicsouApplication.APPLICATION_VERSION));
//                            value(AppVersionInformation.LATEST_AVALAIBLE_JAR_VERSION, PicsouApplication.JAR_VERSION),
//                            value(AppVersionInformation.LATEST_BANK_CONFIG_SOFTWARE_VERSION, PicsouApplication.BANK_CONFIG_VERSION),
//                            value(AppVersionInformation.LATEST_AVALAIBLE_SOFTWARE_VERSION, PicsouApplication.APPLICATION_VERSION)
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
      InitialSeries.run(repository);
    }
    finally {
      repository.completeChangeSet();
    }
  }
}
