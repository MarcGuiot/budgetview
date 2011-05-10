package org.designup.picsou.gui.upgrade;

import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.PicsouInit;
import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.importer.analyzer.TransactionAnalyzerFactory;
import org.designup.picsou.model.*;
import org.designup.picsou.triggers.savings.UpdateMirrorSeriesChangeSetVisitor;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.model.utils.GlobUtils;
import org.globsframework.utils.Log;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Date;

public class UpgradeTrigger implements ChangeSetListener {
  private Directory directory;
  private HashMap<Integer, Key[]> savingsSeriesToOp = new HashMap<Integer, Key[]>();

  public UpgradeTrigger(Directory directory) {
    this.directory = directory;
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    PicsouInit.createTransientDataForNewUser(repository);

    Glob userVersion = repository.get(UserVersionInformation.KEY);

    Glob userPreferences = repository.findOrCreate(UserPreferences.KEY);
    if (userPreferences.get(UserPreferences.LAST_VALID_DAY) == null) {
      repository.update(userPreferences.getKey(), UserPreferences.LAST_VALID_DAY,
                        Month.addDurationMonth(TimeService.getToday()));
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

    if (currentJarVersion < 48) {
      SignpostStatus.setAllCompleted(repository);
    }
    if (currentJarVersion < 49) {
      repository.safeApply(Series.TYPE, ALL, new DisableSeriesReportGlobFunctor());
    }
    if (currentJarVersion < 54) {
      repository.safeApply(Series.TYPE, GlobMatchers.fieldEquals(Series.BUDGET_AREA, BudgetArea.SAVINGS.getId()), new GlobFunctor() {
        public void run(Glob glob, GlobRepository repository) throws Exception {
          repository.update(glob.getKey(), Series.IS_AUTOMATIC, Boolean.FALSE);
        }
      });
      repository.update(UserPreferences.KEY, UserPreferences.MULTIPLE_PLANNED, true); // declenche les triggers
    }
    if (currentJarVersion < 59) {
      updateTargetSavings(repository);
      createMirorSeries(repository, savingsSeriesToOp);
    }
    if (currentJarVersion < 61){
      repository.update(userPreferences.getKey(), UserPreferences.LAST_VALID_DAY,
                        Month.addDurationMonth(TimeService.getToday()));
    }
    if (currentJarVersion < 65){
      repository.update(userPreferences.getKey(),
                        FieldValue.value(UserPreferences.MONTH_FOR_PLANNED, 3),
                        FieldValue.value(UserPreferences.PERIOD_COUNT_FOR_PLANNED, 10));
    }

    deleteDeprecatedGlobs(repository);

    Glob appVersion = repository.get(AppVersionInformation.KEY);
    if (userVersion.get(UserVersionInformation.CURRENT_BANK_CONFIG_VERSION)
        < (appVersion.get(AppVersionInformation.LATEST_BANK_CONFIG_SOFTWARE_VERSION))) {
      directory.get(UpgradeService.class).upgradeBankData(repository, appVersion);
    }

    repository.update(UserVersionInformation.KEY, UserVersionInformation.CURRENT_JAR_VERSION, PicsouApplication.JAR_VERSION);
  }

  private void updateTargetSavings(GlobRepository repository) {
    repository.findOrCreate(Account.EXTERNAL_KEY,
                            value(Account.IS_IMPORTED_ACCOUNT, Boolean.FALSE)
    );
    GlobList savingsSeries = repository.getAll(Series.TYPE, GlobMatchers.fieldEquals(Series.BUDGET_AREA, BudgetArea.SAVINGS.getId()));
    for (Glob series : savingsSeries) {
      Boolean sens = null;
      GlobList budget = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID)).getGlobs();
      for (Glob glob : budget) {
        if (glob.get(SeriesBudget.AMOUNT) == null){
          continue;
        }
        if (glob.get(SeriesBudget.AMOUNT) > 0) {
          sens = true;
          break;
        }
        if (glob.get(SeriesBudget.AMOUNT) < 0) {
          sens = false;
          break;
        }
      }
      if (series.get(Series.FROM_ACCOUNT) == null) {
        repository.update(series.getKey(), Series.FROM_ACCOUNT, Account.EXTERNAL_ACCOUNT_ID);
      }
      if (series.get(Series.TO_ACCOUNT) == null) {
        repository.update(series.getKey(), Series.TO_ACCOUNT, Account.EXTERNAL_ACCOUNT_ID);
      }
      if (sens == null) {
        if (series.isTrue(Series.IS_MIRROR)) {
          repository.update(series.getKey(), Series.TARGET_ACCOUNT, series.get(Series.TO_ACCOUNT));
        }
        else {
          repository.update(series.getKey(), Series.TARGET_ACCOUNT, series.get(Series.FROM_ACCOUNT));
        }
      }
      else if (sens) {
        repository.update(series.getKey(), Series.TARGET_ACCOUNT, series.get(Series.TO_ACCOUNT));
      }
      else {
        repository.update(series.getKey(), Series.TARGET_ACCOUNT, series.get(Series.FROM_ACCOUNT));
      }
    }
  }

  private void createMirorSeries(GlobRepository repository, HashMap<Integer, Key[]> seriesToOp) {
    GlobList savingsSeries = repository.getAll(Series.TYPE, GlobMatchers.fieldEquals(Series.BUDGET_AREA, BudgetArea.SAVINGS.getId()));
    for (Glob series : savingsSeries) {
      Integer mirrorSeries = series.get(Series.MIRROR_SERIES);
      if (mirrorSeries == null) {
        Integer mirorSeriesId = UpdateMirrorSeriesChangeSetVisitor.createMirrorSeries(series.getKey(), repository);
        GlobList seriesBudgets = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID)).getGlobs();
        for (Glob budget : seriesBudgets) {
          GlobBuilder builder = GlobBuilder.init(SeriesBudget.TYPE);
          for (Field field : SeriesBudget.TYPE.getFields()) {
            if (field == SeriesBudget.AMOUNT) {
              builder.set(SeriesBudget.AMOUNT, budget.get(SeriesBudget.AMOUNT) == null ? null : -budget.get(SeriesBudget.AMOUNT));
            }
            else if (field == SeriesBudget.SERIES) {
              builder.setValue(SeriesBudget.SERIES, mirorSeriesId);
            }
            else if (field != SeriesBudget.OBSERVED_AMOUNT && field != SeriesBudget.ID) {
              builder.setValue(field, budget.getValue(field));
            }
          }
          repository.create(SeriesBudget.TYPE, builder.toArray());
        }
      }
      else {
        seriesToOp.put(mirrorSeries,
                       repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, mirrorSeries).getGlobs().getKeys());
      }
      seriesToOp.put(series.get(Series.ID),
                     repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, series.get(Series.ID)).getGlobs().getKeys());
    }
  }

  private void deleteDeprecatedGlobs(GlobRepository repository) {
    repository.deleteAll(Category.TYPE, AccountPositionThreshold.TYPE);
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
    if (Account.isMain(account)) {
      repository.update(series.getKey(), accountField, Account.MAIN_SUMMARY_ACCOUNT_ID);
      GlobList planned =
        repository.getAll(Transaction.TYPE,
                          GlobMatchers.and(
                            GlobMatchers.fieldEquals(Transaction.SERIES, series.get(Series.ID)),
                            GlobMatchers.fieldEquals(Transaction.ACCOUNT, account.get(Account.ID)),
                            GlobMatchers.isTrue(Transaction.PLANNED)));
      for (Glob glob : planned) {
        repository.update(glob.getKey(),
                          value(Transaction.ACCOUNT_POSITION, null),
                          value(Transaction.ACCOUNT, Account.MAIN_SUMMARY_ACCOUNT_ID));
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
    PicsouInit.createPersistentDataForNewUser(repository, directory);
  }

  private void migrateBankEntity(GlobRepository repository) {
    GlobList accounts = repository.getAll(Account.TYPE);
    for (Glob account : accounts) {
      Glob bankEntity = repository.findLinkTarget(account, Account.BANK_ENTITY);
      if (bankEntity != null) {
        Glob bank = repository.findLinkTarget(bankEntity, BankEntity.BANK);
        repository.update(account.getKey(),
                          value(Account.BANK_ENTITY_LABEL, bankEntity.get(BankEntity.LABEL)),
                          value(Account.BANK, bank.get(Bank.ID)));

      }
      else {
        repository.update(account.getKey(),
                          value(Account.BANK_ENTITY, -1),
                          value(Account.BANK_ENTITY_LABEL, ""),
                          value(Account.BANK, -123456));
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

  private static class DisableSeriesReportGlobFunctor implements GlobFunctor {
    public void run(Glob glob, GlobRepository repository) throws Exception {
      repository.update(glob.getKey(), Series.SHOULD_REPORT, false);
    }
  }


  public void postTraitement(GlobRepository repository) {
    for (Map.Entry<Integer, Key[]> entry : savingsSeriesToOp.entrySet()) {
      Glob series = repository.find(Key.create(Series.TYPE, entry.getKey()));
      if (series == null){
        continue;
      }
      Glob targetAccount = repository.findLinkTarget(series, Series.TARGET_ACCOUNT);
      boolean positif = series.get(Series.TO_ACCOUNT).equals(series.get(Series.TARGET_ACCOUNT));
      for (Key key : entry.getValue()) {
        Glob transaction = repository.find(key);
        if (transaction != null) {
          if (isSame(targetAccount, transaction, repository)) {
            if (transaction.get(Transaction.AMOUNT) > 0 == positif) {
              repository.update(transaction.getKey(), Transaction.SERIES, series.get(Series.ID));
            }
            else {
              Log.write("Operation : " + GlobPrinter.toString(transaction) + " not valide for seires : " + GlobPrinter.toString(series));
            }
          }
        }
      }
    }
    savingsSeriesToOp.clear();
  }

  private boolean isSame(Glob targetAccount, Glob transaction, GlobRepository repository) {
    Glob account = repository.findLinkTarget(transaction, Transaction.ACCOUNT);
    return targetAccount.get(Account.ID).equals(transaction.get(Transaction.ACCOUNT)) ||
           (targetAccount.get(Account.ID) == Account.MAIN_SUMMARY_ACCOUNT_ID && Account.isMain(account));
  }

}
