package org.designup.picsou.gui.upgrade;

import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.PicsouInit;
import org.designup.picsou.gui.TimeService;
import org.designup.picsou.importer.analyzer.TransactionAnalyzerFactory;
import org.designup.picsou.model.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.LinkField;
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
    PicsouInit.createTransientDataForNewUser(repository);

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
      upgradeFromV9(repository);
    }

    if (currentJarVersion < 10) {
      upgradeFromV10(repository);
    }

    if (currentJarVersion < 16) {
      removeOccasionalBudgetArea(repository);
      migrateCategoriesToSubSeries(repository);
    }

    if (currentJarVersion < 19) {
      migrateProfileTypes(repository);
    }

    if (currentJarVersion < 21) {
      migrateBankEntity(repository);
    }

    if (currentJarVersion < 24) {
      repository.safeApply(Transaction.TYPE, isTrue(Transaction.PLANNED), new RemovePlanedPrefixFunctor());
    }

    if (currentJarVersion < 34) {
      updateSavings(repository);
    }

    if (currentJarVersion < 35) {
      repository.update(UserPreferences.KEY, UserPreferences.SHOW_BUDGET_VIEW_WIZARD, true);
    }

    if (currentJarVersion < 44) {
      if (!repository.get(UserPreferences.KEY).isTrue(UserPreferences.SHOW_CATEGORIZATION_HELP_MESSAGE)) {
        SignpostStatus.setAllCompleted(repository);
      }
    }

    repository.update(UserVersionInformation.KEY, UserVersionInformation.CURRENT_JAR_VERSION, PicsouApplication.JAR_VERSION);
  }

  private void updateSavings(GlobRepository repository) {
    GlobList series = repository.getAll(Series.TYPE, GlobMatchers.fieldEquals(Series.BUDGET_AREA, BudgetArea.SAVINGS.getId()));
    for (Glob oneSeries : series) {
      updateAccount(repository, oneSeries, Series.FROM_ACCOUNT);
      updateAccount(repository, oneSeries, Series.TO_ACCOUNT);
    }
  }

  private void updateAccount(GlobRepository repository, Glob series, final LinkField accountField) {
    Glob account = repository.findLinkTarget(series, accountField);
    if (account != null && account.get(Account.ACCOUNT_TYPE).equals(AccountType.MAIN.getId())) {
      repository.update(series.getKey(), accountField, Account.MAIN_SUMMARY_ACCOUNT_ID);
      GlobList planned =
        repository.getAll(Transaction.TYPE,
                          GlobMatchers.and(
                            GlobMatchers.fieldEquals(Transaction.SERIES, series.get(Series.ID)),
                            GlobMatchers.fieldEquals(Transaction.ACCOUNT, account.get(Account.ID)),
                            GlobMatchers.isTrue(Transaction.PLANNED)));
      for (Glob glob : planned) {
        repository.update(glob.getKey(),
                          FieldValue.value(Transaction.ACCOUNT_POSITION, null),
                          FieldValue.value(Transaction.ACCOUNT, Account.MAIN_SUMMARY_ACCOUNT_ID));
      }
    }
  }

  private void upgradeFromV10(GlobRepository repository) {
    repository.safeApply(Transaction.TYPE,
                         isFalse(Transaction.PLANNED),
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

  private void upgradeFromV9(GlobRepository repository) {
    repository
      .getAll(SeriesBudget.TYPE, isNull(SeriesBudget.DAY))
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
            repository.update(seriesBudget.getKey(), value(SeriesBudget.AMOUNT, 0.0));
          }
        }, repository);
    }

    GlobList globList = repository.getAll(Series.TYPE, fieldEquals(Series.BUDGET_AREA, BudgetArea.SAVINGS.getId()));
    for (Glob glob : globList) {
      if (glob.get(Series.TO_ACCOUNT) == null && glob.get(Series.FROM_ACCOUNT) == null) {
        repository.update(glob.getKey(), Series.FROM_ACCOUNT, Account.MAIN_SUMMARY_ACCOUNT_ID);
      }
    }
  }

  private void migrateBankEntity(GlobRepository repository) {
    GlobList accounts = repository.getAll(Account.TYPE);
    for (Glob account : accounts) {
      Glob bankEntity = repository.findLinkTarget(account, Account.BANK_ENTITY);
      if (bankEntity != null) {
        Glob bank = repository.findLinkTarget(bankEntity, BankEntity.BANK);
        repository.update(account.getKey(),
                          FieldValue.value(Account.BANK_ENTITY_LABEL, bankEntity.get(BankEntity.LABEL)),
                          FieldValue.value(Account.BANK, bank.get(Bank.ID)));

      }
      else {
        repository.update(account.getKey(),
                          FieldValue.value(Account.BANK_ENTITY, -1),
                          FieldValue.value(Account.BANK_ENTITY_LABEL, ""),
                          FieldValue.value(Account.BANK, -123456));
      }
    }
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

    repository.delete(SeriesBudget.TYPE, fieldEquals(SeriesBudget.SERIES, Series.OCCASIONAL_SERIES_ID));

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

  private static class RemovePlanedPrefixFunctor implements GlobFunctor {

    public void run(Glob glob, GlobRepository repository) throws Exception {
      String label = glob.get(Transaction.LABEL);
      if (label.startsWith("Planned: ")) {
        label = label.substring(9);
      }
      else if (label.startsWith("Prévu : ")) {
        label = label.substring(8);
      }
      else if (label.startsWith("Planned:")) {
        label = label.substring(8);
      }
      else if (label.startsWith("Prévu :")) {
        label = label.substring(7);
      }
      repository.update(glob.getKey(), Transaction.LABEL, label);
    }
  }
}
