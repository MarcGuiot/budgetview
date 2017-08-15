package com.budgetview.desktop.upgrade;

import com.budgetview.desktop.Application;
import com.budgetview.desktop.PicsouInit;
import com.budgetview.desktop.accounts.utils.AccountMatchers;
import com.budgetview.desktop.projects.upgrade.ProjectUpgradeV40;
import com.budgetview.desktop.series.upgrade.SeriesUpgradeV40;
import com.budgetview.desktop.series.utils.SeriesErrorsUpgrade;
import com.budgetview.desktop.utils.FrameSize;
import com.budgetview.io.importer.analyzer.TransactionAnalyzerFactory;
import com.budgetview.model.*;
import com.budgetview.model.deprecated.AccountPositionThreshold;
import com.budgetview.model.deprecated.Category;
import com.budgetview.model.upgrade.DeferredAccountUpgradeV40;
import com.budgetview.shared.model.BudgetArea;
import com.budgetview.shared.utils.Amounts;
import com.budgetview.triggers.AccountInitialPositionTrigger;
import com.budgetview.triggers.AccountSequenceTrigger;
import com.budgetview.triggers.AddOnTrigger;
import com.budgetview.triggers.PositionTrigger;
import com.budgetview.triggers.savings.UpdateMirrorSeriesChangeSetVisitor;
import com.budgetview.utils.TransactionComparator;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.*;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.*;

public class UpgradeTrigger implements ChangeSetListener {
  private Directory directory;
  private HashMap<Integer, Key[]> savingsSeriesToOp = new HashMap<Integer, Key[]>();
  private PostProcessor postProcessor = new PostProcessor();

  public UpgradeTrigger(Directory directory) {
    this.directory = directory;
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {

    PicsouInit.createTransientDataForNewUser(repository);

    Glob userVersion = repository.get(UserVersionInformation.KEY);

    final Long currentJarVersion = userVersion.get(UserVersionInformation.CURRENT_JAR_VERSION);
    if (currentJarVersion.equals(Application.JAR_VERSION)) {
      return;
    }

    if (currentJarVersion < 10) {
      upgradeFromV10(repository);
    }

    if (currentJarVersion < 16) {
      removeOccasionalBudgetArea(repository);
    }

    if (currentJarVersion < 19) {
      migrateProfileTypes(repository);
    }

    if (currentJarVersion < 21) {
      migrateBankEntity(repository);
    }

    if (currentJarVersion < 24) {
      repository.safeApply(Transaction.TYPE, isTrue(Transaction.PLANNED), new RemovePlannedPrefixFunctor());
    }

    if (currentJarVersion < 34) {
      updateSavings(repository);
    }

    if (currentJarVersion < 48) {
      SignpostStatus.setAllCompleted(repository);
    }
    if (currentJarVersion < 54) {
      repository.safeApply(Series.TYPE, GlobMatchers.fieldEquals(Series.BUDGET_AREA, BudgetArea.TRANSFER.getId()), new GlobFunctor() {
        public void run(Glob glob, GlobRepository repository) throws Exception {
          repository.update(glob.getKey(), Series.IS_AUTOMATIC, Boolean.FALSE);
        }
      });
      repository.update(UserPreferences.KEY, UserPreferences.MULTIPLE_PLANNED, true); // declenche les triggers
    }
    if (currentJarVersion < 59) {
      updateTargetSavings(repository);
      createMirrorSeries(repository, savingsSeriesToOp);
    }

    Glob userPreferences = repository.findOrCreate(UserPreferences.KEY);
    if (currentJarVersion < 65) {
      repository.update(userPreferences.getKey(),
                        value(UserPreferences.MONTH_FOR_PLANNED, 3),
                        value(UserPreferences.PERIOD_COUNT_FOR_PLANNED, 10));
    }

    if (currentJarVersion < 68) {
      correctSavingMirror(repository);
    }
    if (currentJarVersion < 90) {
      fixHiddenProjectSeriesBudget(repository);
    }
    if (currentJarVersion < 93) {
      updateOpenCloseAccount(repository);
    }

    if (currentJarVersion < 94) {
      updateDeferredAccount(repository);
    }

    if (currentJarVersion < 97) {
      reassignBankId(repository);
    }

    if (currentJarVersion < 117) {
      updateOpenCloseAccount(repository);
    }

    if (currentJarVersion < 133) {
      AccountSequenceTrigger.resetSequence(repository);
      repository.delete(Transaction.TYPE, and(fieldEquals(Transaction.ACCOUNT, Account.MAIN_SUMMARY_ACCOUNT_ID),
                                              fieldEquals(Transaction.PLANNED, true)));
    }
    if (currentJarVersion < 142) {
      repository.deleteAll(LayoutConfig.TYPE);
      normalizeNotImportedMirrorTransactions(repository);
      ProjectUpgradeV40.run(repository, postProcessor);
      SeriesUpgradeV40.run(repository, postProcessor);
      updageAccountGraphs(repository);
      updateColorTheme(repository);
      DeferredAccountUpgradeV40.run(repository);
    }
    if (currentJarVersion < 144) {
      AddOnTrigger.alignWithUser(repository);
    }

    // Dans tous les cas :

    SeriesErrorsUpgrade.fixInvalidTransfers(repository);
    SeriesErrorsUpgrade.fixMissingGroups(repository);

    repository.findOrCreate(AddOns.KEY);
    repository.delete(Transaction.TYPE, and(fieldEquals(Transaction.CREATED_BY_SERIES, true),
                                            fieldEquals(Transaction.AMOUNT, 0.00)));

    if (currentJarVersion < 141) {
      Glob config = LayoutConfig.find(FrameSize.init(directory.get(JFrame.class)), repository, true);
      resetLayout(config, repository);
    }

    UserPreferences.initMobilePassword(repository, false);

    deleteDeprecatedGlobs(repository);

    Glob appVersion = repository.get(AppVersionInformation.KEY);
    if (userVersion.get(UserVersionInformation.CURRENT_BANK_CONFIG_VERSION)
        < (appVersion.get(AppVersionInformation.LATEST_BANK_CONFIG_SOFTWARE_VERSION))) {
      directory.get(UpgradeService.class).upgradeBankData(repository, appVersion);
    }

    repository.update(UserVersionInformation.KEY, UserVersionInformation.CURRENT_JAR_VERSION, Application.JAR_VERSION);
  }

  private void normalizeNotImportedMirrorTransactions(GlobRepository repository) {
    for (Glob transaction : repository.getAll(Transaction.TYPE, isTrue(Transaction.CREATED_BY_SERIES))) {
      repository.update(transaction,
                        value(Transaction.CREATED_BY_SERIES, false),
                        value(Transaction.MIRROR, false));
    }
  }

  private void updageAccountGraphs(GlobRepository repository) {
    for (Glob account : repository.getAll(Account.TYPE, AccountMatchers.userCreatedSavingsAccounts())) {
      repository.update(account.getKey(), Account.SHOW_CHART, false);
    }
  }

  private void resetLayout(Glob config, GlobRepository repository) {
    if (config != null) {
      repository.update(config.getKey(),
                        value(LayoutConfig.BUDGET_HORIZONTAL_1, 0.5),
                        value(LayoutConfig.BUDGET_VERTICAL_LEFT_1, 0.2),
                        value(LayoutConfig.BUDGET_VERTICAL_LEFT_2, 0.5),
                        value(LayoutConfig.BUDGET_VERTICAL_RIGHT_1, 0.6));
    }
  }

  private void reassignBankId(GlobRepository repository) {
    GlobList accounts = repository.getAll(Account.TYPE);
    for (Glob glob : accounts) {
      Glob bankEntity = repository.findLinkTarget(glob, Account.BANK_ENTITY);
      if (bankEntity != null) {
        repository.update(glob.getKey(), Account.BANK, BankEntity.getBank(bankEntity, repository).get(Bank.ID));
      }
    }

    GlobList realAccounts = repository.getAll(RealAccount.TYPE);
    for (Glob account : realAccounts) {
      Glob associatedAccount = repository.findLinkTarget(account, RealAccount.ACCOUNT);
      if (associatedAccount != null) {
        repository.update(account.getKey(), RealAccount.BANK, associatedAccount.get(Account.BANK));
      }
      else {
        Glob bankEntity = repository.findLinkTarget(account, RealAccount.BANK_ENTITY);
        if (bankEntity != null) {
          repository.update(account.getKey(), RealAccount.BANK, bankEntity.get(BankEntity.BANK));
        }
      }
    }
  }

  private void updateDeferredAccount(GlobRepository repository) {
    GlobList all = repository.getAll(Account.TYPE,
                                     fieldEquals(Account.CARD_TYPE, AccountCardType.DEFERRED.getId()));
    Glob currentMonth = repository.get(CurrentMonth.KEY);
    for (Glob glob : all) {
      GlobList globs = repository.findByIndex(DeferredCardDate.ACCOUNT_AND_DATE,
                                              DeferredCardDate.ACCOUNT, glob.get(Account.ID))
        .getGlobs().sort(DeferredCardDate.MONTH);

      int day = -1;
      for (Glob deferredCard : globs) {
        Integer month = deferredCard.get(DeferredCardDate.MONTH);
        if (month < currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH)) {
          day = deferredCard.get(DeferredCardDate.DAY);
          break;
        }
      }
      if (day == -1) {
        if (globs.getLast() != null) {
          day = globs.getLast().get(DeferredCardDate.DAY);
        }
        else {
          day = 31;
        }
      }
      repository.update(glob.getKey(),
                        value(Account.DEFERRED_DAY, day),
                        value(Account.DEFERRED_DEBIT_DAY, day),
                        value(Account.DEFERRED_MONTH_SHIFT, 0));
    }
  }

  private void updateOpenCloseAccount(GlobRepository repository) {
    repository.startChangeSet();
    try {
      repository.findOrCreate(Key.create(Series.TYPE, Series.ACCOUNT_SERIES_ID),
                              value(Series.BUDGET_AREA, BudgetArea.OTHER.getId()),
                              value(Series.PROFILE_TYPE, ProfileType.IRREGULAR.getId()),
                              value(Series.IS_AUTOMATIC, false),
                              value(Series.DAY, 1),
                              value(Series.NAME, Series.getAccountSeriesName()));

      GlobList accounts = repository.getAll(Account.TYPE,
                                            GlobMatchers.not(fieldEquals(Account.CARD_TYPE, AccountCardType.DEFERRED.getId())));

      for (Glob account : accounts) {
        if (!Account.isUserCreatedAccount(account)) {
          continue;
        }
        if (account.get(Account.OPEN_TRANSACTION) != null) {
          continue;
        }
        Glob[] transactions = repository.getSorted(Transaction.TYPE, TransactionComparator.ASCENDING_ACCOUNT,
                                                   fieldEquals(Transaction.ACCOUNT, account.get(Account.ID)));
        Date openDate = account.get(Account.OPEN_DATE);
        int open = Integer.MAX_VALUE;

        if (openDate != null) {
          open = Month.toFullDate(openDate);
        }
        double openAmount = 0;
        if (transactions.length != 0) {
          Glob firstTransaction = transactions[0];
          open = Math.min(open, Month.toFullDate(firstTransaction.get(Transaction.POSITION_MONTH),
                                                 firstTransaction.get(Transaction.POSITION_DAY)));
          open = Math.min(open, Month.toFullDate(firstTransaction.get(Transaction.BANK_MONTH),
                                                 firstTransaction.get(Transaction.BANK_DAY)));
          Double firstPos = firstTransaction.get(Transaction.ACCOUNT_POSITION, 0.);
          openAmount = firstPos - firstTransaction.get(Transaction.AMOUNT);
        }
        AccountInitialPositionTrigger.createOpenTransaction(Month.getMonthIdFromFullDate(open),
                                                            Month.getDayFromFullDate(open), openAmount, repository,
                                                            account.getKey());
        Date closeDate = account.get(Account.CLOSED_DATE);
        int close = Integer.MIN_VALUE;
        if (closeDate != null) {
          close = Month.toFullDate(closeDate);
        }
        if (transactions.length != 0) {
          Glob lastTransaction = transactions[transactions.length - 1];
          close = Math.max(close, Month.toFullDate(lastTransaction.get(Transaction.POSITION_MONTH),
                                                   lastTransaction.get(Transaction.POSITION_DAY)));
          close = Math.max(close, Month.toFullDate(lastTransaction.get(Transaction.BANK_MONTH),
                                                   lastTransaction.get(Transaction.BANK_DAY)));
          double closeAmount = -lastTransaction.get(Transaction.ACCOUNT_POSITION, 0.);
          AccountInitialPositionTrigger.createCloseTransaction(repository, account.getKey(), Month.getDayFromFullDate(close),
                                                               Month.getMonthIdFromFullDate(close), closeAmount);

        }
      }
      TransactionComparator comparator = TransactionComparator.ASCENDING_ACCOUNT;
      PositionTrigger.computeTotal(repository, repository.getSorted(Transaction.TYPE, comparator, GlobMatchers.ALL));
    }
    finally {
      repository.completeChangeSet();
    }
  }

  private void correctSavingMirror(GlobRepository repository) {
    GlobList savingsSeries = repository.getAll(Series.TYPE, fieldEquals(Series.BUDGET_AREA, BudgetArea.TRANSFER.getId()));
    for (Glob series : savingsSeries) {
      Glob mirrorSeries = repository.findLinkTarget(series, Series.MIRROR_SERIES);
      if (mirrorSeries.get(Series.TARGET_ACCOUNT).equals(series.get(Series.TARGET_ACCOUNT))) {
        updateSavingsSeries(repository, series);
        //finalement deux series sont identiques
        if (mirrorSeries.get(Series.TARGET_ACCOUNT).equals(series.get(Series.TARGET_ACCOUNT))) {
          Log.write("[Upgrade] Fixing savings series");
          repository.update(mirrorSeries.getKey(), Series.TARGET_ACCOUNT, mirrorSeries.get(Series.FROM_ACCOUNT));
          repository.update(series.getKey(), Series.TARGET_ACCOUNT, mirrorSeries.get(Series.TO_ACCOUNT));
        }
      }
    }
  }

  private void updateTargetSavings(GlobRepository repository) {
    repository.findOrCreate(Account.EXTERNAL_KEY,
                            value(Account.IS_IMPORTED_ACCOUNT, Boolean.FALSE)
    );
    GlobList savingsSeries = repository.getAll(Series.TYPE, fieldEquals(Series.BUDGET_AREA, BudgetArea.TRANSFER.getId()));
    for (Glob series : savingsSeries) {
      updateSavingsSeries(repository, series);
    }
  }

  private void updateSavingsSeries(GlobRepository repository, Glob series) {
    Boolean positive = null;
    GlobList budgets = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID)).getGlobs();
    for (Glob budget : budgets) {
      if (budget.get(SeriesBudget.PLANNED_AMOUNT) == null) {
        continue;
      }
      if (budget.get(SeriesBudget.PLANNED_AMOUNT) > 0) {
        positive = true;
        break;
      }
      if (budget.get(SeriesBudget.PLANNED_AMOUNT) < 0) {
        positive = false;
        break;
      }
    }
    if (series.get(Series.FROM_ACCOUNT) == null) {
      repository.update(series.getKey(), Series.FROM_ACCOUNT, Account.EXTERNAL_ACCOUNT_ID);
    }
    if (series.get(Series.TO_ACCOUNT) == null) {
      repository.update(series.getKey(), Series.TO_ACCOUNT, Account.EXTERNAL_ACCOUNT_ID);
    }
    if (positive == null) {
      if (series.isTrue(Series.IS_MIRROR)) {
        repository.update(series.getKey(), Series.TARGET_ACCOUNT, series.get(Series.TO_ACCOUNT));
      }
      else {
        repository.update(series.getKey(), Series.TARGET_ACCOUNT, series.get(Series.FROM_ACCOUNT));
      }
    }
    else if (positive) {
      repository.update(series.getKey(), Series.TARGET_ACCOUNT, series.get(Series.TO_ACCOUNT));
    }
    else {
      repository.update(series.getKey(), Series.TARGET_ACCOUNT, series.get(Series.FROM_ACCOUNT));
    }
  }

  private void createMirrorSeries(GlobRepository repository, HashMap<Integer, Key[]> seriesToOp) {
    GlobList savingsSeries = repository.getAll(Series.TYPE, fieldEquals(Series.BUDGET_AREA, BudgetArea.TRANSFER.getId()));
    for (Glob series : savingsSeries) {
      Integer mirrorSeries = series.get(Series.MIRROR_SERIES);
      if (mirrorSeries == null) {
        Integer mirorSeriesId = UpdateMirrorSeriesChangeSetVisitor.createMirrorSeries(series.getKey(), repository);
        GlobList seriesBudgets = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID)).getGlobs();
        for (Glob budget : seriesBudgets) {
          GlobBuilder builder = GlobBuilder.init(SeriesBudget.TYPE);
          for (Field field : SeriesBudget.TYPE.getFields()) {
            if (field == SeriesBudget.PLANNED_AMOUNT) {
              builder.set(SeriesBudget.PLANNED_AMOUNT, budget.get(SeriesBudget.PLANNED_AMOUNT) == null ? null : -budget.get(SeriesBudget.PLANNED_AMOUNT));
            }
            else if (field == SeriesBudget.SERIES) {
              builder.setValue(SeriesBudget.SERIES, mirorSeriesId);
            }
            else if (field != SeriesBudget.ACTUAL_AMOUNT && field != SeriesBudget.ID) {
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
    GlobList series = repository.getAll(Series.TYPE, fieldEquals(Series.BUDGET_AREA, BudgetArea.TRANSFER.getId()));
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
                          and(fieldEquals(Transaction.SERIES, series.get(Series.ID)),
                              fieldEquals(Transaction.ACCOUNT, account.get(Account.ID)),
                              isTrue(Transaction.PLANNED))
        );
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
                             String newLabel = TransactionAnalyzerFactory.removeBlankAndToUppercase(originalLabel);
                             repository.update(transaction.getKey(), Transaction.ORIGINAL_LABEL, newLabel);

                             String label = transaction.get(Transaction.LABEL);
                             String newVisibleLabel = TransactionAnalyzerFactory.removeBlankAndToUppercase(label);
                             repository.update(transaction.getKey(), Transaction.LABEL, newVisibleLabel);

                           }
                         }
    );
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
                          value(Account.BANK, Bank.GENERIC_BANK_ID));
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
                         }
    );
  }

  private void removeOccasionalBudgetArea(GlobRepository repository) {
    repository.safeApply(Transaction.TYPE, GlobMatchers.ALL,
                         new GlobFunctor() {
                           public void run(Glob transaction, GlobRepository repository) throws Exception {
                             if (Series.OCCASIONAL_SERIES_ID.equals(transaction.get(Transaction.SERIES))) {
                               Transaction.uncategorize(transaction, repository);
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

  private static class RemovePlannedPrefixFunctor implements GlobFunctor {

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

  public void postProcessing(GlobRepository repository) {
    for (Map.Entry<Integer, Key[]> entry : savingsSeriesToOp.entrySet()) {
      Glob series = repository.find(Key.create(Series.TYPE, entry.getKey()));
      if (series == null) {
        continue;
      }
      Glob targetAccount = repository.findLinkTarget(series, Series.TARGET_ACCOUNT);
      boolean positive = series.get(Series.TO_ACCOUNT).equals(series.get(Series.TARGET_ACCOUNT));
      for (Key key : entry.getValue()) {
        Glob transaction = repository.find(key);
        if (transaction != null) {
          if (isSame(targetAccount, transaction, repository)) {
            if (transaction.get(Transaction.AMOUNT) > 0 == positive) {
              repository.update(transaction.getKey(), Transaction.SERIES, series.get(Series.ID));
            }
            else {
              Log.write("[Upgrade] Invalid operation: " + GlobPrinter.toString(transaction) + "\n" +
                        "for series: " + GlobPrinter.toString(series));
            }
          }
        }
      }
    }
    savingsSeriesToOp.clear();
    postProcessor.run(repository);
  }

  private boolean isSame(Glob targetAccount, Glob transaction, GlobRepository repository) {
    Glob account = repository.findLinkTarget(transaction, Transaction.ACCOUNT);
    return targetAccount.get(Account.ID).equals(transaction.get(Transaction.ACCOUNT)) ||
           (targetAccount.get(Account.ID) == Account.MAIN_SUMMARY_ACCOUNT_ID && Account.isMain(account));
  }

  private void updateColorTheme(GlobRepository repository) {
    repository.update(UserPreferences.KEY,
                      UserPreferences.COLOR_THEME,
                      ColorTheme.STANDARD.getId());
  }

  private void fixHiddenProjectSeriesBudget(GlobRepository repository) {
    Set<Integer> seriesIds = new HashSet<Integer>();
    for (Glob project : repository.getAll(Project.TYPE)) {
      seriesIds.addAll(repository.findLinkedTo(project, ProjectItem.PROJECT).getValueSet(ProjectItem.SERIES));
    }
    for (Glob seriesBudget : repository.getAll(SeriesBudget.TYPE, fieldIn(SeriesBudget.SERIES, seriesIds))) {
      if (!seriesBudget.isTrue(SeriesBudget.ACTIVE) &&
          Amounts.isNotZero(seriesBudget.get(SeriesBudget.ACTUAL_AMOUNT))) {
        repository.update(seriesBudget.getKey(), SeriesBudget.ACTIVE, true);
      }
    }
  }
}
