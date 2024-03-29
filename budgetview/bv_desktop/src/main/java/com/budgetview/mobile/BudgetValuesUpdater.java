package com.budgetview.mobile;

import com.budgetview.desktop.accounts.position.DailyAccountPositionComputer;
import com.budgetview.desktop.accounts.position.DailyAccountPositionValues;
import com.budgetview.desktop.accounts.utils.AccountMatchers;
import com.budgetview.desktop.budget.summary.TotalBudgetAreaAmounts;
import com.budgetview.desktop.description.Labels;
import com.budgetview.desktop.description.stringifiers.AccountComparator;
import com.budgetview.desktop.model.BudgetStat;
import com.budgetview.desktop.model.SeriesStat;
import com.budgetview.desktop.transactions.utils.TransactionMatchers;
import com.budgetview.desktop.utils.DaySelection;
import com.budgetview.model.*;
import com.budgetview.shared.mobile.model.*;
import com.budgetview.shared.model.AccountType;
import com.budgetview.shared.model.BudgetArea;
import com.budgetview.utils.TransactionComparator;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.Utils;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.*;

public class BudgetValuesUpdater {

  private static BudgetArea[] BUDGET_AREAS;
  private GlobRepository sourceRepository;
  private GlobRepository targetRepository;
  private Integer[] selectedMonths;
  private int currentMonthId;

  static {
    BUDGET_AREAS = new BudgetArea[BudgetArea.INCOME_AND_EXPENSES_AREAS.length + 1];
    for (int i = 0; i < BudgetArea.INCOME_AND_EXPENSES_AREAS.length; i++) {
      BUDGET_AREAS[i] = BudgetArea.INCOME_AND_EXPENSES_AREAS[i];
    }
    BUDGET_AREAS[BUDGET_AREAS.length - 1] = BudgetArea.UNCATEGORIZED;
  }

  public static void process(GlobRepository sourceRepository, GlobRepository targetRepository) {
    BudgetValuesUpdater updater = new BudgetValuesUpdater(sourceRepository, targetRepository);
    updater.run();
  }

  private BudgetValuesUpdater(GlobRepository sourceRepository, GlobRepository targetRepository) {
    this.sourceRepository = sourceRepository;
    this.targetRepository = targetRepository;
    this.currentMonthId = CurrentMonth.getCurrentMonth(sourceRepository);
    this.selectedMonths = new Integer[]{
      Month.previous(Month.previous(Month.previous(currentMonthId))),
      Month.previous(Month.previous(currentMonthId)),
      Month.previous(currentMonthId),
      currentMonthId,
      Month.next(currentMonthId)
    };
  }

  private void run() {
    targetRepository.startChangeSet();
    try {
      targetRepository.deleteAll();

      createVersion();
      createMonths();
      createBudgetAreas();
      createBudgetStats();
      createSeriesValues();
      createAccounts();
      createTransactions();

    }
    finally {
      targetRepository.completeChangeSet();
    }
  }

  private void createVersion() {
    targetRepository.create(BudgetViewVersion.key,
                            value(BudgetViewVersion.MAJOR_VERSION, MobileModel.MAJOR_VERSION),
                            value(BudgetViewVersion.MINOR_VERSION, MobileModel.MINOR_VERSION));
  }

  private void createMonths() {
    for (Integer monthId : selectedMonths) {
      targetRepository.create(MonthEntity.TYPE, value(MonthEntity.ID, monthId));
    }
  }

  private void createBudgetAreas() {
    for (BudgetArea budgetArea : BUDGET_AREAS) {
      targetRepository.create(BudgetAreaEntity.TYPE,
                              value(BudgetAreaEntity.ID, budgetArea.getId()),
                              value(BudgetAreaEntity.LABEL, Labels.get(budgetArea)),
                              value(BudgetAreaEntity.INVERT_AMOUNTS, BudgetArea.shouldInvertAmounts(budgetArea)));
    }
  }

  private void createBudgetStats() {
    TotalBudgetAreaAmounts totalAmounts = new TotalBudgetAreaAmounts() {
      protected Integer getCurrentMonths() {
        return currentMonthId;
      }
    };
    for (Glob budgetStat : sourceRepository.getAll(BudgetStat.TYPE, fieldIn(BudgetStat.MONTH, selectedMonths))) {
      for (BudgetArea budgetArea : BudgetArea.INCOME_AND_EXPENSES_AREAS) {
        totalAmounts.update(new GlobList(budgetStat), budgetArea);
        targetRepository.create(BudgetAreaValues.TYPE,
                                value(BudgetAreaValues.BUDGET_AREA, budgetArea.getId()),
                                value(BudgetAreaValues.MONTH, budgetStat.get(BudgetStat.MONTH)),
                                value(BudgetAreaValues.REMAINDER, totalAmounts.getFutureRemaining() + totalAmounts.getPastRemaining()),
                                value(BudgetAreaValues.OVERRUN, totalAmounts.getFutureOverrun() + totalAmounts.getPastOverrun()),
                                value(BudgetAreaValues.INITIALLY_PLANNED, totalAmounts.getInitiallyPlanned()),
                                value(BudgetAreaValues.ACTUAL, totalAmounts.getActual()));
      }
      Double absUncategorized = budgetStat.get(BudgetStat.UNCATEGORIZED_ABS);
      targetRepository.create(BudgetAreaValues.TYPE,
                              value(BudgetAreaValues.BUDGET_AREA, BudgetArea.UNCATEGORIZED.getId()),
                              value(BudgetAreaValues.MONTH, budgetStat.get(BudgetStat.MONTH)),
                              value(BudgetAreaValues.REMAINDER, 0.00),
                              value(BudgetAreaValues.OVERRUN, absUncategorized),
                              value(BudgetAreaValues.INITIALLY_PLANNED, 0.00),
                              value(BudgetAreaValues.ACTUAL, absUncategorized));
    }
  }

  private void createSeriesValues() {
    Set<Integer> seriesIds = new HashSet<Integer>();
    for (Glob seriesStat : SeriesStat.getAllSummariesForSeries(selectedMonths, sourceRepository)) {
      Glob series = SeriesStat.getSeries(seriesStat, sourceRepository);
      if (seriesStat.isTrue(SeriesStat.ACTIVE) && !series.isTrue(Series.IS_MIRROR)) {
        Integer seriesId = series.get(Series.ID);
        seriesIds.add(seriesId);
        targetRepository.create(SeriesValues.TYPE,
                                value(SeriesValues.SERIES_ENTITY, seriesId),
                                value(SeriesValues.MONTH, seriesStat.get(SeriesStat.MONTH)),
                                value(SeriesValues.BUDGET_AREA, series.get(Series.BUDGET_AREA)),
                                value(SeriesValues.AMOUNT, seriesStat.get(SeriesStat.ACTUAL_AMOUNT)),
                                value(SeriesValues.PLANNED_AMOUNT, seriesStat.get(SeriesStat.PLANNED_AMOUNT)),
                                value(SeriesValues.OVERRUN_AMOUNT, seriesStat.get(SeriesStat.OVERRUN_AMOUNT)),
                                value(SeriesValues.REMAINING_AMOUNT, seriesStat.get(SeriesStat.REMAINING_AMOUNT)));
      }
    }
    for (Integer seriesId : seriesIds) {
      Glob series = sourceRepository.get(Key.create(Series.TYPE, seriesId));
      targetRepository.create(SeriesEntity.TYPE,
                              value(SeriesEntity.ID, seriesId),
                              value(SeriesEntity.NAME, series.get(Series.NAME)),
                              value(SeriesEntity.BUDGET_AREA, series.get(Series.BUDGET_AREA)));
    }
    for (Glob budgetStat : sourceRepository.getAll(BudgetStat.TYPE, fieldIn(BudgetStat.MONTH, selectedMonths))) {
      Glob seriesValues = targetRepository.find(Key.create(SeriesValues.SERIES_ENTITY, Series.UNCATEGORIZED_SERIES_ID,
                                                           SeriesValues.MONTH, budgetStat.get(BudgetStat.MONTH)));
      if (seriesValues != null) {
        Double absAmount = budgetStat.get(BudgetStat.UNCATEGORIZED_ABS);
        targetRepository.update(seriesValues.getKey(),
                                value(SeriesValues.PLANNED_AMOUNT, 0.00),
                                value(SeriesValues.AMOUNT, absAmount),
                                value(SeriesValues.OVERRUN_AMOUNT, absAmount),
                                value(SeriesValues.REMAINING_AMOUNT, 0.00));
      }
    }
  }

  private void createAccounts() {

    GlobMatcher matcher =
      and(AccountMatchers.userCreatedAccounts(),
          new TransactionMatchers.AccountDateMatcher(Utils.set(selectedMonths)));
    GlobList accounts =
      sourceRepository.getAll(Account.TYPE, matcher)
        .sortSelf(new AccountComparator());
    int index = 0;
    for (Glob account : accounts) {
      final Integer accountId = account.get(Account.ID);
      index = createAccountEntity(index, accountId, account, true);
      createUserAccountPositions(accountId);
    }

    Glob mainAccount = sourceRepository.get(Account.MAIN_SUMMARY_KEY);
    createAccountEntity(Account.MAIN_SUMMARY_ACCOUNT_ID, AccountEntity.ACCOUNT_ID_MAIN, mainAccount, false);
    createMainAccountsPositions();

    Glob savingsAccount = sourceRepository.get(Account.SAVINGS_SUMMARY_KEY);
    createAccountEntity(Account.MAIN_SUMMARY_ACCOUNT_ID, AccountEntity.ACCOUNT_ID_SAVINGS, savingsAccount, false);
    createSavingsAccountsPositions();
  }

  private int createAccountEntity(int index, Integer accountEntityId, Glob account, boolean isUserAccount) {
    Date positionDate = account.get(Account.POSITION_DATE);
    targetRepository.create(AccountEntity.TYPE,
                            value(AccountEntity.ID, accountEntityId),
                            value(AccountEntity.LABEL, account.get(Account.NAME)),
                            value(AccountEntity.IS_USER_ACCOUNT, isUserAccount),
                            value(AccountEntity.POSITION, account.get(Account.POSITION_WITH_PENDING)),
                            value(AccountEntity.POSITION_MONTH, positionDate == null ? null : Month.getMonthId(positionDate)),
                            value(AccountEntity.POSITION_DAY, positionDate == null ? null : Month.getDay(positionDate)),
                            value(AccountEntity.ACCOUNT_TYPE, convertAccountType(account.get(Account.ACCOUNT_TYPE))),
                            value(AccountEntity.SEQUENCE_NUMBER, index++));
    return index;
  }

  private void createMainAccountsPositions() {
    DailyAccountPositionComputer positionComputer = new DailyAccountPositionComputer(sourceRepository);
    DailyAccountPositionValues positionValues = positionComputer.getMainValues(Arrays.asList(selectedMonths), currentMonthId);
    createAccountPositions(positionValues, AccountEntity.ACCOUNT_ID_MAIN);
  }

  private void createSavingsAccountsPositions() {
    DailyAccountPositionComputer positionComputer = new DailyAccountPositionComputer(sourceRepository);
    DailyAccountPositionValues positionValues = positionComputer.getSavingsValues(Arrays.asList(selectedMonths), currentMonthId);
    createAccountPositions(positionValues, AccountEntity.ACCOUNT_ID_SAVINGS);
  }

  private void createUserAccountPositions(Integer accountId) {
    DailyAccountPositionComputer positionComputer = new DailyAccountPositionComputer(sourceRepository);
    DailyAccountPositionValues positionValues =
      positionComputer.getDailyValues(Arrays.asList(selectedMonths), currentMonthId,
                                      fieldEquals(Transaction.ACCOUNT, accountId),
                                      DaySelection.EMPTY, Transaction.ACCOUNT_POSITION);
    createAccountPositions(positionValues, accountId);
  }

  private void createAccountPositions(DailyAccountPositionValues positionValues, final int accountId) {
    positionValues.apply(new DailyAccountPositionValues.Functor() {
      public void processPositions(int monthId, Double[] minValues, boolean monthSelected, boolean[] daysSelected) {
        for (int i = 0; i < minValues.length; i++) {
          targetRepository.create(AccountPosition.TYPE,
                                  value(AccountPosition.ACCOUNT, accountId),
                                  value(AccountPosition.MONTH, monthId),
                                  value(AccountPosition.DAY, i + 1),
                                  value(AccountPosition.POSITION, minValues[i]));

        }
      }
    });
  }

  private Integer convertAccountType(Integer accountType) {
    switch (AccountType.get(accountType)) {
      case MAIN:
        return AccountEntity.ACCOUNT_TYPE_MAIN;
      case SAVINGS:
        return AccountEntity.ACCOUNT_TYPE_SAVINGS;
      default:
        throw new InvalidParameter("Unexpected AccountType");
    }
  }

  private void createTransactions() {
    GlobList transactions = sourceRepository
      .getAll(Transaction.TYPE,
              and(fieldIn(Transaction.BUDGET_MONTH, selectedMonths),
                  isNotTrue(Transaction.PLANNED)))
      .sortSelf(TransactionComparator.DESCENDING_BANK_SPLIT_AFTER);
    int sequenceNumber = 0;
    for (Glob transaction : transactions) {
      targetRepository.create(TransactionValues.TYPE,
                              value(TransactionValues.AMOUNT, transaction.get(Transaction.AMOUNT)),
                              value(TransactionValues.ACCOUNT, transaction.get(Transaction.ACCOUNT)),
                              value(TransactionValues.LABEL, transaction.get(Transaction.LABEL)),
                              value(TransactionValues.BANK_DAY, transaction.get(Transaction.BANK_DAY)),
                              value(TransactionValues.BANK_MONTH, transaction.get(Transaction.BANK_MONTH)),
                              value(TransactionValues.SERIES, transaction.get(Transaction.SERIES)),
                              value(TransactionValues.SEQUENCE_NUMBER, sequenceNumber++)
      );
    }
  }
}
