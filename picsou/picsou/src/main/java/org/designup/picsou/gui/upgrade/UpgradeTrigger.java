package org.designup.picsou.gui.upgrade;

import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.PicsouInit;
import org.designup.picsou.gui.projects.utils.ProjectUpgrade;
import org.designup.picsou.gui.utils.FrameSize;
import org.designup.picsou.importer.analyzer.TransactionAnalyzerFactory;
import org.designup.picsou.model.*;
import org.designup.picsou.triggers.AccountInitialPositionTrigger;
import org.designup.picsou.triggers.PositionTrigger;
import org.designup.picsou.triggers.savings.UpdateMirrorSeriesChangeSetVisitor;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.repository.GlobIdGenerator;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Log;
import org.globsframework.utils.Utils;
import org.globsframework.utils.collections.Pair;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.*;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.*;

public class UpgradeTrigger implements ChangeSetListener {
  private Directory directory;
  private HashMap<Integer, Key[]> savingsSeriesToOp = new HashMap<Integer, Key[]>();
  private ProjectUpgrade projectUpgrade = new ProjectUpgrade();

  public UpgradeTrigger(Directory directory) {
    this.directory = directory;
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {

    PicsouInit.createTransientDataForNewUser(repository);

    Glob userVersion = repository.get(UserVersionInformation.KEY);

    final Long currentJarVersion = userVersion.get(UserVersionInformation.CURRENT_JAR_VERSION);
    if (currentJarVersion.equals(PicsouApplication.JAR_VERSION)) {
      return;
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
      repository.safeApply(Transaction.TYPE, isTrue(Transaction.PLANNED), new RemovePlannedPrefixFunctor());
    }

    if (currentJarVersion < 34) {
      updateSavings(repository);
    }

    if (currentJarVersion < 48) {
      SignpostStatus.setAllCompleted(repository);
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

    Glob userPreferences = repository.findOrCreate(UserPreferences.KEY);
    if (currentJarVersion < 65) {
      repository.update(userPreferences.getKey(),
                        value(UserPreferences.MONTH_FOR_PLANNED, 3),
                        value(UserPreferences.PERIOD_COUNT_FOR_PLANNED, 10));
    }

    if (currentJarVersion < 68) {
      correctSavingMirror(repository);
    }
    if (currentJarVersion < 85) {
      updateColorTheme(repository);
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
      deleteDuplicateSynchro(repository);
    }
    if (currentJarVersion < 131) {
      FrameSize frameSize = FrameSize.init(directory.get(JFrame.class));
      LayoutConfig.init(frameSize.screenSize, frameSize.targetFrameSize, repository);
    }
    if (currentJarVersion < 132) {
      projectUpgrade.updateProjectSeriesAndGroups(repository);
    }

    if (currentJarVersion < 132) {
      updateTargetAccount(repository);
    }

    UserPreferences.initMobilePassword(repository, false);

    deleteDeprecatedGlobs(repository);

    Glob appVersion = repository.get(AppVersionInformation.KEY);
    if (userVersion.get(UserVersionInformation.CURRENT_BANK_CONFIG_VERSION)
        < (appVersion.get(AppVersionInformation.LATEST_BANK_CONFIG_SOFTWARE_VERSION))) {
      directory.get(UpgradeService.class).upgradeBankData(repository, appVersion);
    }

    repository.update(UserVersionInformation.KEY, UserVersionInformation.CURRENT_JAR_VERSION, PicsouApplication.JAR_VERSION);
  }

  private void updateTargetAccount(GlobRepository repository) {
    GlobList allSeries = repository.getAll(Series.TYPE, GlobMatchers.not(GlobMatchers.fieldEquals(Series.BUDGET_AREA, BudgetArea.SAVINGS.getId())));
    GlobIdGenerator idGenerator = repository.getIdGenerator();
    for (Glob series : allSeries) {
        GlobList operations = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, series.get(Series.ID))
          .getGlobs().filter(GlobMatchers.isFalse(Transaction.PLANNED), repository);
        Set<Integer> accounts = new HashSet<Integer>();
        for (Glob glob : operations) {
          accounts.add(glob.get(Transaction.ACCOUNT));
        }
        if (accounts.size() == 0) {
          // nothing
        } else if (accounts.size() == 1) {
          repository.update(series.getKey(), Series.TARGET_ACCOUNT, accounts.iterator().next());
        }
        else {
          Glob groups = repository.create(SeriesGroup.TYPE, value(SeriesGroup.NAME, series.get(Series.NAME)),
                                          value(SeriesGroup.BUDGET_AREA, series.get(Series.BUDGET_AREA)));
          boolean first = true;
          Key key = series.getKey();
          for (Integer account : accounts) {
            if (!first) {
              key = Key.create(Series.TYPE, idGenerator.getNextId(Series.ID, 1));
              repository.create(key, series.toArray());
              repository.update(key, value(Series.INITIAL_AMOUNT, 0.));
            }
            first = false;
            repository.update(key,
                              value(Series.GROUP, groups.get(SeriesGroup.ID)),
                              value(Series.TARGET_ACCOUNT, account));
            for (Glob op : operations) {
              if (op.get(Transaction.ACCOUNT).equals(account)) {
                repository.update(op.getKey(), value(Transaction.SERIES, key.get(Series.ID)));
              }
            }
          }
        }
    }
    GlobList allSavingsSeries = repository.getAll(Series.TYPE, GlobMatchers.fieldEquals(Series.BUDGET_AREA, BudgetArea.SAVINGS.getId()));
    Map<Key, Key> saving = new HashMap<Key, Key>();
    for (Glob series : allSavingsSeries) {
      if (!saving.containsKey(series.getKey())){
        saving.put(repository.findLinkTarget(series, Series.MIRROR_SERIES).getKey(), series.getKey());
      }
    }

    for (Map.Entry<Key, Key> entry : saving.entrySet()) {
      Glob series1 = repository.get(entry.getKey());
      Glob series2 = repository.get(entry.getValue());
      Set<Integer> accountId1 = updateTargetAccount(repository, series1, repository.findLinkTarget(series1, Series.TARGET_ACCOUNT));
      Set<Integer> accountId2 = updateTargetAccount(repository, series2, repository.findLinkTarget(series2, Series.TARGET_ACCOUNT));
      if (accountId1.size() > 1 || accountId2.size() > 1) {
        // que faire?
      }
    }
  }

  private Set<Integer> updateTargetAccount(GlobRepository repository, Glob series1, Glob targetAccount) {
    Set<Integer> accounts1 = new HashSet<Integer>();
    if (Account.isMain(targetAccount) && !Account.isUserCreatedMainAccount(targetAccount)) {
      GlobList operations1 = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, series1.get(Series.ID))
        .getGlobs().filter(GlobMatchers.isFalse(Transaction.PLANNED), repository);
      for (Glob glob : operations1) {
        accounts1.add(glob.get(Transaction.ACCOUNT));
      }
      if (accounts1.size() == 1) {
        repository.update(series1.getKey(), Series.TARGET_ACCOUNT, accounts1.iterator().next());
      }
    }
    return accounts1;
  }

  private void deleteDuplicateSynchro(GlobRepository repository) {
    GlobList otherSynchros = repository.getAll(Synchro.TYPE);

    Set<Key> synchroToDelete = new HashSet<Key>();
    for (Glob synchro : otherSynchros) {
      for (Glob otherSynchro : otherSynchros) {
        if (synchro != otherSynchro && !synchroToDelete.contains(otherSynchro.getKey())) {
          if (sameAccount(repository, synchro, otherSynchro)) {
            synchroToDelete.add(synchro.getKey());
          }
        }
      }
    }
    for (Key key : synchroToDelete) {
      GlobList all = repository.getAll(RealAccount.TYPE, GlobMatchers.fieldEquals(RealAccount.SYNCHRO, key.get(Synchro.ID)));
      for (Glob glob : all) {
        repository.update(glob.getKey(), RealAccount.SYNCHRO, null);
      }
      repository.delete(key);
    }
  }

  private boolean sameAccount(GlobRepository repository, Glob sync1, Glob sync2) {
    GlobList acc1 = repository.findLinkedTo(sync1, RealAccount.SYNCHRO);
    GlobList acc2 = repository.findLinkedTo(sync2, RealAccount.SYNCHRO);
    for (Glob glob : acc1) {
      for (Glob glob1 : acc2) {
        if (glob1 != glob) {
          if (Utils.equal(glob.get(RealAccount.NUMBER), glob1.get(RealAccount.NUMBER)) &&
              Utils.equal(glob.get(RealAccount.NAME), glob1.get(RealAccount.NAME))) {
            return true;
          }
        }
      }
    }
    return false;
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
                                     GlobMatchers.fieldEquals(Account.CARD_TYPE, AccountCardType.DEFERRED.getId()));
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
                        FieldValue.value(Account.DEFERRED_DAY, day),
                        FieldValue.value(Account.DEFERRED_DEBIT_DAY, day),
                        FieldValue.value(Account.DEFERRED_MONTH_SHIFT, 0));
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
                                            GlobMatchers.not(GlobMatchers.fieldEquals(Account.CARD_TYPE, AccountCardType.DEFERRED.getId())));

      for (Glob account : accounts) {
        if (!Account.isUserCreatedAccount(account)) {
          continue;
        }
        if (account.get(Account.OPEN_TRANSACTION) != null) {
          continue;
        }
        Glob[] transactions = repository.getSorted(Transaction.TYPE, TransactionComparator.ASCENDING_ACCOUNT,
                                                   GlobMatchers.fieldEquals(Transaction.ACCOUNT, account.get(Account.ID)));
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
      PositionTrigger.computeTotal(repository);
    }
    finally {
      repository.completeChangeSet();
    }
  }

  private void correctSavingMirror(GlobRepository repository) {
    GlobList savingsSeries = repository.getAll(Series.TYPE, GlobMatchers.fieldEquals(Series.BUDGET_AREA, BudgetArea.SAVINGS.getId()));
    for (Glob series : savingsSeries) {
      Glob mirrorSeries = repository.findLinkTarget(series, Series.MIRROR_SERIES);
      if (mirrorSeries.get(Series.TARGET_ACCOUNT).equals(series.get(Series.TARGET_ACCOUNT))) {
        updateSavingsSeries(repository, series);
        //finalement deux series sont identiques
        if (mirrorSeries.get(Series.TARGET_ACCOUNT).equals(series.get(Series.TARGET_ACCOUNT))) {
          Log.write("Correcting savings series.");
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
    GlobList savingsSeries = repository.getAll(Series.TYPE, GlobMatchers.fieldEquals(Series.BUDGET_AREA, BudgetArea.SAVINGS.getId()));
    for (Glob series : savingsSeries) {
      updateSavingsSeries(repository, series);
    }
  }

  private void updateSavingsSeries(GlobRepository repository, Glob series) {
    Boolean sens = null;
    GlobList budgets = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID)).getGlobs();
    for (Glob budget : budgets) {
      if (budget.get(SeriesBudget.PLANNED_AMOUNT) == null) {
        continue;
      }
      if (budget.get(SeriesBudget.PLANNED_AMOUNT) > 0) {
        sens = true;
        break;
      }
      if (budget.get(SeriesBudget.PLANNED_AMOUNT) < 0) {
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
                          and(fieldEquals(Transaction.SERIES, series.get(Series.ID)),
                              fieldEquals(Transaction.ACCOUNT, account.get(Account.ID)),
                              isTrue(Transaction.PLANNED)));
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
      boolean positif = series.get(Series.TO_ACCOUNT).equals(series.get(Series.TARGET_ACCOUNT));
      for (Key key : entry.getValue()) {
        Glob transaction = repository.find(key);
        if (transaction != null) {
          if (isSame(targetAccount, transaction, repository)) {
            if (transaction.get(Transaction.AMOUNT) > 0 == positif) {
              repository.update(transaction.getKey(), Transaction.SERIES, series.get(Series.ID));
            }
            else {
              Log.write("Invalid operation: " + GlobPrinter.toString(transaction) + "\n" +
                        "for series: " + GlobPrinter.toString(series));
            }
          }
        }
      }
    }
    savingsSeriesToOp.clear();
    projectUpgrade.postProcessing(repository);
  }

  private boolean isSame(Glob targetAccount, Glob transaction, GlobRepository repository) {
    Glob account = repository.findLinkTarget(transaction, Transaction.ACCOUNT);
    return targetAccount.get(Account.ID).equals(transaction.get(Transaction.ACCOUNT)) ||
           (targetAccount.get(Account.ID) == Account.MAIN_SUMMARY_ACCOUNT_ID && Account.isMain(account));
  }

  private void updateColorTheme(GlobRepository repository) {
    Glob prefs = repository.find(UserPreferences.KEY);
    if (prefs.get(UserPreferences.COLOR_THEME) == null) {
      repository.update(UserPreferences.KEY,
                        UserPreferences.COLOR_THEME,
                        ColorTheme.STANDARD.getId());
    }
  }

  private void fixHiddenProjectSeriesBudget(GlobRepository repository) {
    Set<Integer> seriesIds = new HashSet<Integer>();
    for (Glob project : repository.getAll(Project.TYPE)) {
      seriesIds.addAll(repository.findLinkedTo(project, ProjectItem.PROJECT).getValueSet(ProjectItem.SERIES));
    }
    for (Glob seriesBudget : repository.getAll(SeriesBudget.TYPE, GlobMatchers.fieldIn(SeriesBudget.SERIES, seriesIds))) {
      if (!seriesBudget.isTrue(SeriesBudget.ACTIVE) &&
          Amounts.isNotZero(seriesBudget.get(SeriesBudget.ACTUAL_AMOUNT))) {
        repository.update(seriesBudget.getKey(), SeriesBudget.ACTIVE, true);
      }
    }
  }

}
